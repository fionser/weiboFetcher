package jnu.fetch.uid;

import jnu.util.BitSet;
import jnu.util.DBUtil;
import jnu.vo.User;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * User: fionser
 * Date: 12-12-14
 * Time: 下午11:11
 */

class FetchList {
    private BitSet bitSet;
    private Queue<FetchUidTask> userToFetch;
    private final Integer queueLock = 0;
    private DBUtil db;
    private void initFromDB() {
        List<User> users = db.getAllUsers();
        bitSet = new BitSet();
        for (User u : users) {
            bitSet.setBit(u.getUid());
        }
    }

    FetchList(DBUtil db, long seed) {
        this.db = db;
        userToFetch = new LinkedBlockingQueue<FetchUidTask>(20000);
        initFromDB();
        put(new FetchUidTask(seed, 0));
    }

    /**
     * @return next uid.
     */
    public FetchUidTask getNext() {
        synchronized (queueLock) {
            if (!userToFetch.isEmpty()) {
                return userToFetch.poll();
            } else {
                return null;
            }
        }
    }

    /**
     *  @param fetchUidTask Insert this uid into the database and queue.
     */
    public void put(FetchUidTask fetchUidTask) {
        synchronized (queueLock) {
            if (!bitSet.testBit(fetchUidTask.getUid())) {
                userToFetch.offer(fetchUidTask);//enqueue
                User user = new User(fetchUidTask.getUid(), 0);
                db.insertNewUser(user);//en-DB
                bitSet.setBit(fetchUidTask.getUid());//set bit.
            }
        }
    }

    public boolean isEmpty() {
        synchronized (queueLock) {
            return userToFetch.isEmpty();
        }
    }
}
