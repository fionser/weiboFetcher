package jnu.util;

import weibo4j.util.WeiboConfig;

import java.util.regex.Pattern;

/**
 * User: fionser
 * Date: 12-12-16
 */
public class RegexFilter {
    private static RegexFilter ourInstance;
    private static Pattern[]patterns;
    static {
        ourInstance = new RegexFilter();
        patterns = new Pattern[WeiboConfig.getIntValue("REGEX_CNT", 0)];
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = Pattern.compile(WeiboConfig.getValue("REGEX_" + i));
        }
    }
    public static RegexFilter getInstance() {
        return ourInstance;
    }

    public static String Filter(String text) {
        for (Pattern pattern : patterns) {
            text = pattern.matcher(text).replaceAll(" ");
        }
        return text;
    }

    private RegexFilter() {
    }
}
