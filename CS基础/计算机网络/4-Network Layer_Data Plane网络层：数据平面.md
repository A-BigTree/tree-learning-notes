# 4 Network Layer:Data Plane网络层：数据平面

## 4.1网络层概述

> Overview of Network Layer

### 4.1.1 转发和路由选择：数据平面和控制平面

> Forwarding and Routing: The Data and Control Planes

- **转发（forwarding）——数据平面**：将分组从一个输入链路接口转移到适当的输出链路接口的路由器的本地动作（==主要利用硬件==）；
- **路由选择（routing）——控制平面**：确定分组从源到目的地所采取的端到端路径的网络范围处理过程（==主要利用软件==）；

每台网络路由器中有一个关键元素是它的转发表（forwarding table）。路由器检查到达分组首部的一个或多个字段值，进而使用这些首部值在其转发表中索引，通过这种方法来转发分组。

**传统方法**是由路由器器中的路由选择算法决定转发表中的值：

<img src="image/image-20230214221845963.png" alt="image-20230214221845963" style="zoom:50%;" />

**软件定义网络（Software-Defined Networking，SDN）方法**是从路由器物理上分离的另一种方法，远程控制器计算和分发转发表以供每台路由器所使用：

<img src="image/image-20230214222430812.png" alt="image-20230214222430812" style="zoom:50%;" />

### 4.1.2 网络服务模型

> Network Service Model

**网络服务模型（network service model）**定义了分组在发送与接收端系统之间的端到端运输特性。可能提供发服务：

- 确保交付；
- 具有时延上界的确保交付；
- 有序分组交付；
- 确保最小带宽；
- 安全性；

因特网的网络层提供了单一的服务，称为**尽力尽为服务（best-effort service）**。传送的分组既不能保证以它们发送顺序接收，也不能保证它们最终交付；既不能保证端到端时延，也不能保证最小的带宽。

某些分组交换机称为**链路层交换机（link-layer switch）**，基于链路层帧中的字段值做出转发决定，这些交换机因此被称为链路层（==第2层==）设备；其他分组交换机称为**路由器（router）**，基于网络层数据报中的首部字段值做出转发决定，路由器因此是网络层（==第3层==）设备。

## 4.2 路由器工作原理

> How Router Works

一台路由器主要由四个组件构成如下图所示：

<img src="image/image-20230214225140416.png" alt="image-20230214225140416" style="zoom:50%;" />

- 输入端口（input port）：
  - 在路由器中终结进入物理链路的物理层；
  - 与位于入链路远端的数据链路层交互；
  - 查找转发表决定路由器的输出端口；
  - 控制分组从输入端口转发到路由选择处理器；
- 输出端口（output port）：
  - 存储从交换结构接收的分组；
  - 执行必要的链路层和物理层功能在输出链路上传输这些分组；
- 交换结构（switch fabric）：
  - 将路由器的输入端口；连接到它的输出端口；
- 路由选择处理器（routing process）：
  - 执行控制平面功能；
  - 传统路由器中，执行路由选择协议，维护路由选择表与关联链路状态信息，并由该路由器计算转发表；
  - SDN路由器中，负责与远程控制器通信；
  - 执行网络管理功能；

### 4.2.1 输入端口处理和基于目的地转发

> Input Port Processing and Destination-Based Forwarding

输入端口处理过程如下图：

<img src="image/image-20230214231914740.png" alt="image-20230214231914740" style="zoom: 50%;" />

在32比特IP地址的情况下，假设路由器有4条链路，分组以如下方式转发到链路接口：

<img src="image/image-20230214232229492.png" alt="image-20230214232229492" style="zoom:50%;" />

路由器中使用分组目的地址的**前缀（prefix）**与该表中的表项进行匹配，如下图所示：

|           前缀匹配           | 链路接口 |
| :--------------------------: | :------: |
|  `11001000 00010111 00010`   |    0     |
| `11001000 00010111 00011000` |    1     |
|  `11001000 00010111 00011`   |    2     |
|             其他             |    3     |

当有多个匹配时，该路由器使用**最长前缀匹配规则（longest prefix matching rule）**，即在该表中寻找最长的匹配项，并向与最长的前缀匹配先关联的链路接口转发分组。

实践中经常使用**三态内容可寻址存储器（Tenary Content Address Memory，TCAM）**来查找。

### 4.2.2 交换

> Switching

交换可以用许多方式完成，如下图所示：

<img src="image/image-20230214233525361.png" alt="image-20230214233525361" style="zoom:50%;" />

- 经内存交换（Switching via memory）：
  - 在CPU直接控制下交换的传统计算机；
  - 数据包复制到系统存储器；
  - 速度受内存带宽限制（每个数据报2个总线交叉）；
- 经总线交换（Switching via a bus）：
  - 通过共享总线从输入端口存储器到输出端口存储器的数据报；
  - 总线竞争：受总线带宽限制的交换速度；
- 经连接网络交换（Switching via an interconnection network）：
  - Crossbar-Clos网络以及最初为连接多处理器中的处理器而开发的其他互连网络；
  - 多级交换机：nxn交换机由多级较小的交换机组成；
  - 利用并行性：入口时将数据报分段为固定长度单元；通过结构交换信元，在出口重新组装数据报；

### 4.2.3 输出端口处理

> Output Port Processing

输出端口处理如下图：

<img src="image/image-20230215000627933.png" alt="image-20230215000627933" style="zoom:50%;" />

### 4.2.4 何时出现排队

> Where Does Queuing Occur

在输入端口和输出端口都可以形成分组队列，随着这些队列的增长，路由器的缓存空间最终将会耗尽，并且当无内存可用于存储到达的分组时将会出现丢包（packet loss）。

#### 输入排队

线路前部（Head-Of-the-Line，HOL）阻塞：即在一个输入队列中排队的分组必须等待通过的交换结构发送（即使输出端口是空闲的），因为它被位于线路前部的另一个分组所阻塞。如下图所示：

<img src="image/image-20230215154559027.png" alt="image-20230215154559027" style="zoom:50%;" />

#### 输出排队

当数据报从结构到达的速度快于链路传输速率时，需要缓冲。输出端口排队如下图所示：

<img src="image/image-20230215155611584.png" alt="image-20230215155611584" style="zoom:50%;" />

### 4.2.5 分组调度

> Packet Scheduling

#### 先进先出（First-In-First-Out，FIFO）

运行中的FIFO队列如下图所示：

<img src="image/image-20230215160614023.png" alt="image-20230215160614023" style="zoom:50%;" />

#### 优先权排队（priority queuing）

假设分组1、3和4是高优先权，分组2和5是低优先权，在**非抢占式优先权排队（non-preemptive priority queuing）**中，运行如下图所示：

<img src="image/image-20230215160904675.png" alt="image-20230215160904675" style="zoom:50%;" />

#### 循环排队规则（round robin queuing discipline）

假设分组1，2和4属于第一类，分组3和5属于第二类，运行如下图所示：

<img src="image/image-20230215161146482.png" alt="image-20230215161146482" style="zoom:50%;" />

## 4.3 网际协议：IPv4、寻址、IPv6及其他

> The Internet Protocol (IP): IPv4,  Addressing, IPv6, and More

### 4.3.1 IPv4数据报格式

> IPv4 Datagram Format

IPv4数据报格式如下图所示：

<img src="image/image-20230215164825113.png" alt="image-20230215164825113" style="zoom:50%;" />

- 版本号（Version）：规定了数据报的IP协议版本；
- 首部长度（Header length）：在无选项（Options）首部时，IP具有==**20字节**==的首部；
- 服务类型（Type of service）：不同类型的数据报可以相互区分；
- 数据报长度（Datagram length）：IP数据报的总长度（首部加上数据），以字节计数；
- 标识（Identifier）、标志（Flags）、片漂移（Fragmentation offset）：用于IP分片；
- 寿命（Time-to-live，TTL）：确保数据段不会永远在网络中循环；
- 上层协议（Upper-layer protocol）：指示IP数据报应交付给哪个运输层协议；
- 首部检验和（Header checksum）：帮助路由器检测收到的IP数据报中的比特错误；
- 源和目的IP地址（Source and Destination  IP address）；
- 选项（Options）：允许IP首部被扩展；
- 数据（Data）：一般为运输层报文段；

### 4.3.2 IPv4数据报分片

> IP fragmentation

一个链路层帧能承载的最大数据量叫作最大传送单元（Maximum Transmission Unit，MTU）。



### 4.3.3 IPv4编址

> IPv4 Adressing

主机与物理链路之间的边界叫做**接口（interface）**。每个IP地址长度为32比特（4字节），因此总共有$2^{32}$个可能的IP地址。这些地址通常按所谓**点分十进制记法（dotted-decimal notation）书**写，及地址中的每个字节用它的十进制形式书写，各字节以句点隔开。

地址`192.32.216.9`的二进制记法是：`11000001 00100000 11011000 00001001`。

下图中，一台路由器（具有三个接口）用于互联7台主机。

<img src="image/image-20230215223524888.png" alt="image-20230215223524888" style="zoom:50%;" />

图中左上侧的3个主机和它们连接的路由器接口，都有一个形如`233.1.1.xxx`的IP地址。用IP的术语说，互联这3个主机接口与1个互联网接口的网络形成一个**子网（subnet）**。IP编址为这个子网分配一个地址`233.1.1.0/24`，其中`/24`记法，有时称为**子网掩码（network mask）**，指示32比特中的最左侧24比特定义了子网地址。

下图是由3个路由器互联的6个子网：

<img src="image/image-20230215224605590.png" alt="image-20230215224605590" style="zoom:50%;" />

因特网的地址分配策略被称为**无类别域间路由选择（Classless Interdomain Routing，CIDR）**。当使用子网寻址时，32比特的IP地址被划分为两部分，并且也具有点分十进制形式`a.b.c.d/x`，其中`x`指示了地址的第一部分中的比特数，并且该部分经常被称为该地址的**前缀（prefix）**（或网络前缀）。

- IP广播地址`255.255.255.255`：当主机发出以广播地址为目的地址的数据报时，该报文会交付给同一个网络中的所有主机。

#### 获取主机地址

主机地址的配置更多的是使用**动态主机配置协议（Dynamic Host Configuration Protocol，DHCP）**。

- 给定主机每次与网络连接时能得到一个相同的IP地址；
- 给某个主机分配一个临时的IP地址（temporary IP address），每次地址可能不同；
- 允许移动设备频繁的加入和离开网络；

DHCP客户和服务器分布如下图：

<img src="image/image-20230215231652261.png" alt="image-20230215231652261" style="zoom:50%;" />

DHCP协议是一个4个步骤过程，`yiaddr`指示分配给该新到达用户的地址，如下图所示：

<img src="image/image-20230215231917425.png" alt="image-20230215231917425" style="zoom: 50%;" />

### 4.3.5 网络地址转换、

> Network Address Translation (NAT) 

**网络地址转换（Network Address Translation，NAT）**：就外界而言，本地网络中的所有设备只共享一个IPv4地址。

NAT具体步骤如下图：

<img src="image/image-20230215233757896.png" alt="image-20230215233757896" style="zoom:50%;" />

### 4.3.6 IPv6

> IPv6

#### 产生动机

- 将完全分配32位IPv4地址空间;
- 速度处理/转发：40字节固定长度首部；
- 启用流标签的不同网络层处理；

#### 报文格式

<img src="image/image-20230215234715846.png" alt="image-20230215234715846" style="zoom:50%;" />

#### 与IPv4区别

- 删掉分片/重新组装；
- 取消首部检验和；
- 将变长选项字段由下一个首部指出；

#### IPv4到IPv6的过渡

**建隧道（tunneling）**如下图：

<img src="image/image-20230215235546792.png" alt="image-20230215235546792" style="zoom:50%;" />



