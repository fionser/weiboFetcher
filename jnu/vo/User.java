package jnu.vo;

/**
 * User: fionser
 * Date: 12-12-12
 * Time: 下午9:10
 */

public class User {
    private long uid;
    private long timestamp;
    public User(){}
    public User(long uid, long timestamp) {
        this.uid = uid;
        this.timestamp = timestamp;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getUid() {
        return this.uid;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
