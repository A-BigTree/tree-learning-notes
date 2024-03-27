# 2 Application layer应用层



## 2.1 应用层协议原理

> **Principles of network applications**

### 2.1.1 网络应用程序体系结构

> Network application structure

**应用程序体系结构（application architecture）**由应用程序研发者设计，规定了如何在各种端系统上组织该应用程序。现代网络应用程序中所使用的两种主流体系结构：

#### 客户-服务器体系结构

> Client-server architecture

1. Server

   - 总是在线（always on host）；
   - 永久的IP地址（permanent IP address）；
   - 配置在数据中心（often in data centers）；

2. Client

   - 与服务器沟通联系；
   - 被间歇性（intermittently）的连接；
   - 拥有动态地址；
   - 客户之间不直接联系；

   <img src="image/image-20221208203604062.png" alt="image-20221208203604062" style="zoom:50%;" />



#### P2P体系结构

> Peer-peer architecture

- 没有一直在线的服务器；
- 端与端之间直接进行连接；
- **自拓展性（self-scalability）**，每个对等方通过向其他对等方分发文件为系统提供服务能力；
- 对等方可间歇性的被连接并且可以改变IP地址；

<img src="image/image-20221208204330596.png" alt="image-20221208204330596" style="zoom:50%;" />

### 2.1.2 进程通信

> Process communicating

用操作系统的术语来说，进行通信的实际上是**进程（process）**而不是程序；在两个不同端系统上的进程，通过跨越计算机网络交换**报文（message）**而相互通信。

#### 客户和服务器进程

- 客户进程：发起通信的进程；
- 服务进程：在会话开始时等待联系的进程；

#### 进程与计算机网络之间的接口

> Socket

进程通过一个称为**套接字（Sockets）**的软件接口向网络发送报文和从网络接受报文。由于该套接字是建立网络应用的可编程接口，因此套接字被称为应用程序和网络之间的**应用程序编程接口（Application Programming Interface，API）**。

<img src="image/image-20221208205802541.png" alt="image-20221208205802541" style="zoom: 33%;" />

#### 进程寻址

> Addressing processes

- 主机地址👉**IP地址（IP address）**标识；
- 目的主机指定**接受进程**的标识符👉目的地**端口号（port number）**；

<img src="image/image-20221208210834463.png" alt="image-20221208210834463" style="zoom: 33%;" />

### 2.1.3 可供应用程序使用的运输服务

> Transport service used in application

1. **可靠数据传输（reliable data transfer）**
   - 一些应用需要100%可靠数据传输；
   - 一些应用允许丢包（loss）；
2. **吞吐量（Throughout）**
   - 具有吞吐量要求是应用程序被称为**带宽敏感的应用（bandwidth-sensitive application）**；
   - **弹性应用（elastic application）**能够根据当时可用的带宽或多或少地利用可供使用的吞吐量；
3. **定时（Timing）**
   - **低延时（low delay）**要求；
4. **安全（Security）**
   - 加密技术（encryption）、数据完整性（data integrity）；

### 2.1.4 因特网提供的运输服务

> Transport protocols services

#### TCP服务

- 面向连接的服务（connection-oriented）：报文开始流动之前，TCP让客户和服务器互相交换运输层控制信息（握手阶段），握手阶段后，一条TCP连接（TCP connection）就在两条进程的套接字之间建立；
- 可靠的数据传送服务（reliable transport）：无差别、按适当顺序交付所有发送的数据；
- 拥塞控制机制（congestion control）；

**<u>*==TCP安全==：*</u>**

**安全套接字层（Secure Sockets Layer，SSL）**提供了关键的进程到进程的安全性服务。

<img src="image/image-20221208214321369.png" alt="image-20221208214321369" style="zoom:50%;" />

我们平时上网冲浪时，网址前面的`http`与`https`的关系其实就是`http + SSL = https`🫡

#### UDP服务

- 提供一种不可靠数据传送服务（unreliable data transfer）；
- 当进程将报文发送至UDP套接字时，UDP并不能保证该报文将到达接收进程；

### 2.1.5 应用层协议

> Application-layer protocol

**应用层协议（Application-layer protocol）**定义了运行在不同端系统上的应用程序进程如何相互传递报文。

### 2.1.6 网络应用

- Web
- 电子邮件
- 目录服务
- 流式视频
- P2P
- 。。。



## 2.2 Web和HTTP

> **Web and http**

### 2.2.1 HTTP概况

> HTTP overview

Web的==应用层协议==是**超文本传输协议（HyperText Transfer Protocol，HTTP）**，它是Web的核心。。

- **Web页面（Web page）**（也叫文档）是由对象组成。一个对象（object）只是一个文件，诸如一个HTML文件、一个JPEG图片、一个Java小程序等等；
- 多数Web页面包含**HTML基本文件（base HTML）**以及几个引用对象；
- **Web浏览器（Web browser）**实现了HTTP的客户端；**Web服务器（Web server）**实现了HTTP的服务器端；
- HTTP使用TCP作为它的支撑运输协议；
- 因为HTTP服务器并不保存关于客户的任何信息，所以我们会说HTTP是一个无状态协议（stateless protocol）；



### 2.2.2 非持续连接和持续连接

> Non-persistent HTTP and Persistent HTTP

#### 非持续连接

1. TCP连接开启；
2. 最多一个对象通过该TCP连接发送；
3. 该TCP连接关闭；

#### 持续连接

1. TCP连接开启；
2. 多个对象通过该TCP连接发送；
3. 该TCP连接关闭；

<img src="image/image-20221208223457565.png" alt="image-20221208223457565" style="zoom:50%;" />

- **往返时间（Round-Trip Time，RTT）**定义：该时间是指一个短分组从客户到服务器然后在返回客户所需要的时间；

<img src="image/image-20221208224233659.png" alt="image-20221208224233659" style="zoom:50%;" />

### 2.2.3 HTTP报文格式

> HTTP message format

#### HTTP请求报文

```http
GET /index.html HTTP/1.1\r\n
Host: www-net.cs.umass.edu\r\n
User-Agent: Firefox/3.6.10\r\n
Accept: text/html,application/xhtml+xml\r\n
Accept-Language: en-us,en;q=0.5\r\n
Accept-Encoding: gzip,deflate\r\n
Accept-Charset: ISO-8859-1,utf-8;q=0.7\r\n
Keep-Alive: 115\r\n
Connection: keep-alive\r\n
\r\n
```

- 请求报文的第一行叫做**请求行（request line）**，其后继的行叫作**首部行（header line）**；
- 请求行有3个字段：方法字段、URL字段和HTTP版本字段；
  - 方法可取`GET,POST,HEAD,PUT,DELETE`；
- 首部行`HOST:`指明了对象所在的主机；首部行`User-Agent:`指明了用户代理，即浏览器类型；。。。

**<u>*HTTP请求报文通用格式：*</u>**

<img src="image/image-20221208230243339.png" alt="image-20221208230243339" style="zoom: 33%;" />

#### HTTP响应报文

```http
HTTP/1.1 200 OK\r\n
Date: Sun, 26 Sep 2010 20:09:20 GMT\r\n
Server: Apache/2.0.52 (CentOS)\r\n
Last-Modified: Tue, 30 Oct 2007 17:00:02 GMT\r\n
ETag: "17dc6-a5c-bf716880"\r\n
Accept-Ranges: bytes\r\n
Content-Length: 2652\r\n
Keep-Alive: timeout=10, max=100\r\n
Connection: Keep-Alive\r\n
Content-Type: text/html; charset=ISO-8859-1\r\n
\r\n
(data data data data data ... )
```

- 上例响应报文包括一个**初始状态行（status line）**，9个**首部行（header line）**，然后是**实体体（entity boby）**；
- 状态行有3个字段：协议版本字段、状态码和状态信息；
  - `200 OK`：请求成功；
  - `301 Moved Permanently`：请求对象以及被永久转移；
  - `400 Bad Request`：一个通用差错代码，该请求不能被服务器理解；
  - `404 Not Found`：被请求的文档不在服务器上；
  - `505 HTTP Version Not Support`：服务器不支持请求报文使用的HTTP协议版本；

### 2.2.4 用户与服务器的交互：cookie🍪

> Maintaining user/server state: cookies🍪

前面提到HTTP服务器为无状态的，而一个Web站点通常希望能够识别用户，可能是因为服务器希望限制用户的访问，或者因为它希望把内容与用户身份联系起来。为此，HTTP使用了cookie🍪。

cookie技术有4个组件：

- HTTP响应报文的cookie首部行；
- HTTP请求报文的cookie首部行；
- 用户端系统中保留一个cookie文行；
- 位于Web站点的一个后端数据库；

<img src="image/image-20221208231952595.png" alt="image-20221208231952595" style="zoom: 33%;" />

### 2.2.5 Web缓存

> Web cache(proxy server)

**Web缓存器（Web cache）**也叫**代理服务器（proxy server）**，它是能够代表**初始Web服务器（origin server）**来满足HTTP网络请求的实体。

<img src="image/image-20221208232922917.png" alt="image-20221208232922917" style="zoom: 50%;" />

请求过程：

1. 浏览器创建一个到Web缓存器的TCP连接，并向Web缓存器中的对象发送一个HTTP请求；
2. Web缓存器进行检查，看看本地是否存储该对象副本。如果有，Web缓存器向客户返回该对象；
3. 如果缓存器中没有该对象，它就打开一个与该对象的初始服务器的TCP连接。Web缓存器向初始服务器发送请求，并得到初始服务器的响应；
4. 当Web缓存器接受对象后，在本地创建给对象的副本，并向客户发送响应报文返回该对象；

通过使用**内容分发网络（Content Distribution Network，CDN）**，Web缓存器正在因特网中发挥着越来越重要的作用。

### 2.2.6 条件GET方法

> Conditional GET

尽管高速缓存器能减少用户感受到的响应时间，但引入了一个新的问题，即存放在缓存器中的副本可能陈旧的。

**条件GET方法：**

1. 请求报文使用`GET`方法；
2. 请求报文中包含一个`If-Modified-Since:`首部行；

响应报文中状态行为`304 Not Modified`表示缓存器可以使用该对象的副本。



## 2.3 因特网种的电子邮件

> E-mail

电子邮件三个主要组成部分：

- 用户代理（User agent）；
- 邮件服务器（mail server）；
- 简单邮件传输协议（Simple Mail Transfer Protocol，SMTP）；

<img src="image/image-20221209202515232.png" alt="image-20221209202515232" style="zoom: 33%;" />

用户代理

- 撰写、编辑、阅读邮件；
- 服务器上存储的传入和传出的消息；

邮件服务器

- 邮箱（mailbox）包括用户传入的消息；
- 报文队列（message queue）中为待发送的邮件报文；

SMTP协议

- 在邮箱服务器之间传输邮件报文；

### 2.3.1 SMTP

假设Alice想给Bob发送一封简单的ASCII报文：

1. Alice调用她的邮件代理程序并提供Bob的邮件地址，撰写报文，然后指示用户代理发送该报文；
2. Alice的用户代理把报文发给她的邮件服务器，在那里该报文被放在报文队列中；
3. 运行在Alice的邮件服务器上的**SMTP客户端**发现了该报文队列中的这个报文，它就创建一个到运行在Bob的邮件服务器上的SMTP服务器的**TCP连接**；
4. 经过一些**初始SMTP握手**后，SMTP客户通过该TCP连接发送Alice的报文；
5. 在Bob的邮件服务器上，**SMTP的服务器端**接受该报文。Bob的邮件服务器然后将该报文放入Bob的邮箱中；
6. 在Bob方便的时候，他调用用户代理阅读该报文；

<img src="image/image-20221209210751824.png" alt="image-20221209210751824" style="zoom:50%;" />

- 如果Bob的邮件服务器没有开机，该报文会保留在Alice的邮件服务器上并等待进行新的尝试，这意味着邮件并**不在中间的某个邮件服务器存留**。

### 2.3.2 与HTTP对比

1. HTTP主要是一个**拉协议（pull protocol）**，TCP连接是由想==接受==文件的机器发起的；SMTP是一个**推协议（push protocol）**，TCP连接是由==发送==文件的机器发起；
2. SMTP要求报文采用7比特ASCII码格式，如果含有非7比特ASCII字符，则这些数据必须按照7比特ASCII码进行编码；HTTP不受这种限制；
3. HTTP把每个对象分别封装在不同的HTTP响应报文中；SMTP则把所有报文对象放在一个报文中；

### 2.3.3 邮件报文格式

```HTTP
To:发件人地址
From:收件人地址
Subject:邮件主题

...邮件正文
```

### 2.3.4 邮件访问协议

> Mail accss protocol

收件人的用户代理不能使用SMTP得到报文，因为取得报文是一个拉操作，而SMTP协议是一个推协议。通过引用一个特殊的邮件访问协议来解决这个问题，该协议将收件人邮件服务器上的报文传送给他的本地PC。目前有一些流行的邮件访问协议，包括第三版的邮局协议（Post Office Protocol-Version 3，POP3）（不常用）、因特网邮件访问协议（Internet Mail Access Protocol，IMAP）以及HTTP。

<img src="image/image-20221209224058847.png" alt="image-20221209224058847" style="zoom:50%;" />

<img src="image/image-20221209230325479.png" alt="image-20221209230325479" style="zoom: 33%;" />

## 2.4 DNS：因特网的目录服务

> Domain Name System
> 

### 2.4.1 DNS提供的服务

> DNS:services

识别主机的两种方式：通过**主机名（hostname）**或者**IP地址（IP address）**。人们喜欢记忆主机名标识方式，而路由器喜欢定长的、有着层次结构的IP地址。域名系统的主要任务：==主机名到IP地址转换的目录服务==。

DNS是：

1. 一个由分层的**DNS服务器（DNS service）**实现的分布式数据库；
2. 一个使得主机能够查询分布式数据库的应用层协议；

DNS协议运行在`UDP`之下，使用`53`号端口；

<img src="image/image-20230203200841260.png" alt="image-20230203200841260" style="zoom: 33%;" />

DNS重要的服务：

- 主机名到IP地址的转换（hostname to IP address translation）；
- 主机别名（host aliasing）：应用程序调用DNS获取主机别名对应的**规范主机名（canonical hostname）**以及主机的IP地址；
- 邮件服务器别名（mail server aliasing）；
- 负载分配（load distribution）：繁忙的站点被**冗余分布在多台服务器**上，每台服务器运行在不同的端系统上，每个都有着不同的IP地址。由于这些冗余的Web服务器，一个==IP地址集合==于**一个规范主机名**相联系。当客户对映射到某处到某地址集合的名字发出一个DNS请求时，该服务器用IP地址的整个集合进行响应，但在每个回答中循环这些地址次序。因为客户通常**<u>总是向IP地址排在最前面的服务器发送HTTP请求报文</u>**，所以DNS就在所有这些冗余的Web服务器之间循环分配了负载；

### 2.4.2 DNS工作机理概述

> Overview of How DNS Works

集中式设计的问题包括：

- 单点故障（a single point of failure）；
- 通信容量（traffic volume）；
- 远距离的集中式数据库（distant centralized database）；
- 维护（maintenance）；

#### 分布式，层次数据库

分布式DNS服务器的层次结构：

- 根DNS服务器（root DNS servers）；
- 顶级域服务器（Top-Level Domain DNS servers）；
- 权威DNS服务器（authoritative DNS servers）；
- 本地服务器（local DNS servers）：不属于该服务器的层次结构，但它对DNS层次结构是至关重要的；

<img src="image/image-20230206160128655.png" alt="image-20230206160128655" style="zoom:50%;" />

#### 查询方式

1. 递归查询（Recursive query）

<img src="image/image-20230206160453845.png" alt="image-20230206160453845" style="zoom:50%;" />

2. 迭代查询（Iterative query）

<img src="image/image-20230206160612559.png" alt="image-20230206160612559" style="zoom:50%;" />

==从请求主机到本地的DNS服务器的查询是**递归**的，其余的查询是**迭代**的。==

#### DNS缓存

在某一个请求链中，当某DNS服务器接收一个DNS回答，它能映射在本地存储器中。由于主机和主机名与IP地址的映射并不是永久的，DNS服务器在一段时间后将丢弃缓存的信息。

### 2.4.3 DNS记录和报文

> DNS Record and Protocol Message

#### DNS记录

所有DNS服务器存储了**资源记录（Resourse Record，RR）**，资源记录是一个包含了下列字段的4元组：

- `(Name, Value, Type, TTL)`

下面为不同类型`Type`的`Name`和`Value`：

<img src="image/image-20230206162243812.png" alt="image-20230206162243812" style="zoom:50%;" />

#### DNS报文

DNS报文中各字段的语义如下：

<img src="image/image-20230206162527469.png" alt="image-20230206162527469" style="zoom:50%;" />

## 2.5 P2P文件分发

> Peer-to-Peer File Distribution  

CS系统极大依赖于总是打开的基础设施服务器，P2P对总是打开的服务器有最小的（或者没有）依赖。

### P2P体系结构的扩展性

> Scalability of P2P Architectures

文件分发问题的示例图如下：

<img src="image/image-20230206170149419.png" alt="image-20230206170149419" style="zoom:50%;" />

对于客户-服务器体系结构的分发时间如下：
$$
D_{cs}=\max\{\frac{NF}{u_s},\frac{F}{d_{min}}\}
$$
对于P2P体系结构分发时间如下：
$$
D_{P2P}=\max\{\frac{F}{u_s},\frac{F}{d_{min}},\frac{NF}{u_s+\sum^N_{i=1}u_i}\}
$$
P2P和客户-服务器体系结构的分发时间随用户数量变化如下：

<img src="image/image-20230206170711770.png" alt="image-20230206170711770" style="zoom:50%;" />

### BitTorrent

参与一个特定文件分发的所有对等方的集合被称为一个**洪流（torrent）**。在一个洪流中的对等方彼此下载等长度的**文件块（chunk）** ，典型块长度为256KB。当一个对等方下载块时，也为其他对等方上载了多个块。每个洪流具有一个基础设施节点，称为**追踪器（tracker）**。当一个对等方加入某洪流时，它向追踪器注册自己，并周期性地通知追踪器它仍在该洪流中。

<img src="image/image-20230206193748505.png" alt="image-20230206193748505" style="zoom:50%;" />

- 决定请求哪些块的过程：**最稀缺优先（rarest first）**技术；
- 决定响应哪个请求：根据当前能够以最高速率的**疏通（unchoked）**的对等方提供数据的邻居，给出优先权；



## 2.6 视频流和内容分发网

> Video Streaming and Content Distribution Networks

### 2.6.1 HTTP流和DASH

HTTP流所有客户接受到相同编码的视频，但对不同用户或者不同时间，客户可用的带宽大小有很大不同。**HTTP的动态适应性流（Dynamic Adaptive Streaming over HTTP，DASH）**：视频编码成几个不同的版本，其中每个版本具有不同的比特率，对应于不同的质量水平。客户动态地请求来自不同版本且长度为几秒的视频段数据块。

每个视频版本存储在HTTP服务器中，每个版本都有一个不同的URL。HTTP服务器也有一个**告示文件（manifest file）**，为每个版本提供了一个URL及其比特率。

### 2.6.2 内容分发网

为了应对向分布于全世界的用户分发巨量视频数据的挑战，几乎所有主要的视频流公司都利用**内容分发网（Content Distribution Network，CDN）**。CDN管理分布在多个地理位置上的服务器，在它的服务器中存储视频的副本，并且所有试图将每个用户请求定向到一个将提供最好的用户体验的CDN位置。CDN可以是**专用CDN（private CDN）**，即它由内容提供商自己所拥有；另一种CDN可以是**第三方CDN（third-party CDN）**，它代表多个内容提供商分发内容。

#### CDN操作

当客户请求一个特定视频时，CDN必须截获该请求，以便能够：

1. 确定此时适合用于该客户的CDN服务器集群；
2. 将客户的请求重定向到该集群的某台服务器；

CDN操作步骤如下：

<img src="image/image-20230206213205148.png" alt="image-20230206213205148" style="zoom:50%;" />

## 2.7 套接字编程：生成网络应用

> Socket Programming: Creating Network Applications

### 2.7.1 UDP套接字编程

<img src="image/image-20230206213634173.png" alt="image-20230206213634173" style="zoom:50%;" />

### 2.7.2 TCP套接字编程

<img src="image/image-20230206213723686.png" alt="image-20230206213723686" style="zoom:50%;" />



