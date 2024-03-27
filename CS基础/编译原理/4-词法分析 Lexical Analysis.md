# 3 词法分析 Lexical Analysis

```mermaid
graph LR
re[正归式Regular Expression]
fa[有限自动机Finite Automata]
rg[正归文法Regular Grammar]
re --> fa
rg-->re
fa-->rg
```



## 3.1 正规文法



### 3.1.1 正规文法、正规集与正规式

由正规文法产生的语言称作正规集。正规集是集合，可以是有穷的也可以是无穷的，用一种形式化的方法——正规式（Regular Expression **Re**）进行描述。

设$A$是非空的有限字母表，$A =\{a_i|i=1,2,\dots,n\}$，则：

- $\varepsilon,\varnothing,a_i(i=1,2,\dots,n)$都是Re；
- 若$\alpha,\beta$是Re，则$\alpha|\beta,\alpha\beta,\alpha^*,\beta^*$也是Re；
- Re只能通过有限次使用1，2规则来获得。

### 3.1.2 正则规则

1. $\varepsilon$是基本的正规表达式；

2. 构成符号：

   - $|$：或；

   - $\cdot$：连接connect；

   - $*$：闭包closure；

     优先级：$*>\cdot>|$

3. 代数定律

|              定律              |            描述             |
| :----------------------------: | :-------------------------: |
|           $r|s=s|r$            |          $|$可交换          |
|       $r|(s|t)=(r|s)|t$        |          $|$可结合          |
|         $r(st)=(rs)t$          |         连接可结合          |
|  $r(s|t)=rs|rt;(s|t)r=sr|tr$   |       连接对$|$可分配       |
| $\varepsilon r=r\varepsilon=r$ | $\varepsilon$是连接的单元位 |
|    $r^*=(r|\varepsilon)^*$     | 闭包中一定包含$\varepsilon$ |
|          $r^{**}=r^*$          |      $^*$具有灯幂等性       |

4. 正规定义

如果$\sum$是基本符号的集合，那么一个正则定义（regular definition）是具有如下形式的定义序列：
$$
d_1\Rightarrow r_1\\
d_2\Rightarrow r_2\\
\dots\\
d_n\Rightarrow r_n\\
$$
其中：

- 每个$d_i$都是一个新符号，它们不在$\sum$中，并且各不相同；
- 每个$r_i$是字母表$\sum\cup\{d_1,d_2,\dots,d_{i-1}\}$上的正则表达式；



5. 其他简化符号

- `+`：一个或多个；
- `?`：零个或一个，$r?$是$r|\varepsilon$的简写；
- `[a-z]`：字符类；



## 3.2 有穷自动机Finaite Automata



### 3.2.1 状态转换图

- 状态state，用节点或圆圈表示。状态包括开始状态和最终状态
  - 最终状态：又称接收状态，用双圈圆圈表示；
  - 开始状态：又称起始状态，该状态用一条没有出发节点、标号为start的边知名；
- 边edge，从一个状态指向另一个状态，每个标号包括一个或多个符号；

<img src="image/image-20221126162936413.png" alt="image-20221126162936413" style="zoom:50%;" />

### 3.2.2 有穷自动机

有穷自动机Finite automata，简称FA，是识别器，它们只能对每个可能的输入串简单地回答“是”或“否”。有穷自动机分为不确定的有穷自动机（NFA）和确定的有穷自动机（DFA）。

FA范式表达：$FA(S,s_0,F,\sum,map/move)$

- $S$：有穷状态集合；
- $s_0$：开始状态；
- $F$：终止状态集合；
- $\sum$：输入符号集合；
- $map/move$：映射；



### 3.2.3 不确定的有穷自动机

**不确定的有穷自动机(Nondeterministic Finite Automata，NFA)**对其边上的标号没有任何限制。每个符号标记离开同一状态的多于边，并且空串$\varepsilon$也可以作为标号。

NFA范式表达：$M(S,\sum,move,s_0,F)$

- $S$：有穷状态集合；
- $\sum$：输入符号集合；
- $move$：转换函数，它为每个状态中的每个符号都会给出相应的后继状态的集合；

>A mapping $from\ S\times\sum\ to\ 2^S,move(s,a)=2^S,2^S\in S$

- $s_0$：开始状态；
- $F$：终止状态集合；



### 3.2.4 确定的有穷自动机

**确定的有穷自动机(Deterministic Finite Automata，DFA)**中每个状态及自动机输入字母表中的每个符号有且只有一条离开该状态、以该符号为标号的边。

DFA范式表达：$M(S,\sum,move,s_0,F)$

- $S$：有穷状态集合；
- $\sum$：输入符号集合；
- $move$：转换函数，它为每个状态中的每个符号都会给出相应的后继状态的集合；

> A mapping $from\ S\times\sum\ to\ S,move(s,a)=s'$

- $s_0$：开始状态；
- $F$：终止状态集合；



## 3.3 从正则表达式到自动机



### 3.3.1 从正则表达式构造NFA



#### 分解法

较为简介，但这种方法**计算机没法使用**。

1. 引入开始状态和结束状态；
2. 逐步分解；

**<u>*基本组合：*</u>**

<img src="image/image-20221126170728887.png" alt="image-20221126170728887" style="zoom:50%;" />

#### Thumpson算法

1. 把正则表达式的中缀形式转化为后缀表达式；
2. 将后缀表达式从左向右依次做组合；

**<u>*基本组合*</u>**

- 终结符

<img src="image/image-20221126171020168.png" alt="image-20221126171020168" style="zoom:50%;" />

- $|$操作：$N(s|t)$

<img src="image/image-20221126171113883.png" alt="image-20221126171113883" style="zoom:50%;" />

- 连接操作：$N(st)$

<img src="image/image-20221126171158844.png" alt="image-20221126171158844" style="zoom:50%;" />

- 闭包操作：$N(s^*)$

<img src="image/image-20221126171245168.png" alt="image-20221126171245168" style="zoom:50%;" />

**<u>*例题：*</u>**
$$
N:=(a|b)^*ab
$$
Thumpson算法构造NFA结果如下：

<img src="image/NFA.jpg" alt="NFA" style="zoom: 80%;" />



### 3.3.2 从NFA构造DFA

#### 空串闭包$\varepsilon-closure(s)$

$\varepsilon-closure(s)$表示由状态$s$经由条件$\varepsilon$可以到达的所有状态的集合。

以上面得到的NFA结果为例，各状态的的空串闭包如下：

- $\varepsilon-closure(0)=\{0,1,2,4,7\}$
- $\varepsilon-closure(1)=\{1,2,4\}$
- $\varepsilon-closure(2)=\{2\}$
- $\varepsilon-closure(3)=\{1,2,3,4,6,7\}$
- $\varepsilon-closure(4)=\{4\}$
- $\varepsilon-closure(5)=\{1,2,4,5,6,7\}$
- $\varepsilon-closure(6)=\{1,2,4,6,7\}$
- $\varepsilon-closure(7)=\{7\}$
- $\varepsilon-closure(8)=\{8\}$
- $\varepsilon-closure(9)=\{9\}$



#### 构建转换表

**<u>*构造步骤：*</u>**

1. 求出初始状态集合$I_0=\varepsilon-closure(\{x\})，x\text{为初始状态}s_0$；
2. 根据$I_i\text{求}move(I_i,\text{输入})$；
3. 根据$move(I_i,\text{输入}),\text{求闭包}I_{i+1}=\varepsilon-closure(move(I_i,\text{输入}))$；
4. 重复2，3，直到**不出现新的状态集合**；

**<u>*以上面的为例：*</u>**

初始状态集合$I_0=\varepsilon-closure(0)=\{0,1,2,4,7\}$；

$move(I_0,a)$表示从状态$I_0$，经过a到达的状态，即对$I_0=\{0,1,2,4,7\}$里每个元素都考虑是否通过a到达某个状态，从NFA图中我们可以发现，只有状态2和7记过a，分别到达了3和8，所以$move(I_0,a)=\{3,8\}$；

闭包$\varepsilon-closure(move(I_0,a))=\varepsilon-closure(\{3,8\})$，该闭包为状态3和状态8闭包的**<u>并集</u>**，即$\varepsilon-closure(3)\cup\varepsilon-closure(8)=\{1,2,3,4,6,7,8\}$，该状态从未出现过，我们将其作为$I_1$；

整个过程如下

- $ε-closure(0)=\{0,1,2,4,7\}=I_0$;
- $ε-c l o s u r e ( m o v e ( I_0 , a ) ) = ε-c l o s u r e ( \{ 3 , 8 \} ) = \{ 1 , 2 , 3 , 4 , 6 , 7 , 8 \} = I_1 $;
- $ε - c l o s u r e ( m o v e ( I_0 , b ) ) = ε-c l o s u r e ( 5 ) = \{ 1 , 2 , 4 , 5 , 6 , 7 \} = I_2$;
- $ε - c l o s u r e ( m o v e ( I_1 , a ) ) = ε - c l o s u r e ( \{ 3 , 8 \} ) = \{ 1 , 2 , 3 , 4 , 6 , 7 , 8 \} = I_1 $;
- $ε - c l o s u r e ( m o v e ( I_1 , b ) ) = ε - c l o s u r e ( \{ 5 , 9 \} ) = \{ 1 , 2 , 4 , 5 , 6 , 7 , 9 \} = I_3 $;
- $ε - c l o s u r e ( m o v e ( I_2 , a ) ) = ε - c l o s u r e ( \{ 3 , 8 \} ) = \{ 1 , 2 , 3 , 4 , 6 , 7 , 8 \} = I_1 $;
- $ε - c l o s u r e ( m o v e ( I_2 , b ) ) = ε - c l o s u r e ( 5 ) = \{ 1 , 2 , 4 , 5 , 6 , 7 \} = I_2 $;
- $ε - c l o s u r e ( m o v e ( I_3, a ) ) = ε - c l o s u r e ( \{ 3 , 8 \} ) = \{ 1 , 2 , 3 , 4 , 6 , 7 , 8 \} = I_1 $;
- $ε - c l o s u r e ( m o v e ( I_3 , b ) ) = ε - c l o s u r e ( 5 ) = \{ 1 , 2 , 4 , 5 , 6 , 7 \} = I_2 $;

构建DFA转换表：

| 状态  | 输入a | 输入b |
| :---: | :---: | :---: |
| $I_0$ | $I_1$ | $I_2$ |
| $I_1$ | $I_1$ | $I_3$ |
| $I_2$ | $I_1$ | $I_2$ |
| $I_3$ | $I_1$ | $I_2$ |

- 如何判断DFA**终止状态**？

  如果$I_i\cap F \not= \varnothing,F\text{为}NFA\text{终止状态集合}$，则$I_i$为DFA终止状态；

该例中，NFA终止状态为{9}，所以DFA终止状态为$\{I_3\}$；

所以得到的DFA范式表达为：
$$
DFA:=(S=\{I_0,I_1,I_2,I_3\},\sum=\{a,b\},move,s_0=I_0,F=\{I_3\})\\
move(I_i,s),I_i\in S,s\in \sum,\text{即刚刚构建的}DFA\text{转换表}
$$


### 3.3.3 DFA简化

#### 消除多余状态

**<u>*多余状态：*</u>**

- 从该状态出发没有通路到达最终状态；
- 从开始状态出发，任何输入串也不能到达的那个状态；

处理方法：直接删除

#### 合并等价状态

**<u>*等价状态：*</u>**

- **一致性条件**：状态s和状态t必须同时为终态或非终态；
- **蔓延性条件**：对于所有输入符号，状态s和状态t必须转化到等价的状态里；

处理方法：可通过状态转换表判断是否为等价状态（不适合复杂DFA）

#### 状态最小化算法

对于一个$DFA:=(S,\sum,move,s_0,F)$：

1. 构造包含两个组F和S-F的初始划分$\Pi$，两个组分别是DFA的最终状态组和非最终状态组；
2. 最初，令$\Pi_{new}=\Pi$；**对于$\Pi$中的每个组G**：将G分划为更小的组，使得两个状态s和t在同一组中当且仅当所有的输入符号a，状态s和t在a上的转换都到达$\Pi$中同一组；（最坏情况下，每个状态各成一组）在$\Pi_{new}$中将G替换为对G进行分划得到的那些小组，下面处理$\Pi$中的下一组；
3. 如果$\Pi_{new}=\Pi$，令$\Pi_{final}=\Pi$并进行步骤4；否则，将$\Pi_{new}$代替$\Pi$并重复步骤2；
4. 在分划$\Pi_{final}$的每个组中选取一个状态作为改组的代表。这些代表构成最少DFA'的状态。DFA'的其他部分按如下步骤构建：
   - DFA'的开始状态是包含DFA的开始状态的代表；
   - DFA'的接受状态是那些包含了DFA的接受状态组的代表；
   - 令s是$\Pi_{final}$中某个组G的代表，并令DFA中在输入a上离开s的转换到达t。令r为t所在组H的代表。那么DFA'中存在一个从s到r在输入a上的转换。
5. 消除DFA'上的多余状态



**<u>*示例：*</u>**

最小化下面的DFA

<img src="image/image-20221127200007924.png" alt="image-20221127200007924" style="zoom:50%;" />

1. 初始化：$\Pi=\{\{0,1,2\},\{3,4,5,6\}\}$；
2. 1 对与$\Pi$中组{0，1，2}
   - 我们将其分为{0，2}、{1}
     - 对于输入a，$move(\{0,2\},a)=\{1\},move(\{1\},a)=\{3\}$，1与3不在$\Pi$的同一组里，继续划分；
     - 我们将{0,2}分为{0}、{2}
       - 对于输入b:$move(\{0\},b)=\{2\},move(\{2\},b)=\{5\}$，2与5不在$\Pi$的同一组里，组中各个状态已成一组；
   - $\Pi_{new}=\{\{0\},\{1\},\{2\},\{3,4,5,6\}\}$；

2. 2对与$\Pi$中组{3，4，5，6}
   - 对于输入a：$move(\{3,4,5,6\},a)=\{3,6\}$，3与6在同一组里；
   - 对于输入b：$move(\{3,4,5,6\},b)=\{4,5\}$，4与5在同一组里；
   - 不需划分；
   - $\Pi_{new}=\{\{0\},\{1\},\{2\},\{3,4,5,6\}\}$；
3. $\Pi_{new}\not=\Pi,\text{令}\Pi=\Pi_{new}$执行步骤2，已不可划分，得到$\Pi_{new2}$
   - $\Pi_{new2}=\{\{0\},\{1\},\{2\},\{3,4,5,6\}\}=\Pi_{new}$；
   - $\Pi_{final}=\Pi_{new}$；
4. 状态3代替{3,4,5,6}，得到最小化结果：

<img src="image/image-20221127202836448.png" alt="image-20221127202836448" style="zoom:50%;" />



