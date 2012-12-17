package jnu.fetch.uid;

/**
 * User: fionser
 * Date: 12-12-15
 * Time: 下午3:30
 */

class FetchUidTask {
    private long uid;
    private int level;
    FetchUidTask(long uid, int level) {
        this.uid = uid;
        this.level = level;
    }
    long getUid() {
        return uid;
    }
    int getLevel() {
        return level;
    }
}
