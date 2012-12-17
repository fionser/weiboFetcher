package jnu.util;

import jnu.vo.User;
import weibo4j.util.WeiboConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: fionser
 * Date: 12-12-12
 * Time: 下午8:20
 */

public class DBUtil {
    private Connection conn = null;
    private PreparedStatement pstmtInsert = null;
    private PreparedStatement pstmtUpdate = null;
    private PreparedStatement pstmtSelect = null;
    private PreparedStatement pstmtGetnew = null;

    public DBUtil() {
        String driver = WeiboConfig.getValue("DB_DRI");
        String url    = WeiboConfig.getValue("DB_URL");
        String usr    = WeiboConfig.getValue("DB_USR");
        String pwd    = WeiboConfig.getValue("DB_PWD");
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, usr, pwd);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed())
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertNewUser(User user) {
        if (pstmtInsert == null) {
            String sql = WeiboConfig.getValue("SQL_INUSR");
            try {
                pstmtInsert = conn.prepareStatement(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            pstmtInsert.setLong(1, user.getUid());
            pstmtInsert.setLong(2, user.getTimestamp());
            pstmtInsert.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsers() {
        return getUpdateUsers(System.currentTimeMillis(), 0, -1);
    }
    /**
     * getUpdateUsers
     * @param since 时间点
     * @param limitF
     * @param limitT
     * @return 返回需要更新的用户列表。
     */
    public List<User> getUpdateUsers(long since, long limitF, long limitT) {
        List<User> result = new ArrayList<User>();
        if (pstmtSelect == null) {
            String sql = WeiboConfig.getValue("SQL_UDUSR");
            try {
                pstmtSelect = conn.prepareStatement(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            pstmtSelect.setLong(1, since);
            pstmtSelect.setLong(2, limitF);
            pstmtSelect.setLong(3, limitT);
            ResultSet resultSet = pstmtSelect.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setUid(resultSet.getLong(1));
                result.add(user);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    /**
     *
     * @param limit
     * @return 数据库里需要取数据的用户id列表
     */
    public List<User> getNewUsers(long limit) {
        List<User> result = new ArrayList<User>();
        if (pstmtGetnew == null) {
            String sql = WeiboConfig.getValue("SQL_NWUSR");
            try {
                pstmtGetnew = conn.prepareStatement(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            pstmtGetnew.setLong(1, limit);
            ResultSet resultSet = pstmtGetnew.executeQuery();
            while (resultSet.next()) {
                User user = new User();
                user.setUid(resultSet.getLong(1));
                result.add(user);
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void updateUser(User user) {
        if (pstmtUpdate == null) {
            String sql = WeiboConfig.getValue("SQL_REUSR");
            try {
                pstmtUpdate = conn.prepareStatement(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        try {
            pstmtUpdate.setLong(1, user.getTimestamp());
            pstmtUpdate.setLong(2, user.getUid());
            pstmtUpdate.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
