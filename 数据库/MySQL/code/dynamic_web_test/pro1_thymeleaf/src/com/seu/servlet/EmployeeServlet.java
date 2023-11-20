package com.seu.servlet;

import com.seu.beans.Employee;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeServlet extends ViewBaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Employee> employeeList = new ArrayList<>();
        employeeList.add(new Employee(100, "Wang", 300.0));
        employeeList.add(new Employee(1001, "Chen", 1000.0));
        employeeList.add(new Employee(1001, "Li", 888.8));

        request.setAttribute("employeeList", employeeList);
        super.processTemplate("list", request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
