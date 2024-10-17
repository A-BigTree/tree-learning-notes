# G1中的Young GC、Mixed GC、Full GC和日志查看

[toc]

---

## 1 G1的GC模式

G1提供了两种GC模式，Young GC和Mixed GC，两种都是完全Stop The World的。 

- Young GC：**选定所有年轻代里的Region**。通过控制年轻代的region个数，即年轻代内存大小，来控制young GC的时间开销。 
- Mixed GC：选定所有年轻代里的Region，外加根据Global concurrent marking统计得出收集收益高的**若干**老年代Region。在用户指定的开销目标范围内尽可能选择收益高的老年代Region。

由上面的描述可知，**Mixed GC不是full GC**，它只能回收**==部分==**老年代的Region，如果mixed GC实在无法跟上程序分配内存的速度，导致老年代填满无法继续进行Mixed GC，就会**使用serial old GC（full GC）**来收集整个GC heap。所以我们可以知道，**G1是不提供full GC的**。

### 1.1 Young GC

Young GC垃圾回收过程中，分为以下几种情况：

1. 新创建对象（非巨大对象）分配到Eden区；
2. Eden区对象转移到幸存区；
3. 幸存区对象转移到另一幸存区；
4. 幸存区对象转移到老年代；

用几张图片解释Young GC流程：

- 这里有 eden，survivor，old 还有个 free region，黄色为存活对象

![image-20241017205459406](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/image-20241017205459406.png)

- G1会把橙色对象拷贝到free region，或转移到幸存或老年代

![image-20241017205633665](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/image-20241017205633665.png)

- 当拷贝完毕，free region 就会晋升为 survivor region，以前的 eden 就被释放了

![image-20241017205736969](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/image-20241017205736969.png)

### 1.2 Mixed GC

Mixed GC会选取**所有的** Young Region ➕ 收益高的**若干个** Old Region。让我们回顾一下G1回收器工作过程（其实就是Mixed GC的过程），即我们上面一直提到的Global concurrent marking，一共分为四个步骤：

1. 初始标记（initial mark，STW）：它标记了从GC Root开始直接可达的对象。 
2. 并发标记（Concurrent Marking）：这个阶段从GC Root开始对heap中的对象标记，标记线程与应用程序线程并行执行，并且收集各个Region的存活对象信息。 
3. 最终标记（Remark，STW）：标记那些在并发标记阶段发生变化的对象，将被回收。 
4. 清除垃圾（Cleanup）：清除空Region（没有存活对象的），加入到free list。

![g1-garbage-collector](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/g1-garbage-collector.png)

同时值得注意的是，**对于第一阶段的初始标记，==Mixed GC是共用了Young GC的SWT==**，这是因为他们可以复用root scan操作，所以可以说Global concurrent marking操作是伴随Young GC而发生的。

具体执行过程如下图所示：

![image-20241017210704874](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/image-20241017210704874.png)

![image-20241017210713858](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/image-20241017210713858.png)

![image-20241017210724882](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/image-20241017210724882.png)

同样的，被回收的 Region 就变回 Free Region 了，从上图可以看到 Mixed GC 只能回收部分的老年代。

> **G1 是如何选择要回收的 regions 的？**
>
> - `-XX:G1MaxNewSizePercent` 与 Young GC关联；
> - `-XX:MixedGCCountTarget` 与 Old GC关联；
>
> `-XX:MixedGCCountTarget`默认是8，意味着要在8次以内回收完所有的 Old Region
>
> - 换句话说，如果对立有 800 个 old region, 那么一次Mixed gGC最大会回收 100 个 Old Region

G1 当然也可以被调整成不做这么多工作，也就是少回收，一定程度上浪费堆内存，导致更多的堆空间被使用

- `-XX:G1MixedGCLiveThresholdPercent` (默认:85) 可能会提高堆使用率
- `-XX:G1HeapWastePercent` (默认:5) 如果可回收低于这个值, 那么将不会启动Mixed gc

### 1.3 Mixed GC发生时机

Young GC发生的时机大家都知道（文章开头已表明），那什么时候发生Mixed GC呢？其实是由一些参数控制着的，另外也控制着哪些老年代Region会被选入CSet。 

- `G1HeapWastePercent`：在Global concurrent marking结束之后，我们可以知道老年代regions中有多少空间要被回收，在每次YGC之后和再次发生Mixed GC之前，会检查垃圾占比是否达到此参数，只有达到了，下次才会发生Mixed GC；
- `G1MixedGCLiveThresholdPercent`：老年代Region中的存活对象的占比，只有在此参数之下，才会被选入CSet。 
- `G1MixedGCCountTarget`：一次Global Concurrent marking之后，最多执行Mixed GC的次数；
- `G1OldCSetRegionThresholdPercent`：一次Mixed GC中能被选入CSet的最多老年代 Region数量。



### 1.4 Full GC

由上面的描述可知，**Mixed GC不是Full GC**，它只能回收部分老年代的Region，如果mixed GC实在无法跟上程序分配内存的速度，导致老年代填满无法继续进行Mixed GC，就会使用serial old GC（Full GC）来收集**整个GC heap和方法区（元空间）**。所以我们可以知道，G1是**没有**Full GC的机制的，G1 GC是使用的Serial Old GC的代码（后面被优化为多线程，但是速度相对来说依然比较慢），**因此Full GC会暂停很久**，因此在生产环境中，一定注意Full GC。

> **`G1 Full GC`的原因一般有：**
>
> - `Mixed GC`赶不上内存分配的速度，只能通过`Full GC`来释放内存，这种情况解决方案后面再说
> - `MetaSpace`不足，对于大量使用反射，动态代理的类，由于动态代理的每个类都会生成一个新的类，同时`class`信息会存放在元空间，因此如果元空间不足，`G1`会靠`Full GC`来扩容元空间，这种情况解决方案就是扩大初始元空间大小。
> - `Humongous`分配失败，前面说过`G1`分配大对象时，回收是靠`Concurrent Marking`或`Full GC`，因此如果大对象分配失败，则可能会引发`Full GC`



## 2 G1回收日志

### 2.1 G1 GC周期

![image-20241017213742481](./4-G1%E4%B8%AD%E7%9A%84Young%20GC%E3%80%81Mixed%20GC%E3%80%81Full%20GC%E5%92%8C%E6%97%A5%E5%BF%97%E6%9F%A5%E7%9C%8B.assets/image-20241017213742481.png)

G1 有两个阶段，它会在这两个阶段往返，分别是 Young-only，Space Reclamation.

- Young-only阶段包含一系列逐渐填满老年代Regin的 GC
- Space Reclamation 阶段G1会递进地回收老年代Regin的空间，同时也处理新生代Region

上图来自 oracle 上对GC周期的描述，实心圆都表示一次 GC 停顿

- 蓝色：Young GC的停顿
- 黄色：标记过程的停顿
- 红色：Mixed GC停顿

在几次GC后，老年代Regin的对象占有比超过了 `InitiatingHeapOccupancyPercent`，GC就会进入并发标记准备(concurrent mark)。



### 2.2 G1日志开启与设置

在java程序运行时，加入运行参数来开启GC日志，不同的参数日志打印的精细程度也不同

- **`-verbosegc`** (等同于 **`-XX:+PrintGC`**)：打印细节（最不细节）GC日志

示例：

```shell
[GC pause (G1 Humongous Allocation) (young) (initial-mark) 24M- >21M(64M), 0.2349730 secs]
[GC pause (G1 Evacuation Pause) (mixed) 66M->21M(236M), 0.1625268 secs]  
```

- **`-XX:+PrintGCDetails`**：打印比较细节的CG日志
  - 显示每个阶段的平均时间、最小时间、和最大时间；
  - 根扫描、RSet 更新（带有已处理的缓冲区信息）、RSet 扫描、对象复制、终止（带有尝试次数）；
  - 还显示其他执行时间，例如选择 CSet、引用处理、引用排队和释放 CSet 所花费的时间；
  - 显示Eden区、幸存区和的整个堆的占用情况；

示例：

```shell
[Ext Root Scanning (ms): Avg: 1.7 Min: 0.0 Max: 3.7 Diff: 3.7]
[Eden: 818M(818M)->0B(714M) Survivors: 0B->104M Heap: 836M(4096M)->409M(4096M)]
```

- **`-XX:+UnlockExperimentalVMOptions -XX:G1LogLevel=finest`** ：打印最细节的CG日志
  - 在上面配置的基础上多了线程显示

示例：

```shell
[Ext Root Scanning (ms): 2.1 2.4 2.0 0.0
           Avg: 1.6 Min: 0.0 Max: 2.4 Diff: 2.3]
       [Update RS (ms):  0.4  0.2  0.4  0.0
           Avg: 0.2 Min: 0.0 Max: 0.4 Diff: 0.4]
           [Processed Buffers : 5 1 10 0
           Sum: 16, Avg: 4, Min: 0, Max: 10, Diff: 10]
```

还有其他参数可以控制打印日志时间和其他显示项目。

### 2.3 Young GC日志

```shell
# 开始Young GC
[gc,start     ] GC(78) Pause Young (Normal) (G1 Evacuation Pause)
[gc,task      ] GC(78) Using 10 workers of 10 for evacuation
[gc,phases    ] GC(78)   Pre Evacuate Collection Set: 3.2ms
[gc,phases    ] GC(78)   Evacuate Collection Set: 28.8ms
[gc,phases    ] GC(78)   Post Evacuate Collection Set: 1.8ms
[gc,phases    ] GC(78)   Other: 1.1ms
[gc,heap      ] GC(78) Eden regions: 538->0(871)
[gc,heap      ] GC(78) Survivor regions: 69->33(76)
# 老年代被分配
[gc,heap      ] GC(78) Old regions: 1041->1077
[gc,heap      ] GC(78) Humongous regions: 3->1
[gc,metaspace ] GC(78) Metaspace: 71777K->71777K(1114112K)
[gc           ] GC(78) Pause Young (Normal) (G1 Evacuation Pause) 3300M->2220M(6144M) 34.907ms
[gc,cpu       ] GC(78) User=0.24s Sys=0.05s Real=0.04s
```



### 2.4 Mixed GC

```shell
# Young GC(并发标记开始，1.初始标记)
[gc,start     ] GC(79) Pause Young (Concurrent Start) (G1 Humongous Allocation)
[gc,task      ] GC(79) Using 10 workers of 10 for evacuation
[gc,phases    ] GC(79)   Pre Evacuate Collection Set: 0.2ms
[gc,phases    ] GC(79)   Evacuate Collection Set: 22.3ms
[gc,phases    ] GC(79)   Post Evacuate Collection Set: 0.9ms
[gc,phases    ] GC(79)   Other: 1.8ms
[gc,heap      ] GC(79) Eden regions: 569->0(656)
[gc,heap      ] GC(79) Survivor regions: 33->55(113)
[gc,heap      ] GC(79) Old regions: 1077->1077
[gc,heap      ] GC(79) Humongous regions: 1->1
[gc,metaspace ] GC(79) Metaspace: 71780K->71780K(1114112K)
[gc           ] GC(79) Pause Young (Concurrent Start) (G1 Humongous Allocation) 3357M->2264M(6144M) 25.305ms
[gc,cpu       ] GC(79) User=0.21s Sys=0.00s Real=0.03s
# 2.开始并发标记
[gc           ] GC(80) Concurrent Cycle
[gc,marking   ] GC(80) Concurrent Clear Claimed Marks
[gc,marking   ] GC(80) Concurrent Clear Claimed Marks 0.147ms
[gc,marking   ] GC(80) Concurrent Scan Root Regions
[gc,marking   ] GC(80) Concurrent Scan Root Regions 16.125ms
[gc,marking   ] GC(80) Concurrent Mark (373.358s)
[gc,marking   ] GC(80) Concurrent Mark From Roots
[gc,task      ] GC(80) Using 4 workers of 4 for marking
[gc,marking   ] GC(80) Concurrent Mark From Roots 57.029ms
[gc,marking   ] GC(80) Concurrent Preclean
[gc,marking   ] GC(80) Concurrent Preclean 0.454ms
[gc,marking   ] GC(80) Concurrent Mark (373.358s, 373.415s) 57.548ms
# 3.SWT 最终标记阶段
[gc,start     ] GC(80) Pause Remark
[gc,stringtable] GC(80) Cleaned string and symbol table, strings: 36361 processed, 315 removed, symbols: 192117 processed, 500 removed
[gc            ] GC(80) Pause Remark 2326M->956M(6144M) 14.454ms
[gc,cpu        ] GC(80) User=0.08s Sys=0.03s Real=0.02s
# 并发构建RSet
[gc,marking    ] GC(80) Concurrent Rebuild Remembered Sets
[gc,marking    ] GC(80) Concurrent Rebuild Remembered Sets 38.843ms
# 4.清楚整理
[gc,start      ] GC(80) Pause Cleanup
[gc            ] GC(80) Pause Cleanup 974M->974M(6144M) 0.660ms
[gc,cpu        ] GC(80) User=0.00s Sys=0.00s Real=0.00s
[gc,marking    ] GC(80) Concurrent Cleanup for Next Mark
[gc,marking    ] GC(80) Concurrent Cleanup for Next Mark 16.673ms
[gc            ] GC(80) Concurrent Cycle 146.748ms
# Mixed GC
[gc,start      ] GC(81) Pause Young (Mixed) (G1 Evacuation Pause)
[gc,task       ] GC(81) Using 10 workers of 10 for evacuation
[gc,mmu        ] GC(81) MMU target violated: 61.0ms (60.0ms/61.0ms)
[gc,phases     ] GC(81)   Pre Evacuate Collection Set: 0.1ms
[gc,phases     ] GC(81)   Evacuate Collection Set: 76.8ms
[gc,phases     ] GC(81)   Post Evacuate Collection Set: 0.9ms
[gc,phases     ] GC(81)   Other: 1.1ms
[gc,heap       ] GC(81) Eden regions: 211->0(136)
[gc,heap       ] GC(81) Survivor regions: 55->17(34)
# Mixed GC 老年代被回收
[gc,heap       ] GC(81) Old regions: 392->312
[gc,heap       ] GC(81) Humongous regions: 3->1
[gc,metaspace  ] GC(81) Metaspace: 71780K->71780K(1114112K)
[gc            ] GC(81) Pause Young (Mixed) (G1 Evacuation Pause) 1320M->919M(6144M) 78.857ms
[gc,cpu        ] GC(81) User=0.41s Sys=0.37s Real=0.08s
```

1. GC（79）：因为（G1 Humongous Allocation）巨大对象分配开始Young GC同时并发标记开始；
2. GC（80）：GC（79）已完成第一阶段的初始标记，开始并发标记、最终标记、清楚整理；
3. GC（81）：GC（80）已获取新生代和老年代标记信息，开始进行Mixed GC，**新生代和老年代Regin同时被回收整理**；



### 2.5 Full GC

下面的日志正好是博主在工作时排查Full GC问题时真实遇到到，生产环境日志比较复杂，这里稍微删减标注了供大家参考～

```shell
# 1.Mixed GC尝试分配巨大对象
[gc,start      ] GC(1362) Pause Young (Mixed) (G1 Humongous Allocation)
[gc,task       ] GC(1362) Using 8 workers of 8 for evacuation
[gc,phases     ] GC(1362)   Pre Evacuate Collection Set: 0.3ms
[gc,phases     ] GC(1362)   Evacuate Collection Set: 23.9ms
[gc,phases     ] GC(1362)   Post Evacuate Collection Set: 19.6ms
[gc,phases     ] GC(1362)   Other: 4.3ms
[gc,heap       ] GC(1362) Eden regions: 3->0(755)
[gc,heap       ] GC(1362) Survivor regions: 2->2(74)
[gc,heap       ] GC(1362) Old regions: 1617->1443
[gc,heap       ] GC(1362) Humongous regions: 144->133
[gc,metaspace  ] GC(1362) Metaspace: 419538K->419538K(1300480K)
[gc            ] GC(1362) Pause Young (Mixed) (G1 Humongous Allocation) 7054M->6305M(9832M) 47.441ms
[gc,cpu        ] GC(1362) User=0.27s Sys=0.01s Real=0.05s
# 2.Young GC（并发标记）
[gc,start      ] GC(1363) Pause Young (Concurrent Start) (G1 Humongous Allocation)
[gc,task       ] GC(1363) Using 8 workers of 8 for evacuation
[gc,phases     ] GC(1363)   Pre Evacuate Collection Set: 0.6ms
[gc,phases     ] GC(1363)   Evacuate Collection Set: 21.2ms
[gc,phases     ] GC(1363)   Post Evacuate Collection Set: 19.5ms
[gc,phases     ] GC(1363)   Other: 3.1ms
[gc,heap       ] GC(1363) Eden regions: 5->0(735)
[gc,heap       ] GC(1363) Survivor regions: 2->2(95)
[gc,heap       ] GC(1363) Old regions: 1443->1443
[gc,heap       ] GC(1363) Humongous regions: 144->138
[gc,metaspace  ] GC(1363) Metaspace: 419538K->419538K(1300480K)
[gc            ] GC(1363) Pause Young (Concurrent Start) (G1 Humongous Allocation) 6365M->6325M(9832M) 43.719ms
[gc,cpu        ] GC(1363) User=0.25s Sys=0.00s Real=0.05s
# 3.开始并发标记
[gc            ] GC(1364) Concurrent Cycle
[gc,marking    ] GC(1364) Concurrent Clear Claimed Marks
[gc,marking    ] GC(1364) Concurrent Clear Claimed Marks 0.509ms
[gc,marking    ] GC(1364) Concurrent Scan Root Regions
[gc,marking    ] GC(1364) Concurrent Scan Root Regions 5.111ms
[gc,marking    ] GC(1364) Concurrent Mark From Roots
[gc,task       ] GC(1364) Using 2 workers of 2 for marking
# 4.第一次Young GC仍然空间不够，为在年轻代分配巨大对象，继续引发Young GC
[gc,start      ] GC(1365) Pause Young (Normal) (G1 Humongous Allocation)
[gc,task       ] GC(1365) Using 8 workers of 8 for evacuation
[gc,phases     ] GC(1365)   Pre Evacuate Collection Set: 3.0ms
[gc,phases     ] GC(1365)   Evacuate Collection Set: 9.0ms
[gc,phases     ] GC(1365)   Post Evacuate Collection Set: 17.9ms
[gc,phases     ] GC(1365)   Other: 2.3ms
# 回收无效果
[gc,heap       ] GC(1365) Eden regions: 0->0(735)
[gc,heap       ] GC(1365) Survivor regions: 2->2(93)
[gc,heap       ] GC(1365) Old regions: 1443->1443
[gc,heap       ] GC(1365) Humongous regions: 138->138
[gc,metaspace  ] GC(1365) Metaspace: 419538K->419538K(1300480K)
[gc            ] GC(1365) Pause Young (Normal) (G1 Humongous Allocation) 6325M->6325M(9832M) 28.565ms
[gc,cpu        ] GC(1365) User=0.18s Sys=0.00s Real=0.02s
# 5.开始Full GC
[gc,task       ] GC(1366) Using 8 workers of 8 for full compaction
[gc,start      ] GC(1366) Pause Full (G1 Humongous Allocation)
[gc,phases,start] GC(1366) Phase 1: Mark live objects
[gc,stringtable ] GC(1366) Cleaned string and symbol table, strings: 141014 processed, 117 removed, symbols: 838192 processed, 0 removed
[gc,phases      ] GC(1366) Phase 1: Mark live objects 246.403ms
[gc,phases,start] GC(1366) Phase 2: Prepare for compaction
[gc,phases      ] GC(1366) Phase 2: Prepare for compaction 56.600ms
[gc,phases,start] GC(1366) Phase 3: Adjust pointers
[gc,phases      ] GC(1366) Phase 3: Adjust pointers 148.139ms
[gc,phases,start] GC(1366) Phase 4: Compact heap
[gc,phases      ] GC(1366) Phase 4: Compact heap 138.928ms
[gc,heap        ] GC(1366) Eden regions: 0->0(450)
[gc,heap        ] GC(1366) Survivor regions: 2->0(93)
[gc,heap        ] GC(1366) Old regions: 1443->320
[gc,heap        ] GC(1366) Humongous regions: 138->130
[gc,metaspace   ] GC(1366) Metaspace: 419538K->419538K(1300480K)
[gc             ] GC(1366) Pause Full (G1 Humongous Allocation) 6325M->1769M(6000M) 744.919ms
[gc,cpu         ] GC(1366) User=4.18s Sys=0.19s Real=0.75s
# 6. Full GC后巨大对象已分配，停止并打标记
[gc,marking     ] GC(1364) Concurrent Mark From Roots 777.174ms
[gc,marking     ] GC(1364) Concurrent Mark Abort
[gc             ] GC(1364) Concurrent Cycle 782.954ms
```
