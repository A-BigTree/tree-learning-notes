/**
 * ==================================================
 * Project: dynamic_web_test
 * Package: com.dao.api
 * =====================================================
 * Title: UserDao.java
 * Created: [2023/3/22 20:57] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/22, created by Shuxin-Wang.
 * 2.
 */

package com.dao.api;

import com.beans.User;

import java.util.List;

public interface UserDao {
    /**
     * @descirption 返回所用用户
     * @return: 用户列表
     */
    List<User> getAllUsers();

    void deleteUserById(String userID);

    void addUser(User user);

    void modifyUser(User newUser);
}
