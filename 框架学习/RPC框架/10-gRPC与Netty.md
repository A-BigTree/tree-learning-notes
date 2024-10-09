# gRPC与Netty

[toc]

---

gRPC跟Netty的关系主要包括如下几方面的依赖：

1. 依赖Netty实现的HTTP/2协议的封装，通过Listener机制监听HTTP/2的数据报文事件，完成网络相关处理；
2. Reactor IO模型的依赖，Nio/Epoll；
3. 依赖Netty的ByteBuf完成流数据在内部中的缓存与流转；



## 1 几种常见的RPC IO模型

### 1.1 BIO模型

![image-20241009203356452](./10-gRPC%E4%B8%8ENetty.assets/image-20241009203356452.png)

主要特点

1. 每个请求一个IO线程处理
2. Client消息请求与应答由IO线程完成
3. IO线程在没有处理完之前会同步阻塞

优点

1. 想啥呢？ 没啥优点

缺点

1. 吞吐低：每个请求一个线程很容易导致server端出现资源瓶颈从而影响吞吐，频繁创建线程也不能合理利用线程资源（可以通过使用线程池来解决）
2. 稳定性：由于是阻塞模型，遇到网络抖动就凉了
3. 可维护性：IO线程数无法预估，资源利用率低



### 1.2 NIO模型

![image-20241009203447181](./10-gRPC%E4%B8%8ENetty.assets/image-20241009203447181.png)

俗称IO多路复用模型，该线程模型中前端挂载一个Selector用于轮询各个Channel的IO事件，收到后Dispatch到后端Worker线程池去做实际的IO处理（读写等），Worker线程池可以有多种模式，取决于具体设计

主要特点

1. IO多路复用
2. 非阻塞

优点

1. IO非阻塞，可靠性和效率都相应提高

缺点

1. 轮询的效率可能不高，取决于底层使用的系统库函数（select/poll）
2. 内存拷贝相关还是会阻塞



### 1.3 AIO模型

AIO模型跟NIO模型本质的区别是异步，这里异步的含义是指当数据处理完成之后才通知使用方，而NIO是数据就绪之后。

### 1.4 RPC性能三要素

1. IO模型：决定了数据发送的效率，可靠性等
2. 协议：通常内部协议性能会更优
3. 线程模型：决定编解码、消息接收与返回等在哪个线程执行



## 2 gRPC client侧线程模型

gRPC client端可以划分出三种线程

1. Caller线程：业务当前线程
2. Worker线程（grpc-default-executor）：请求处理与响应回调线程
3. IO线程（grpc-nio-worker-ELG）：使用Netty的ELG

以futureClient为例，一次client请求的线程切换如下图：

![image-20241009203755241](./10-gRPC%E4%B8%8ENetty.assets/image-20241009203755241.png)

整体一次执行流程描述如下：

1. 创建链接，通常是Delayed的模式，第一次创建Stream时候之前才会创建连接
2. 创建Stream，代表发起一次请求，在Caller Thread完成
3. 发送Header，将Header Frame投递到WriteQueue，Call Thread完成，ELG异步消费WriteQueue
4. 发送Message，将Message Frame投递到WriteQueue，Call Thread完成，ELG异步消费WriteQueue
5. 接收Header，这里完成一次从ELG -> Worker线程池的切换，需要注意的是如果创建Client时我们指定了directExecutor模式，那么将统一由ELG线程完成
6. 接收Message，这里完成一次从ELG -> Worker线程池的切换，需要注意的是如果创建Client时我们指定了directExecutor模式，那么将统一由ELG线程完成
7. 接收关闭Stream请求，线程切换同5，6，最终会回调Future的set方法
8. Caller Thread通过Future.get获取到结果



## 3 gRPC server侧线程模型

gRPC server侧主要包括两个线程：

1. Worker线程（grpc-default-executor）：Server启动时候指定
2. IO线程（grpc-nio-worker-ELG）：使用Netty的ELG

以一次Unary请求为例，Server侧线程切换如下：

![image-20241009203941191](./10-gRPC%E4%B8%8ENetty.assets/image-20241009203941191.png)

整体一次执行流程描述如下：

1. 接收Client发送的创建Stream的请求，ELG -> Worker线程切换，最终创建一个ServerCall及其Listener
2. 接收Client发送的Msg，ELG -> Worker线程切换，反序列化保存
3. Client完成发送，ELG -> Worker线程切换，回调Server业务逻辑
4. **处理完成**发送Header，扔到WriteQueue，ELG异步消费发送Header Frame到Client
5. 处理完成发送Message，扔到WriteQueue，ELG异步消费发送Message Frame到Client
6. 完成全部消息发送，标记*end of frame*，对于unary而言跟5合成一步，扔到WriteQueue，ELG异步消费发送Message Frame到Client
7. Client发送RstStream请求，并不一定会发送，取决于配置，回调Listener



综上完成了Client与Server线程模型的介绍，结合之前篇章gRPC API设计思路部分，相信大家可以更好的理解一次请求在gRPC内部是如何流转的。下面将介绍一个连接从建立到释放状态流转的过程，从另一种维度介绍gRPC是如何维护连接的。



## 4 gRPC链接状态机

gRPC内部在Channel构建到销毁的生命周期内，维护了该链接的整个状态的运转，了解内部具体的运转流程有助于我们更好的定位问题。gRPC内部链接主要分成如下几个状态：

1. `CONNECTING`：代表Channel正在初始化建立连接，流程会涉及DNS解析、TCP建连、TLS握手等
2. `READY`：成功建立连接，包括HTTP2协商，代表Channel可以正常收发数据
3. `TRANSIENT_FAILURE`：建连失败或者CS之间网络问题导致，Channel最终会重新发起建立连接请求，gRPC提供了一套backoff Retry机制来保证不会出现重连风暴
4. `IDLE`：Channel中长期没有请求或收到HTTP2的GO_AWAY信号会进入此状态，此时CS之间连接已经断开，一旦新请求发起会转移到CONNECTING。主要目的是保障SERVER连接数不会太大造成压力
5. `SHUTDOWN`：Channel已经关闭，状态不可逆，新请求会立即失败掉，排队的请求会继续处理完

下图描述了五种状态之间的转换：

![image-20241009204345249](./10-gRPC%E4%B8%8ENetty.assets/image-20241009204345249.png)