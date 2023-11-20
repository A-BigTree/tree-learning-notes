/**
 * ==================================================
 * Project: jdbc_test
 * Package: com
 * =====================================================
 * Title: Main.java
 * Created: [2023/3/15 20:53] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/15, created by Shuxin-Wang.
 * 2.
 */

package com.test;

import com.beans.User;
import com.impl.UserImpl;

public class MainTest {
    public static void main(String[]args){
        UserImpl userImpl = new UserImpl();
        // 查找
        System.out.println(userImpl.getUserById("000000"));
        User user1 = new User("000004", "小云", 1000.99);
        // 添加
        userImpl.addUser(user1);
        System.out.println(userImpl.getUserById("000004"));
        // 修改
        user1.setSalary(6666.00);
        userImpl.modifyUser(user1);
        System.out.println(userImpl.getUserById("000004"));
        // 删除
        userImpl.deleteUser(user1.getUserId());
    }
}
