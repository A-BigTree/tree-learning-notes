# 3 Transport layer运输层



## 3.1 概述和运输层服务

> Introduction and Transport-Layer Services

运输层协议为运行在不同的主机上的应用进程之间提供了**逻辑通信（logic communication）**功能从应用程序的角度看，通过逻辑通信，运行不同进程的主机好像直接相连一样。逻辑通信的概念如下：

<img src="image/image-20230207180453857.png" alt="image-20230207180453857" style="zoom: 33%;" />

运输层协议是**在端系统中**而不是在路由器中实现。在发送端，运输层将从发送应用程序接收到的报文转换成传输层分组，用因特网术语来讲该分组称为运输层**报文段（segment）**。

### 3.1.1 运输层和网络层的关系

> Relationship Between Transport and Network Layers

在协议栈中，运输层刚好位于网络层之上。网络层提供了**主机之间**的逻辑通信，而运输层为运行在**不同主机上的进程之间**提供了逻辑通信。运输协议能够能够提供的服务常常受制于底层网络层协议的服务模型。如果网络层协议无法为主机之间发送的运输层报文段提供时延或带宽保证的话，运输层协议也就无法为进程之间发送的应用程序报文提供时延或带宽保证。

即使底层网络协议是不可靠的，也就是说网络层协议会使分组丢失、篡改和冗余，运输层协议也能为应用程序提供可靠的数据传输服务。另一方面，即使网络层不能保证运输层报文段的机密性，运输协议也能使用加密来确保应用程序报文不被入侵者读取。

### 3.1.2 因特网运输层概述

> Overview of the Transport Layer in the Internet

因特网提供两种不同的可用的运输层协议。**UDP（User Datagram Protocol）**，它为调用它的应用程序提供了一种不可靠、无连接的服务；**TCP（Transmission Control Protocol）**，它为调用它的应用程序提供了一种可靠的、面向连接的服务。

因特网网络层协议有一个名字叫IP（Internet Protocol），即网际协议，IP的服务模型是**尽力而为交付服务（best-effort delivery service）**，同时IP被称为**不可靠服务（unreliable service）**。

UDP和TCP最基本的责任是，将两个端系统间IP的交付服务扩展为运行在端系统上的两个进程之间的交付服务。将主机间交付扩展到进程间交付被称为**运输层的多路复用（transport-layer multiplexing）**与**多路分解（demultiplexing）**。



## 3.2 多路复用与多路分解

> Multiplexing and Demultiplexing

运输层的多路复用与多路分解，也就是将由网络层提供的主机到主机的交付服务延伸到为运行在主机上的应用程序提供进程到进程的服务。一个进程（作为网络应用的一部分）有一个或多个**套接字（socket）**，它相当于从网络向进程传递数据和从进程向网络传递数据的门户。下图为进程交付过程：

<img src="image/image-20230207191811825.png" alt="image-20230207191811825" style="zoom:50%;" />

- 多路分解（Demultiplexing）：在接收端，运输层检查这些字段，标识出接收套接字，进而将报文段定向到该套接字，即将运输层报文段的数据交付到正确的套接字的工作；
- 多路复用（Multiplexing）：在源主机从不同套接字中收集数据块，并为每个数据块封装上首部信息（这将在以后用于分解）从而生成报文段，然后将报文传递到网络层；

运输层多路复用要求：

1. 套接字由唯一标识符；
2. 每个报文段有特殊字段来指示该报文段所要交付的套接字；

这些特殊字段是**源端口号字段（source port number field）**和**目的端口号字段（destination port number field）**，如下图所示：

<img src="image/image-20230207195316382.png" alt="image-20230207195316382" style="zoom:50%;" />

端口号是一个16比特的数，其大小在`0~65535`之间。`0~1023`范围的端口号称为**周知端口号（well-know port number）**，是受限制的。

### UDP的socket表示

二元组表示👉`(源端口， 目的端口)`，具体过程如下图所示：

<img src="image/image-20230207195813879.png" alt="image-20230207195813879" style="zoom:50%;" />

### TCP的socket表示

四元组表示👉`(源IP地址， 源端口号， 目的IP地址， 目的端口号)`，下图为两个客户使用相同的目的端口号（80）与同一个Web服务器应用通信：

<img src="image/image-20230207200254765.png" alt="image-20230207200254765" style="zoom: 50%;" />

## 3.3 无连接运输：UDP

> Connectionless Transport: UDP

使用UDP时，在发送报文段之前，发送方和接收方的运送层实体之间没有握手，UDP被称为**无连接的（connectionless）**。许多应用更适合用UDP的原因如下：

- 无拥塞控制（no congestion control）；
- 无须连接建立（no connection establishment)；
- 无连接状态（no connection state）；
- 分组首部开销小（small header size）；

流行的因特网应用及其下面的运输协议如下图：

<img src="image/image-20230208135204680.png" alt="image-20230208135204680" style="zoom:50%;" />

### 3.3.1 UDP报文段结构

> UDP Segment Structure

报文段包括上面提到的源端口号和目的端口号；还有长度字段，指明UDP报文段中的字节数（首部➕数据）；报文段中的检验和字段来检查在该报文段中是否出现差错。报文段结构如下图：

<img src="image/image-20230208135833107.png" alt="image-20230208135833107" style="zoom:50%;" />

### 3.3.2 UDP检验和

> UDP Checksum

发送发的UDP对报文段中的所有16比特字的和进行反码运算，求和时遇到的任何溢出都被**回卷（溢出位与低16位相加）**，下图为两个16比特字相加过程：

<img src="image/image-20230208143928024.png" alt="image-20230208143928024" style="zoom:50%;" />

接收方，全部的16比特字（包括检验和）加在一起，如果分组中没有差错，接收方处该和将是`1111111111111111`，如果比特字中出现`0`，那么就认为该分组出现差错。



## 3.4 可靠数据传输原理

> Principles of Reliable Data Transfer

可靠数据传输的框架：为上层实体提供的服务抽象是：数据可以通过一条可靠的信道进行传输。如下图所示：

<img src="image/image-20230208194116823.png" alt="image-20230208194116823" style="zoom:50%;" />

实现这种服务抽象是**可靠数据传输协议（reliable data transfer protocol，rdt）**的责任，下图为可靠数据传输协议的相关接口（Interface）：

<img src="image/image-20230208211608497.png" alt="image-20230208211608497" style="zoom:50%;" />

### 3.4.1 构造可靠数据传输协议

> Building a Reliable Data Transfer Protocol

#### 经完全可靠信道的可靠数据传输：rdt1.0

发送方和接收方的表示使用==**有限状态机（Finite-State Machine，FSM）**==定义，如果对一个事件没有动作，或没有就发生发生而采取了一个动作，我们将在横线上方或下方使用==**符号$\Lambda$**==，以分别明确地表示缺少动作或事件。

我们考虑最简单的情况，**即==底层信道==是完全可靠的**，我们称该协议为`rdt1.0`，发送方和接收方的FSM定义如下：

<img src="image/image-20230209132658120.png" alt="image-20230209132658120" style="zoom:50%;" />

#### 经具有比特差错信道的可靠数据传输：rdt2.0

底层信道更为实际的模型是分组中的比特可能受损的模型。在分组传输、传播或缓存的过程中，这种比特差错通常会出现在网络的物理部件中。在接收方得到比特差错的信息时，需要发送方进行重传。在计算机网络环境中，基于这种重传机制的可靠数据传输协议称为**自动重传请求（Automatic Repeat reQuest，ARQ）协议**。ARQ协议中还需要另外三种协议来处理存在比特差错的情况：

- 差错检测：发送方同时发送检测和（checksum）到接收端判断是否出现比特差错；
- 接收方反馈：
  - **肯定确认（Acknowledgement，ACK）**：接收方告诉发送方包数据无差错；
  - **否定确认（Negative acknowledgement，NAK）**：接收方告诉发送方包数据有错误；
- 重传：接收方收到有差错的分组时，发送方将重传该分组文；

`rdt2.0`的FSM如下图：

<img src="image/image-20230209154021285.png" alt="image-20230209154021285" style="zoom:50%;" />

当发送方处于等待ACK或NAK的状态时，它不能从上层获得更多的数据；这就是说，`rdt_send()`事件不可能出现；仅当接收到ACK并离开该状态时才能发生这样的事件。因此，发送方将不会发送一块新数据，除非发送方确信接收方已正确接收当前分组。由于这种行为，`rdt2.0`这样的协议被称为**停等（stop-and-wait）协议**。

`rdt2.0`协议并没有考虑ACK和NAK受损的情况，为了解决这一问题，就是在数据分组中添加一新字段，让发送方对其数据分组编号，即将发送数据分组的**序号（sequence number）**放在该字段。于是，接收方只需要检查序号即可确定收到的分组是否一次重传。

`rdt2.1`的FSM如下图：

<img src="image/image-20230210155101388.png" alt="image-20230210155101388" style="zoom:50%;" />

<img src="image/image-20230210155131256.png" alt="image-20230210155131256" style="zoom:50%;" />

如果收到受损的分组，则接收方将发送一个否定确认。如果不发送NAK，而是对上次正确接收的分组发送一个ACK，我们也能实现与NAK一样的效果。发送方接收到对同一个分组的两个ACK，即冗**余ACK（duplicate ACK）**后，就知道接收方没有正确接收到跟在被确定两次的分组后面的分组。

`rdt2.2`的FSM如下图所示：

<img src="image/image-20230210155706335.png" alt="image-20230210155706335" style="zoom:50%;" />

<img src="image/image-20230210155805310.png" alt="image-20230210155805310" style="zoom:50%;" />

#### 经具有比特差错的丢包信道的可靠数据传输：rdt3.0

- 因为特别大的时延，发送方和接收方的信道中引入了**冗余数据分组（duplicate data packet）**的可能性；
- 需要一个**倒计数定时器（countdown timer）**，实现基于时间的重传机制；

`rdt3.0`发送方FSM如下图：

<img src="image/image-20230210161617949.png" alt="image-20230210161617949" style="zoom:50%;" />

因为分组序号在0和1之间交替，因此`rdt3.0`有时被称为**比特交替协议（alternating-bit protocol）**。运行如下图所示：

<img src="image/image-20230210165655134.png" alt="image-20230210165655134" style="zoom:50%;" />

### 3.4.2 流水线可靠数据传输协议

> Pipelined Reliable Data Transfer Protocols

定义发送方（或信道）的**利用率（utilization）**为：发送方实际忙于将放送比特送进信道的那部分时间与发送时间之比。对于一个等停协议的发送方利用率$U_{sender}$：
$$
U_{sender}=\frac{L/R}{RTT+L/R}
$$
对于停等协议的发送发利用率是非常低的，解决该问题的方法是：不以停等方式运行，允许发送方发送多个分组而无须等待确认。因为许多从发送方向接收方输送可以看成是填充到一条流水线中，故这种技术被称为**流水线（pipeline）**，下图为停等和流水线发送示意图：

<img src="image/image-20230212213158363.png" alt="image-20230212213158363" style="zoom:50%;" />

### 3.4.3 回退N步

> Go-Back-N,GBN

在回退N步协议中，将基序号（base）定义为最早未确认分组的序号，将下一个序号（nextseqnum）定义为最小的未使用序号，则将序号范围分割成如下4段：

<img src="image/image-20230212220220437.png" alt="image-20230212220220437" style="zoom: 50%;" />

- `[0,base-1]`👉已经发送并得到确认的分组；
- `[base,nextseqnum-1]`👉已经发送但未被确认的分组；
- `[nextseqnum,base+N-1]`👉能用于那些要被立即发送的分组；
- `[base+N,end]`👉不能使用；

那些已被发送但还未被确认的分组的许可序号范围可以被看成一个在序号范围内长度未`N`的窗口。随着协议的运行，该窗口在序号空间向前滑动。因此，N常被称为**窗口长度（window size）**，GBN协议也常被称为**滑动窗口协议（sliding-window protocol）**。

为一个基于ACK、无NAK的GBN协议的发送方和接收方两端的扩展FSM描述，因为加入了变量`base`和`nextseqnum`所以我们称其为扩展FSM，下图为发送端的扩展FSM描述：

<img src="image/image-20230212224217547.png" alt="image-20230212224217547" style="zoom:50%;" />

下图为接收端的FSM描述：

<img src="image/image-20230212224419837.png" alt="image-20230212224419837" style="zoom:50%;" />

在GBN协议中，接收方丢弃所有失序分组。这种方法的优点是接收缓存简单，即接收方不需要缓存任何失序分组。因此，虽然发送方必须维护窗口的上下边界及`nextseqnum`在该窗口的位置，但接收方需要维护的唯一信息就是下一个按序接收的分组序号。运行中的GBN如下图所示：

<img src="image/image-20230212230429927.png" alt="image-20230212230429927" style="zoom:50%;" />

### 3.4.4 选择重传

> Selective Repeat，SR

在GBN发送方有窗口N的基础上，接收方同时维护一个窗口N，下图为SR发送方和接收方的序号空间：

<img src="image/image-20230212232753731.png" alt="image-20230212232753731" style="zoom:50%;" />

SR具体操作如下：

<img src="image/image-20230212233115187.png" alt="image-20230212233115187" style="zoom:50%;" />

## 3.5 面向连接的运输：TCP

> Connection-Oriented Transport: TCP

### 3.5.1 TCP连接

> TCP Connection

- **面向连接（connection-oriented）**：相互发送预备报文段，以建立确保数据传输的参数；
- **全双工服务（full-duplex service）**：如果一台主机上的进程A与另一台主机上的进程B存在一条TCP连接，那么应用层数据就可以在从进程B流向进程A的同时，也从进程A流向进程B；
- **点对点（point-to-point）**：即在单个发送方与单个接收方之间的连接；
- **三次握手（three-way handshake）**：客户先发送一个特殊TCP报文段，服务器用另一个特殊的TCP报文段来响应，最后客户再用第三个特殊报文段作为响应。

客户进程通过套接字，TCP会将要发送的数据引导到该连接的发送缓存（send buffer），发送缓存是发起三次握手期间设置的缓存之一，TCP发送缓存与接收缓存如下图所示：

<img src="image/image-20230213140246196.png" alt="image-20230213140246196" style="zoom:50%;" />

TCP可以从缓存中取出并放入报文段中数据数量限制于**最大报文段长度（Maximum Segment Size，MSS）**，MSS通常根据最初确认的由本地发送主机发送的最大链路层帧长度（即所谓的**最大传输单元（Maximum Transmission Unit，MTU）**）来设置。

### 3.5.2 TCP报文段结构

> TCP Segment Structure

TCP报文段结构如下图：

<img src="image/image-20230213141152970.png" alt="image-20230213141152970" style="zoom:50%;" />

与UDP一样，首部包括**源端口号（Source port）**和**目的端口号（Dest port）**，它被用于多路复用和多路分解来自或送到上层应用的数据，也包括**检验和字段（Internet checksum）**，首部还包括以下字段：

- 32bit的**序号字段（Sequence number）**和32bit的**确认号字段（Acknowledgement number）**；
- 16bit的**接受窗字段（Receive window）**：用于控制流量；
- 4bit的**首部长度字段（Header length）**：指示了以32bit的字为单位的TCP首部长度；
- 可选和变长的**选项字段（Options）**：用于发送方与接收方协商最大报文段长度（MSS）时，或在高速网络环境下用作窗口调节因子时使用；
- 6bit的**标志字段（flag field）**：
  - `ACK`：确认字段中的值是否有效；
  - `RST,SYN,FIN`：用于连接建立和拆除；
  - `CWR,ECE`：在明确拥塞通过中使用；
  - `PSH`：被置位时，指示接收方立即将数据交给上层；
  - `URG`：指示报文段中存放着被发送端上层实体置为“紧急”的数据；
- 16bit的**紧急数据指针字段（Urgent data point）**：指出紧急数据的最后一个字节；

#### 序号和确认号

> Sequence number and Acknowledgement number

一个**报文段的序号（sequence number for a segment）**是该报文段首字节的字节流编号，而不是建立在传送的报文段的序列之上。

TCP是全双工的，因此主机A在向主机B发送数据的同时，也许也接收来自主机B的数据。从主机B到达的每个报文段都有一个序号用于从B流向A的数据。==**<u>主机A填充进报文段的确认号是主机A期望从主机B收到的下一字节的序号</u>**==。如下图所示：

<img src="image/image-20230213145744785.png" alt="image-20230213145744785" style="zoom:50%;" />

### 3.5.3 往返时间的估计与超时

> Round-Trip Time Estimation and Timeout

#### 估计往返时间

报文段的样本RTT（表示为`SampleRTT`），在任意时刻，仅为一个已发送的但目前尚未确认的报文段估计SampleRTT，从而产生一个接近每个RTT的新SampleRTT值。另外，TCP绝不为已重传的报文段计算SampleRTT；它仅为传输一次的报文段测量。

TCP维持一个SampleRTT均值（称为`EstimatedRTT`），EstimatedRTT更新方式如下：
$$
EstimatedRTT=(1-\alpha)\times EstimatedRTT+\alpha\times SampleRTT\\
\alpha\text{推荐值为}\alpha=0.125
$$
这种平均方法被称为指**数加权移动平均（Exponential Weighted Moving Average，EWMA）**。

定义RTT偏差`DevRTT`，用于估算SampleRTT一般会偏离EstimatedRTT的程度：
$$
DevRTT=(1-\beta)\times DevRTT+\beta\times |SampleRTT-EstimatedRTT|\\
\beta\text{推荐值}\beta=0.25
$$

#### 设置和管理重传超时间隔

超时间隔应该大于等于EstimatedRTT，否则，将造成不必要的重传。在TCP确定重传超时间隔的设置方法如下：
$$
TimeoutInterval = EstimatedRTT + 4\times DevRTT
$$

### 3.5.4 可靠数据传输

> Reliable Data Transfer

TCP在IP不可靠的尽力而为服务之上创建了一种**可靠数据传输服务（reliable data transfer service）**。为了减少多个定时器管理带来的相当大的开销，TCP在即使有多个已发送但未确认的报文段的情况下，定时器的管理过程仅使用==**单一**==的定时器。简化的TCP发送方如下：

```c
/* Assume sender is not constrained by TCP flow or congestion control, that data from above is less than MSS in size, and that data transfer is in one direction only. */

NextSeqNum=InitialSeqNumber
SendBase=InitialSeqNumber
    
loop (forever) {
    switch(event)
        
        event: data received from application above
            create TCP segment with sequence number NextSeqNum
            if (timer currently not running)
            	start timer
            pass segment to IP
            NextSeqNum=NextSeqNum+length(data)
            break;
    
        event: timer timeout
            retransmit not-yet-acknowledged segment with smallest sequence number
            start timer
            break;
    
        event: ACK received, with ACK field value of y
            if (y > SendBase) {
                SendBase=y
                if (there are currently any not-yet-acknowledged segments)
                	start timer
            }
    		break;
} /* end of loop forever */
```

#### 超时间隔加倍

当发生超时事件时，TCP重传时都会将下一次的超时间隔设为先前值的两倍；当发生其他两个事件计时器重启时，`TimeoutInterval`由最近的`EstimatedRTT`值与`DevRTT`值推算得到。这种修改提供了一个形式受限的拥塞控制。

#### 快速重传

当比期望序号大的失序报文段到达，接收方立即发送**冗余ACK（duplicate ACK）**，指示下一个期待字节的序号（其为间隔的低端的序号）。如果TCP发送方接收到对相同数据的3个冗余ACK，TCP就执行**快速重传（fast retransmit）**，即在该报文段的定时器过期之前重传丢失的报文段。快速重传示例如下图：

<img src="image/image-20230213224124025.png" alt="image-20230213224124025" style="zoom:50%;" />

#### 选择确认

> selective acknowledgment

允许TCP接收方有选择地确认失序报文段，而不是积累地确认最后一个正确接收的有序报文。当该机制与选择重传机制结合起来使用时，TCP看起来就很像通常的SR协议。因此，TCP的差错恢复机制最好分类为GBN协议与SR协议的混合体。

### 3.5.5 流量控制

> Flow Control

TCP通过让发送方维护一个称为**接收窗口（receive window）**的变量来提供流量控制。通俗的说，接收窗口用于给发送方一个指示——该接收方还有多少可用的缓存空间。因为TCP是全双工通信，在连接两端的发送方都各自维护一个接收窗口。

接收窗口（`rwnd`）和接收缓存（`RcvBuffer`）如下图所示：

<img src="image/image-20230214112628843.png" alt="image-20230214112628843" style="zoom:50%;" />

### 3.5.6 TCP连接管理

> TCP Connection Management

#### 三次握手连接

> three-way handshake

TCP三次握手：报文交换交换过程如下：

<img src="image/image-20230214113356520.png" alt="image-20230214113356520" style="zoom:50%;" />

#### 四次握手关闭

> four-way handshake

由客户端关闭一条TCP连接的四次握手过程如下：

<img src="image/image-20230214113550739.png" alt="image-20230214113550739" style="zoom:50%;" />

#### TCP状态

> TCP State

客户TCP经历的典型的TCP状态序列如下图：

<img src="image/image-20230214114116272.png" alt="image-20230214114116272" style="zoom:50%;" />

- `ESTABLISHED`状态：已建立连接状态，TCP客户能发送和接收包含有效载荷数据；

服务器端TCP经历的典型的TCP状态序列如下图：

<img src="image/image-20230214114437801.png" alt="image-20230214114437801" style="zoom:50%;" />

## 3.6 拥塞控制原理

> Principles of Congestion Control

### 3.6.1 拥塞原因与代价

> The Causes and the Costs of Congestion

- 当分组的到达速率接近链路容量时，分组经历巨大的排队时延；
- 发送方在遇到大时延时所进行的不必要重传会引起路由器利用其链路带宽来转发不必要的分组副本；
- 当一个分组沿一条路径被丢弃时，每个上游路由器用于转发该分组到丢弃该分组而使用的传输容量最终被浪费掉；



### 3.6.2 拥塞控制方法

> Approaches to Congestion Control

- **端到端拥塞控制（End-to-end congestion control）**：在端到端拥塞控制方法中，网络层没有为运输层拥塞控制显示支持。即使网络中存在拥塞，端系统也必须通过对网络行为的观察来判断；
- **网络辅助的拥塞控制（Network-assisted congestion control）**：网络辅助的拥塞控制中，路由器向发送方提供关于网络中拥塞状态的显示反馈信息；



## 3.7 TCP拥塞控制

> TCP Congestion Control

运行在发送方的TCP拥塞控制机制跟踪一个额外的变量，即**拥塞窗口（congestion window）**。拥塞窗口表示为`cwnd`，它对一个TCP发送方能向网络中发送流量的速率进行限制。发送方中未被确认的数据量不会超过`cwnd`和`rwnd`中的最小值，即：
$$
LastByteSent-LastByteACK\le \min\{cwnd,rwnd\}
$$
因为TCP使用确认来触发（或计时）增大它的拥塞窗口长度，TCP被说成是自计时（self-clocking）的。TCP基于本地信息设置它们的发送速率的指导性原则：

- 一个丢失的报文段表意味着拥塞，因此当丢失报文段时应当降低TCP发送方的速率；
- 一个确认报文段指示该网络正在向接收方交付发送方的报文段，因此，当对先前未确认报文段的确认到达时，能够增加发送方的速率；
- 带宽探测；

### 3.7.1 TCP拥塞控制算法

> TCP congestion control algorithm

该算法主要包括3个部分：**慢启动（slow-start）**、**拥塞避免（congestion avoidance）**、**快速恢复（fast recovery）**。

在慢启动（slow-start）状态，`cwnd`的值以1个MSS开始并且每当传输的报文段首次被确认就增加1个MSS。TCP发送速率起始很慢，但在慢启动阶段以指数增长，TCP慢启动如下图所示：

<img src="image/image-20230214150155279.png" alt="image-20230214150155279" style="zoom:50%;" />

- 如果存在一个由超时指示的丢包事件，TCP发送方将`cwnd`设置为1并重新开始慢启动过程。它还将第二状态变量`ssthresh`（慢启动阈值）设置为`cwnd/2`，三个状态变换FSM如下图：

<img src="image/image-20230214151326294.png" alt="image-20230214151326294" style="zoom:50%;" />

一旦进入拥塞避免状态，`cwnd`的值大约是上次遇到拥塞时的值的一半，即距离拥塞可能并不遥远；在任意状态冗余ACK达到3个就会进入快速恢复状态的缺失报文段。

因此，TCP拥塞控制常常被称为**加性增、乘性减（Additive-Increase，Multiplicative-Decrease，AIMD）**拥塞控制方式。

#### 对TCP吞吐量的宏观描述

$$
\text{一条连接的平均吞吐量}=\frac{0.75\times W}{RRT}
$$



