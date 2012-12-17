package jnu.fetch.user;

import jnu.util.DBUtil;
import jnu.vo.Task;
import jnu.vo.User;
import weibo4j.Timeline;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.WeiboException;

import java.util.Calendar;

/**
 * Name:FetchUserTask
 * User:fionser
 * Date:12-12-16
 */
class FetchUserTask implements Task {
    public final static int PAGE_SIZE = 100;
    private final int MAX_PAGE = 100;
    private User user;
    private String accessToken;
    private Paging page;
    private int totalPage = -1; //-1作为标记位，有可能出现连第一个page都拿不到就超速了
    private PageWriter pageWriter;
    private DBUtil db;
    private int priority = DEFAULT_PRIO;

    FetchUserTask(User user, DBUtil db) {
        this.user = user;
        this.db = db;
        this.page = new Paging(1, PAGE_SIZE);
        this.pageWriter = new PageWriter(user);
    }

    private void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    private void getPageNr() throws WeiboException {
        if (totalPage == -1) {
            String uid = String.valueOf(user.getUid());
            Timeline timeline = new Timeline();
            timeline.setToken(accessToken);
            StatusWapper statusWapper =
                    timeline.getUserTimelineByUid(uid, page, 0, 0);
            totalPage = (int) (statusWapper.getTotalNumber() / PAGE_SIZE + 1);
            totalPage = totalPage < MAX_PAGE ? totalPage : MAX_PAGE;
            for (Status status : statusWapper.getStatuses()) {
                if (!pageWriter.write(status)) {
                    page.setPage(totalPage);
                    break;
                }
            }
        }
    }

    private void getPage() throws WeiboException {
        String uid = String.valueOf(user.getUid());
        StatusWapper statusWapper;
        Timeline timeline = new Timeline();
        timeline.setToken(accessToken);
        System.out.printf(">User %s TotalPage %d\n", uid, totalPage);
        L:
        for (int i = page.getPage(); i <= totalPage; i++) {
            page.setPage(i);
            statusWapper = timeline.getUserTimelineByUid(uid, page, 0, 0);
            for (Status status : statusWapper.getStatuses()) {
                    /* 当出现了太旧的消息，则直接退出当前任务，并设置完成标志 */
                if (!pageWriter.write(status)) {
                    page.setPage(totalPage);
                    break L;
                }
                pageWriter.write(status);
            }
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException ignore) {
            }
        }
        page.setPage(totalPage);
        /*写到磁盘上*/
        pageWriter.flush();
        /*更新时间戳*/
        this.user.setTimestamp(Calendar.getInstance().getTimeInMillis());
        db.updateUser(this.user);
    }

    public String toString() {
        return String.format("%d", user.getUid());
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void upPriority() {
        priority = (priority << 1);
    }

    @Override
    public void settings(Object[] objs) {
        String accessToken = (String) objs[0];
        this.setAccessToken(accessToken);
    }

    @Override
    public void execute() {
        try {
            getPageNr();
            page.setPage(2);
            getPage();
        } catch (WeiboException ignore) {
            System.err.printf(">Uid %d out of rate limit\n", user.getUid());
        } catch (Exception e) {
            System.err.printf(">Error %s\n", e.getMessage());
        }
    }

    @Override
    public boolean isFinish() {
        return totalPage != -1 && (page.getPage() >= totalPage);
    }

    @Override
    public int compareTo(Task o) {
        int delta = getPriority() - o.getPriority();
        if (delta == 0) {
            return 0;
        } else if (delta > 0) {
            return -1;
        }
        return 1;
    }
}
