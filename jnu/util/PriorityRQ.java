package jnu.util;

import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Name:PriorityRQ
 * User:fionser
 * Date:12-12-17
 */
public class PriorityRQ <E extends Comparable>{
    private final int DEFAULT_CAP = 1000;
    private Queue<E> rq;

    public PriorityRQ(int queueCap) {
        if (queueCap <= 0) {
            queueCap = DEFAULT_CAP;
        }
        rq = new PriorityQueue<E>(queueCap);
    }

    public synchronized void put(E tsk) {
        if (tsk != null) {
            rq.offer(tsk);
        }
    }

    public synchronized E get() {
        return isEmpty() ? null : rq.remove();
    }

    public synchronized boolean isEmpty() {
        return rq.isEmpty();
    }

    public synchronized int size() {
        return rq.size();
    }
}
