# gRPC与PB&桩代码生成与扩展

[toc]

---

PB即protobuf全名是ProtocolBuffers，是谷歌推出的二进制序列化协议，提供IDL文件来定义各种类型的数据。目前整体协议版本是proto3，protobuf提供了从proto文件编译生成各个语言文件的功能。与此同时protobuf提供了丰富的插件机制，用户可以扩展生成的对应语言的文件，俗称桩代码生成。gRPC正是通过插件式的扩展机制完成相关桩代码的生成。

## 1 ProtocolBuffers协议详详解

### 1.1 proto文件路径说明

```protobuf
syntax = "proto3";

package com.grpc.test;

option java_package = "com.grpc.demo";
option java_outer_classname = "Demo";
option java_multiple_files = true;

message TestRequest {
    string value = 1;
}

message TestResponse {
    string value = 1;
}

service TestService {
    rpc Test (TestRequest) returns (TestResponse);
    rpc TestDeadline (TestRequest) returns (TestResponse);
    rpc TestCost (TestRequest) returns (TestResponse);
    rpc TestEmpty (TestRequest) returns (TestResponse);
}

service TestStreamService {
    rpc Test (TestRequest) returns (stream TestResponse);
    rpc TestFailed (TestRequest) returns (stream TestResponse);
    rpc TestSlow (TestRequest) returns (stream TestResponse);
    rpc TestUpStream(stream TestRequest) returns (TestResponse);
    rpc TestUpStreamSlow(stream TestRequest) returns (TestResponse);
    rpc TestDuplex(stream TestRequest) returns (stream TestResponse);
    rpc TestOutboundFlowControl (TestRequest) returns (stream TestResponse);
}
```

如上面proto文件所示，一共有三个路径：

1. proto文件本身所在路径：假设是grpc/demo；使用场景是当其他proto文件需要引入当前proto文件时依赖这个路径，形如` import "grpc/demo/demo.proto"`。这样就可以使用文件内部定义的message了；
2. proto文件内部package指定路径：com.grpc.test；这个路径非常的核心，任何迁移或者修改proto文件的操作都不应该修改这个package路径，因为它决定了文件内部service的full path name。一旦发生变化就会造成在实际gRPC调用时找不到服务方法而抛异常（Unimplemented）；
3. option参数指定java输出路径：com.grpc.demo；这个路径也比较核心，定义好之后也不要轻易修改，因为它决定了proto中message类的生成路径，如果修改会造成源码不兼容编译阻塞；



### 1.2 proto文件输出样式

1. proto文件的名称，例如`grpc_demo.proto`；在没指定2的前提下，会默认按照驼峰生成OuterClass，对于本例为`GrpcDemo.java`；有意思的是当proto文件中的message跟默认生成java类名重名的时候，会在生成类名后面加上OuterClass，对于本例为`GrpcDemoOuterClass`；
2. `option java_outer_classname = "Demo"`：如果指定此选项，则输出文件名称为指定名称Demo；
3. `option java_multiple_files = true`：指定此选项proto文件中的message会生成独立的java文件，此选项定义好后也不要轻易修改，否则也会造成编译阻塞，推荐开启这个选项；



## 2 gRPC装代码生成

gRPC通过Protocol Buffers提供的Plugin机制来实现原生桩代码生成，Protobuf的插件化桩代码生成机制主体思想如下：

1. `FileDescriptor` 作为Plugin的输入参数，代表一个proto文件的描述；
2. `ServiceDescriptor`代表一个service的描述符；
3. `MethodDescriptor`代表一个service中方法的描述符，包括之前描述的四种`MethodType`；
4. `Printer`作为输出写文件到对应路径；

### 2.1 gRPC原生桩代码生成

gRPC生成的桩代码可以拆解为如下几个部分：

1. `ServiceDescriptor`、`MethodDescriptor`单例变量，出于性能考虑
2. ClientStub，主要包括三种：
   - `XXXBlockingStub`：官方已经弃用；
   - `XXXFutureStub`：返回`ListeningFuture`，也是我们使用最多的方式；
   - `XXXStub`：异步方式，业务通过`StreamObserver`回调来感知数据流转；
   - `XXXImplBase`：服务实现基类；

### 2.2 gRPC桩代码生成扩展

gRPC桩代码生成的扩展方式有两种：

1. 在原生桩代码生成之上做修改，需要修改为**c++**文件重新编译构建
2. 直接利用Protocol Buffers提供的扩展机制自己生成全新的文件

#### 2.2.1 原生桩代码修改

以Unary模式屏蔽StreamObserver为例：

proto文件如下：

```protobuf
syntax = "proto3";
 
package grpc.health.v1;
 
option csharp_namespace = "Grpc.Health.V1";
option java_multiple_files = true;
 
message HealthCheckRequest {
    string service = 1;
}
 
message HealthCheckResponse {
    enum ServingStatus {
        UNKNOWN = 0;
        SERVING = 1;
        NOT_SERVING = 2;
    }
    ServingStatus status = 1;
    oneof opt_idle {
        double idle = 2; // range (0,1]
    }
    uint32 active_thread_count = 3;
    uint32 max_thread_count = 4;
}
 
service Health {
    rpc Check (HealthCheckRequest) returns (HealthCheckResponse);
    rpc StreamCheck (stream HealthCheckRequest) returns (stream HealthCheckResponse);
}
```

新生成的代码实例如下：

```java
public static abstract class HealthImplBaseV2 extends HealthImplBase {
 
  public abstract grpc.health.v1.HealthCheckResponse check(grpc.health.v1.HealthCheckRequest request) throws java.lang.Throwable;
 
   @java.lang.Override
  public final void check(grpc.health.v1.HealthCheckRequest request,
      io.grpc.stub.StreamObserver<grpc.health.v1.HealthCheckResponse> responseObserver) {
    try {
      grpc.health.v1.HealthCheckResponse response = check(request);
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    } catch (java.lang.Throwable t) {
      com.google.common.base.Throwables.throwIfUnchecked(t);
      throw new com.google.common.util.concurrent.UncheckedExecutionException("GRPC_INTERNAL", t);
    }
  }
```

扩展如下：

```java
private static class Impl extends HealthGrpc.HealthImplBaseV2 {
 
        @Override
        public HealthCheckResponse check(HealthCheckRequest request) throws java.lang.Throwable;
            try {
                //todo 写下你的业务逻辑
                return null;
            } catch (Exception e) {
                //添加fallback逻辑
                return null;
            }
        }
}
```

#### 2.2.2 插件实现

1. 自定义一个Generator，并实现`GenerateFiles`方法

```xml
<dependency>
    <groupId>com.salesforce.servicelibs</groupId>
    <artifactId>jprotoc</artifactId>
    <version>xxx</version>
</dependency>
```



```java
package com.grpc.test;

...
public class XXXXGrpcGenerator extends com.salesforce.jprotoc.Generator {
 
    public static void main(String[] args) {
        com.salesforce.jprotoc.ProtocPlugin.generate(new XXXXGrpcGenerator());
    }
    ...
        /**
     * Processes a generator request into a set of files to output.
     *
     * @param request The raw generator request from protoc.
     * @return The completed files to write out.
     */
    public List<PluginProtos.CodeGeneratorResponse.File> generateFiles(PluginProtos.CodeGeneratorRequest request) throws GeneratorException {
        //customize protoc generate code here
        return Collections.emptyList();
    }
}
```

2. plugin配置插件类

```xml
<plugin>
    <groupId>org.xolstice.maven.plugins</groupId>
    <artifactId>protobuf-maven-plugin</artifactId>
    <version>xxxx</version>
    <configuration>
        <protocArtifact>com.google.protobuf:protoc:${protoc.version}:exe:${os.detected.classifier}</protocArtifact>
        <pluginId>grpc-java</pluginId>
        <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
    </configuration>
    <executions>
        <execution>
            <goals>
               <goal>compile</goal>
               <goal>compile-custom</goal>
               <goal>test-compile</goal>
               <goal>test-compile-custom</goal>
             </goals>
            <configuration>
                <protocPlugins>
                    <protocPlugin>
                        <id>rxgrpc</id>
                        <groupId>com.grpc.test</groupId>
                        <artifactId>grpc-code-generate-plugin</artifactId>
                        <version>${plugin.version}</version>
                        <mainClass>com.grpc.test.XXXXGrpcGenerator</mainClass>
                    </protocPlugin>
                </protocPlugins>
            </configuration>
        </execution>
    </executions>
</plugin>
```

