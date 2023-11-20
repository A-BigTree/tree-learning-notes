# SSM

**@author: Shuxin_Wang**

**@time: 2023.04.08**

---

[toc]

---

# 1 Spring和Mybatis整合

**本质**：Spring接管一切，代码更加简洁。

- Spring管理数据源，连接数据库；
- Spring管理`SqlSessionFactory`，开启`SqlSession`的细节被屏蔽了；
- Spring的IOC容器负责注入Mapper接口；
- Spring管理声明式事务；

## 1.1 连接数据库

### 1.1.1 整体所需依赖

```xml
<!-- SpringMVC -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
    <version>5.3.1</version>
</dependency>
 
<!-- Spring 持久化层所需依赖 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-orm</artifactId>
    <version>5.3.1</version>
</dependency>
 
<!-- 日志 -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.2.3</version>
</dependency>
 
<!-- ServletAPI -->
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.1.0</version>
    <scope>provided</scope>
</dependency>
 
<!-- Spring5和Thymeleaf整合包 -->
<dependency>
    <groupId>org.thymeleaf</groupId>
    <artifactId>thymeleaf-spring5</artifactId>
    <version>3.0.12.RELEASE</version>
</dependency>
 
<!-- Mybatis 和 Spring 的整合包 -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>2.0.6</version>
</dependency>
 
<!-- Mybatis核心 -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.5.7</version>
</dependency>
 
<!-- MySQL驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.3</version>
</dependency>
 
<!-- 数据源 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.0.31</version>
</dependency>
 
<!-- Spring 的测试功能 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-test</artifactId>
    <version>5.3.1</version>
</dependency>
 
<!-- junit4 -->
<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>4.12</version>
    <scope>test</scope>
</dependency>

<!-- Lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.12</version>
    <scope>provided</scope>
</dependency>
```

### 1.1.2 配置数据源

#### 创建jdbc.properties

```properties
jdbc.user=root
jdbc.password=atguigu
jdbc.url=jdbc:mysql://192.168.200.100:3306/mybatis-example
jdbc.driver=com.mysql.jdbc.Driver
```

#### 加入日志和Spring配置文件

```xml
<!-- 加载外部属性文件 -->
<context:property-placeholder location="classpath:jdbc.properties"/>
 
<!-- 配置数据源 -->
<bean id="druidDataSource" class="com.alibaba.druid.pool.DruidDataSource">
    <property name="username" value="${jdbc.user}"/>
    <property name="password" value="${jdbc.password}"/>
    <property name="driverClassName" value="${jdbc.driver}"/>
    <property name="url" value="${jdbc.url}"/>
</bean>
```

#### 测试

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(value = "classpath:spring-persist.xml")
@Slf4j
public class SSMTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void testConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        log.debug("connection = " + connection);
    }

}
```

## 1.2 整合Mybatis

### 1.2.1 思路

<img src="SSM.assets/image-20230408173211001.png" alt="image-20230408173211001" style="zoom:50%;" />

### 1.2.2 Mybatis-Spring技术

#### 版本匹配说明

| MyBatis-Spring | MyBatis | Spring Framework | Spring Batch | Java     |
| :------------- | :------ | :--------------- | :----------- | :------- |
| **3.0**        | 3.5+    | 6.0+             | 5.0+         | Java 17+ |
| **2.1**        | 3.5+    | 5.x              | 4.x          | Java 8+  |
| **2.0**        | 3.5+    | 5.x              | 4.x          | Java 8+  |
| **1.3**        | 3.4+    | 3.2.2+           | 2.1+         | Java 6+  |

#### 加入依赖

```xml
<!-- https://mvnrepository.com/artifact/org.mybatis/mybatis-spring -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>2.0.6</version>
</dependency>
```

### 1.2.3 创建Mybatis全局配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
 
    <!-- Mybatis全局配置 -->
    <settings>
        <!-- 将数据库表字段映射到驼峰式命名的Java实体类属性中 -->
        <!-- 数据库表字段格式：单词_单词 -->
        <!-- Java实体类属性：首字母小写的驼峰式命名 -->
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>
 
</configuration>
```

### 1.2.4 创建模型

物理模型继续沿用之前创建的t_emp表。

```Java
public class Emp {
 
    private Integer empId;
    private String empName;
    private Double empSalary;
```

### 1.2.5 创建Mapper接口

```java
public interface EmpMapper {
 
    List<Emp> selectAll();
 
}
```

### 1.2.6 创建Mapper配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
 
<!-- mapper是根标签。namespace属性是找到当前配置的依据 -->
<!-- 由于最理想的Mybatis使用方式是：通过Mapper接口调用接口方法，访问数据库 -->
<!-- 这样的理想方式要求：能够通过接口全类名找到Mapper配置 -->
<!-- 所以：我们就用Mapper接口的全类名来给namespace属性赋值 -->
<mapper namespace="com.atguigu.ssm.mapper.EmpMapper">
 
    <!-- List<Emp> selectAll(); -->
    <select id="selectAll" resultType="Emp">
        select emp_id,emp_name,emp_salary from t_emp
    </select>
 
</mapper>
```

### 1.2.7 配置`SqlSessionFactoryBean`

#### 保留Mybatis全局配置文件

```xml
<!-- 配置 SqlSessionFactoryBean -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
 
    <!-- 指定 Mybatis 全局配置文件位置 -->
    <property name="configLocation" value="classpath:mybatis-config.xml"/>
 
    <!-- 指定 Mapper 配置文件位置 -->
    <property name="mapperLocations" value="classpath:mappers/*Mapper.xml"/>
 
    <!-- 装配数据源 -->
    <property name="dataSource" ref="druidDataSource"/>
 
</bean>
```

#### 彻底舍弃Mybatis全局配置文件

```xml
<!-- 配置 SqlSessionFactoryBean -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
 
    <!-- 舍弃 Mybatis 全局配置文件，使用 configuration 属性 -->
    <property name="configuration">
        <bean class="org.apache.ibatis.session.Configuration">
            <property name="mapUnderscoreToCamelCase" value="true"/>
        </bean>
    </property>
 
    <!-- 舍弃 Mybatis 全局配置文件，使用 typeAliasesPackage 属性配置实体类所在包 -->
    <property name="typeAliasesPackage" value="com.atguigu.ssm.entity"/>
 
    <!-- 指定 Mapper 配置文件位置 -->
    <property name="mapperLocations" value="classpath:mappers/*Mapper.xml"/>
 
    <!-- 装配数据源 -->
    <property name="dataSource" ref="druidDataSource"/>
 
</bean>
```

> 注意：上面两种方式如果并存，会抛出异常： `java.lang.IllegalStateException: Property 'configuration' and 'configLocation' can not specified with together`



### 1.2.8 配置接口扫描器

#### 使用扫描器

```xml
<!-- 配置 Mapper 接口类型的bean的扫描器 -->
<bean id="mapperScannerConfigurer" class="org.mybatis.spring.mapper.MapperScannerConfigurer">
    <property name="basePackage" value="com.atguigu.ssm.mapper"/>
</bean>
```

#### 使用`mybatis-spring`命名空间

```xml
<mybatis-spring:scan base-package="com.atguigu.ssm.mapper"/>
```



### 1.2.9 测试

```java
@Autowired
private EmpMapper empMapper;

@Test
public void testMybatis() {
    List<Emp> empList = empMapper.selectAll();
 
    for (Emp emp : empList) {
        log.debug(emp.toString());
    }
}
```



## 1.3 加入声明式事务

### 1.3.1 配置事务管理器

```xml
<!-- 配置事务管理器 -->
<bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
    <!-- 装配数据源 -->
    <property name="dataSource" ref="druidDataSource"/>
</bean>
 
<!-- 开启基于注解的声明式事务 -->
<tx:annotation-driven transaction-manager="transactionManager"/>
```

### 1.3.2 创建Service组件

```java
@Service
public class EmpServiceImpl implements EmpService {
 
    @Autowired
    private EmpMapper empMapper;
 
    @Override
    @Transactional(readOnly = true)
    public List<Emp> getAll() {
        return empMapper.selectAll();
    }
}
```

### 1.3.3 配置自动扫描的包

```xml
<context:component-scan base-package="com.atguigu.ssm.service"/>
```

### 1.3.4 测试

```java
@Autowired
private EmpService empService;
 
@Test
public void testTx() {
    List<Emp> empList = empService.getAll();
    for (Emp emp : empList) {
        log.debug("emp = " + emp);
    }
}
```

> 在框架打印的日志中能够看到事务打开、提交、回滚等操作即可确认声明式事务已生效。



# 2 Spring和SpringMVC整合

## 2.1 配置`web.xml`

```xml
<!-- ContextLoaderListener -->
<!-- 通过 context-param 指定 Spring 框架的配置文件位置 -->
<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:spring-persist.xml</param-value>
</context-param>
 
<!-- 配置 ContextLoaderListener -->
<listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>
 
<!-- DispatcherServlet -->
<servlet>
    <servlet-name>dispatcherServlet</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value>classpath:spring-mvc.xml</param-value>
    </init-param>
    <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
    <servlet-name>dispatcherServlet</servlet-name>
    <url-pattern>/</url-pattern>
</servlet-mapping>
 
<!-- 需要注意两个 Filter 的顺序：字符集过绿器在前，转换请求方式过绿器在后 -->
<!-- CharacterEncodingFilter -->
<filter>
    <filter-name>characterEncodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
        <param-name>encoding</param-name>
        <param-value>UTF-8</param-value>
    </init-param>
    <init-param>
        <param-name>forceRequestEncoding</param-name>
        <param-value>true</param-value>
    </init-param>
    <init-param>
        <param-name>forceResponseEncoding</param-name>
        <param-value>true</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>characterEncodingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
 
<!-- HiddenHttpMethodFilter -->
<filter>
    <filter-name>hiddenHttpMethodFilter</filter-name>
    <filter-class>org.springframework.web.filter.HiddenHttpMethodFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>hiddenHttpMethodFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```



## 2.2 配置SpringMVC

```xml
<!-- SpringMVC 只sao描 handler 类所在的包 -->
<!-- Spring 和 SpringMVC sao描各自负责的组件，sao描的范围没有重合的部分，直接避免了重复创建对象 -->
<context:component-scan base-package="com.atguigu.ssm.handler"/>
 
<!-- 配置 Thymeleaf 的视图解析器 -->
<bean id="thymeleafViewResolver" class="org.thymeleaf.spring5.view.ThymeleafViewResolver">
    <property name="order" value="1"/>
    <property name="characterEncoding" value="UTF-8"/>
    <property name="templateEngine">
        <bean class="org.thymeleaf.spring5.SpringTemplateEngine">
            <property name="templateResolver">
                <bean class="org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver">
                    <property name="prefix" value="/WEB-INF/templates/"/>
                    <property name="suffix" value=".html"/>
                    <property name="characterEncoding" value="UTF-8"/>
                    <property name="templateMode" value="HTML5"/>
                </bean>
            </property>
        </bean>
    </property>
</bean>
 
<!-- SpringMVC 注解驱动（标配） -->
<mvc:annotation-driven/>
 
<!-- 对于没有映射的请求直接转发放行，主要是静态资源 -->
<mvc:default-servlet-handler/>
 
<!-- 匹配请求路径直接前往视图，不经过 handler 方法 -->
<mvc:view-controller path="/" view-name="portal"/>
<mvc:view-controller path="/index.html" view-name="portal"/>
```

## 2.3 显示数据列表

### 2.3.1 显示首页

#### 声明view-controller

```xml
<!-- 匹配请求路径直接前往视图，不经过 handler 方法 -->
<mvc:view-controller path="/" view-name="portal"/>
<mvc:view-controller path="/index.html" view-name="portal"/>
```

#### 创建页面

```html
<!DOCTYPE html>
<html lang="en" xml:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Title</title>
</head>
<body>
	<a th:href="@{/get/all}">显示全部数据</a>
</body>
</html>
```

### 2.3.2 显示数据列表

#### 创建处理类

```java
@Controller
public class EmpHandler {
 
    @Autowired
    private EmpService empService;
     
    @RequestMapping("/get/all")
    public String getAll(Model model) {
 
        // 1、查询数据
        List<Emp> empList = empService.getAll();
         
        // 2.存入模型
        model.addAttribute("empList", empList);
 
        return "emp-list";
    }
 
}
```

#### 页面显示

```html
<table>
    <tr>
        <th>ID</th>
        <th>NAME</th>
        <th>SALARY</th>
    </tr>
    <tbody th:if="${#lists.isEmpty(empList)}">
        <tr>
            <td colspan="3">抱歉！没有查询到数据！</td>
        </tr>
    </tbody>
    <tbody th:if="${not #lists.isEmpty(empList)}">
        <tr th:each="emp : ${empList}">
            <td th:text="${emp.empId}">这里显示员工ID</td>
            <td th:text="${emp.empName}">这里显示员工NAME</td>
            <td th:text="${emp.empSalary}">这里显示员工SALARY</td>
        </tr>
    </tbody>
</table>
 
<a th:href="@{/}">回首页</a>
```



# 3 分页

## 3.1 分页的概念

### 3.1.1 提出问题

如果应用程序显示数据不分页，会有三个问题：

- 用户查看数据非常不方便。
- 所有数据不分冷热全部显示出来，冷数据白白占用存储空间，浪费内存。
- 在服务器端查询全部数据占用内存很大，给整个系统增加了很大压力。

### 3.1.2 分页的概念

把系统中要显示的数据分成较小的单元，每个单元作为『一页』显示给用户。每次访问服务器只查询一页数据。

分页的好处：

- 用户体验较好。
- 服务器端每次只查询一部分数据，内存压力减小。
- 对冷数据减少查询的次数，据此对系统性能进行优化。

### 3.1.3 实现分页的基本逻辑

#### 物理分页

具体数据库不同，分页语法有区别。下面我们以 MySQL 为例来说明。MySQL 的分页需要借助 LIMIT 子句来完成。

```SQL
select emp_id,emp_name,emp_salary from t_emp limit 0,5; # 查询第一页数据
select emp_id,emp_name,emp_salary from t_emp limit 5,5; # 查询第二页数据
select emp_id,emp_name,emp_salary from t_emp limit 10,5;# 查询第三页数据
```

公式：
$$
limit\ \ (pageNo-1)\times pageSize,pageSize
$$
注意：在 SQL 的语法中，要么就不加LIMIT子句，如果非要写LIMIT子句，那么LIMIT子句必须出现在SQL语句最后，什么ORDER BY、GROUP BY、WHERE 等子句都必须在 LIMIT 子句前面。

#### 逻辑分页

为了能够在页面上全面显示分页相关的细节数据，总页数需要计算得到。

#### 分页流程

- 查询总记录数
- 查询当前页数据
- 根据总记录数和每页条数计算总页数
- 在1~总页数之间修正页码
- 封装上述所有数据，发送到页面显示

## 3.2 Mybatis分页插件

### 3.2.1 操作

#### 导入依赖

```xml
<!-- https://mvnrepository.com/artifact/com.github.pagehelper/pagehelper -->
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper</artifactId>
    <version>5.2.0</version>
</dependency>
```

#### 配置

```xml
<!-- 配置 SqlSessionFactoryBean -->
<bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
 
    ……
 
    <!-- 在 plugins 属性中配置 Mybatis 插件 -->
    <property name="plugins">
        <array>
            <bean class="com.github.pagehelper.PageInterceptor">
                <property name="properties">
                    <props>
                        <!-- 设置 reasonable 为 true 表示将页码进行合理化修正。页码的有效范围：1~总页数 -->
                        <prop key="reasonable">true</prop>
                        
                        <!-- 数据库方言：同样都是 SQL 语句，拿到不同数据库中，在语法上会有差异 -->
                        <!-- 默认情况下，按照 MySQL 作为数据库方言来运行 -->
                        <prop key="helperDialect">mysql</prop>
                    </props>
                </property>
            </bean>
        </array>
    </property>
 
</bean>
```

#### 非侵入式

开启分页功能，就在 SQL 语句后面附加 LIMIT 子句并查询总记录数；不开启就还是按照原样查询。分页功能对原有的 Mapper 接口、SQL 语句没有任何影响。这个效果可以称之为是非侵入式，也可以说是可插拔的。

## 3.3 案例中分页

### 3.3.1 首页超链接

```html
<a th:href="@{/get/page/1}">显示分页数据</a>
```

### 3.3.2 处理方法

```java
@RequestMapping("/get/page/{pageNo}")
public String getPage(
        @PathVariable("pageNo") Integer pageNo, 
        Model model) {
 
    // PageInfo 对象封装了和分页相关的所有信息
    PageInfo<Emp> pageInfo = empService.getPageInfo(pageNo);
    
    // 将 PageInfo 对象存入模型
    model.addAttribute("pageInfo", pageInfo);
    
    return "emp-page";
}
```

### 3.3.3 Service方法

```java
@Override
public PageInfo<Emp> getPageInfo(Integer pageNo) {
 
    // 1、确定每页显示数据的条数
    int pageSize = 5;
 
    // 2、设定分页数据：开启分页功能。开启后，后面执行的 SELECT 语句会自动被附加 LIMIT 子句，
    // 而且会自动查询总记录数
    PageHelper.startPage(pageNo, pageSize);
 
    // 3、正常执行查询
    List<Emp> empList = empMapper.selectAll();
 
    // 4、封装为 PageInfo 对象返回
    return new PageInfo<>(empList);
}
```

### 3.3.4 页面展示

#### 显示数据

```html
……
  <tr th:each="emp : ${pageInfo.list}">
……
```

#### 显示翻页导航栏

```html
<tr>
    <td colspan="5">
 
        <span th:each="targetNum : ${pageInfo.navigatepageNums}">
 
            <!-- 不是当前页显示为超链接 -->
            <a th:if="${targetNum != pageInfo.pageNum}"
               th:href="@{/employee/page/}+${targetNum}"
               th:text="'['+${targetNum}+']'">目标页面的页码</a>
 
            <!-- 是当前页不需要显示为超链接 -->
            <span th:if="${targetNum == pageInfo.pageNum}" th:text="'['+${targetNum}+']'">当前页页码</span>
 
        </span>
    </td>
</tr>
<tr>
    <td colspan="5">
        <span th:if="${pageInfo.hasPreviousPage}">
            <a th:href="@{/employee/page/1}">首页</a>
            <a th:href="@{/employee/page/}+${pageInfo.prePage}">上一页</a>
        </span>
 
        [[${pageInfo.pageNum}]]/[[${pageInfo.pages}]]
        [[${pageInfo.pageNum}+'/'+${pageInfo.pages}]]
 
        <span th:if="${pageInfo.hasNextPage}">
            <a th:href="@{/employee/page/}+${pageInfo.nextPage}">下一页</a>
            <a th:href="@{/employee/page/}+${pageInfo.pages}">末页</a>
        </span>
    </td>
</tr>
<tr>
    <td colspan="5">
        <!-- 绑定值改变事件响应函数 -->
        <input id="jumpToPageNumInput" type="text" name="jumpToPageNum" placeholder="请输入您想直接跳转的页码" />
    </td>
</tr>
```

#### 配套的JS代码

```js
// 获取页码文本框的元素对象
var jumpPageNumEle = document.getElementById("jumpToPageNumInput");
 
// 给页码文本框绑定值改变响应函数
jumpPageNumEle.onchange = function () {
 
    // 触发值改变响应函数后，获取当前文本框中用户输入的值
    var targetNum = this.value;
 
    // 检测用户输入的数据是否是数字
    if (isNaN(targetNum)) {
 
        // 如果不是数字则恢复文本框
        this.value = "";
 
        // 当前函数结束
        return ;
    }
 
    // 如果用户输入合法，则跳转页面
    // [[@{/employee/page/}]] 表示解析 Thymeleaf 表达式
    window.location.href = "[[@{/employee/page/}]]" + targetNum;
 
};
```

