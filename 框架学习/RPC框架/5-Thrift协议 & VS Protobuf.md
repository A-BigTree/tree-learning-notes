# Thrift协议 & VS Protobuf

[toc]

---

## 1 简介

Thrift是一套包含序列化功能和支持服务通信的RPC框架，主要包含三大部分：代码生成、序列化框架、RPC框架，大致相当于protoc + protobuffer + grpc，并且支持大量语言，保证常用功能在跨语言间功能一致，是一套全栈式的RPC解决方案。Thrift整体架构图如下：

![thrift_layer_arch](./5-Thrift%E5%8D%8F%E8%AE%AE%20&%20VS%20Protobuf.assets/thrift_layer_arch.png)

Thrift 本身是一个比较大的话题，本文不会涉及到全部内容，重点介绍其中的序列化协议。同时，Thrift 也是某音内部主要使用的 RPC 序列化协议，在本文的末尾也将其与 Protobuf 进行了简单的比较。

## 2 Thrift请求响应模型

### 2.1 请求响应模型

![thrift_req_rsp](./5-Thrift%E5%8D%8F%E8%AE%AE%20&%20VS%20Protobuf.assets/thrift_req_rsp.png)

在Thrift的官方Doc中将Thrift的RPC请求响应描述为上面的四个步骤。图中，最外层只有Message和Struct。
这里可以将Message和Struct类比为TCP中的首部和负载。Message中放的是传递的元信息(metadata)，Struct则包含的是具体传递的数据(payload)

注意这里**不要理解成了Client，Server在一个TCP上Send了两次**，而应该理解为字节流，2的数据紧跟在1的数据后面，4的数据紧跟在3的数据后面。

### 2.2 理解Message和Struct

#### Message

Message中主要包含Name,Message Type,Sequence ID等数据。

1. Name：为调用的方法名
2. Message Type:有Call, OneWay, Reply, Exception四种，在实际传递的时候，传递的是Type ID,这四种Type对应的Type ID如下

```shell
Call      ---> 1
OneWay    ---> 2
Reply     ---> 3
Exception ---> 4
```

其中Call、OneWay用于Request， Reply、 Exception用于Response中。
四者的含义如下:

- Call: 调用远程方法，并且期待对方发送响应。
- OneWay: 调用远程方法，不期待响应。即没有步骤3,4。
- Reply: 表明处理完成，响应正常返回。
- Exception:表明出理出错。

3. Sequence ID : 序列号, 有符号的四字节整数。在一个传输层的连接上所有未完成的请求必须有唯一的序列号，客户端使用序列号来处理响应的失序到达，实现请求和响应的匹配。服务端不需要检查该序列号，也不能对序列号有任何的逻辑依赖，只需要响应的时候将其原样返回即可。这里注意将Thrift序列号和我们常用的用于防止非幂等请求多次提交的unique ID区分开来。

#### Struct

在上面的Thrift请求响应模型中，有两种Struct：

- Request Struct

- Response Struct

这两种Struct的结构是一样的，都是由多个Field组成。

## 3 Thrift序列化协议

Thrift 在传输协议上总体划分为文本（Text）和二进制（Binary）传输协议。为节约带宽，提高传输效率，一般情况下使用二进制类型的传输协议。

常用协议有以下几种：

- TBinaryProtocol：二进制编码格式进行数据传输
- TCompactProtocol：高效率的、密集的二进制编码格式进行数据传输
- TJSONProtocol： 使用JSON文本的数据编码协议进行数据传输
- TSimpleJSONProtocol：只提供JSON只写的协议，适用于通过脚本语言解析

Thrift协议IDL Demo如下：

```protobuf
// 接口
service SupService {
    SearchByKeywordResponse SearchByKeyword(
        1: SearchByKeywordRequest request)
}
// 请求
struct SearchByKeywordRequest {
    1: optional string Keyword
    2: optional i32 Limit      
    3: optional i32 Offset 
}
// 假设request的payload如下：
{
    Keyword: "kwaishop",
    Limit: 50,
    Offset: null  
}
```



### 3.1 Binary协议

binary序列化是一种二进制的序列化方式。不可读，但传输效率高。

#### 3.1.1 Message序列化

Message的序列化分为两种，strict encoding和old encoding。 在有些实现中，会通过
检查Thrift消息的第一个bit来判断使用了那种encoding：

- 1 —-> strict encoding

- 0 —-> old encoding

Message的Binary序列化下面的一张图就够了:

![message_binary_encoding](./5-Thrift%E5%8D%8F%E8%AE%AE%20&%20VS%20Protobuf.assets/message_binary_encoding.png)

#### 3.1.2 Struct序列化

Struct装的是Thrift通信的实际参数，一个Struct由很多基本类型组合而成，要了解Struct怎么序列化的必须知道这些基本类型的序列化。下面我们就由大到小来逐一分析这些基本类型:

| 类型名    | idl类型名 | 占用字节数  | 类型ID |
| :-------- | :-------- | :---------- | :----- |
| bool      | bool      | 1           | 2      |
| byte      | byte      | 1           | 3      |
| short     | i16       | 2           | 6      |
| int       | i32       | 4           | 8      |
| long      | i64       | 8           | 10     |
| double    | double    | 8           | 4      |
| string    | string    | 4+N         | 11     |
| []byte    | binary    | 4+N         |        |
| list      | list      | 1+4+N       | 15     |
| set       | set       | 1+4+N       | 14     |
| map       | map       | 1+1+4+NX+NY | 13     |
| field     |           | 1+2+X       |        |
| struct    | struct    | N*X         | 12     |
| enum      |           |             |        |
| union     |           |             |        |
| exception |           |             |        |

##### 定长编码

上表中的 bool, byte, short, int, long, double采用的都是固定字节数编码，各类型占用的字节数见上。

##### 长缀编码

string, byte array采用的是长度前缀编码，前四个字节(无符号四字节整数)表示长度，后面跟着的就是实际的内容。

##### map编码

![thrift_map](./5-Thrift%E5%8D%8F%E8%AE%AE%20&%20VS%20Protobuf.assets/thrift_map.png)

其中key-type和value-type可以是任何基本类型。注意将此处的map与python中的dict区分，这里的key和value各自都必须是同种类型，而python中dict是多态字典。

##### list&set编码

![thrift_list_set](./5-Thrift%E5%8D%8F%E8%AE%AE%20&%20VS%20Protobuf.assets/thrift_list_set.png)

注意与python中的list,set区分，这里的list，set中的元素必须是同一种类型。

##### field编码

![thrift_field](./5-Thrift%E5%8D%8F%E8%AE%AE%20&%20VS%20Protobuf.assets/thrift_field.png)

iled不是一个实际存在的类型，而是一个抽象概念。field不独立出现，而是出现在struct内部，其中field-type可以是任何其他的类型，field-id就是定义IDL时该field在struct的编号，field-value是对应类型的值的序列化结果。

##### struct编码

struct的编码，一个struct就是由多个field编码而成，最后一个field排列完成之后是一个stop field，这个field是一个8bit全为0的字节，它标志着一条Thrift消息的结束。**这也是上面思考题的答案**。

```shell
---------------------------------------------
| field1 | field2 |...| fieldN | stop field |        stop field: 00000000
|    M   |    M   |...|    M   |            |     所以Message Type编码的时候不能用0
---------------------------------------------
```

thrift序列化的时候并没有将字段名给序列化进去，所以在idl文件中更改字段名是没有任何影响的。

### 3.2 Compact协议

Compact序列化也是一种二进制的序列化，不同于Binary的点主要在于整数类型采用了zigzag 和 varint压缩编码实现，这里简要介绍下zigzag 和 varint整数编码。

#### 3.2.1 VarInt编码

对于一个整形数字，一般使用 4 个字节来表示一个整数值。但是经过研究发现，消息传递中大部分使用的整数值都是很小的非负整数，如果全部使用 4 个字节来表示一个整数会很浪费。比如数字1用四字节表示就是这样:

`00000000 00000000 00000000 00000001`

对较小整数来说，这种固定字节数编码很浪费bit。所以人们就发明了一个类型叫变长整数varint。数值非常小时，只需要使用一个字节来存储，数值稍微大一点可以使用 2 个字节，再大一点就是 3 个字节，它还可以超过 4 个字节用来表达长整形数字。

其原理也很简单，就是保留每个字节的最高位的bit来标识后一个字节是否属于该bit，1表示属于，0表示不属于。

示意图如下:

![thrift_zigzag](./5-Thrift%E5%8D%8F%E8%AE%AE%20&%20VS%20Protobuf.assets/thrift_zigzag.png)

由于大多数时候使用的是较小的整数，所以总体上来说，Varint编码的方式可以有效的压缩多字节整数。

那么对于负数怎么办呢?大家知道负数在计算机中是以==补码==的形式存在的。

```shell
10000000 00000000 00000000 00000001  -1的原码
11111111 11111111 11111111 11111110  -1的反码
11111111 11111111 11111111 11111111  -1的补码
```

所以-1在计算机中就是`11111111 11111111 11111111`,如果按照Varint编码，那么需要6个字节才能存的下，但是在现实生活中，-1却是个常用的整数。越大的负数越常见，编码需要的字节数越大，这显然是不能容忍的。为了解决这个问题, 就要使用ZigZag编码压缩技术了。

#### 3.2.2 ZigZag编码

zigzag 编码专门用来解决负数编码问题。zigzag 编码将整数范围一一映射到自然数范围，然后再进行 varint 编码。

```shell
0 => 0
-1 => 1
1 => 2
-2 => 3
2 => 4
-3 => 5
3 => 6
```

zigzag 将负数编码成正奇数，正数编码成偶数。解码的时候遇到偶数直接除 2 就是原值，遇到奇数就加 1 除 2 再取负就是原值。

Compact编码——压缩的二进制。Compact序列化的实现大致逻辑和Binary序列化实现是一样的，就是将i16、i32、i64三种类型使用zigzag+varint编码实现，string、binary、map、list、set复合类型的长度只采用varint编码没使用zigzag，其他逻辑几乎一样。

### 3.3 JSON协议

不过多介绍

## 4 optional、require 实现原理

optional 表示字段可填，require 表示必填

- 字段被标识为 optional 之后：
  1. 基本类型会被编译为指针类型
  2. 序列化代码会做空值判断，如果字段为空，则不会被编码

- 字段被标识为 require 之后：
  1. 基本类型会被编译为非指针类型（复合类型 optional 和 require 没区别）
  2. 序列化不会做空值判断，字段一定会被编码。如果没有显式赋值，就编码默认值（默认空值，或者 IDL 显式指定的默认值）



## 5 Thrift协 VS Protobuf协议

对于 Protobuf，本文不做过多介绍。此处简要比较 Thrift 与 Protobuf 在几个方面的不同。

- Protobuf 协议编解码方式只有一种，而 Thrift 有 3 种（Compact 协议与 Protobuf 编码比较类似）
- Protobuf 以小端序传输，Thrift 以大端序传输
- Protobuf 支持反射、Thrift 不支持
- Protobuf 3.0 后支持更丰富的类型比如 time、date、duration，Thrift 不支持
- 早期 Thrift 支持的语言更广泛一些（尤其是对 Go 语言的支持），后来 Protobuf 也逐渐支持更多语言