# 7 中间代码生成Intermediate Code Generation

<img src="image/image-20221204195222101.png" alt="image-20221204195222101" style="zoom:50%;" />



## 7.1 语法树的变体

为表达式构建的**无环有向图（Directed Acyclic Graph，DAG）**，其指出了表达式中*公共子表达式*（多次出现的子表达式）。



### 7.1.1 表达式的有向无环图

和表达式的语法树类似，一个DAG的叶子节点对应于原子运算分量，而内部结点对应于运算符。与语法树不同的是，如果DAG中的一个结点N表示一个公共子表达式，而N可能有多个父结点。因此，DAG不仅更简洁地表示了表达式，而且可以为最终生成表达式的高效代码提供重要信息。

下图为表达式$a+a*(b-c)+(b-c)*d$的DAG：

<img src="image/image-20221204201432747.png" alt="image-20221204201432747" style="zoom:50%;" />



### 7.1.2 构造DAG的值编码方法

语法树或DAG图中的结点通常存放在一个记录数组中，在每个记录中，第一个字段是一个运算符代码，也是该结点的标号。各叶子结点还有还有一个附加字段，它存放了标识符的词法值；内部结点则有两个附加的字段，分别指明其左右子节点。

下图为表达式$i=i+10$的DAG与记录数组：

<img src="image/image-20221204204512426.png" alt="image-20221204204512426" style="zoom:50%;" />



## 7.2 三地址代码Three-address Code (TAC)

在三地址代码中，一条指令的右侧最多有一个运算符。三地址代码是一棵语法树或者一个DAG的**<u>线性表示</u>**形式。下图为一个表达式的DAG及其对应的三地址代码：

<img src="image/image-20221204215502376.png" alt="image-20221204215502376" style="zoom:50%;" />



### 7.2.1 地址和指令

==地址==可以具有如下形式之一：

- 名字：实现中，源程序名字被替换为指向符号表条目的指针；
- 常量：不同类型的常量与变量；
- 编译器生成的临时变量；



常见的三地址==指令==形式：
$$
\begin{aligned}
&1)\ x=y\ op\ z\ \text{双目运算符}\\
&2)\ x = op\ y\ \text{单目运算符}\\
&3)\ x=y\ \text{复制指令}\\
&4)\ goto\ L\ \text{转移指令}\\
&5)\ if\ x\ goto\ L\text{或}\ if\ False\ x\ goto\ L\ \text{条件转移指令}\\
&6)\ if\ x\ relop\ y\ goto\ L\ \text{条件转移指令}\\
&7)\ param\ x\ \text{参数传递}; call\ p,n\ \text{过程调用} ; y=call\ p,n\ \text{函数调用};return\ y\ \text{返回指令}\\
&8)\ x=y[i],x[i]=y\ \text{带下标的复制指令}\\
&9)\ x=\&y,x=*y,*x=y\ \text{地址及指复制指令}
\end{aligned}
$$


下图为语句`do i = i + 1;while(a[i] < v);`的两种翻译：

<img src="image/image-20221204221719301.png" alt="image-20221204221719301" style="zoom:50%;" />



### 7.2.2 四元式表示

一个 ***四元式（quadruple）*** 有四个字段，我们分别称为$op,arg_1,arg_2,result$。字段op包含一个运算符的内部编码。下面为一些特例：

1. 单目运算符指令与复制指令不使用$arg_2$；
2. 像$param$这样的运算符既不使用$arg_2$，也不适用result；
3. 条件或者非条件转移指令将目标标号放入result字段；

下图为语句`a = b * -c + b * -c`的三地址代码与对应四元式：

<img src="image/image-20221204222524132.png" alt="image-20221204222524132" style="zoom:50%;" />



### 7.2.3 三元式表示

一个 ***三元式（triple）*** 只有三个字段，我们分别称为$op,arg_1,arg_2$。使用三元式时，我们将运用计算结果的位置来表示它的结果，而不是一个显式的临时名字来表示。

- 表达式的DAG表示和三元式表示时等价的；
- 复制语句`a=b`，字段$arg_1$中放置`a`，字段$arg_2$放置`b`；
- 像`x[i]=y`这样的三元运算在三元式中需要两个条目；

下图为语句`a = b * -c + b * -c`的DAG与对应三元式：

<img src="image/image-20221204223312581.png" alt="image-20221204223312581" style="zoom:50%;" />

***间接三元式（indirect triple）*** 包含了一个指向三元式的指针的列表，而不是列出三元式序列本身。如下图：

<img src="image/image-20221204223823134.png" alt="image-20221204223823134" style="zoom:50%;" />



### 7.2.4 静态单赋值形式

***静态单赋值形式（Static Single-Assignment Form，SSA）*** 是另一种中间表示形式，它有利于实现某些类型的代码优化。

- SSA中的所有赋值都是针对具有不同名字的变量的；

下图为三地址代码和静态单赋值形式表示的中间程序：

<img src="image/image-20221204224829594.png" alt="image-20221204224829594" style="zoom:50%;" />

在同一个程序中，同一变量可能在两个不同的控制流路径中被定值，SSA使用一种称为$\varphi$函数的表示规则将变量定值合并起来，如下图：

<img src="image/image-20221204225225154.png" alt="image-20221204225225154" style="zoom:50%;" />



## 7.3 类型和声明

可以将类型的应用划分为类型检查和翻译：

1. **类型检查（type checking）**。类型检查利用一组逻辑规则来推理一个程序在运行时刻的行为。更明确的讲，类型检查保证运算分量的类型和运算符的预期类型相匹配；
2. **翻译时的应用（translation application）**。根据一个名字的类型，编译器可以确定这个名字在运行时刻需要多大的存储空间；



### 7.3.1 类型表达式

类型自身也有结构，***类型表达式（type expression）*** 来表示这种结构，可能是基本类型，也可能通过把称为 *类型构造算子* 的运算符作用于类型表达式而得到．

基本类型的集合和类型构造算子根据被检查的具体语言而定．我们将使用如下的类型表达式的定义：

1. 基本类型是一个类型表达式。一个语言的基本类型通常含`boolean, char, integer, floatvoid`；
2. 类名是一个类型表达式；
3. 将类型构造算子`array`作用于一个数字和一个类型表达式可以得到一个类型表达式；
4. 一个记录是包含有名字段的数据结构。将record类型构造算子应用于字段名和相应的类型可构造得到一个类型表达式；
5. 使用类型构造算子→可以构造得到函数类型的类型表达式；
6. 如果s和t是类型表达式，则其笛卡尔积$x\times y$也是类型表达式，可以用于描述类型的列表或元组；
7. 类型表达式可以包含取值为类型表达式的变量；

下图为`int[2][3]`的类型表达式：

<img src="image/image-20221204231248750.png" alt="image-20221204231248750" style="zoom:50%;" />

### 7.3.2 类型等价

当用图来表示类型表达式的时候，两种类型之间 ***结构等价（structurally equivalent）*** 当且仅当下面的某个条件为真：

1. 它们是相同的基本类型；
2. 它们是将相同的类型构造算子应用于结构等价的类型而构造得到；
3. 一个类型是另一个类型表达式的名字；



### 7.3.3 声明

下面为声明基本数据类型`int,float`和对应数组的文法：
$$
\begin{aligned}
&D\rightarrow T\ \mathbf{id};D|\varepsilon\\
&T\rightarrow BC|record'\{'D'\}'\\
&B\rightarrow \mathbf{int}|\mathbf{float}\\
&C\rightarrow \varepsilon|[\mathbf{num}]C
\end{aligned}
$$


### 7.3.4 局部变量名的存储信息

名字的类型和相对地址信息保存在相应的符号表条目中。

下面图给出的翻译方案（SDT）计算了基本类型和数组宽度以及它们的宽度：

<img src="image/image-20221204232804395.png" alt="image-20221204232804395" style="zoom:50%;" />

下图展示了类型`int[2][3]`的语法分析树与依赖图：

<img src="image/image-20221204233102987.png" alt="image-20221204233102987" style="zoom:50%;" />

### 7.3.5 声明的序列

在考虑第一个声明之前，`offset=0`，下图为计算被声明变量的相对地址的的SDT：

<img src="image/image-20221204234824596.png" alt="image-20221204234824596" style="zoom:50%;" />



### 7.3.6 记录和类的字段

文法加上产生式
$$
T \rightarrow \mathbf{record} '\{' D '\}'
$$
这个记录类型中的字段D由生成的声明序列描述，需小心地处理下面两件事：

1. 一个记录中各个字段的名字必须互不相同；
2. 字段的偏移量是相对于该记录的数据区字段而言的；

为方便起见，记录类型将使用一个专用的符号表，对它们的各个字段的类型和相对地址进行编码．记录类型形如record(t)，其中record是类型构造算子，t是一个符号表对象，它保存了有关该记录类型的各个字段的信息。

记录类型SDT如下：

<img src="image/image-20221204235926836.png" alt="image-20221204235926836" style="zoom:50%;" />

## 7.4 表达式的翻译

> Translation of expressions

### 7.4.1 表达式的运算

<img src="image/image-20221209103204698.png" alt="image-20221209103204698" style="zoom: 33%;" />

- 一个地址可以是变量名字、常量或者编译器产生的临时变量；
- `S.code,E.code`分别表示S、E对应的三地址码；
- `top.get`表示返回对应的符号表条目；
- 记号`gen(x '=' y '+' z)`来表示三地址指令`x = y + z`；



### 7.4.2 增量翻译

code属性可能是很长的字符串，因此我们不像上式一样构建`E.code`，我们可以像下图翻译方案一样只生成新的三地址指令：

<img src="image/image-20221209113039531.png" alt="image-20221209113039531" style="zoom:33%;" />



### 7.4.3 数组元素寻址

- 一维数组

假设每个数组元素的宽度是$w$，那么数组A的第i个元素的开始地址为：
$$
base+i\times w
$$
其中$base$是分配给数组A的内存块的相对地址，即$A[0]$的相对地址；

- 二维数组

假设一行的宽度是$w_1$，同一行中每个元素的宽度是$w_2$。$A[i][j]$的相对地址可以使用下面的公式计算：
$$
base+i_1\times w_1+i_2\times w_2
$$

- k维数组

相应的公式为：
$$
base+i_1 \times w_2+i_2\times w_2+\dots+i_k\times w_k
$$
其中，$w_j(1\le j\le k)$是二维数组中$w_1,w_2$的推广；



**<u>*二维数组的存储布局：*</u>**

- 按行存放；
- 按列存放；

<img src="image/image-20221214142533135.png" alt="image-20221214142533135" style="zoom:50%;" />



### 7.4.4 数组引用的翻译

令非终结符号L生成一个数组名字再加上一个下标表达式的序列：
$$
L\rightarrow L[E]\ |\ \mathbf{id}[E]
$$
处理数组引用的语义动作：

<img src="image/image-20221214143300421.png" alt="image-20221214143300421" style="zoom: 50%;" />

非终结符L有三个综合属性：

1. $L.addr$指示一个临时变量；
2. $L.array$是一个指向数组名字对应的符号表条目的指针；
3. $L.type$是L生成的子数组的类型；



## 7.5 类型检查

> Type Checking

为了进行类型检查（type checking），编译器需要给源程序的每一个组成部分赋予一个类型表达式。然后，编译器要去确定这些类型表达式是否满足一组逻辑规则。这些规则称为源语言的类型系统（type system）。

一个**健全（sound）的类型系统**可以消除对动态类型错误检查的需要，因为它可以帮助我们静态地确定这些错误不会在目标程序运行的时候发生。如果编译器可以保证它接受的程序运行时刻不会发生类型错误，那么该语言的这个实现就称为**强类型（strongly typed）**。

### 7.5.1 类型检查规则

> Rules for Type Synthesis

#### 类型综合（type synthesis）

类型综合**根据子表达式的类型构造出表达式的类型**。它要求名字先声明再使用。表达式$E_1+E_2$的类型是根据$E_1$和$E_2$的类型定义的。一个典型的类型综合规则具有如下形式：
$$
\begin{aligned}
&\mathbf{if}\ f\text{的类型为}s\rightarrow t\text{且}x\text{的类型为}s\\
&\mathbf{then}\ \text{表达式}f(x)\text{的类型为}t

\end{aligned}
$$

#### 类型推导（type inference）

类型推导**根据一个语言结构的<u>使用方式</u>**来确定该结构的类型。

一个典型的类型推导规则具有下面的形式：

$$
\begin{aligned}
&\mathbf{if}\ f(x)\text{是一个表达式}\\
&\mathbf{then}\ \text{对某些}\alpha\text{和}\beta，f\text{的类型为}\alpha\rightarrow\beta\text{且}x\text{的类型为}\alpha
\end{aligned}
$$

### 7.5.2 类型转换

> Type Conversions

对于表达式`2*3.14`，我们要先将整数使用单目运算符`(float)`转为浮点数，如下：

- `t1 = (float)2`
- `t2 = t1*3,14`

不同语言具有不同的类型转换规则。Java的转换规则区分了拓宽（widening）转换和窄化（narrowing）转换。

#### 拓宽（widening）转换

- 可以保持原有信息，在层次结构中位于底层的类型可以拓宽为较高层次的类型；

#### 窄化（narrowing）转换

- 可能丢失信息，如果存在一条从s到t的路径，则可以将类型s窄化为类型t；

Java中简单类型的转换如下图：

<img src="image/image-20230129172628581.png" alt="image-20230129172628581" style="zoom:50%;" />

#### 自动类型转换（Coercion）

- 类型转换由编译器自动完成，那么这样的转换就称为隐形转换，即自动类型转换；

#### 强制类型转换（Cast）

- 由程序员写出某些代码来引发类型转换运算，那么这个转换就称为显示转换；



### 7.5.3 函数和运算符的重载

> Overloading of Functions and Operators

依据符号所在的上下文不同，被重载（overloading）的符号会有不同的含义。在这里我们只考虑那些只需要查看函数参数就能解决的函数重载。

针对重载函数的类型转换综合规则：
$$
\begin{aligned}
&\mathbf{if}\ f\text{可能的类型为}s_i\rightarrow t_i(1\le i\le n),\text{其中}s_i\not =s_j(i\not=j)\\
&\mathbf{and}\ x\text{的类型为}s_k(1\le k\le n)\\
&\mathbf{then}\ \text{表达式}f(x)\text{的类型为}t_k
\end{aligned}
$$


### 7.5.4 类型推导和多态函数

> Type Interface and Polymorphic Functions

#### 多态（polymorphic）

- 任何可以在不同的参数类型上运行的代码片段；

#### 参数多态（parametric polymorphism）

- 这种多态通过参数和类型变量来刻划；

函数`length`的描述可以描述为：
$$
“\text{对于任何类型}\alpha,length\text{函数将元素类型}\alpha\text{的列表映射为整数}。”
$$
函数`length`定义：
$$
\begin{aligned}
&\mathbf{fun}\ length(x)=\\
&&\mathbf{if}\ null(x)\ \mathbf{then}\ 0\ \mathbf{else}\ length(tl(x))+1
\end{aligned}
$$

- 预定义函数`null(x)`测试一个列表是否为空；
- 预定义函数`tl`(tail的缩写)移除列表中的第一个元素，然后返回列表的余下部分；

使用符号$\forall$以及类型构造算子list，length的类型可以写作：
$$
\forall\ \alpha.list(\alpha)\rightarrow integer
$$

- 其中带有$\forall$符号表达式被称为“多态类型”；



## 7.6 控制流

> Control Flow

- if-else语句；
- while语句；
- 布尔表达式
  - 改变控制流：`if(E)S`；
  - 计算逻辑值：使用带有逻辑运算符的三地址指令进行求值；



### 7.6.1 布尔表达式

> Boolean Expressions

本节中考虑如下文法生成的布尔表达式：
$$
B\rightarrow B\Vert B\ |\ B\&\&B\ |\ !B\ |\ (B)\ |\ E\ \mathbf{rel}\ E\ |\ \mathbf{true}\ |\ \mathbf{false}
$$

- 通过属性$\mathbf{rel}.op$指明运算符`<,<=,=,!=,>,>=`中的一种；



### 7.6.2 短路（跳转）代码

> Short-Circuit(or jumping) Code of Boolean Expression

在短路（跳转）代码中，布尔运算符$\Vert,\&\&,!$被翻译成跳转指令。==运算符本身不出现在代码中==，布尔表达式的值是通过代码序列中的位置来表示的。

对于语句
$$
if(x<100)\ \Vert\  x>200\ \&\&\ x!=y)x=0;
$$
翻译的跳转代码如下：
$$
\begin{aligned}
&if\ x<100\ goto\ L_2\\
&goto\ L_3\\
L_3:\ &if\ x>200\ goto\ L_4\\
&goto\ L_1\\
L_4:\ &if\ x!=y\ goto\ L_2\\
&goto\ L_1\\
L_2:\ &x=0\\
L_1:\ &...
\end{aligned}
$$

### 7.6.3 控制流语句

> Flow-of-Control Statements

将布尔表达式翻译成为三地址码：
$$
\begin{aligned}
&S\rightarrow \mathbf{if}(B)S_1\\
&S\rightarrow \mathbf{if}(B)S_1\ \mathbf{else}\ S_2\\
&S\rightarrow \mathbf{while}(B)S_1
\end{aligned}
$$
翻译过程如下图表示：

<img src="image/image-20230129191956938.png" alt="image-20230129191956938" style="zoom:50%;" />

语法制导定义如下：

<img src="image/image-20230129192142660.png" alt="image-20230129192142660" style="zoom: 33%;" />

### 7.6.4 布尔表达式的控制流翻译

> Semantic Rules of Boolean Expression

针对布尔表达式的语义规则如下图所示：

<img src="image/image-20230129192500273.png" alt="image-20230129192500273" style="zoom:33%;" />

==**实例：**==

```python
while a < b do
    if c < d then
        x = y + z
	else
    	x = y - z
```

翻译结果如下：
$$
\begin{aligned}
L_1:\ &if\ a<b\ goto\ L_2\\
&goto\ L_{next}\\
L_2:\ &if\ c<d\ goto\ L_3\\
&goto\ L_4\\
L_3:\ &t_1=y+z\\
&x=t_1\\
&goto\ L_1\\
L_4:\ &t_2=y-z\\
&x=t_2\\
&goto\ L_1\\
L_{next}:\ &...
\end{aligned}
$$

## 7.7 回填

> Backpatching

对于$if(B)S$中的布尔表达式B的翻译结果中包含一条跳转指令。当B为假时，该指令将跳转到紧跟在S的代码之后的指令处。在一趟式的翻译中，B必须在处理S之前就翻译完毕。那么跳过S的goto指令的目标是什么呢？我们解决这个问题的方法是将标号作为继承属性传递到生成相关跳转指令的地方。但是，这样的做法要求在进行一趟处理，将标号和具体地址绑定起来。

本节介绍一种称为回填的补充性技术，它把一个由跳转指令组成的列表以综合属性的形式进行传递。明确的讲，生成一个跳转指令时暂时不指定该跳转指令的目标。这样的指令都被放入一个由跳转指令组成的列表中。等到能确定正确的目标标号时才去填充这些指令的目标标号。同一列表的所有跳转指令具有相同的目标标号。



### 7.7.1 回填技术目标代码生成

- `makelist(i)`：创建一个只包含i的列表；
- `merge(p1,p2)`：将列表p1和p2进行合并，返回合并后列表的指针；
- `backpatch(p,i)`：将i作为目标标号插入p列表中；



### 7.7.2 布尔表达式的回填

> Backpatching for Bolean Expressions

布尔表达式文法如下：
$$
\begin{aligned}
&B\rightarrow B\Vert M B\ |\ B\&\&MB\ |\ !B\ |\ (B)\ |\ E\ \mathbf{rel}\ E\ |\ \mathbf{true}\ |\ \mathbf{false}\\
&M\rightarrow\varepsilon
\end{aligned}
$$
翻译方案如下：

<img src="image/image-20230129210758907.png" alt="image-20230129210758907" style="zoom:33%;" />

### 7.7.3 控制转移语句

> Flow-of-Control Statements

控制流语句文法如下：
$$
\begin{aligned}
&S\rightarrow \mathbf{if}(B)MS\ |\ \mathbf{if}(B)MSN\ \mathbf{else}\ MS\ |\ \mathbf{while}M(B)MS\ |\ \{L\}\ |\ A\\
&M\rightarrow \varepsilon\\
&N\rightarrow\varepsilon\\
&L\rightarrow LMS\ |\ S
\end{aligned}
$$

- S表示一个语句；
- L是一个语句的列表；
- A是一个赋值语句；
- B是一个布尔表达式；

翻译方案如下：

<img src="image/image-20230129213719617.png" alt="image-20230129213719617" style="zoom: 50%;" />



