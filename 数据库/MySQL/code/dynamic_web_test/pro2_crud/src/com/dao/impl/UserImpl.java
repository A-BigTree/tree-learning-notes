/**
 * ==================================================
 * Project: dynamic_web_test
 * Package: com.dao.impl
 * =====================================================
 * Title: UserImpl.java
 * Created: [2023/3/22 21:02] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/22, created by Shuxin-Wang.
 * 2.
 */

package com.dao.impl;

import com.beans.User;
import com.dao.BaseDao;
import com.dao.api.UserDao;

import java.util.List;

public class UserImpl extends BaseDao<User> implements UserDao {
    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT id userId, name userName, salary salary FROM user";
        return getBeanList(sql);
    }

    @Override
    public void deleteUserById(String userID) {
        String sql = "DELETE FROM user WHERE id = ?";
        update(sql, userID);
    }

    @Override
    public void addUser(User user) {
        String sql = "INSERT INTO user(id, name, salary) VALUES(?,?,?)";
        update(sql, user.getUserId(), user.getUserName(), user.getSalary());
    }

    @Override
    public void modifyUser(User newUser) {
        String sql = "UPDATE user SET name = ?, salary = ? WHERE id = ?";
        update(sql, newUser.getUserName(), newUser.getSalary(), newUser.getUserId());
    }
}
