package jnu.fetch;

import weibo4j.Timeline;
import weibo4j.model.Paging;
import weibo4j.model.Status;
import weibo4j.model.StatusWapper;
import weibo4j.model.WeiboException;
import weibo4j.util.WeiboConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * User: fionser
 * Date: 12-12-15
 * Time: 下午9:06
 */

public class GetUser_Test {
    private static long threeMonthInMillSec;
    static {
        threeMonthInMillSec = 30L * 24 * 60 * 60 * 1000;
    }
    long millSecNow;
    public void test() {
        String accessToken = WeiboConfig.getValue("AT_0");
        int regexCnt = WeiboConfig.getIntValue("REGEX_CNT", 0);
        Pattern []patterns = new Pattern[regexCnt];
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = Pattern.compile(WeiboConfig.getValue("REGEX_" + i));
        }
        millSecNow =  Calendar.getInstance().getTimeInMillis();
        long inThreeMonth = (millSecNow - threeMonthInMillSec);
        long inSixMonth   = (inThreeMonth - threeMonthInMillSec);
        long inNinMonth   = (inSixMonth - threeMonthInMillSec);
        File []files = new File[3];
        FileOutputStream []fileOutputStreams = new FileOutputStream[3];
        OutputStreamWriter []writers = new OutputStreamWriter[3];
        StringBuilder []stringBuilders = new StringBuilder[3];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(String.format("Withn %d.txt", (i + 1) * 3));
        }
        for (int i = fileOutputStreams.length - 1; i >= 0; i--) {
            stringBuilders[i] = new StringBuilder();
            try {
                if (i + 1< files.length&& files[i].exists()) {
                    files[i + 1].delete();
                    files[i].renameTo(files[i + 1]);
                }
                fileOutputStreams[i] = new FileOutputStream(files[i]);
                writers[i] = new OutputStreamWriter(fileOutputStreams[i], "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Timeline timeline = new Timeline();
        timeline.setToken(accessToken);

        try {
            Paging paging = new Paging(1, 100);

            StatusWapper sw = timeline.getUserTimelineByUid
                    ("1979415381", paging, 0, 0);
            System.out.println(sw.getTotalNumber());
            int remainds = (int)Math.round(sw.getTotalNumber() / 100.0) + 1;
            System.out.println(remainds);
            long createDate;
            String text;

            for (Status status : sw.getStatuses()) {
                createDate = status.getCreatedAt().getTime();
                text = status.getText();
                for (Pattern pattern : patterns) {
                    text = pattern.matcher(text).replaceAll(" ");
                }
                try {
                    if (createDate > inThreeMonth) {
                        stringBuilders[0].append(text);
                        stringBuilders[0].append("\n");
                    } else if (createDate > inSixMonth) {
                        stringBuilders[1].append(text);
                        stringBuilders[1].append("\n");
                    } else if (createDate > inNinMonth) {
                        stringBuilders[2].append(text);
                        stringBuilders[2].append("\n");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (int i = 2; i <= remainds; i++) {
                paging.setPage(i);
                sw = timeline.getUserTimelineByUid
                        ("1979415381", paging, 0, 0);
                for (Status status : sw.getStatuses()) {
                    createDate = status.getCreatedAt().getTime();
                    text = status.getText();
                    for (Pattern pattern : patterns) {
                        text = pattern.matcher(text).replaceAll(" ");
                    }
                    try {
                        if (createDate > inThreeMonth) {
                            stringBuilders[0].append(text);
                            stringBuilders[0].append("\n");
                        } else if (createDate > inSixMonth) {
                            stringBuilders[1].append(text);
                            stringBuilders[1].append("\n");
                        } else if (createDate > inNinMonth) {
                            stringBuilders[2].append(text);
                            stringBuilders[2].append("\n");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            for (int i = 0; i < stringBuilders.length; i++) {
                String str = stringBuilders[i].toString();
                writers[i].write(str, 0, str.length());
                writers[i].close();
            }
            for (FileOutputStream fo : fileOutputStreams) {
                fo.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        new GetUser_Test().test();
    }
}
