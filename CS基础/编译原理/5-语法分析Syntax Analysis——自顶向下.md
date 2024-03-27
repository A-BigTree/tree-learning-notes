# 4 语法分析Syntax Analysis——自顶向下



## 4.0 概述

语法分析器是编译器的核心，语法分析器从词法分析器获得一个由词法单元组成的串，并验证这个串可以由源语言的文法生成。

语法分析器大体上可以分为三种类型：通用的、**<u>自顶向下</u>**的和**<u>自底向上</u>**的。

编译器中常用的方法可以分为自顶向下的和自底向上的，**<u>*自顶向下的方法从语法分析树的顶部（根节点）开始向底部（叶子节点）构造语法分析树，自底向上则相反*</u>**。语法分析器的输入总是按照从左向右的方式被扫描，每次扫描一个符号。

<img src="image/image-20221127212325846.png" alt="image-20221127212325846" style="zoom:50%;" />

自顶向下处理文法典型为**LL(1)文法**，自下而上处理文法典型为LR文法。其中LR文法包括SLR、LR(1)、LALR(1)文法。



### 4.0.1 语法错误处理

程序可能有不同层次的错误：

- 词法错误（Lexical）；
- 语法错误（Syntactic）；
- 语义错误（Semantic）；
- 逻辑错误（Logical）；

错误处理目标：

- 清晰精准地报告出现的错误；
- 能很快地从各个错误中恢复，以继续检测后面的错误；
- 尽可能少地增加处理正确程序时的开销；

错误恢复策略：

- 恐慌模式的恢复；
- 短语层次的恢复；
- 错误产生式；



## 4.1 LL(1)文法



### 4.1.1 文法简介

自顶向下语法分析可以被看作是为输入串构造语法分析树的问题，它从语法分析树的根节点开始，按照先跟次序（**深度优先**地）创建这棵语法分析树的各个结点，可以被看作寻找输入串最左推导的过程。

在一个自顶向下语法分析的每一步中，关键问题是确定对一个非终结符号应用哪个产生式。一旦选择了某个产生式，语法分析过程的其余部分负责将相应产生式体中的终结符号和输入相匹配。

**预测分析技术**通过在输入中向前看固定多个符号来选择正确的A的产生式。对于有些文法，**我们可以构造出向前看<u>k个输入符号</u>的预测分析器，这类文法有时也称为LL(k)文法类**。通常情况下我们只需要向前看一个符号（即只看下一个输入符号），即我们讨论研究的**LL(1)**文法。



### 4.1.2 LL(1)文法定义

一个文法G是LL(1)的，当且仅当G的任意两个不同的产生式$A\rightarrow \alpha|\beta$满足下面的条件：

1. **<u>不存在</u>**终结符号a使得$\alpha\text{和}\beta$**都能**够推导出以a开头的串；
2. $\alpha\text{和}\beta$最多只有一个可以推导出空串；
3. 如果$\beta\Rightarrow^*\varepsilon$，那么$\alpha$不能推导出任何以$FOLLOW(A)$中某个终结符号开头的串，反之亦然；

==**注意：**==

- 条件1，2等价于说$FIRST(\alpha)\text{和}FIRST(\beta)$是不相交的集合；
- 条件3等价于，如果$\varepsilon\text{在}FIRST(\beta)$中，那么$FIRST(\alpha)$和$FOLLOW(A)$是不相交的集合，反之亦然成立；

$FIRST\text{集与}FOLLOW\text{集}$将在后面进行介绍；



## 4.2 LL(1)文法判断⭐

LL(1)文法判断有两种方法，一种是根据以上LL(1)文**法定义进行判断**；另一种更常用的方法是**<u>*构造出预测分析表*</u>**进行判断。

LL(1)文法表驱动分析过程：

1. 预处理，对文法进行消除左递归和提取左公因子；
2. 写出预处理文法的First集和Follow集；
3. 构造LL(1)预测分析表(Predictive Parsing Table, PPT)；

后面讲对各个步骤进行详细介绍

### 4.2.1 消除左递归

#### 立即左递归(Immediate left recursion)

存在形如$A\rightarrow A\alpha$的产生式，这是我们认为该产生式存在左递归，该左递归为**立即左递归**，自顶向下语法分析方法不能处理左递归的文法，因此需要一个转换方法来消除左递归。

**<u>*通用消除立即左递归方法：*</u>**

首先将A**全部**产生式分组如下：
$$
A\rightarrow A\alpha_1|A\alpha_2|\dots|A\alpha_m|\beta_1|\beta_2|\dots|\beta_n
$$
其中$\beta_i$**都不是**以$A$开头。然后将这些A产生式替换为：
$$
A\rightarrow \beta_1A'|\beta_2A'|\dots|\beta_nA'\\
A'\rightarrow \alpha_1A'|\alpha_2A'|\dots|\alpha_mA'|\varepsilon
$$
**<u>*例子：*</u>**
$$
A\rightarrow Aa|ab|b
$$
消除左递归为：
$$
(1)A\rightarrow abA'|bA'\\
(2)A'\rightarrow aA'|\varepsilon
$$

#### 非立即左递归(Indirect left recursion)

形如$P\rightarrow Aa,A\rightarrow Pb|c$的两个产生式，两个非终结符间**构成一个环**，这是我们认为产生式存在非立即左递归（间接左递归）。

**<u>*消除左递归算法：*</u>**

1. 将文法G中的**非终结符**以**<u>某种顺序（由内到外）</u>**排序为$P_1,P_2,\dots,P_n$；
2. 从1到n的每个终结符$P_i$：
   - 从1到i-1的每个终结符$P_j$：
     - 将每个形如$P_i\rightarrow P_j\gamma$的产生式替换为产生式组$P_i\rightarrow \delta_1\gamma|\delta_2\gamma|\dots|\delta_k\gamma$，其中$P_j\rightarrow \delta_1|\delta_2|\dots|\delta_k$是所有$P_j$产生式==（**<u>即将$P_i$产生式中的$P_j$替换成$P_j$产生式的结果</u>**）==；
   - 消除$P_i$产生式之间的立即左递归；
3. 重新排列语法；

**<u>*例子：*</u>**
$$
(1)S\rightarrow Qc|c\\
(2)Q\rightarrow Rb|b\\
(3)R\rightarrow Sa|a
$$

1. 将非终结符由内到外排序为：R,Q,S；

2. 对于R：无操作；

   对于Q：$Q\rightarrow Rb|b \Rightarrow Q\rightarrow Sab|ab|b$；

   - Q无立即左递归；

   对于S：$S\rightarrow Qc|c\Rightarrow S\rightarrow Sabc|abc|bc|c$；

   - S存在立即左递归：

   $$
   (1)S\rightarrow (abc|b|c)S'\\
   (2)S'\rightarrow abcS'|\varepsilon
   $$

3. 因为无法到达R，Q所以删除，最终语法为：

$$
(1)S\rightarrow (abc|b|c)S'\\
(2)S'\rightarrow abcS'|\varepsilon
$$

==**<u>*消除原则：*</u>**==

- 由内到外、从左到右；
- 消除的结果中开始符不能变；

### 4.2.2 提取左公因子

当不清楚应该在两个A产生式中如何选择时，我们可以通过改写产生式来退后这个决定，等我们读入足够多的输入，获得足够信息后再做出选择。

**<u>*提取左公因子方法：*</u>**

对于所有A产生式：
$$
A\rightarrow \alpha\beta_1|\alpha\beta_2|\dots|\alpha\beta_n|\gamma
$$
替换为：
$$
(1)A\rightarrow \alpha A'|\gamma\\
(2)A'\rightarrow \beta_1|\beta_2|\dots|\beta_n
$$


### 4.2.3 构建First集

$FIRST(\alpha)$被定义为可从$\alpha$推导得到的串的首符号，其中$\alpha$是任意的文法符号串。

计算FIRST(`X`)方法规则：

1. 如果X是一个终结符，那么FIRST(X)=X；
2. 如果X是非终结符，X的产生式形如$X\rightarrow aB(a\text{是终结符})$，那么将a加入到FIRST(X)集合中；
3. 如果X是非终结符，X的产生式形如$X\rightarrow Y_1Y_2\dots Y_k(Y_i\text{都为非终结符})$。从左向右看$Y_i$，把$First（Y_i)$加入到FIRST(X)中，如果$First（Y_i)\text{包括}\varepsilon$，则再把$First（Y_{i+1})$加入到FIRST(X)，如此重复，直到下一个Y的FIRST不包含$\varepsilon$；
4. 如果$X\rightarrow \varepsilon$是一个产生式，把$\varepsilon$加入到FIRST(X)中；

==**规则2与规则3可以归纳为规则3，为方便理解，这里分开解释**==

### 4.2.4 构建Follow集

对于非终结符A，FOLLOW(A)被定义为可能某些句型中紧跟在A右边的终结符号的集合。

计算规则：

1. 将\$\text{放到}FOLLOW(S)\text{中}，**\text{其中}S\text{是开始符号}**，\$是输入右端的结束符号；
2. 如果存在一个产生式$A\rightarrow \alpha B\beta$，那么$FIRST(\beta)-\{\varepsilon\}$加入到FOLLOW(B)中；
3. 如果存在一个产生式$A\rightarrow \alpha B,\text{或存在}A\rightarrow \alpha B\beta\text{且}\varepsilon \in FIRST(\beta)$，那么将FOLLOW(A)符号加入FOLLOW(B)中；



### 4.2.5 构建预测分析表

构造方法：

对于文法G的每个产生式$A\rightarrow \alpha$，进行如下处理：

1. 对于$FIRST(\alpha)$中的每个终结符号a，将$A\rightarrow \alpha$加入到M[A,a]中；
2. 如果$\varepsilon\in FIRST(\alpha)$中，那么对于FOLLOW(A)中的每个终结符b，将$A\rightarrow \alpha$加入到M[A,b]中。如果$\varepsilon\in FIRST(\alpha)\text{且}\$\in FOLLOW(A)$，\text{也将}$A\rightarrow \alpha$\text{加入到}M(A, \$)中；

在完成上述操作后，如果M[A,a]中没有产生式，那么将M[A,a]设置为error（在表中用空白表示）

### 4.2.6 示例

判断下列文法G是否为LL(1)文法：
$$
0.E\rightarrow E+T|T\\
1.T\rightarrow T*F|F\\
2.F\rightarrow (E)|id
$$

#### 消除左递归

由内到外，从左到右顺序：

1. 产生式2无左递归；
2. 产生式1存在左递归，进行消除

$$
T\rightarrow FT'\\
T'\rightarrow *FT'|\varepsilon
$$

3. 产生式0存在左递归，进行消除

$$
E\rightarrow TE'\\
E'\rightarrow +TE'|\varepsilon
$$

综上，消除左递归后产生文法G'为： 
$$
\begin{aligned}
&(0)E\rightarrow TE'\\
&(1)E'\rightarrow +TE'|\varepsilon\\
&(2)T\rightarrow FT'\\
&(3)T'\rightarrow *FT'|\varepsilon\\
&(4)F\rightarrow (E)|id
\end{aligned}
$$

#### 构建First和Follow集

对每个产生式我们可以很容易得到每个产生式的**FIRST集**，如下表所示：
$$
\begin{array}{cc}
\text{产生式}&FIRST\\
\hline
E\rightarrow TE' &\{(,id\} \\
\hline
E'\rightarrow +TE' &\{+\} \\

E'\rightarrow \varepsilon &\{\varepsilon\} \\
\hline
T\rightarrow FT' &\{(,id\} \\
\hline
T'\rightarrow *FT' &\{*\} \\

T'\rightarrow \varepsilon &\{\varepsilon\}\\
\hline
F\rightarrow (E) &\{(\} \\

F\rightarrow id &\{id\} \\
\hline
\end{array}
$$
下面我们获取**FOLLOW集**：

`FOLLOW(E)`:

- E为开始符号，所以加入\$；\text{根据产生式}$F\rightarrow (E)$得，FOLLOW(E)中加入`)`；
- $FOLLOW(E)=\{),\$\}$；

`FOLLOW(E')`

- E'只出现在E产生式的右部尾部，所以FOLLOW(E')=FOLLOW(E)；
- $FOLLOW(E’)=\{),\$\}$；

`FOLLOW(T)`

- 产生式$E'\rightarrow +TE'$，E'在T后面，所以加入FIRST(E')-{ε}，即`+`；又$\varepsilon\in FIRST(E')$，所以FOLLOW(E')加入FOLLOW(T)中；
- $FOLLOW(T)=\{+,),\$\}$；

`FOLLOW(T')`

- T'只出现在T产生式右部尾部，所以FOLLOW(T')=FOLLOW(T)；
- $FOLLOW(T')=\{+,),\$\}$；

`FOLLOW(F)`

- 产生式$T'\rightarrow *FT'$，T'在F后面，所以加入FIRST(T')-{ε}，即`*`；又$\varepsilon\in FIRST(T')$，所以FOLLOW(T')加入FOLLOW(F)中；
- $FOLLOW(T)=\{*,+,),\$\}$；

构建FIRST和FOLLOW表：
$$
\begin{array}{c|c|c}
\text{产生式}&FIRST&FOLLOW\\
\hline
E\rightarrow TE' &\{(,id\}&\{(,\$\} \\
\hline
E'\rightarrow +TE' &\{+\} &\{(,\$\} \\

E'\rightarrow \varepsilon &\{\varepsilon\}&\{(,\$\} \\
\hline
T\rightarrow FT' &\{(,id\}&\{+,(,\$\} \\
\hline
T'\rightarrow *FT' &\{*\} &\{+,(,\$\}\\

T'\rightarrow \varepsilon &\{\varepsilon\}&\{+,(,\$\}\\
\hline
F\rightarrow (E) &\{(\} & \{*,+,(,\$\}\\

F\rightarrow id &\{id\} & \{*,+,(,\$\}\\
\hline
\end{array}
$$

#### 构建预测分析表

根据构建规则，构建预测分析表如下：
$$
\begin{array}{c|c|c|c|c|c|c}
(\text{非})\text{终结符}&id&+&*&(&)&\$\\
\hline
E&E\rightarrow TE' &  & &E\rightarrow TE' & &\\
\hline
E'& &E'\rightarrow +TE'  & & &E'\rightarrow \varepsilon & E'\rightarrow \varepsilon\\
\hline
T&T\rightarrow FT' &  & &T\rightarrow FT' & &\\
\hline
T'& &T'\rightarrow \varepsilon  &T'\rightarrow *FT' & &T'\rightarrow \varepsilon &T'\rightarrow \varepsilon\\
\hline
F&F\rightarrow id &  & &F\rightarrow (E) & &\\

\end{array}
$$
表中**<u>未出现二义性条目</u>**，**所以文法G'是LL(1)文法，文法G==存在等价==的LL(1)文法**。



