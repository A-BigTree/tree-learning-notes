# Protobuf入门

[toc]

---

## 1 前言

在IM应用中，优化数据流量消耗过多的基本方法就是使用高度压缩的通讯协议，而数据压缩后流量减小带来的自然结果也就是省电：因为大数据量的传输必然需要更久的网络操作、数据序列化及反序列化操作，这些都是电量消耗过快的根源。

当前IM应用中最热门的通讯协议无疑就是Google的Protobuf了，基于它的优秀表现，微信和手机QQ这样的主流IM应用也早已在使用它。



## 2 什么是Protocol Buffer

什么是 Google Protocol Buffer？

> Google Protocol Buffer（简称 Protobuf）是 Google 公司内部的混合语言数据标准，目前已经正在使用的有超过 48,162 种报文格式定义和超过 12,183 个 .proto 文件。他们常用于 RPC 系统和持续数据存储系统等应用场景。

**实际上：**Protocol Buffers（简称 Protobuf）是一种轻便高效的结构化数据存储格式，可以用于结构化数据串行化，或者说序列化。它很适合做数据存储或 RPC 数据交换格式。可用于通讯协议、数据存储等领域的语言无关、平台无关、可扩展的序列化结构数据格式。

**目前：**Protobuf官方工程主页上显示的已支持的开发语言多达10种，分别有：C++、Java、Python、Objective-C、C#、Ruby、Go、PHP、Dart、Javascript，基本上主流的语言都已支持（具体详见Protobuf工程主页：https://github.com/protocolbuffers/protobuf）。

Protobuf数据类型与其他语言的对照：

| .proto Type | C++ Type | Java/Kotlin Type[1] | Python Type[3]                  | Go Type | Ruby Type                      | C# Type    | PHP Type       | Dart Type |
| ----------- | -------- | ------------------- | ------------------------------- | ------- | ------------------------------ | ---------- | -------------- | --------- |
| double      | double   | double              | float                           | float64 | Float                          | double     | float          | double    |
| float       | float    | float               | float                           | float32 | Float                          | float      | float          | double    |
| int32       | int32    | int                 | int                             | int32   | Fixnum or Bignum (as required) | int        | integer        | int       |
| int64       | int64    | long                | int/long                        | int64   | Bignum                         | long       | integer/string | Int64     |
| uint32      | uint32   | int                 | int/long                        | uint32  | Fixnum or Bignum (as required) | uint       | integer        | int       |
| uint64      | uint64   | long                | int/long                        | uint64  | Bignum                         | ulong      | integer/string | Int64     |
| sint32      | int32    | int                 | int                             | int32   | Fixnum or Bignum (as required) | int        | integer        | int       |
| sint64      | int64    | long                | int/long                        | int64   | Bignum                         | long       | integer/string | Int64     |
| fixed32     | uint32   | int                 | int/long                        | uint32  | Fixnum or Bignum (as required) | uint       | integer        | int       |
| fixed64     | uint64   | long                | int/long                        | uint64  | Bignum                         | ulong      | integer/string | Int64     |
| sfixed32    | int32    | int                 | int                             | int32   | Fixnum or Bignum (as required) | int        | integer        | int       |
| sfixed64    | int64    | long                | int/long                        | int64   | Bignum                         | long       | integer/string | Int64     |
| bool        | bool     | boolean             | bool                            | bool    | TrueClass/FalseClass           | bool       | boolean        | bool      |
| string      | string   | String              | str/unicode                     | string  | String (UTF-8)                 | string     | string         | String    |
| bytes       | string   | ByteString          | str (Python 2) bytes (Python 3) | []byte  | String (ASCII-8BIT)            | ByteString | string         | List      |



### 2.1 技术背景

 `Protobuf`最先开始是 Google用来解决索引服务器 `request/response` 协议的。在没有`Protobuf`之前，Google 已经存在了一种 `request/response` 格式，用于手动处理 `request/response` 的编解码。

如果是非常明确的格式化协议，会使新协议变得非常复杂。因为开发人员必须确保请求发起者与处理请求的实际服务器之间的所有服务器都能理解新协议，然后才能切换开关以开始使用新协议。这也就是每个服务器开发人员都遇到过的低版本兼容、新旧协议兼容相关的问题。为了解决这些问题，于是Protobuf就诞生了。



### 2.2 Protobuf在谷歌业务中的地位

Protobuf 现在是 Google 用于数据交换和存储的通用语言。谷歌代码树中定义了 48162 种不同的消息类型，包括12183个 `.proto` 文件。它们既用于 RPC 系统，也用于在各种存储系统中持久存储数据。

Protobuf 诞生之初是为了解决服务器端新旧协议（高低版本）兼容性问题，名字也很体贴——“协议缓冲区”，只不过后期慢慢发展成用于传输数据。



## 3 Protocol协议的消息定义

Protobuf 的消息是在idl文件（`.proto`）中描述的。

```protobuf
syntax = "proto3";
 
package domain;
 
option java_package = "com.Protobuf.generated.domain";
option java_outer_classname = "CustomerProtos";
 
message Customers {
    repeated Customer customer = 1;
}
 
message Customer {
    int32 id = 1;
    string firstName = 2;
    string lastName = 3;
 
    enum EmailType {
        PRIVATE = 0;
        PROFESSIONAL = 1;
    }
 
    message EmailAddress {
        string email = 1;
        EmailType type = 2;
    }
 
    repeated EmailAddress email = 5;
}
```

上面的消息比较简单，`Customers`包含多个`Customer`（`Customer`包含一个id字段、一个`firstName`字段、一个`lastName`字段以及一个email的集合）。

**除了上述定义外，文件顶部还有三行可帮助代码生成器的声明：**

- `syntax = "proto3"`：用于idl语法版本，目前有两个版本proto2和proto3，两个版本语法不兼容，如果不指定，默认语法是proto2（由于proto3比proto2支持的语言更多，语法更简洁，本文使用的是proto3）；
- `package domain`：此配置用于嵌套生成的类/对象；
- `option java_package`：生成器还使用此配置来嵌套生成的源（此处的区别在于这仅适用于Java，在使用Java创建代码和使用JavaScript创建代码时，使用了两种配置来使生成器的行为有所不同。也就是说，Java类是在包`com.Protobuf.generated.domain`下创建的，而JavaScript对象是在包domain下创建的）；



## 4 Protobuf的代码生成

首先安装 Protobuf 编译器 protoc（点[这里](https://developers.google.com/protocol-buffers/docs/downloads)有详细的安装教程）。

**安装完成后，可以使用以下命令生成 Java 源代码：**

```java
protoc --java_out=./src/main/java ./src/main/idl/customer.proto
```

**上述命令的意图是：**从项目的根路径执行该命令，并添加了两个参数 `java_out`（即定义 `./src/main/java/` 为Java代码的输出目录；而 `./src/main/idl/customer.proto` 是`.proto`文件所在目录）。

生成的代码很复杂，但用法却很简单：

```java
CustomerProtos.Customer.EmailAddress email = CustomerProtos.Customer.EmailAddress.newBuilder()
        .setType(CustomerProtos.Customer.EmailType.PROFESSIONAL)
        .setEmail("crichardson@email.com").build();
 
CustomerProtos.Customer customer = CustomerProtos.Customer.newBuilder()
        .setId(1)
        .setFirstName("Lee")
        .setLastName("Richardson")
        .addEmail(email)
        .build();
// 序列化
byte[] binaryInfo = customer.toByteArray();
System.out.println(bytes_String16(binaryInfo));
System.out.println(customer.toByteArray().length);
// 反序列化
CustomerProtos.Customer anotherCustomer = CustomerProtos.Customer.parseFrom(binaryInfo);
System.out.println(anotherCustomer.toString());
```



## 5 Protobuf的优点

### 5.1 效率高

从序列化后的数据体积角度，与XML、JSON这类文本协议相比，`Protobuf`通过 `T-(L)-V（TAG-LENGTH-VALUE）`方式编码，不需要`", {, }, :`等分隔符来结构化信息。同时在编码层面使用varint压缩。

所以描述同样的信息，Protobuf序列化后的体积要小很多，在网络中传输消耗的网络流量更少，进而对于网络资源紧张、性能要求非常高的场景。比如在移动网络下的IM即时通讯应用中，Protobuf协议就是非常不错的选择。

从序列化/反序列化速度角度，与XML、JSON相比，Protobuf序列化/反序列化的速度更快，比XML要快20-100倍。

### 5.2 支持跨平台、多语言

Protobuf是平台无关的，无论是Android、iOS、PC，还是C#与Java，都可以利用Protobuf进行无障碍通讯。

### 5.3 扩展性、兼容性好

Protobuf具有向后兼容的特性：更新数据结构以后，老版本依旧可以兼容，这也是Protobuf诞生之初被寄予解决的问题，因为编译器对不识别的新增字段会跳过不处理。

### 5.4 使用简单

Protobuf 提供了一套编译工具，可以自动生成序列化、反序列化的样板代码，这样开发者只要关注业务数据idl，简化了编码解码工作以及多语言交互的复杂度。



## 6 Protobuf的缺点

- 不具备自描述能力：跟XML、JSON相比，这两者是自描述的，而`ProtoBuf`则不是；
- 数据可读性非常差：`ProtoBuf`是二进制协议，如果没有idl文件，就无法理解二进制数据流，对调试非常不友好；

由于没有idl文件无法解析二进制数据流，`ProtoBuf`在一定程度上可以保护数据，提升核心数据被破解的门槛，降低核心数据被盗爬的风险（也算是缺点变优点的典型范例）。



