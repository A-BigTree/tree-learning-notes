# gRPC API设计详解

[toc]

---

如前面所述，gRPC整体设计思路依附于HTTP/2协议，而HTTP/2是一个双向流协议，因此gRPC在API设计上也采用了Stream的方式。通常意义上（Unary）一次gRPC请求可以理解为**一个Stream创建到销毁的过程**，框架内部封装了Stream的整个生命周期，暴露给用户的是何时发送数据以及何时完成发送，因此gRPC通过暴露给业务Listener的方式来完成这项工作。看到这里你会问："**为什么Unary模式也要暴露StreamAPI给用户？**"，答案很简单，就是为了==**API设计上的一致性**==，毕竟是框架。因为是双向流协议，因此可以分成如下四种调用模式：

1. Unary模式：即请求响应模式；
2. Client Streaming模式：Client发送多次，Server回复一次；
3. Server Streaming模式：Client发送一次，Server发送多次；
4. 双向 Streaming模式：Client/Server都发送多次；

下面将分成Server跟Client两部分进行详细描述gRPC API设计（**使用Unary模式进行说明**）

## 1 Server部分

Server端我们能接触到API包括：

1. StreamObserver
2. ServerCall
3. ServerCall.Listener

下面结合一些使用例子来具体说明一下三者的功能定位跟联系。通常我们通过proto文件定义好service之后会生成一下XXXGrpc.class文件（先不讨论是怎么生成的），里面提供了一个XXXImplBase抽象类，我只需继承该抽象类然后实现方法即可，例如：

```java
@Override
public void margin(RecoHotMarginReq request, StreamObserver responseObserver) {
    final List resp = hide(request.getUserId(), request.getDeviceId(),
            request.getLlsid(), request.getMixResultList(), request.getRecentUploadBitmap());
    responseObserver.onNext(RecoHotMarginResp.newBuilder().addAllMixResult(resp).build());
    responseObserver.onCompleted();
}
```

这里我们我们会看到一个叫StreamObserver的东西，先贴出源码并加注释说明：

```java
public interface StreamObserver<V>  {
  
  //代表发送一个完整的数据，但不一定是一次完整请求
  void onNext(V value);

  //代表本次请求出现问题，给对方回执一个异常，这个异常会被序列化到对端进行回放
	void onError(Throwable t);

  //代表本次请求已经完成，由于TCP是全双工的，因此仅仅代表当前端不会再继续发送消息//否则会报错
  void onCompleted();
}
```

对于我们而言，能使用就这仨方法，**需要注意的是onError/onCompleted都是终止请求的回调，我们不能既调用onError又调用onCompleted。**StreamObserver作为一个Stream的观察者似乎能做的不多，gRPC类似其他RPC框架提供一套Interceptor机制，通过Interceptor我们可以操作更多的API或者说HOOK上一次请求更多的流程。

```java
public interface ServerInterceptor {
  //顾名思义，Call代表一次请求，下面会分析一下这个方法每个参数的含义
   ServerCall.Listener interceptCall(
      ServerCall call,
      Metadata headers,
      ServerCallHandler next);
}
```

ServerCall代表Server的一次请求，整理其核心API如下：

```java
public abstract class ServerCall {
 
  //代表给客户端回发Reponse Headers
  public abstract void sendHeaders(Metadata headers);   
  
  //代表给客户端返回实际消息数据
  public abstract void sendMessage(RespT message); 

  //代表当前请求在服务端已经结束，不会再接收或发送请求，请求成功与否取决于Status的具体值//Status后续我们会详细介绍
  public abstract void close(Status status, Metadata trailers);

}
```



ServerCall.Listener代表ServerCall的Listener，服务于整个请求生命周期，整理其核心API如下:

```java
public abstract static class Listener {
  
  //代表收到客户端请求的消息public void onMessage(ReqT message) {}

  //代表Client完成全部消息发送，不会再发更多，也代表服务端需要开始处理请求了
	public void onHalfClose() {}

  //代表本次请求被取消掉，通常发生在服务端执行出现异常的情况会被调用
	public void onCancel() {}

  //代表本次请求正常结束
  public void onComplete() {}

}
```

综上所述我们完整看到了三个组件提供的完整API，下面详细讲解一下三者API是如何联动的。按照一次请求在Server的完整流程，大致的执行顺序如下：

1. Listener.onMessage被执行，代表服务端已经收到客户端一个完整的Message
2. Listener.onHalfClose被执行，服务端即将执行我们实现的实际方法，譬如上述的margin方法
3. StreamObserver.onNext被调用，代表服务端完成消息处理并将结果返回
4. ServerCall.sendHeaders被执行，结果返回时会优先触发Header发送，符合预期
5. SeverCall.sendMessage被执行，通过该方法将返回结果投递出去
6. StreamObserver.onCompleted/onError被调用，代表业务标识本次请求服务端处理完成或者失败
7. ServerCall.close被调用，具体成功失败取决于6
8. Listener.onComplete/onCancel，具体成功失败取决于6

综上我们大致分析一下一个gRPC使用者能够感到的几种API以及他们大致的执行顺序



## 2 Client部分

相比于Server端，Client由于有Stub（例如XXXGrpc.newFutureStub）的缘故，我们不会感知到gRPC内部的API，但为便于大家深入理解，下面将简要介绍一个gRPC请求是如何通过Stub发送以及何时接收到请求的返回。与ServerAPI类似Client同样对应三个API：

1. StreamObserver
2. ClientCall
3. ClientCall.Listener

StreamObserver跟服务端一样不做介绍，下面着重介绍一下ClientCall及其Listener

ClientCall代表Client的一次请求，整理其核心API如下:

```java
public abstract class ClientCall<ReqT, RespT> {
   
    //顾名思义，代表开启一次请求，这个方法内部会对这个请求相关的内容进行初始化
    public abstract void start(Listener responseListener, Metadata headers);    
    
    //发送数据
  	public abstract void sendMessage(ReqT message);         
    
    //客户端发送数据完毕，不会再发更多消息
   	public abstract void halfClose();      
    
    //客户端取消本次请求，通常是出现异常或者超时被调用
    public abstract void cancel(@Nullable String message, @Nullable Throwable cause);
}
```

对应Listener核心API如下：

```java
public abstract static class Listener {

  //代表接收到服务端发送的Headers
  public void onHeaders(Metadata headers) {}

  //代表接收到服务端一次请求完整的数据
  public void onMessage(T message) {}

  //代表本次请求彻底结束，ListenableFuture的set or setException方法会被执行
  public void onClose(Status status, Metadata trailers) {}   
}
```

下面详细介绍一下客户端发起一次请求大致的流程

1. 通过用户XXXGrpc.newFutureStub获取的XXXFutureStub调用方法发起请求
2. ClientCall.start被调用，客户端创建当前请求相关的Stream及其上下文
3. ClientCall.sendMessage被调用，数据被发送到Buffer中等待被投递
4. ClientCall.halfClose被调用，代表Client完成本次请求的数据发送不会再进行更多数据发送
5. Listener.onHeaders被调用，代表服务端开始回数据，首先获取到Headers
6. Listener.onMessage被调用，收到完整的返回数据
7. Listener.onClose被调用，代表本次请求结束，最终用户拿到的ListenableFuture会被set，然后用户获取到本次的结果