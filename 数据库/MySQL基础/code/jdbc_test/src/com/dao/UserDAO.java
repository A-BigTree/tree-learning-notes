/**
 * ==================================================
 * Project: jdbc_test
 * Package: com.dao
 * =====================================================
 * Title: UserDAO.java
 * Created: [2023/3/15 20:45] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/15, created by Shuxin-Wang.
 * 2.
 */

package com.dao;

import com.beans.User;

import java.sql.Connection;

public interface UserDAO {
    User getUserById(String userId);

    void addUser(User user);

    void modifyUser(User user);

    void deleteUser(String userId);
}
