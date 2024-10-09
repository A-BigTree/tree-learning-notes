# gRPC常见异常类型与分析

[toc]

---

## 1 gRPC常见状态码

如下链接给出gRPC全部状态码及其解释说明

https://github.com/grpc/grpc/blob/master/doc/statuscodes.md

从中选取一些大家会遇到的进行简单描述

1. **OK**：代表请求成功执行
2. **CANCELLED：**请求被取消，通常是超时 or Deadline Exceed导致
3. **UNKNOWN**：通常是Server抛异常导致，一般Server业务执行抛异常非StatusException or RuntimeStatusException，所以gRPC内部会用UNKNOWN状态码进行处理
4. **DEADLINE_EXCEEDED：**客户端请求配置了withDeadlineAfter CallOption，双端都生效，服务端超时会取消请求，Client侧表现为收到这个状态码的RuntimeStatusException，也是我们最常见的异常
5. **PERMISSION_DENIED：**顾名思义，代表没有访问权限，可以对应HTTP的403，常见如IP白名单
6. **UNAUTHENTICATED：**gRPC特意用这个状态码代表没有授权的访问，通常是开启tls的服务
7. **RESOURCE_EXAUSTED：**代表服务端资源不足，如带宽，磁盘，连接数等等；业务也可以使用它来描述业务资源不足的情况
8. **UNIMPLEMENTED：**gRPC内部异常，请求的FullPath方法未实现，如果没有FallbackRegistry会抛这个状态码异常，通常可能是**proto文件有不兼容改动**
9. **UNAVAILABLE：**代表当前服务不可用，一般是**暂时性问题**，代表客户端可以通过重试来解决的情况
10. **INTERNAL：**gRPC内部严重错误，通常是有严重bug才会出现，业务不应该使用此状态码



## 2 gRPC常见异常与分析

### 2.1 INTERNAL

#### Case 1:Shade包导致SPI加载问题

```shell
Caused by: java.lang.IllegalStateException: Could not find policy 'pick_first'. Make sure its implementation is either registered to LoadBalancerRegistry or included in META-INF/services/io.grpc.LoadBalancerProvider from your jar files.
z
```

这个case通常是使用shade包的工程，在打包时候改变了gRPC的SPI实现的路径名，导致找不到实际实现所以出现上述异常，解决方案如下：

```xml
<executions>
  <execution>
    <goals>
      <goal>shade</goal>
    </goals>
    <configuration>
      <transformers>
        <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>		
      </transformers>
    </configuration>
  </execution>
</executions>
```

在shade plugin中加入上述transformer来解决即可



#### Case2:服务端忘记结束请求

```shell
io.grpc.StatusRuntimeException: INTERNAL: No value received for unary call
	at io.grpc.stub.ClientCalls.toStatusRuntimeException(ClientCalls.java:210)
	at io.grpc.stub.ClientCalls.getUnchecked(ClientCalls.java:191)
	at io.grpc.stub.ClientCalls.blockingUnaryCall(ClientCalls.java:124)
	at com.dd.search.rpc.stub.SearchServiceGrpc$SearchServiceBlockingStub.getResourcesList(SearchServiceGrpc.java:298)
	at com.dd.search.grpc.client.GrpcSearchClientService.getResourcesList(GrpcSearchClientService.java:102)
	at com.dd.search.grpc.client.GrpcSearchClientController.getResourcesList(GrpcSearchClientController.java:26)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)
	at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133)
	at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:97)
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827)
	at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738)
	at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85)
	at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:967)
	at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:901)
	at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)
	at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:861)
	at javax.servlet.http.HttpServlet.service(HttpServlet.java:635)
```

这个case通常是服务端问题导致，服务端一直没有回调onComplete or onError，所以client表现为拿不到结果，另外已经不建议继续使用BlockingStub，官方也已经Deprecated掉

#### Case 3：收到无法识别的Stream的报文

```shell 
WARNING: Stream Error
io.netty.handler.codec.http2.Http2Exception$StreamException: Received DATA frame for an unknown stream 2001
	at io.netty.handler.codec.http2.Http2Exception.streamError(Http2Exception.java:129)
	at io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder$FrameReadListener.shouldIgnoreHeadersOrDataFrame(DefaultHttp2ConnectionDecoder.java:535)
	at io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder$FrameReadListener.onDataRead(DefaultHttp2ConnectionDecoder.java:187)
	at io.netty.handler.codec.http2.Http2InboundFrameLogger$1.onDataRead(Http2InboundFrameLogger.java:48)
	at io.netty.handler.codec.http2.DefaultHttp2FrameReader.readDataFrame(DefaultHttp2FrameReader.java:421)
	at io.netty.handler.codec.http2.DefaultHttp2FrameReader.processPayloadState(DefaultHttp2FrameReader.java:251)
	at io.netty.handler.codec.http2.DefaultHttp2FrameReader.readFrame(DefaultHttp2FrameReader.java:160)
	at io.netty.handler.codec.http2.Http2InboundFrameLogger.readFrame(Http2InboundFrameLogger.java:41)
	at io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder.decodeFrame(DefaultHttp2ConnectionDecoder.java:118)
	at io.netty.handler.codec.http2.Http2ConnectionHandler$FrameDecoder.decode(Http2ConnectionHandler.java:390)
	at io.netty.handler.codec.http2.Http2ConnectionHandler.decode(Http2ConnectionHandler.java:450)
	at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:489)
	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:428)
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:265)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:340)
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1434)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:362)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:348)
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:965)
	at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:808)
	at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:408)
	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:297)
	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:884)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.lang.Thread.run(Thread.java:748)
```

该异常在不频繁的情况下是正常现象，通常是由Client发起Cancel导致，可能是DEADLINE_EXCEED或者是TimeoutCancel（取决于是否开启该功能）。

#### Case4:Stream提前关闭，导致Server抛异常

```shell
2019-08-27 18:39:34,155 WARN  [grpc-nio-worker-ELG-3-1] i.g.n.s.i.g.n.NettyServerHandler: Stream Error
io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2Exception$StreamException: Stream closed before write could take place
at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2Exception.streamError(Http2Exception.java:167)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2RemoteFlowController$FlowState.cancel(DefaultHttp2RemoteFlowController.java:481)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2RemoteFlowController$1.onStreamClosed(DefaultHttp2RemoteFlowController.java:105)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2Connection.notifyClosed(DefaultHttp2Connection.java:356)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2Connection$ActiveStreams.removeFromActiveStreams(DefaultHttp2Connection.java:1000)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2Connection$ActiveStreams.deactivate(DefaultHttp2Connection.java:956)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2Connection$DefaultStream.close(DefaultHttp2Connection.java:512)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.DefaultHttp2Connection.close(DefaultHttp2Connection.java:152)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler$BaseDecoder.channelInactive(Http2ConnectionHandler.java:209)
at io.grpc.netty.shaded.io.netty.handler.codec.http2.Http2ConnectionHandler.channelInactive(Http2ConnectionHandler.java:417)
at io.grpc.netty.shaded.io.grpc.netty.NettyServerHandler.channelInactive(NettyServerHandler.java:586)
at io.grpc.netty.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:257)
at io.grpc.netty.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:243)
at io.grpc.netty.shaded.io.netty.channel.AbstractChannelHandlerContext.fireChannelInactive(AbstractChannelHandlerContext.java:236)
at io.grpc.netty.shaded.io.netty.channel.DefaultChannelPipeline$HeadContext.channelInactive(DefaultChannelPipeline.java:1416)
at io.grpc.netty.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:257)
at io.grpc.netty.shaded.io.netty.channel.AbstractChannelHandlerContext.invokeChannelInactive(AbstractChannelHandlerContext.java:243)
at io.grpc.netty.shaded.io.netty.channel.DefaultChannelPipeline.fireChannelInactive(DefaultChannelPipeline.java:912)
at io.grpc.netty.shaded.io.netty.channel.AbstractChannel$AbstractUnsafe$8.run(AbstractChannel.java:816)
at io.grpc.netty.shaded.io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163)
at io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:416)
at io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:515)at io.grpc.netty.shaded.io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:918)
at io.grpc.netty.shaded.io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
at io.grpc.netty.shaded.io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
at java.base/java.lang.Thread.run(Thread.java:834)
```

这个case跟上面的类似，都是由于一些原因导致Stream已经关闭，Server端回数据的时候无法识别，通常可能是Server的WriteQueue有堆积或者业务处理过慢；还有极端case是Client突然被kill了也可能会导致这个问题。

### 2.2 RESOURCE_EXAUSTED

#### Case1:超过消息最大限制

```shell
WARNING: Exception processing message
io.grpc.StatusRuntimeException: RESOURCE_EXHAUSTED: io.grpc.netty.NettyServerStream$TransportState: Frame size 59000005 exceeds maximum: 4194304. 
	at io.grpc.Status.asRuntimeException(Status.java:517)
	at io.grpc.internal.MessageDeframer.processHeader(MessageDeframer.java:391)
	at io.grpc.internal.MessageDeframer.deliver(MessageDeframer.java:271)
	at io.grpc.internal.MessageDeframer.request(MessageDeframer.java:165)
	at io.grpc.internal.AbstractStream$TransportState.requestMessagesFromDeframer(AbstractStream.java:202)
	at io.grpc.netty.NettyServerStream$Sink$1.run(NettyServerStream.java:100)
	at io.netty.util.concurrent.AbstractEventExecutor.safeExecute(AbstractEventExecutor.java:163)
	at io.netty.util.concurrent.SingleThreadEventExecutor.runAllTasks(SingleThreadEventExecutor.java:403)
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:463)
	at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:858)
	at io.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.java:138)
	at java.lang.Thread.run(Thread.java:745)
```

顾名思义Message Size超过了Channel配置的max message size，需要业务自己调整大小或结合业务场景改成流式调用；



### 2.3 UNKNOWN

#### Case1:Server异常

```shell
io.grpc.StatusRuntimeException: UNKNOWN
	at io.grpc.Status.asRuntimeException(Status.java:545)
	at io.grpc.stub.ClientCalls$StreamObserverToCallListenerAdapter.onClose(ClientCalls.java:395)
	at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl.close(ClientCallImpl.java:481)
	at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl.access$600(ClientCallImpl.java:398)
	at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:513)
	at io.grpc.internal.ContextRunnable.run(ContextRunnable.java:52)
	at io.grpc.internal.SerializingExecutor$TaskRunner.run(SerializingExecutor.java:154)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	at java.lang.Thread.run(Thread.java:745)
```

这种没有特殊message提示的基本上都是服务端抛了一个异常



### 2.4 UNAVAILABLE

#### Case1:网络异常

```shell
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
```

通常是网络问题，Client跟Server的链接已经断开，Channel可能进入Transient Failure状态，短时间出现这个问题可能是网络抖动造成

#### Case2:gRPC低版本bug，在1.23.0后修复

```shell
com.google.api.gax.rpc.UnavailableException: io.grpc.StatusRuntimeException: UNAVAILABLE: Connection closed while performing protocol negotiation for [HttpClientCodec#0, HttpProxyHandler#0, ProtocolNegotiators$WaitUntilActiveHandler#0, WriteBufferingAndExceptionHandler#0, DefaultChannelPipeline$TailContext#0]
at com.google.api.gax.rpc.ApiExceptionFactory.createException(ApiExceptionFactory.java:69)
at com.google.api.gax.grpc.GrpcApiExceptionFactory.create(GrpcApiExceptionFactory.java:72)
at com.google.api.gax.grpc.GrpcApiExceptionFactory.create(GrpcApiExceptionFactory.java:60)
at com.google.api.gax.grpc.GrpcExceptionCallable$ExceptionTransformingFuture.onFailure(GrpcExceptionCallable.java:97)
at com.google.api.core.ApiFutures$1.onFailure(ApiFutures.java:68)
at com.google.common.util.concurrent.Futures$CallbackListener.run(Futures.java:1015)
at com.google.common.util.concurrent.DirectExecutor.execute(DirectExecutor.java:30)
at com.google.common.util.concurrent.AbstractFuture.executeListener(AbstractFuture.java:1137)
at com.google.common.util.concurrent.AbstractFuture.complete(AbstractFuture.java:957)
at com.google.common.util.concurrent.AbstractFuture.setException(AbstractFuture.java:748)
at io.grpc.stub.ClientCalls$GrpcFuture.setException(ClientCalls.java:515)
at io.grpc.stub.ClientCalls$UnaryStreamToFuture.onClose(ClientCalls.java:490)
at io.grpc.PartialForwardingClientCallListener.onClose(PartialForwardingClientCallListener.java:39)
at io.grpc.ForwardingClientCallListener.onClose(ForwardingClientCallListener.java:23)
at io.grpc.ForwardingClientCallListener$SimpleForwardingClientCallListener.onClose(ForwardingClientCallListener.java:40)
at com.google.ads.googleads.lib.logging.LoggingInterceptor$1$1.onClose(LoggingInterceptor.java:111)
at io.grpc.PartialForwardingClientCallListener.onClose(PartialForwardingClientCallListener.java:39)
at io.grpc.ForwardingClientCallListener.onClose(ForwardingClientCallListener.java:23)
at io.grpc.ForwardingClientCallListener$SimpleForwardingClientCallListener.onClose(ForwardingClientCallListener.java:40)
at io.grpc.internal.CensusStatsModule$StatsClientInterceptor$1$1.onClose(CensusStatsModule.java:700)
at io.grpc.PartialForwardingClientCallListener.onClose(PartialForwardingClientCallListener.java:39)
at io.grpc.ForwardingClientCallListener.onClose(ForwardingClientCallListener.java:23)
at io.grpc.ForwardingClientCallListener$SimpleForwardingClientCallListener.onClose(ForwardingClientCallListener.java:40)
at io.grpc.internal.CensusTracingModule$TracingClientInterceptor$1$1.onClose(CensusTracingModule.java:399)
at io.grpc.internal.ClientCallImpl.closeObserver(ClientCallImpl.java:510)
at io.grpc.internal.ClientCallImpl.access$300(ClientCallImpl.java:66)
at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl.close(ClientCallImpl.java:630)
at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl.access$700(ClientCallImpl.java:518)
at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInternal(ClientCallImpl.java:692)
at io.grpc.internal.ClientCallImpl$ClientStreamListenerImpl$1StreamClosed.runInContext(ClientCallImpl.java:681)
at io.grpc.internal.ContextRunnable.run(ContextRunnable.java:37)
at io.grpc.internal.SerializingExecutor.run(SerializingExecutor.java:123)
at java.util.concurrent.Executors$RunnableAdapter.call(Executors.java:511)
at java.util.concurrent.FutureTask.run(FutureTask.java:266)
at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.access$201(ScheduledThreadPoolExecutor.java:180)
at java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:293)
at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
at java.lang.Thread.run(Thread.java:745)
Suppressed: com.google.api.gax.rpc.AsyncTaskException: Asynchronous task failed
at com.google.api.gax.rpc.ApiExceptions.callAndTranslateApiException(ApiExceptions.java:57)
at com.google.api.gax.rpc.UnaryCallable.call(UnaryCallable.java:112)
```

