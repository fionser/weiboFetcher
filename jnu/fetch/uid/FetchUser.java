package jnu.fetch.uid;

import jnu.util.CallBacker;
import jnu.util.DBUtil;
import weibo4j.util.WeiboConfig;

/**
 * User: fionser
 * Date: 12-12-12
 * Time: 下午9:37
 */

public class FetchUser extends Thread implements CallBacker{
    private FetchList fetchList;
    private String []accessTokens;
    private int  threadRunning;
    private final Integer threadLock = 0;
    private DBUtil db;
    private void getAccessTokens() {
        int accessTokenCnt = WeiboConfig.getIntValue("AT_CNT", 0);
        accessTokens = new String[accessTokenCnt];
        for (int i = 0; i < accessTokenCnt; i++) {
            accessTokens[i] = WeiboConfig.getValue("AT_" + i);
        }
    }

    public FetchUser() {
        String rootSeed = WeiboConfig.getValue("ROOT_SEED");
        db = new DBUtil();
        fetchList = new FetchList(db, Long.parseLong(rootSeed));
        getAccessTokens();
    }

    public static void main(String args[]) {
        new FetchUser().start();
    }

    public void run() {
        final int maxLevel = WeiboConfig.getIntValue("MAXI_LVL", 2);

        while (!fetchList.isEmpty()) {
            for (String accessToken : accessTokens) {
                try {
                    new Thread(new FetchWorker(accessToken, maxLevel, fetchList, this)).start();
                    synchronized (threadLock) {
                        threadRunning++;
                    }
                    Thread.sleep(1000 * 10);//10秒发射一个新任务
                } catch (InterruptedException ignore) {
                }
            }
            while (true) {
                synchronized (threadLock) {
                    if (threadRunning == 0) {
                        break;
                    }
                }
                try {
                    Thread.sleep(1000 * 30);//30秒报告一次任务状态
                } catch (InterruptedException ignore) {}
            }
            try {
                if (!fetchList.isEmpty()) {
                    System.out.println("Time to Wait");//超速, 休眠30分钟重启
                    Thread.sleep(1000L * 60 * 30);
                }
            } catch (InterruptedException ignore){}
        }

        System.out.println("Mission Over!");
        db.close();
    }

    @Override
    public void callback(boolean success) {
        synchronized (threadLock) {
            System.out.println("A worker has completed.");
            threadRunning--;
            interrupt();
        }
    }
}
