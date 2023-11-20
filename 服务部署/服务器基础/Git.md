# Git

[toc]

---

# 1 什么是版本控制

## 1.1 为什么需要版本控制

一个人无法完成一个庞大项目的开发，而多人协作开发时需要使用相应的辅助工具。

<img src="./Git.assets/image-20230611205447368.png" alt="image-20230611205447368" style="zoom:50%;" />

## 1.2 版本控制工具

- 集中式版本控制工具
  - CVS、**SVN(Subversion)**、VSS……
- 分布式版本控制工具
  - **Git**、Mercurial、Bazaar、Darcs……



# 2 Git概述

## 2.1 简介

Git官网的网址是：https://git-scm.com/，Git的Logo中特意凸显了**分支**功能，说明这是Git官方认为的Git最大特色。而我们在实际使用中也确实能够体会到Git的分支功能确实如丝般顺滑，非常好用。



## 2.2 Git简史

<img src="./Git.assets/image-20230611210118889.png" alt="image-20230611210118889" style="zoom:50%;" />

## 2.3 Git工作机制

### 2.3.1 本地库工作机制

Git使用本地库在我们本地的电脑上就可以记录版本信息，不需要联网。

<img src="./Git.assets/image-20230611210503180.png" alt="image-20230611210503180" style="zoom:50%;" />

### 2.3.2 代码托管中心

代码托管中心负责维护**远程库**，让团队成员可以彼此协作。

- 局域网

  - Gitlab：如果有特殊需求不能使用外网的代码托管中心，可以在局域网内搭建自己的代码托管中心服务器。

- Internet

  - GitHub：国外网站，有非常多优秀的开源项目托管代码，但是从国内访问很慢。

  - 码云：国内的代码托管中心，在国内互联网开发圈子中的地位举足轻重。

### 2.3.3 远程库工作机制

#### 团队内协作

<img src="./Git.assets/image-20230611210727902.png" alt="image-20230611210727902" style="zoom:50%;" />

#### 跨团队协作

<img src="./Git.assets/image-20230611210802356.png" alt="image-20230611210802356" style="zoom:50%;" />

# 3 命令行本地操作

## 3.1 初始化本地库并设置签名

### 3.1.1 初始化本地库

创建一个专门的目录，使用`git init`命令初始化为本地库，后面我们的版本控制操作都是在这个目录下进行。

```bash
git init
```

### 3.1.2 设置用户签名

签名的作用是区分不同操作者身份。用户的签名信息在每一个版本的提交信息中能够看到，以此确认本次提交是谁做的。

==注意：这里设置用户签名和将来登录GitHub（或其他代码托管中心）的账号没有任何关系。==

#### 设置用户名

```bash
git config --global user.name [用户名]
```

#### 设置用户邮箱

```bash
git config --global user.email [用户邮箱]
```

## 3.2 基础版本控制操作

### 3.2.1 创建文件并进行版本控制

#### 新建文件

在初始化好本地库的这个目录中随便创建一个文本文件即可。

#### 查看本地库状态

<img src="./Git.assets/image-20230617120204024.png" alt="image-20230617120204024" style="zoom:50%;" />

#### 追加文件并添加到暂存区

`git add <文件名>`命令有两个作用：

- 对“未追踪”的文件进行追踪，也就是加入版本控制体系，被Git管理；
- 将工作区的变动（新增和修改）添加到暂存；

<img src="./Git.assets/image-20230617120524484.png" alt="image-20230617120524484" style="zoom:50%;" />

#### 将暂存区中的修改提交到本地库

<img src="./Git.assets/image-20230617120720001.png" alt="image-20230617120720001" style="zoom:50%;" />

### 3.2.2 修改文件进行版本控制

#### 修改文件后查看本地库状态

<img src="./Git.assets/image-20230617120954399.png" alt="image-20230617120954399" style="zoom:50%;" />

#### 工作区文件修改后添加到暂存区

<img src="./Git.assets/image-20230617121214954.png" alt="image-20230617121214954" style="zoom:50%;" />

#### 后续操作

<img src="./Git.assets/image-20230617121656554.png" alt="image-20230617121656554" style="zoom:50%;" />

### 3.2.3 不add直接commit

两种情况：

- 新建的文件尚未纳入版本控制体系：必须先add纳入版本控制体系后才可以commit
- **已纳入版本控制体系的文件被修改：可以不add直接commit，Git自动执行了add**

### 3.2.4 版本穿梭

#### 查看版本记录

<img src="./Git.assets/image-20230617122442042.png" alt="image-20230617122442042" style="zoom:50%;" />

#### 切换到指定版本

<img src="./Git.assets/image-20230617123017605.png" alt="image-20230617123017605" style="zoom:50%;" />

#### 底层其实是移动HEAD指针

<img src="./Git.assets/image-20230617123132331.png" alt="image-20230617123132331" style="zoom:50%;" />

## 3.3 分支

### 3.3.1 什么是分支

在使用版本控制工具开发的过程中，同时推进多个任务

<img src="./Git.assets/image-20230618104342376.png" alt="image-20230618104342376" style="zoom:50%;" />

### 3.3.2 分支的好处

- 同时并行推进多个功能开发，提高开发效率
- 各个分支在开发过程中，如果某一个分支开发失败，不会对其他分支有任何影响。失败的分支删除重新开始即可。

### 3.3.3 分支的底层实现

Git的分支操作之所以能够非常平滑，就是因为创建分支时，Git底层并没有把本地库中的内容复制出来，而仅仅是创建新的指针。有新版本提交后移动指针。

<img src="./Git.assets/image-20230618104518066.png" alt="image-20230618104518066" style="zoom:50%;" />

master、hotfix其实都是指向具体版本记录的指针。当前所在的分支，其实是由HEAD决定的。所以创建分支的本质就是多创建一个指针。

- HEAD如果指向master，那么我们现在就在master分支上。
- HEAD如果指向hotfix，那么我们现在就在hotfix分支上。

所以切换分支的本质就是移动HEAD指针。

### 3.3.4 分支操作

#### 创建分支和切换分支

<img src="./Git.assets/image-20230618105032048.png" alt="image-20230618105032048" style="zoom:50%;" />

#### 在不同分支上修改

<img src="./Git.assets/image-20230618105921853.png" alt="image-20230618105921853" style="zoom:50%;" />

<img src="./Git.assets/image-20230618105942554.png" alt="image-20230618105942554" style="zoom:50%;" />

#### 分支合并

<img src="./Git.assets/image-20230618110041825.png" alt="image-20230618110041825" style="zoom:50%;" />

合并分支时一定是涉及到两个分支。这两个分支一个是“当前所在分支”，一个是“目标分支”；

命令写法：`git merge 目标分支`；

所以分支合并命令的本质就是把**<u>“目标分支”合并到“当前分支”</u>**；

> 例如：把hotfix合并到master git merge hotfix 需要确保当前所在的分支是master
>
> 例如：把master合并到hotfix git merge master 需要确保当前所在的分支是hotfix

### 3.3.5 冲突

分支合并时，如果**同一个文件**的**同一个位置**有不同内容就会产生冲突。

#### 冲突的表现

<img src="./Git.assets/image-20230618112122324.png" alt="image-20230618112122324" style="zoom:50%;" />

Git使用“`<<<<<<<`、`=========`、`>>>>>>>>>>`”符号帮我们标记出来，现在产生冲突的内容

```html
<<<<<<< HEAD
Hello Git!I am very happy! &&&&&&&&&&&&
Hello Git!I am very happy!
=======
表示HEAD指针指向的位置（其实就是当前分支）在冲突中的内容
```

```html
=======
Hello Git!I am very happy!
Hello Git!I am very happy! ************
>>>>>>> hotfix
表示hotfix指针指向的位置在冲突中的内容
```

所以所谓冲突其实就是让我们在这二者中选择一个，Git无法替我们决定使用哪一个。必须人为决定新代码内容。

此时使用git status命令查看本地库状态

<img src="./Git.assets/image-20230618112631936.png" alt="image-20230618112631936" style="zoom:50%;" />

#### 冲突解决

1. 编辑有冲突的文件，删除特殊符号，决定要使用的内容；
2. 添加到暂存区：`git add <冲突文件>`；
3. 执行提交（注意：使用`git commit`命令时不能带文件名）；



# 4 命令行远程库操作



