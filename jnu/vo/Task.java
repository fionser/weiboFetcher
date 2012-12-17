package jnu.vo;

import java.util.Comparator;

/**
 * User: fionser
 * Date: 12-12-16
 * Time: 上午10:26
 */
public interface Task extends Comparable<Task> {
    public final int DEFAULT_PRIO = 1;
    public int getPriority();
    public void upPriority();
    public void settings(Object []objs);
    public void execute();
    public boolean isFinish();
}
