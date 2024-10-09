# gRPC是什么

[toc]

---

gRPC是一款高性能开源通用的RPC框架，同时面向服务端跟移动端，基于HTTP/2协议设计。gRPC**不是**一款服务治理框架，但是提供了服务治理的若干原材料，例如客户端负载均衡、KeepAlive、流控（自动跟手动）等等。下面简单介绍一下何为**RPC框架**，gRPC作为RPC框架的特点和依赖的重要协议 -- **HTTP/2协议**。

## 1 RPC框架

![image-20241008214051575](./7-gRPC%E6%98%AF%E4%BB%80%E4%B9%88.assets/image-20241008214051575.png)

一个RPC框架的基本构成如上图所示，主要包括如下几个部分：

### 1.1 序列化协议

万物皆字节，我们需要一种途径将万物转化为字节序列在网络传输，这个转化器便是序列化协议，常见如Java原生序列化协议、Thrift、Hession、Json/XML、ProtoBuf。

### 1.2 传输层

目前而言主要是TCP/UDP，对于Java生态而言大多使用NettyAPI来屏蔽底层实现细节。

### 1.3 动态代理层

屏蔽业务感知远程调用，等同于一个本地服务调用一般

## 2 gRPC特色

1. 支持多语言（其实是每个语言实现了一遍...）
2. 基于IDL定义服务，即proto文件（其实是使用了Protocol Buffers协议，ProtoBuf也没大家想象中的那么好，第二章节会进行说明），每个语言提供一个代码生成工具，因此可以基于同一份proto生成不同语言的文件从而支持跨语言使用
3. 序列化支持PB、JSON等，支持自定义Marshaller
4. Client支持Netty跟Okhttp（一般给客户端使用的），Server是Netty；同时Client与Server都支持InProcess方式调用（可以理解为Mock）
5. 暴露较多LowLevel API（类似StreamObserver，XXXCall之类的给使用者），在**gRPC API设计思路**子节中会详细讲解它为何这么做
6. 对于实现全链路异步而言，个人认为gRPC是首选，会在第五章进行详细介绍如果实现Reactive-gRPC



## 3 HTTP/2协议简介

想要理解好gRPC必须了解HTTP/2协议，因为不论是日常看到的异常栈还是gRPC涉及的相关定义都跟HTTP/2存在强绑定的关系。关于HTTP/2详细介绍请参见：https://developers.google.com/web/fundamentals/performance/http2/?hl=zh-CN

本文简单介绍跟gRPC息息相关的几个概念:



### 3.1 协议协商

- HTTP/2部署需要基于HTTPS是当前主流浏览器的要求，但是并非强制，因此主要包括如下两种协商类型：
  1. 基于TLS的HTTP/2协议，使用h2标识（ALPN）；
  2. 基于TCP的HTTP/2协议，使用h2c标识；

- gRPC客户端跟服务端建立连接时会进行协议协商，过程如下：
  1. 客户端在不确定服务端是否支持HTTP/2的情况下发起协商升级请求，如果服务端支持HTTP/2会通过header带回来Upgrade：h2/h2c标识，如果不支持就默认按照HTTP/1.1返回；
  2. 协议协商成功后，如果使用HTTP/2协议双方会互发SETTINGS帧（下面会介绍）作为连接序言，回执后开始发送数据，Client侧可以不必等待Server的回执来提高效率；

- gRPC支持的三种协议协商策略
  1. **PlainText**：明确服务端支持HTTP/2协议，省去上述协商升级过程，直接通过SETTINGS帧作为连接序言建立连接后即可发送数据帧；
  2. **PlainTextUpgrade：**不清楚服务端是否支持HTTP/2，即2中描述的内容；
  3. **TLS**：基于TLS建立HTTP/2连接，协商采用ALPN扩展协议，以h2作为标识；



### 3.2 多路复用

1. 这里多路复用是指多个请求使用同一个连接互相不干扰，跟操作系统select/epoll的多路复用不是一个概念
2. 在HTTP/1.X里面双方想并行发多个请求必须占用多个连接，占用连接资源；同时FIFO的方式如果前面请求处理时间长也会导致队首阻塞（**需要明确的是这里不是指TCP的队首阻塞，HTTP/2解决不了TCP队首阻塞问题，HTTP/3可以**）问题，效率低下；HTTP/2通过抽象Stream概念来实现多路请求复用同一连接
3. HTTP/2中双方建立连接之后，每个实际报文请求可以理解为一个Stream，一个Stream又分成若干个Frame（帧是最小的传输单元），数据传输主要包括Header Frame跟Data Frame。每个Stream都分配一个StreamId。gRPC内部也是通过StreamId来识别不同请求的报文
4. 常见的帧类型：
   - **SETTINGS**帧：用于设置连接级别的配置，协议协商与流控窗口变更相关依赖于它；
   - **PING**帧：用于心跳保持，gRPC KeepAlive就是利用这个帧实现；
   - **GOAWAY**帧：发起关闭连接请求时候使用，通常是连接达到IDLE状态之后；
   - **RST_STREAM**帧：关闭当前流的帧，gRPC会经常用到，特别是当错误发生的时候；
   - **WINDOW_UPDATE**帧：流控帧，gRPC的流控实现依赖于它；
   - **DATA**帧：实际传输数据帧，如果数据发送完毕会带END_STEAM标识；
   - **HEADER**帧：消息头帧，通常是一个请求开始帧，基于此帧创建请求初始化相关内容；



### 3.3 流控机制

1. 流控机制是确保同一个TCP连接上的Stream不会互相干扰，因为TCP毕竟也有其局限性
2. 流控机制同时作用于单个Stream和整个连接，对于Stream而言只作用于Data帧（保证不影响重要帧）
3. 初始窗口大小都是65535字节，发送端需要严格遵守接收端的窗口限制，连接序言中可以通过SETTINGS帧设置**SETTINGS_INITIAL_WINDOW_SIZE**来指定Stream的初始窗口大小，但是无法配置连接级别的初始窗口大小，gRPC支持设置初始窗口的大小，后续文章内容会详细介绍
4. gRPC通过**WINDOW_UPDATE**帧来实现流量控制，但相关说明如下：
   - gRPC支持自定义流控但默认没开启，仍处于VisableForTesting阶段；使用Netty内置的默认流控功能：基本思路就是当已经处理过的数据超过窗口一半是就发送WINDOW_UPADTE来更新窗口；
   - gRPC默认流控初始窗口大小是1M；
   - gRPC如果开启KeepAlive功能那么Ping也会占用窗口大小；
   - gRPC默认给每个Stream分配的字节数是16K；
   - gRPC内部支持的自动跟手动流控，只是针对gRPC本身而言，这里流控的含义是Client何时发起从buffer中读取需要数据的请求，类似于一种应用层级的流控，默认是自动的，当有特殊需求时可以开启手动控制，但是比较复杂而且容易出错，不推荐使用；