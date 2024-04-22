/**
 * ==================================================
 * Project: dynamic_web_test
 * Package: com.dao
 * =====================================================
 * Title: BaseDao.java
 * Created: [2023/3/22 10:18] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/22, created by Shuxin-Wang.
 * 2.
 */

package com.dao;

import com.utils.JDBCUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDao<T> {
    private final Class<T> type;

    @SuppressWarnings("unchecked")
    public BaseDao(){
        Class<?> clazz = this.getClass();
        ParameterizedType parameterizedType = (ParameterizedType) clazz.getGenericSuperclass();
        Type[]types = parameterizedType.getActualTypeArguments();
        type = (Class<T>) types[0];
    }

    public int update(String sql, Object...params){
        int count = -1;
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
        }finally{
            JDBCUtils.closeResource(ps);
            JDBCUtils.releaseConnection(conn);
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
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; i++){
                ps.setObject(i + 1, params[i]);
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            if(rs.next()){
                bean = type.getDeclaredConstructor().newInstance();
                for(int i = 0; i < columnCount; i++){
                    Object columnVal = rs.getObject(i + 1);
                    String columnLabel = rsmd.getColumnLabel(i + 1);
                    Field field = type.getField(columnLabel);
                    field.setAccessible(true);
                    field.set(bean, columnVal);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            JDBCUtils.closeResource(rs, ps);
            JDBCUtils.releaseConnection(conn);
        }
        return bean;
    }

    public List<T> getBeanList(String sql, Object...params){
        List<T> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try{
            conn = JDBCUtils.getConnection();
            ps = conn.prepareStatement(sql);
            for(int i = 0; i < params.length; i++){
                ps.setObject(i + 1, params[i]);
            }
            rs = ps.executeQuery();
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while(rs.next()){
                T bean = type.getDeclaredConstructor().newInstance();
                for(int i = 0; i < columnCount; i++){
                    Object columnVal = rs.getObject(i + 1);
                    String columnLabel = rsmd.getColumnLabel(i + 1);
                    Field field = type.getDeclaredField(columnLabel);
                    field.setAccessible(true);
                    field.set(bean, columnVal);
                }
                list.add(bean);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            JDBCUtils.closeResource(rs, ps);
            JDBCUtils.releaseConnection(conn);
        }
        return list;
    }
}
