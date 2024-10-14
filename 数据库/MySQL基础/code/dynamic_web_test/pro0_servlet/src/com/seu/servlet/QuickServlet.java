package com.seu.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class QuickServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getContextPath();
        System.out.println(path);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取包含全部请求参数的Map
        Map<String, String[]> parameterMap = request.getParameterMap();

        // 遍历这个包含全部请求参数的Map
        Set<String> keySet = parameterMap.keySet();

        for (String key : keySet) {

            String[] values = parameterMap.get(key);

            System.out.println(key + "=" + Arrays.asList(values));
        }

        System.out.println("---------------------------");

        // 根据请求参数名称获取指定的请求参数值
        // getParameter()方法：获取单选框的请求参数
        String season = request.getParameter("season");
        System.out.println("season = " + season);

        // getParameter()方法：获取多选框的请求参数
        // 只能获取到多个值中的第一个
        String team = request.getParameter("team");
        System.out.println("team = " + team);

        // getParameterValues()方法：取单选框的请求参数
        String[] seasons = request.getParameterValues("season");
        System.out.println("Arrays.asList(seasons) = " + Arrays.asList(seasons));

        // getParameterValues()方法：取多选框的请求参数
        String[] teams = request.getParameterValues("team");
        System.out.println("Arrays.asList(teams) = " + Arrays.asList(teams));
    }
}
