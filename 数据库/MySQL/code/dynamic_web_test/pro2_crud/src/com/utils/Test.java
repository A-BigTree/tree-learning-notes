/**
 * ==================================================
 * Project: dynamic_web_test
 * Package: com.utils
 * =====================================================
 * Title: Test.java
 * Created: [2023/3/22 10:09] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/22, created by Shuxin-Wang.
 * 2.
 */

package com.utils;

import com.dao.impl.UserImpl;

import java.sql.Connection;

public class Test {
    public static void main(String[]args){
        System.out.println(new UserImpl().getAllUsers());
    }
}
