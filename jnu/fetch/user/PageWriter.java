package jnu.fetch.user;

import jnu.util.RegexFilter;
import jnu.vo.User;
import weibo4j.model.Status;
import weibo4j.util.WeiboConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/**
 * Name:PageWriter
 * User:fionser
 * Date:12-12-16
 */
class PageWriter {
    private static final String DEST_DIR = WeiboConfig.getValue("DEST_DIR", "/home");
    private static final String CHAR_SET = WeiboConfig.getValue("CHARSET", "UTF8");
    public static final int FILE_NR = 3;
    /*一个月有多少毫秒*/
    public static final long MILLSEC_IN_MONTH = 60L * 60 * 24 * 30 * 1000;

    private File[] files = new File[FILE_NR];
    private StringBuilder[] stringBuilders = new StringBuilder[FILE_NR];

    PageWriter(User user) {
        String userDir = DEST_DIR + File.separator + user.getUid();
        setUpDir(userDir);
        setUpFiles(userDir);
        setUpStringBuilders();
    }

    private void setUpDir(String userDir) {
        File dir = new File(userDir);
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                System.err.printf("Can not mkdir %s\n", dir.getAbsolutePath());
                System.exit(-1);
            }
        }
    }

    private void setUpFiles(String userDir) {
        for (int i = 0; i < FILE_NR; i++) {
            files[i] = new File(String.format("%s%sWithin_%d.txt", userDir, File.separator, (i + 1) * 3));
        }
        boolean[] flags = new boolean[FILE_NR];
        /* 只考虑 files[1..FILE_NR -1] */
        for (int i = FILE_NR - 2; i >= 0; i--) {
            if (files[i].exists()) {
                flags[i + 1] = true;
                files[i + 1].delete();
                files[i].renameTo(files[i + 1]);
            }
        }
        for (int i = 1; i < FILE_NR; i++) {
            if (flags[i]) {
                files[i] = null;
            }
        }
    }

    private void setUpStringBuilders() {
        for (int i = 0; i < FILE_NR; i++) {
            if (files[i] != null) {
                stringBuilders[i] = new StringBuilder();
            }
        }
    }

    /**
     * @param status
     * @return 当出现了9个月之前的消息时就返回false, 否则返回true
     */
    public boolean write(Status status) {
        long createTime = status.getCreatedAt().getTime();
        long timeNow = Calendar.getInstance().getTimeInMillis();
        long delta = timeNow - createTime;
        /*若该status是9个月之前的就抛弃掉*/
        if (delta > 9 * MILLSEC_IN_MONTH) {
            return false;
        }
        /*正则过滤*/
        String text = RegexFilter.Filter(status.getText());

        if (delta > 6 * MILLSEC_IN_MONTH) { /*Between 6 and 9 months*/
            if (files[2] != null) {
                stringBuilders[2].append(text);
                stringBuilders[2].append("\n");
            }
        } else if (delta > 3 * MILLSEC_IN_MONTH) { /*Between 3 and 6 months*/
            if (files[1] != null) {
                stringBuilders[1].append(text);
                stringBuilders[1].append("\n");
            }
        } else { /*Within 3 months*/
            stringBuilders[0].append(text);
            stringBuilders[0].append("\n");
        }
        return true;
    }

    public void flush() {
        for (int i = 0; i < stringBuilders.length; i++) {
            if (stringBuilders[i] != null) {
                try {
                    FileOutputStream fOStream = new FileOutputStream(files[i]);
                    OutputStreamWriter oSWriter = new OutputStreamWriter(fOStream, CHAR_SET);
                    String text = stringBuilders[i].toString();
                    oSWriter.write(text, 0, text.length());
                    oSWriter.close();
                    fOStream.close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                stringBuilders[i] = null;
            }
        }
    }
}
