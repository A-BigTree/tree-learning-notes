# 4 Tomcat

## 4.1 配置文件

### 4.1.1 配置

#### 设置

所谓设置其实就是通过修改一个一个的**参数**，告诉**应用软件**它该**怎么工作**。

#### 配置

本质上配置和设置是一样的，只是对象和形式不同：

|      | 配置                       | 设置               |
| ---- | -------------------------- | ------------------ |
| 对象 | 开发中使用的应用程序或框架 | 应用软件           |
| 形式 | 特定格式的配置文件         | 应用软件的友好界面 |

### 4.1.2 配置文件

#### XML

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    <!-- 配置SpringMVC前端控制器 -->
    <servlet>
        <servlet-name>dispatcherServlet</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <!-- 在初始化参数中指定SpringMVC配置文件位置 -->
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:spring-mvc.xml</param-value>
        </init-param>
        <!-- 设置当前Servlet创建对象的时机是在Web应用启动时 -->
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>dispatcherServlet</servlet-name>
        <!-- url-pattern配置斜杠表示匹配所有请求 -->
        <!-- 两种可选的配置方式：
                1、斜杠开头：/
                2、包含星号：*.atguigu
             不允许的配置方式：前面有斜杠，中间有星号
                /*.app
         -->
        <url-pattern>/</url-pattern>
    </servlet-mapping>
</web-app>
```

##### 名词介绍

**可扩展标记语言（eXtensible Markup Language，XML）**，XML和HTML一样都是标记语言，也就是说它们的基本语法都是标签。

##### 可扩展

**可扩展**三个字**表面上**的意思是XML允许**自定义格式**。但是**不代表** **可以随便写**。

<img src="./image/image-20230317142518169.png" alt="image-20230317142518169" style="zoom:50%;" />

在XML基本语法规范的基础上，你使用的那些第三方应用程序、框架会通过设计**『XML约束』**的方式**『强制规定』**配置文件中可以写什么和怎么写，规定之外的都不可以写。

XML基本语法这个知识点的定位是：我们不需要从零开始，从头到尾的一行一行编写XML文档，而是在第三方应用程序、框架**已提供的配置文件**的基础上**修改**。要改成什么样取决于你的需求，而怎么改取决于**XML基本语法**和**具体的XML约束**。

##### XML基本语法

- **<u>XML声明</u>**

这部分基本上就是固定格式，大家知道就好

```xml
<?xml version="1.0" encoding="UTF-8"?>
```

- **<u>根标签</u>**

根标签有且只能有一个；

- **<u>标签关闭</u>**
  - 双标签：开始标签和结束标签必须成对出现；
  - 单标签：单标签在标签内关闭；
- **<u>标签嵌套</u>**
  - 可以嵌套，但是==**<u>不能交叉嵌套</u>**==；
- **<u>注释不能嵌套</u>**
- **<u>标签名、属性名建议使用小写字母</u>**
- **<u>属性</u>**
  - 属性必须有值；
  - 属性值必须加引号，单双都行；

看到这里大家一定会发现XML的基本语法和HTML的基本语法简直如出一辙。其实这不是偶然的，==**<u>XML基本语法+HTML约束=HTML语法</u>**==。在逻辑上HTML确实是XML的子集。

```html
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
```

从HTML4.01版本的文档类型声明中可以看出，这里使用的DTD类型的XML约束。也就是说`http://www.w3.org/TR/html4/loose.dtd`这个文件定义了HTML文档中可以写哪些标签，标签内可以写哪些属性，某个标签可以有什么样的子标签。

##### XML约束

将来我们主要就是根据XML约束中的规定来编写XML配置文件。而XML约束主要包括DTD和Schema两种。如果XML配置文件使用的是DTD，那么对我们几乎没有影响。如果是Schema约束，需要我们稍微参与一点点。

- **<u>文档类型定义（Document Type Definition，DTD）</u>**

将来在IDEA中有代码提示的协助，在DTD文档的约束下进行配置非常简单。

```dtd
<!ENTITY % fontstyle
 "TT | I | B | U | S | STRIKE | BIG | SMALL">

<!ENTITY % phrase "EM | STRONG | DFN | CODE |
                   SAMP | KBD | VAR | CITE | ABBR | ACRONYM" >

<!ENTITY % special
   "A | IMG | APPLET | OBJECT | FONT | BASEFONT | BR | SCRIPT |
    MAP | Q | SUB | SUP | SPAN | BDO | IFRAME">

<!ENTITY % formctrl "INPUT | SELECT | TEXTAREA | LABEL | BUTTON">

<!-- %inline; covers inline or "text-level" elements -->
<!ENTITY % inline "#PCDATA | %fontstyle; | %phrase; | %special; | %formctrl;">

<!ELEMENT (%fontstyle;|%phrase;) - - (%inline;)*>
<!ATTLIST (%fontstyle;|%phrase;)
  %attrs;                              -- %coreattrs, %i18n, %events --
  >
```

- **<u>Schema</u>**

我们将来使用SSM框架中的`Spring`、`SpringMVC`框架时，会涉及到一点点对`Schema`约束的设置。不过不必紧张，有IDEA的支持操作会非常简单，我们现在只需要理解基本概念即可。

首先我们要理解一个概念：**『名称空间』**，英文：`name space`。

<img src="./image/image-20230317143736869.png" alt="image-20230317143736869" style="zoom:50%;" />

Schema约束要求我们一个XML文档中，所有标签，所有属性都必须在约束中有明确的定义。

下面我们以web.xml的约束声明为例来做个说明：

```xml
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
```

| 属性名             | 作用                                                         |
| ------------------ | ------------------------------------------------------------ |
| xmlns              | 指出当前XML文档约束规则的名称空间在哪里 我们就是通过这个属性来引用一个具体的名称空间 |
| xmlns:xsi          | 指出xmlns这个属性是在哪个约束文档中被定义的                  |
| xsi:schemaLocation | 语法格式：在xsi名称空间下引用schemaLocation属性 配置含义：指定当前XML文档中所用到的约束文档本身的文件的地址 |

`xmlns`和`xsi:schemaLocation`对应关系如下图：

<img src="./image/image-20230317144044210.png" alt="image-20230317144044210" style="zoom:50%;" />

引入多个名称空间的例子如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:context="http://www.springframework.org/schema/context"
	xmlns:mvc="http://www.springframework.org/schema/mvc"
	xsi:schemaLocation="http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc-4.0.xsd
		http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd">

	<context:component-scan base-package="com.atguigu.crud.component"/>
	
	<bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
		<property name="prefix" value="/WEB-INF/pages/"/>
		<property name="suffix" value=".jsp"/>
	</bean>
	
	<mvc:default-servlet-handler/>
	<mvc:annotation-driven/>

</beans>
```

看到这么复杂，这么长的名称空间字符串，我们会觉得很担心，根本记不住。但是其实不需要记，在IDEA中编写配置文件时，IDEA会协助我们导入，会有提示。我们**理解各个部分的含义能够调整**即可。

#### 属性文件

以properties作为扩展名的文件。

```properties
atguigu.jdbc.url=jdbc:mysql://192.168.198.100:3306/mybatis1026
atguigu.jdbc.driver=com.mysql.jdbc.Driver
atguigu.jdbc.username=root
atguigu.jdbc.password=atguigu
```

- 由键值对组成；

- 键和值之间的符号是等号；
- 每一行都必须顶格写，==**<u>前面不能有空格之类</u>**==的其他符号；

#### 其他形式

- YAML语言的配置文件：在SpringBoot中使用；

```yaml
spring:
  profiles:
    active: fc
  datasource:
    name: mydb
    type: com.alibaba.druid.pool.DruidDataSource
    url: jdbc:mysql://192.168.41.100:3306/spring_boot?serverTimezone=UTC
    username: root
    password: atguigu
    driver-class-name: com.mysql.cj.jdbc.Driver
mybatis:
  mapper-locations: classpath*:/mybatis-mapper/*Mapper.xml
logging:
  level:
    com.atguigu.function.compute.mapper: debug
```

- JSON格式的配置文件：一般是前端使用；

## 4.2 Tomcat的部署和启动

### 4.2.1 Tomcat扮演的角色

#### 对外：Web服务器

<img src="./image/image-20230317144715321.png" alt="image-20230317144715321" style="zoom:50%;" />

#### 对内：Servlet

<img src="./image/image-20230317144744773.png" alt="image-20230317144744773" style="zoom:50%;" />

### 4.2.2 部署

#### 前提

Tomcat本身是一个Java程序，所以当前系统中必须正确配置了`JAVA_HOME`环境变量。

Tomcat支持的Java版本对照：

<img src="./image/image-20230317150008098.png" alt="image-20230317150008098" style="zoom:50%;" />

#### 启动关闭Tomcat

启动Tomcat：运行Tomcat解压后根目录下`\bin\startup.bat`即可。

```shell
PS D:\JavaWeb\apache-tomcat-10.1.7\bin> .\\startup.bat
```

Tomcat首页网址为：`localhost:8080`；

如果需要停止Tomcat，则运行`shutdown.bat`程序。

```shell
PS D:\JavaWeb\apache-tomcat-10.1.7\bin> .\\shutdown.bat
```

小提示：将来我们在IDEA中启动Tomcat，如果IDEA卡死强关，Tomcat不会正常退出。下次再启动Tomcat会因为残留进程仍然占用`8080`端口，导致新的进程无法启动。此时可以使用`shutdown.bat`结束残留进程。

具体部署过程可在IDEA中进行。

