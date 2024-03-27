# 6 The Link Layer and LANs链路层和局域网

## 6.1 链路层概述

> Introduction to the Link Layer

- 节点（node）：运行链路层协议的任何人设备；
- 链路（link）：沿着通信路径连接相邻节点的通信信道；

### 6.1.1 链路层提供的服务

> The Service Provide by the Link Layer

- 成帧（Framing）；
- 链路接入（Link access）：**媒体访问控制（Medium Access Control，MAC）**协议规定了帧在链路上传输规则；
- 可靠交付（Reliable deliver）；
- 差错检测和纠正（Error detection and correction）；

### 6.1.2 链路在何处实现

> Where Is the Link Layer Implemented

链路层的主体部分在**网络适配器（network adapter）**中实现，网络适配器有时也称为**网络接口卡（Network Interface Card，NIC）**，一个典型的主机体系结构如下图所示：

<img src="image/image-20230221225810279.png" alt="image-20230221225810279" style="zoom:50%;" />

## 6.2 差错检测和纠正技术

> Error-Detection and -Correction Techniques

差错检测与纠正的场景如下图所示：

<img src="image/image-20230221230120447.png" alt="image-20230221230120447" style="zoom:50%;" />

### 6.2.1 奇偶校验

> Parity Checks

单个**奇偶校验位（parity bit）**：发送d比特信息附加一个比特使d+1比特中1的总数是偶数（偶校验）或奇数（奇校验），偶校验如下图所示：

<img src="image/image-20230221230512165.png" alt="image-20230221230512165" style="zoom:50%;" />

- 只能检测不能纠错，不能检测超过1比特位的错误；

**二维奇偶校验（two-dimension parity）**如下图所示：

<img src="image/image-20230221230735437.png" alt="image-20230221230735437" style="zoom:50%;" />

- 能检测并纠正1bit差错；

### 6.2.2 检验和方法

> Checksumming Methods

**因特网检验和（Internet checksum）**基于这种方法，即数据的字节作为16比特的整数对待并求和。

### 6.2.3 循环冗余检测

> Cyclic Redundancy Check(CRC)

计算机网络中广泛应用的差错检测技术基于**循环冗余检测（Cyclic Redundancy Check，CRC）**编码，也称为**多项式编码（polynomial code）**，CRC如下图所示：

<img src="image/image-20230221231303707.png" alt="image-20230221231303707" style="zoom:50%;" />

`R`计算：
$$
R=remainder\frac{D\cdot2^r}{G}
$$
<img src="image/image-20230221231624651.png" alt="image-20230221231624651" style="zoom:50%;" />

## 6.3 多路访问链路和协议

> Multiple Acess Links and Protocol

- 两种类型的网络链路：**点对点链路（point-to-point link）**，**广播链路（broadcast link）**；
- 传输的帧在接收方可能在接收方处碰撞（collide）；
- 3种类型多路访问协议：信道划分协议，随机接入协议，轮流协议；

### 6.3.1 信道划分协议

> Channel Partitioning Protocols

- **时分多路复用（time-division multiplexing，TDM）**；
- **频分多路复用（frequency-division multiplexing，FDM）**；
- **码分多址（Code Division Multiple Access，CDMA）**；

前两种传输速率：R\N bps

### 6.3.2 随机接入协议

> Random Access Protocols

#### 时隙ALOHA

在每个节点中，时隙ALOHA的操作如下：

- 当节点有一个新帧要发送时，它等到下一个间隙开始并在该时隙传输整个帧；
- 如果没有碰撞，该节点成功地传输它的帧，从而不需要考虑重传该帧；
- 如果有碰撞，该节点在时隙结束之前检测到这次碰撞，该节点以概率p在后续的每个时隙中重传它的帧，直到该帧被无碰撞的传输出去；

<img src="image/image-20230222000508480.png" alt="image-20230222000508480" style="zoom:50%;" />

#### ALOHA

在纯ALOHA中，当一帧首次到达，节点立刻将该帧完整地传输进广播信道；

#### 波载侦听多路访问（CSMA/CD）

> Carrier Sense Multiple Access with Collision Detection



