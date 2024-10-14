/**
 * ==================================================
 * Project: jdbc_test
 * Package: com.utils
 * =====================================================
 * Title: JDBCUtils.java
 * Created: [2023/3/15 16:51] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/15, created by Shuxin-Wang.
 * 2.
 */

package com.utils;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtils {
    public static Connection getConnection() throws Exception{
        InputStream in = JDBCUtils.class.getClassLoader().getResourceAsStream("jdbc.properties");
        Properties properties = new Properties();
        properties.load(in);

        String user = properties.getProperty("user");
        String password = properties.getProperty("password");
        String url = properties.getProperty("url");
        String driverClass = properties.getProperty("driverClass");

        Class.forName(driverClass);

        return DriverManager.getConnection(url, user, password);
    }

    public static void closeResource(Connection conn, Statement ps){
        try {
            if(ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeResource(Connection conn, Statement ps, ResultSet rs){
        try {
            if(ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if(rs != null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
