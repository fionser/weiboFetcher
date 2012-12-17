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
    private final int trys = 3;
    private String accessToken;
    private final long sleep = 2000;
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
            for (int i = 0; i < trys; i++) {
                try {
                    task = rq.get();
                    if (task != null) {
                        break;
                    }
                    Thread.sleep(sleep << i);
                } catch (Exception ignore) {
                }
            }

            if (task == null) {
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
