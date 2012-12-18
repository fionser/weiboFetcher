package jnu.fetch.user;

import jnu.util.CallBacker;
import jnu.util.DBUtil;
import jnu.util.PriorityRQ;
import jnu.vo.Task;
import jnu.vo.User;
import weibo4j.util.WeiboConfig;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Name:Scheduler
 * User:fionser
 * Date:12-12-16
 */
class Scheduler extends Thread implements CallBacker {
    private PriorityRQ<Task> rq;
    private final int threadCnt = WeiboConfig.getIntValue("AT_CNT", 0);
    private final String[] accessTokens = new String[threadCnt];
    private int nextAccessToken;
    private AtomicInteger runningThread = new AtomicInteger(0);
    private AtomicInteger overFlag = new AtomicInteger(0);
    private List<User> users;
    private Boolean daemon = true;
    private Thread threadsDaemon, rqDaemon;
    private DBUtil db;

    public static void main(String args[]) {
        new Scheduler().start();
    }

    public void run() {
        schedule();
    }

    private Runnable threadsDaemon() {
        return new Runnable() {
            @Override
            public void run() {
                while (daemon) {
                    long time = System.currentTimeMillis();
                    if (!rq.isEmpty()) {
                        int tskInQue = rq.size();
                            /*一次发射所有的线程，10秒间隔*/
                        for (int i = 0; i < tskInQue && i < threadCnt; i++) {
                            sendThread();
                            try {
                                Thread.sleep(1000L * 10);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                    long timeSleep = 1000L * 60 * 30 - System.currentTimeMillis() + time;
                    System.out.printf(">====ThreadDaemon going to wait %d sec.====\n", timeSleep / 1000);
                    synchronized (this) {
                        try {
                            wait(timeSleep);
                        } catch (InterruptedException ignore) {
                        }
                    }
                }
                System.out.println("==========Thread Daemon Dead=========");
            }
        };
    }

    private Runnable rqDaemon() {
        return new Runnable() {
            @Override
            public void run() {
                while (daemon) {
                    try {
                        System.out.printf(">Running Thread %d RQ %d.%s\n",
                                runningThread.intValue(),
                                rq.size(),
                                Calendar.getInstance().getTime());
                        Thread.sleep(1000L * 60);
                    } catch (Exception ignore) {
                    }
                }
                System.out.println("==========Rq Daemon Dead=========");
            }
        };
    }

    Scheduler() {
        long limitF = WeiboConfig.getIntValue("SQL_LIMIF", 0);
        long limitT = WeiboConfig.getIntValue("SQL_LIMIT", -1);
        rq = new PriorityRQ<Task>((int) (limitT - limitF) + 10);
        initAccessTokens();
        initFromDB();
        initTask();

    }

    private void initAccessTokens() {
        for (int i = 0; i < threadCnt; i++) {
            accessTokens[i] = WeiboConfig.getValue("AT_" + i);
        }
        nextAccessToken = 0;
    }

    private void initFromDB() {
        db = new DBUtil();
        Calendar calendar = Calendar.getInstance();
        /*更新只对三个月前的*/
        calendar.add(Calendar.MONTH, -3);
        long since = calendar.getTimeInMillis();
        long limitF = WeiboConfig.getIntValue("SQL_LIMIF", 0);
        long limitT = WeiboConfig.getIntValue("SQL_LIMIT", -1);
        users = db.getUpdateUsers(since, limitF, limitT);
    }

    private void initTask() {
        while (!users.isEmpty()) {
            rq.put(new FetchUserTask(users.remove(0), db));
        }
        users.clear();
    }

    private void sendThread() {
        FetchUserThread runnable = new FetchUserThread(rq, this, accessTokens[nextAccessToken]);
        runningThread.incrementAndGet();
        System.out.printf(">Active Thread %d with AT_%d\n", runnable.getId(), nextAccessToken);
        nextAccessToken = nextAccessToken + 1 >= threadCnt ? 0 : nextAccessToken + 1;
        new Thread(runnable).start();
    }

    private void killDaemons() {
        daemon = false;
        if (threadsDaemon.isAlive()) {
            threadsDaemon.interrupt();
        }
        if (rqDaemon.isAlive()) {
            rqDaemon.interrupt();
        }
    }

    private void schedule() {
        threadsDaemon = new Thread(threadsDaemon());
        threadsDaemon.start();
        rqDaemon = new Thread(rqDaemon());
        rqDaemon.setPriority(Thread.MAX_PRIORITY);
        rqDaemon.start();
        try {
            Thread.sleep(5000L);/*等守护进程先启动一下*/
        } catch (InterruptedException ignore) {
        }
        while (true) {
            if (runningThread.get() <= 0) {
                if (rq.isEmpty()) {
                    break;
                } else {//没有了工作线程，但是还有任务。
                    /*说明都超速率*/
                    System.out.println(">All threads got rate limit.");
                }
            }
            System.out.println(">Main is checking...");
            try {
                Thread.sleep(1000 * 60L);
            } catch (InterruptedException ignore) {
            }
        }
        killDaemons();
        System.out.println(">Main Over-ing");
        db.close();
    }

    @Override
    public void callback(boolean success) {
        runningThread.decrementAndGet();
        if (success) {//队列为空
                /* 当累积有一半的线程发现任务队列为空
                *
                * */
            if (overFlag.incrementAndGet() >= (threadCnt >> 1)) {
                killDaemons();
            }
            interrupt();
        }
    }
}
