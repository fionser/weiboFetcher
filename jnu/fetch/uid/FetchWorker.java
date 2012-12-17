package jnu.fetch.uid;

import jnu.util.CallBacker;
import weibo4j.Friendships;
import weibo4j.model.Paging;
import weibo4j.model.WeiboException;

/**
 * User: fionser
 * Date: 12-12-15
 * Time: 上午8:39
 */

class FetchWorker implements Runnable{
    private FetchList fetchList;
    private CallBacker callBacker;
    private String accessToken;
    private final int maxLevel;
    FetchWorker(String accessToken, int maxLevel, FetchList fetchList, CallBacker callBacker) {
        this.accessToken = accessToken;
        this.maxLevel = maxLevel;
        this.fetchList = fetchList;
        this.callBacker = callBacker;
    }
    @Override
    public void run() {
        try {
            Friendships friendships = new Friendships();
            friendships.setToken(accessToken);
            FetchUidTask fetchUidTask;
            while ((fetchUidTask = fetchList.getNext()) != null) {
                if (fetchUidTask.getLevel() < maxLevel) {
                    String []uids = friendships.getFriendsBilateralIds
                        (String.valueOf(fetchUidTask.getUid()), 0, 2000, new Paging(1));
                    if (uids.length > 500) {
                        continue;
                    }
                    for (String u : uids) {
                        fetchList.put
                                (new FetchUidTask(Long.parseLong(u), fetchUidTask.getLevel() + 1));
                    }
                }
            }
        } catch (WeiboException ignored) {
        }
    }
}
