package jnu.fetch.user;

import jnu.util.CallBacker;
import jnu.util.PriorityRQ;
import jnu.vo.Task;


/**
 * Name:FetchUserThread
 * User:fionser
 * Date:12-12-16
 */
class FetchUserThread implements Runnable {
    private static int ID = 0;
    private PriorityRQ<Task> rq;
    private CallBacker callBacker;
    private String accessToken;
    private final int sleep = 2000;    /* 基本等待时间 */
    private final int trys = 3;        /* 最大尝试取任务次数 */
    private final int id = ID++;

    FetchUserThread(PriorityRQ<Task> rq,
                    CallBacker callBacker,
                    String accessToken) {
        this.rq = rq;
        this.callBacker = callBacker;
        this.accessToken = accessToken;
    }

    public int getId() {
        return this.id;
    }

    @Override
    public void run() {
        Object[] pars = new Object[]{accessToken};
        boolean ret;
        while (true) {
            Task task = null;
                /* 尝试取任务队列三次
                *  每次失败都停止一段时间
                *  等待间隔采取二进制指数退逼算法
                * */
            for (int i = 1; i <= trys; i++) {
                try {
                    task = rq.get();
                    if (task != null) {
                        break;
                    }
                        /* 加1是为了避免出现睡眠时间为0的情况 */
                    Thread.sleep(sleep * (int)(Math.random()* (1 << i)) + 1);
                } catch (Exception ignore) {
                }
            }

            if (task == null) {//任务队列为空直接返回
                ret = true;
                break;
            }

            task.settings(pars);
            System.out.printf(">Thread %d got %s.\n", this.id, task.toString());
            task.execute();
            if (!task.isFinish()) {
                System.err.printf(">Thread %d reschedule Task %s.\n", this.id, task.toString());
                task.upPriority();
                rq.put(task);
                ret = false;
                break;
            } else {
                System.out.printf(">Thread %d completed %s.\n", id, task.toString());
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignore) {
            }
        }
        callBacker.callback(ret);
    }
}
