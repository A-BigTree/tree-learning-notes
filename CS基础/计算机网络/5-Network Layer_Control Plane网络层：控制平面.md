# 5 Network Layer:Control Plane网络层：控制平面



## 5.1 概述

> Introduction

- **每个路由器控制（Per-router control）**：每台路由器有一个路由选择组件，用于与其他路由器中的路由选择组件通信，以计算其转发表的值。如下图所示：

<img src="image/image-20230216150036525.png" alt="image-20230216150036525" style="zoom:50%;" />

- **逻辑集中式控制（Logically centralized control）**：逻辑集中式控制器计算并分发转发表以供每台路由器使用，如下图所示：

<img src="image/image-20230216150303115.png" alt="image-20230216150303115" style="zoom:50%;" />

## 5.2 路由选择算法

> Routing Algorithms

- **集中式路由选择算法（centralized routing algorithm）**：用完整、全局性的网络知识计算出从源到目的地之间的最低开销路径。具有全局状态信息的算法常被称作**链路状态（Link State，LS）算法**，因为该算法必须知道网络中每条链路的开销。
- **分散式路由选择算法（decentralized routing algorithm）**：路由器以迭代、分布式计算的方式计算出最低开销路径。没有节点拥有关于网络链路开销的完整信息。一个分散式路由选择算法为**距离向量（Distance-Vector，DV）算法**，每个节点维护到网络中所有其他节点的开销估计的向量。

### 5.2.1 链路状态路由选择算法

> Link State Routing Algorithms

在实践中，这经常由链路状态广播（link state broadcast）算法完成。下面给出的链路状态路由选择算法叫做`Dijkstra`算法，其计算从某节点（源节点`u`）到网络中所有其他节点的最低开销路径。

基本思想：经算法的第k次迭代后，可知道到k个目的节点的最低开销路径，在到所有目的节点的最低开销路径之中，这k条路径具有k个最低开销。

> After the kth iteration of the algorithm, the least-cost paths are known to k destination nodes, and among the least-cost paths to all destination  nodes, these k paths will have the k smallest costs.

我们定义如下符号：

- `D(v)`：到算法的本次迭代，从源节点到目的节点v的最低开销；
- `p(v)`：从源到v沿着当前最小开销路径的前一个节点（v的邻居）；
- `N'`：节点子集；如果从源到v的最低开销路径已经确定，v在`N'`中；

源节点u的链路状态（LS）算法如下：

<img src="image/image-20230216161651349.png" alt="image-20230216161651349" style="zoom:50%;" />

对于下图的网络，链路状态算法迭代运行如下：

<img src="image/image-20230216161927410.png" alt="image-20230216161927410" style="zoom:50%;" />

<img src="image/image-20230216161949640.png" alt="image-20230216161949640" style="zoom:50%;" />

路线上的流量变化和拥塞会使LS算法产生**路由震荡（Routing Oscillations）**。

### 5.2.2 距离向量路由选择算法

> The Distance-Vector（DV）Routing Algorithm

**距离向量（Distance-Vector，DV）**算法是一种**迭代的（iterative）、异步的（asynchronous）、分布式的（distributed）和自我终止的（self-termination）**。

令$d_x(y)$是从节点x到节点y的最低开销路径。则该最低开销与著名的Bellman-Ford方程相关，即：
$$
d_x(y)=\min_v\{c(x,v)+d_v(y)\}
$$
方程中的$\min_v$是对于x的所有邻居的。

算法基本思想：每个节点x以$D_x(y)$开始，对在N中的所有节点y，估计从x到y的最低开销路径的开销路径。

> Each node x begins with $D_x(y)$, an estimate of the cost of the least-cost path from itself to node y, for all nodes, y, in N. 

距离向量算法具体过程如下：

<img src="image/image-20230216172437150.png" alt="image-20230216172437150" style="zoom: 50%;" />

DV算法具体实例如下：

<img src="image/image-20230216172528939.png" alt="image-20230216172528939" style="zoom:50%;" />

#### 距离向量算法：链路开销改变与链路故障

链路开销发生改变，如下图所示：

<img src="image/image-20230216175843412.png" alt="image-20230216175843412" style="zoom:50%;" />

这时我们会遇到**路由选择环路（routing loop）**，这样的问题也被称为**无穷计数（count-to-infinity）**问题。

#### 距离向量算法：增加毒性逆转

**毒性逆转（poisoned reverse）**思想：如果z通过y路由选择目的地x，则z将通告y，它到x的距离是无穷大，也就是z将通告$D_z(x)=\infin$。

但毒性逆转并不能解决一般的无穷计数问题。

## 5.3 因特网中自治系统内部的路由选择：OSPF

> Intra-AS Routing in the Internet: OSPF

随着路由器规模增大和管理自治的要求，可以通过将路由器组织进**自治系统（Autonomous System，AS）**来解决。在一个自治系统内运行的路由算法叫做**自治系统内部路由选择协议（intra-autonomous system routing protocol）**。

### 开放最短路优先（Open Shortest Path First，OSPF）

OSPF是一种链路状态协议，它使用洪泛链路状态信息和Dijkstra最低开销路径算法。使用OSPF，一台路由器构建了一幅关于整个自治系统的完整拓扑图。于是，每台路由器在本地运行Dijkstra的最短路径算法，以确定一个自身为根节点到所有子网的最短路径树。

使用OSPF时，路由器向自治系统内所有其他路由器广播路由选择信息，而不仅仅是向其相邻路由器广播。每当一条链路的状态发生变化时，路由器就会广播链路状态信息。

OFPF的优点：

- 安全（Security）：能够鉴别OSPF路由器之间的交换；
- 多条相同开销的路径（Multiple same-cost paths）：允许使用多条路径；
- 对单播与多播路由选择的综合支持（Integrated support for unicast and multicast routing）；
- 支持在单个AS中的层次结构（Support for hierarchy within a single AS）；

## 5.4 ISP之间的路由选择：BGP

> Routing Among the ISPs:BGP

当分组跨越多个AS进行路由时，我们需要一个**自治系统间路由协议（inter-autonomous system routing protocol）**。在因特网中，所有的AS运行相同的AS间路由选择协议，称为**边界网关协议（Broder Gateway Protocol，BGP）**。

### 5.4.1 BGP的作用

> The Role of BGP

在BGP中，分组并不是路由到一个特定的目的地址，相反是路由到CIDR化的前缀，其中每个前缀表示一个子网或者一个子网集合。

> In BGP, packets are not routed to a specific destination address, but instead to CIDRized prefixes, with each prefix representing a subnet or a collection of subnets.

BGP为每台服务器提供完成以下任务的手段：

- 从邻居AS获得前缀的可达性信息；

> Obtain prefix reachability information from neighboring ASs.

- 确定到该前缀的“最好”的路由；


> Determine the “best” routes to the prefixes.

### 5.4.2 通告BGP路由信息

> Advertising BGP Route Information

对于每个AS，每台路由器要么是一台**网关路由器（getaway router）**，要么是一台**内部路由器（internal router）**。在BGP中，每对路由器通过使用`179`端口的半永久TCP连接交换路由选择信息。跨越两个AS的BGP连接称为**外部BGP（eBGP）**连接，而在相同AS中的两台路由器之间的BGP会话称为**内部BGP（iBGP）**连接。两种连接如下图所示：

<img src="image/image-20230216231327105.png" alt="image-20230216231327105" style="zoom:50%;" />

### 5.4.3 确定最好的路由

> Determining the Best Routes

通告前缀包括一些BGP属性（BGP attribute），前缀及其属性称为路由（route）。

#### 热土豆路由选择

> hot potato routing

热土豆路由选择的基本思想是：对于一个路由，尽可能快地将分组送出其AS，而不必担心其AS外部到目的地的余下部分的开销。

在路由器转发表中增加AS外部目的地的步骤：

<img src="image/image-20230217103850062.png" alt="image-20230217103850062" style="zoom:50%;" />

#### 路由选择算法

> Route-Selection Algorithm

如果到相同前缀有两条或多条路由，则顺序地调用下列消除规则知道余下一条路由：

1. 路由被指派一个本地偏好（local preference）值作为其属性值之一，具有最高本地偏好值的将被选择；
2. 从余下的路由中，将选择具有最短`AS-PATH`的路由；
3. 从余下的路由中，使用热土豆路由选择，即选择具有最靠近`NEXT-HOP`路由器的路由；
4. 如果仍留下多条路由，该路由器使用BGP标识符来选择路由；

### 5.4.4 IP任播

> IP-Anycast

IP任播的动机：

1. 在许多分散的不同地理位置，替换不同服务器上的相同内容；
2. 让每个用户从最靠近的服务器访问内容；

使用IP任播将用户引向最近的CDN服务器如下图：

<img src="image/image-20230218222118235.png" alt="image-20230218222118235" style="zoom:50%;" />

### 5.4.5 路由选择策略

> Routing Policy

如下图所示，X是一个**多宿主接入ISP（multi-homed stub network）**，因为它是经由两个不同的提供商连到网络的其余部分。

<img src="image/image-20230218222545315.png" alt="image-20230218222545315" style="zoom:50%;" />

ISP遵循的法则：任何穿越某ISP主干网的流量必须是其源或者目的位于该ISP的某个客户网络中；不然的话这些流量将会免费搭车通过该ISP的网络。

#### 区分AS间和AS内部路由选择的原因

- 策略（Policy）；
- 规模（Scale）；
- 性能（Performance）；



## 5.5 SDN控制平面

> The SDN Control Plane

SDN体系结构具有四个关键特征：

- 基于流的转发（Flow-based forwarding）；
- 数据平面与控制平面分离（Separation of data plane and control plane）；
- 网络控制功能（Network control functions）；
- 可编程网络（A programmable network）；

SDN体系结构如下图所示：

<img src="image/image-20230218235117690.png" alt="image-20230218235117690" style="zoom:50%;" />

## 5.6 ICMP：因特网控制报文协议

> ICMP: The Internet Control Message Protocol

**因特网控制报文协议（the Internet Control Message Protocol，ICMP）**，被主机和路由器用来彼此沟通网络层的信息。ICMP最典型的用途是差错报告。

ICMP报文类型如下图所示：

<img src="image/image-20230219000618602.png" alt="image-20230219000618602" style="zoom:50%;" />

## 5.7 网络管理和SNMP

> Network Management and SNMP

网络管理包括了硬件、软件和人类元素的设置、综合和协调，以监视、测试、轮询、配置、分析、评价和控制网络及网元资源，用合理的成本满足实时性、运营性能和服务质量的要求。

> Network management includes the deployment, integration, and coordination of the hardware, software, and human elements to monitor, test, poll, configure, analyze, evaluate, and control the network and element resources to meet the real-time, operational performance, and Quality of Service requirements at a reasonable cost.  

### 5.7.1 网络管理框架

> The Network Management Framework

网络管理的关键组件如下图所示：

<img src="image/image-20230220224109346.png" alt="image-20230220224109346" style="zoom:50%;" />

- **管理服务器（managing server）**：一个应用程序，通常有人参与，并运行在网络运营中心（NOC）的集中式网络管理工作站上，执行网络管理活动的地方，控制网络管理信息的收集、处理、分析和显示；
- **被管设备（managed device）**：被管对象（managed object）是被管设备中硬件的实际部分和用于这些硬件及软件组件的配置参数；
- **信息管理库（Management Information Base，MIB）**：收集被管设备中每个被管对象的关联信息，信息值可供管理服务器所用；
- **网路管理代理（network management agent）**：运行在被管设备中的一个进程，该进程与管理服务器通信，在管理服务器的命令和控制下在被管设备中采取本地动作；
- **网络管理协议（network management protocol）**：运行在管理服务器和被管设备之间，允许管理服务器查询被管设备的状态，并经过其代理间接地在这些设备上采取行动；

### 5.7.2 简单网络管理协议

> Simple Network Management Protocol

**简单网络管理协议（Simple Network Management Protocol，SNMP）**是一个应用层协议，用于在管理服务器和代表管理服务器执行的代理之间传递网络管理控制和信息报文。

SNMPv2定义了7种类型报文，这些报文一般称为协议数据单元（PDU），PDU格式如下图所示：

<img src="image/image-20230220225559590.png" alt="image-20230220225559590" style="zoom:50%;" />

