/**
 * ==================================================
 * Project: dynamic_web_test
 * Package: com.utils
 * =====================================================
 * Title: JDBCUtils.java
 * Created: [2023/3/22 9:59] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/22, created by Shuxin-Wang.
 * 2.
 */

package com.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class JDBCUtils {
    private static DataSource dataSource;
    // 初始化数据库连接池
    static {
        Properties properties = new Properties();
        InputStream inputStream = JDBCUtils.class.getClassLoader().getResourceAsStream("jdbc.properties");
        try{
            properties.load(inputStream);
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){
        Connection connection = null;
        try{
            connection = dataSource.getConnection();
        }catch (SQLException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void releaseConnection(Connection connection){
        if(connection != null){
            try{
                connection.close();
            }catch (SQLException e){
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public static void closeResource(Statement ps) {
        try {
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void closeResource(ResultSet rs, Statement ps){
        try {
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try{
            if(rs != null)
                rs.close();
        }catch (SQLException e){
            e.printStackTrace();
        }
    }
}
