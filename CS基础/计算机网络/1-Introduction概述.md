# 1 Introduction概述



## 1.1 Why computer network?



### 1.1.1 构建计算机网络的目的

> Purpose of constructing computer networks

- 促进信息交流；

> Facilitate information exchange



### 1.1.2 计算机网络的功能

> Functionality of computer networks

- 信息资源分享；
- 改变生活方式；

> - Information/resource sharing
> - Change our way of life



### 1.1.3 为什么要理解计算机网络

> Why should we understand computer networks

- 人们正处于信息时代；
- 计算机网络塑造我们的社会和我们自己；

> - The era of information
> - Computer networks shape our society and ourselves



## 1.2 计算机网络的结构组成

> The structure of computer networks



### 1.2.1 计算机网络概念

> Computer network structure

- 计算机网络，通常简称为网络，**是由==硬件组件和计算机==组成的集合，通过通信通道互连，从而实现资源和信息的共享**。如果一个设备中的至少一个进程能够向驻留在远程设备中的最少一个进程发送/接收数据，则称这两个设备位于网络中。

> A **computer network**, often simply referred to as a network, **is a collection of hardware components and computers interconnected by communication channels that allow sharing of resources and information**. Where at least one process in one device is able to send/receive data to/from at least one process residing in a remote device, then the two devices are said to be in a network. 



### 1.2.2 物理结构

> **Physical structure: a “nuts and bolts” view**



#### 结构组成

- Connected computing **devices**:
  - host(主机)=end systems(端系统)
  - running network apps at Internet's edge(在网络边缘运行应用程序)
- **Packet switches(分组交换机)**: forward packets(chunks of data)(转发数据包（数据块）)
  - routers(路由器)
  - switches(交换机)
- **Communication links(通信链路)**
  - fiber, copper, radio, satellite(光纤、铜缆、无线电、卫星)
  - transmission rate:bandwidth(传输速率：带宽)
- **Network(网络)**
  - collection of devices, routers, links: managed by an organization(设备、路由器、链接的集合：由组织管理)



#### Protocols(协议)

- 协议定义了网络实体之间发送和接收的消息的**格式**、**顺序**，以及在消息传输和接收上采取的**操作**；

> *Protocols* *define the* ***format***,***order*** *of* *messages sent and received* *among network entities, and* ***actions** taken* *on msg transmission, receipt* 



### 1.2.3 功能结构

> **Functional structure**



#### 结构组成

- **Network egde(网络边缘)**:
  - applications and hosts
  - Interface between end system and networks(端系统与网络间的接口)
- **Access networks(网络接入)**:
  - wired, wireless communication links(有线、无线通信链路)
  - Interface between network edge and network core(网络边缘和网络核心之间的接口)
- **Network core(网络核心)**:
  - Edge network(边缘网络)
  - Core/backbone network(核心/骨干网)



### 1.2.4 Functional structure-Network edge

- hosts: clients and servers
- servers often in data centers



### 1.2.5 Functional structure-Access networks

1. **Residential access nets(家庭接入网)**

   - cable-based access(基于电缆接入)

     - 频分复用（FDM）：在不同频带传输的不同信道

       > *frequency division multiplexing (FDM):* different channels transmitted in different frequency bands

     - HFC：混合光纤同轴电缆；不对称：高达40 Mbps–1.2 Gbs下行传输速率，30-100 Mbps上行传输速率

       > HFC: hybrid fiber coax; Asymmetric: up to 40 Mbps – 1.2 Gbs downstream transmission rate, 30-100 Mbps upstream transmission rate

     - 电缆、光纤网络将家庭连接到ISP路由器

       > network of cable, fiber attaches homes to ISP router

     - 电缆调制解调器接入网络

       > Cable modem access network

     - 电力线宽带接入网

       > Power line broadband access network

   - digital subscriber line (DSL)(数字用户线)

     - 使用现有电话线连接中央办公室DSLAM

       > use *existing* telephone line to central office DSLAM

2. **mobile access networks (WiFi, 4G/5G)(移动接入网络)**

   - Wireless local area networks(WLANs)无线局域网
     - typically within or around building (~100 ft)
     - 802.11b/g/n (WiFi): 11, 54, 450 Mbps transmission rate
   - Wide-area cellular access networks(广域蜂窝接入网)
     - provided by mobile, cellular network operator (10’s km)
     - 10’s Mbps 

3. **institutional access networks(机构介入网络)**

   - 混合使用有线、无线链路技术，连接混合的交换机和路由器；
     - 以太网：100Mbps、1Gbps、10Gbps有线接入；
     - WiFi：无线接入点速度为11、54、450 Mbps；

   > mix of wired, wireless link technologies, connecting a mix of switches and routers 
   >
   > - Ethernet: wired access at 100Mbps, 1Gbps, 10Gbps;
   > - WiFi: wireless access points at 11, 54, 450 Mbps; 



### 1.2.5 Functional structure-Network core

- 互连路由器网格
- 数据包交换：主机将应用层消息分成数据包
  - 将数据包从一个路由器转发到下一个路由器，在从源到目标的路径上通过链路；
  - 以全链路容量传输每个数据包；

> - Mesh of interconnected routers
> - Packet-switching: hosts break application-layer messages into *packets*
>   - forward packets from one router to the next, across links on path from source to destination;
>   - each packet transmitted at full link capacity;



### 1.2.6 Function structure-work flow



#### Host: sends packets of data

- 获取应用程序消息
- 分为更小的块，称为数据包，长度L位
- 以传输速率R将数据包传输到接入网络
  - 链路传输速率，即链路容量，即链路带宽

$$
T_{\text{packet transmission delay}}=\frac{L_{\text{bits}}}{R_{\text{bits/sec}}}
$$

> Host sending function:
>
> - Takes application message;
> - Breaks into smaller chunks, known as *packets*, of length *L* bits;
> - Transmits packet into access network at *transmission rate R*
>   - link transmission rate, aka link *capacity, aka link bandwidth*



#### Communication link: physical media

- 位(Bit)：在发射机/接收机对之间传播
- 物理链接(Physical link)：发射机和接收机之间的内容
- 导向介质(Guided media)：
  - 信号在固体介质中传播：铜、光纤、同轴电缆
- 非导向介质(Unguided media)：
  - 信号自由传播，例如无线电



***<u>导向介质：</u>***

1. 双绞线(Twisted pair)(TP)
   - 两条绝缘铜线
     - 5类：100 Mbps，1 Gbps以太网；
     - 6类：10Gbps以太网；
2. 同轴电缆(Coaxial cable)
   - 两根同心(concentric)铜导线
   - 双向(bidirectional)
   - 宽带(broadband)：
     - 电缆上的多个频率通道每通道100 Mbps
3. 光纤网线(Fiber optic cable)
   - 玻璃纤维携带光脉冲，每脉冲一位
   - 高速运转：
     - 高速点对点传输（10’s-100 Gbps）
   - 低错误率：
     - 中继器间隔很远
     - 对电磁噪音免疫



***<u>非导向介质：</u>***

- 电磁频谱中携带的信号

- 无物理“线”
- 广播和“半双工”（发送方到接收方）
- 传播环境影响：
  - 反射
  - 物体障碍物干扰
- 链接类型：
  - 地面微波(terrestrial)
    - 高达45 Mbps信道
  - 无线局域网（WiFi）(wireless LAN)
    - 高达100 Mbps
  - 广域（如蜂窝）4G蜂窝(wide-area)
    - ~10 Mbps
  - 卫星(stellite)
    - 高达45 Mps每信道
    - 270毫秒终端延迟
    - 地球同步与低地球轨道



#### Access networks

- 见1.2.4节



#### Network core

- **Routing(路由)：**
  - 全局操作：确定数据包采用的源目标路径
  - 路由算法
- **Forwarding(转发)：**
  - 本地操作：将到达的数据包从路由器的输入链路移动到适当的路由器输出链路



### 1.2.7 Packet switching分组交换



#### 存储转发

- 整个数据包必须到达路由器才能在下一链路上传输；

对于包(packet)数据长度为L，传输速率为R，通信链路条数为N，则：
$$
t_{\text{trans}}=N\frac{L}{R}
$$


#### 排队时延与分组丢失

如果链路的到达速率（bps）在一段时间内超过链路的传输速率（bps）：

- 数据包将排队等待在输出链路上传输;
- 如果路由器中的内存（缓冲区）已满，数据包可能会被丢弃（丢失）



### 1.2.8  Circuit switching线路交换



**分配给的终端资源，保留给源和目标之间的“调用”（缓存，链路传输速率）**

> End-end resources allocated to, reserved for “call” between source and destination

- 专用资源：无共享
  - 电路类（保证的）性能；
- 如果呼叫未使用，则电路段空闲（无共享）；
- 常用于传统电话网络；



#### 频分复用Frequency Division Multiplexing (FDM)

- 光、电磁频率被划分为（窄）频带；
- 每个呼叫分配自己的频带，可以以该窄带的最大速率传输；



#### 时分复用Time Division Multiplexing (TDM)

- 时间被划分为周期性时隙；
- 每个呼叫分配的周期时隙，可以以（更宽）频带的最大速率传输，但只能在其时隙内传输；



### 1.2.9 Circuit vs. Packet switching

- Circuit switching: PSTN( Public Switched Telephone Network )定义：公共交换电话网络，一种常用旧式电话系统。即我们日常生活中常用的电话网；
- Packet switching: Computer network

***动机不同：***

PSTN：稳定、恒定、稳定的流量；

计算机网络：突发流量；

数据包交换为计算机网络显示了更高的效率；同时允许更多的用户去使用网络；

***数据包交换的特点：***

- 非常适合“突发”数据——有时需要发送数据，但有时不需要；
- 可能出现过度拥塞：缓冲区溢出导致的数据包延迟和丢失：
  - 可靠数据传输、拥塞控制所需的协议；
- 传统上用于音频/视频应用的带宽保证来提供类电路行为；



### 1.2.10 Network core-network of networks

- 主机通过访问Internet服务提供商（ISP）；
  - 住宅，企业（公司、大学、商业）接入ISP；
- ISP必须相互连接；
  - 以便任何两台主机都可以相互发送数据包；
- 结果网络网络的演变非常复杂；
  - 这是由经济和国家政策驱动的；



### 1.2.11 服务结构

- 从源到目标的可靠数据传输（FTP、WWW）；
- “尽力而为(unreliable)”（不可靠）数据传输（Skype、Voice on IP）；



## 1.3 计算机网络性能评估

> To evaluate the performance of computer networks



- **服务品质Quality of service(QoS)**
  - Loss
  - Delay
  - Throughput
- **网络安全Network security**



路由器缓冲区中的数据包队列

- 数据包队列，等待输出；
- 到达率链路（暂时）超过输出链路容量：数据包丢失；



### 1.3.1 包延迟 Packet delay: four sources

$$
d_{\text{nodal}}=d_{\text{proc}}+d_{\text{queue}}+d_{\text{trans}}+d_{\text{prop}}
$$

$d_{proc}$: nodal processing

- 检查位错误；
- 确定输出链接；
- 通常<毫秒；



$d_{queue}$:queueing delay

- 输出链路等待传输的时间；
- 取决于路由器的拥塞级别；



