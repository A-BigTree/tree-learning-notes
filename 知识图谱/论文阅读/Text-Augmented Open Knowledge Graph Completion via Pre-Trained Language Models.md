# Text-Augmented Open Knowledge Graph Completion via Pre-Trained Language Models

> 通过预先训练的语言模型完成文本增强开放知识图谱

[toc]

---

## 摘要

现有知识图谱补全方法：

- 现有事实三元组扩大图推理空间；
- 手动设计提示从PLM中提取知识；

> 缺点：性能有限，需要专家知识

文章提出TagReal方法，自定生成高质量查询提示，从大型文本语料库中检索**支持信息**，在从PLM中探索知识。



## 1 介绍

<img src="./Text-Augmented%20Open%20Knowledge%20Graph%20Completion%20via%20Pre-Trained%20Language%20Models.assets/image-20231204141302631.png" alt="image-20231204141302631" style="zoom:50%;" />

知识图谱补全：

- 大多数大规模KG不完整；
- KG补全：在给定实体和关系的情况下找到一个或多个对象实体；

现有知识图谱补全方法限制：

- 在丰富结构信息的密集图上表现良好，反之表现不佳；
- 只假设在封闭世界的KG上，不考虑大量的开放知识；

现有PLM-KG知识补全方法：

- 手动设计提示：代价高昂且质量有限；

TagReal模型：

- 端到端框架联合利用plm中的隐性知识和语料库中的文本信息来执行知识图谱补全；
- 开发提示自动生成和信息检索的方法；



## 2 相关工作

### 2.1 KG补全方法

基于嵌入的方法：

- 将实体和关系表示为嵌入向量；
- TransE、DistMult、RotatE；

基于PLM的方法：

- 用提示和支持信息作为PLM输入完成知识图谱补全；

TagReal模型KG补全方法：

- 不需要任何领域专家知识的情况下自动生成更高质量的提示；
- 采用信息检索的方法从语料库中搜索相关的文本信息，而不是预先假设支持信息的存在；

### 2.2 利用提示探索知识

- LAMA：用占位符和对象未填充空间来构成提示；
- BertNet：利用GPT-3自动生成带有权重的提示集合；

### 2.3 提示挖掘方法

- MINE：在大型文本语料库(例如Wikipedia)中搜索给定输入和输出之间的中间词或依赖路径；通过加权提示个体在PLM上的表现来优化挖掘提示的集合；
- MetaPAD：通过带有模式质量函数的上下文感知分割生成高质量的元模式；
- TruePIE：提出了模式嵌入和自我训练框架的概念，可以自动发现正确提示模式；



## 3 方法论

<img src="./Text-Augmented%20Open%20Knowledge%20Graph%20Completion%20via%20Pre-Trained%20Language%20Models.assets/image-20231204145029081.png" alt="image-20231204145029081" style="zoom:50%;" />

> 红色为输入、绿色为输出

### 3.1 任务符号化

KG补全：向已知三元组集合中添加新的三元组

- 分类任务：三元组$(h,r,t)$是否属于KG；
- 链接预测：查询尾部实体$(h,r,?)$或者查询头部实体$(?,r,t)$；

### 3.2 提示生成

将KG中的三元组作为唯一的输入，使用文本模式挖掘方法从大型语料库中挖掘质量模式，适用原因：

- 类似的数据源；
- 类似的目标；
- 类似的性能标准；

<img src="./Text-Augmented%20Open%20Knowledge%20Graph%20Completion%20via%20Pre-Trained%20Language%20Models.assets/image-20231204151449028.png" alt="image-20231204151449028" style="zoom: 50%;" />



#### 3.2.1 子语料库挖掘

- 对于给定KG的关系集$R=(r_1,r_2,...)$，对于每个关系的所有元组$t_j$从大型语料库和其他可靠来源中搜索包含头尾的句子$s_{t_j}$（固定数量），将这些句子添加到子语料库$C_{r_i}$中；

#### 3.2.2 短语分割和频繁模式挖掘

- 使用AutoPhrase进行语料库分割，使用FP-Growth算法挖掘频繁的提示模式；

> AutoPhrase:
>
> - 
>
> TP-Growth:
>
> - 

#### 3.2.3 提示选择

- MetaPAD应用模式质量函数：
  - 频率和一致性；
  - 信息性->p1；
  - 完整性->p{m-2}；
  - 覆盖范围->p4；
- 对MetaPAD选择的提示应用TruePIE：过滤与正样本低余弦相似度的提示 $\rightarrow p_3,p_{m-1}$；

打分方式：
$$
P(y|x,r_i)=\sum^n_{j=1}w_{i,j}P_{LM}(y|x,p_{i,j})
$$

- 提示的权重由PLM优化给出

> MetaPAD:
>
> - 
>
> TruePTE:



### 3.3 支持信息的检索

<img src="./Text-Augmented%20Open%20Knowledge%20Graph%20Completion%20via%20Pre-Trained%20Language%20Models.assets/image-20231204160453070.png" alt="image-20231204160453070" style="zoom:67%;" />

 