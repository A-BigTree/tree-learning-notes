/**
 * ==================================================
 * Project: jdbc_test
 * Package: com.dao
 * =====================================================
 * Title: BaseDao.java
 * Created: [2023/3/15 17:10] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/15, created by Shuxin-Wang.
 * 2.
 */

package com.dao;

import com.utils.JDBCUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;

public abstract class BaseDao<T> {
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    public BaseDao() {
        // 利用反射确定泛型`T`的类型
        Class<?> clazz = this.getClass();
        ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        Type[] types = parameterizedType.getActualTypeArguments();
        type = (Class<T>) types[0];
    }

    public int update(String sql, Object...params){
        int count = 0;
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = JDBCUtils.getConnection();
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; i++){
                ps.setObject(i + 1, params[i]);
            }
            count = ps.executeUpdate();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            JDBCUtils.closeResource(conn, ps);
        }
        return count;
    }

    public T getBean(String sql, Object...params){
        T bean = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            conn = JDBCUtils.getConnection();
            System.out.println(conn);
            ps = conn.prepareStatement(sql);
            for(int i = 0; i<params.length; i++){
                ps.setObject(i + 1, params[i]);
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            if(rs.next()){
                //利用反射构建目标实例
                bean = type.getDeclaredConstructor().newInstance();
                for(int i = 0; i<columnCount; i++){
                    Object columnVal = rs.getObject(i + 1);
                    String columnLabel = rsmd.getColumnLabel(i + 1);
                    Field field = type.getDeclaredField(columnLabel);
                    field.setAccessible(true);
                    field.set(bean, columnVal);
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            JDBCUtils.closeResource(conn, ps, rs);
        }
        return bean;
    }
}
