/**
 * ==================================================
 * Project: jdbc_test
 * Package: com.impl
 * =====================================================
 * Title: UserImpl.java
 * Created: [2023/3/15 20:49] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/15, created by Shuxin-Wang.
 * 2.
 */

package com.impl;

import com.beans.User;
import com.dao.BaseDao;
import com.dao.UserDAO;


public class UserImpl extends BaseDao<User> implements UserDAO {
    @Override
    public User getUserById(String userId) {
        String sql = "SELECT id userId, name userName, salary salary FROM user WHERE id = ?";
        return getBean(sql, userId);
    }

    @Override
    public void addUser(User user) {
        String sql = "INSERT INTO user(id, name, salary) VALUES(?,?,?)";
        update(sql, user.getUserId(), user.getUserName(), user.getSalary());
    }

    @Override
    public void modifyUser(User user) {
        String sql = "UPDATE user SET name = ?, salary = ? WHERE id = ?";
        update(sql, user.getUserName(), user.getSalary(), user.getUserId());
    }

    @Override
    public void deleteUser(String userId) {
        String sql = "DELETE FROM user WHERE id = ?";
        update(sql, userId);
    }
}
