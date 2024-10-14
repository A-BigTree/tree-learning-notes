/**
 * ==================================================
 * Project: dynamic_web_test
 * Package: com.seu.beans
 * =====================================================
 * Title: Employee.java
 * Created: [2023/3/21 14:18] by Shuxin-Wang
 * =====================================================
 * Description: description here
 * =====================================================
 * Revised History:
 * 1. 2023/3/21, created by Shuxin-Wang.
 * 2.
 */

package com.seu.beans;

public class Employee {
    private Integer empId;
    private String empName;
    private Double empSalary;

    public Employee(Integer empId, String empName, Double empSalary){
        this.empId = empId;
        this.empName = empName;
        this.empSalary = empSalary;
    }

    public void setEmpId(Integer empId) {
        this.empId = empId;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public void setEmpSalary(Double empSalary) {
        this.empSalary = empSalary;
    }

    public Integer getEmpId() {
        return empId;
    }

    public String getEmpName() {
        return empName;
    }

    public Double getEmpSalary() {
        return empSalary;
    }
}
