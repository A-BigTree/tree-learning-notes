/**
 * ==================================================
 * Project: jdbc_test
 * Package: com.beans
 * =====================================================
 * Title: User.java
 * Created: [2023/3/15 17:21] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/15, created by Shuxin-Wang.
 * 2.
 */

package com.beans;



public class User{
    private String userId;
    private String userName;
    private double salary;

    public User(){

    }

    public User(String userId, String userName, double salary){
        this.userId = userId;
        this.userName = userName;
        this.salary = salary;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public double getSalary() {
        return salary;
    }
    @Override
    public String toString(){
        return "Id: " + userId +"\tName: " + userName + "\tSalary: " + salary;
    }
}
