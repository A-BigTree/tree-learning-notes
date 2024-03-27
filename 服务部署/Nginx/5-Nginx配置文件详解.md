# 5 Nginx配置文件详解

Nginx**<u>默认编译</u>**安装后，配置文件都会保存在`/usr/local/nginx/conf`目录下，在配置文件目录下，Nginx默认的主配置文件是 `nginx.conf`，这也是Nginx唯一的默认配置入口。

## 5.1 配置文件目录

Nginx配置文件在conf目录下，其默认目录结构如下：

```bash
conf/
    ├── fastcgi.conf
    ├── fastcgi.conf.default
    ├── fastcgi_params
    ├── fastcgi_params.default
    ├── koi-utf
    ├── koi-win
    ├── mime.types
    ├── mime.types.default
    ├── nginx.conf
    ├── nginx.conf.default
    ├── scgi_params
    ├── scgi_params.default
    ├── uwsgi_params
    ├── uwsgi_params.default
    └── win-utf
```

其中，以"`.default`"为扩展名的文件是Nginx配置文件的配置样例文件。各配置文件的说明如下：

- `fastcgi_params`：Nginx在配置FastCGI代理服务时会根据`fastcgi_params`文件的配置向FastCGI服务器传递变量，该配置文件现已由`fastcgi.conf`代替；
- `fastcgi.conf`：为了规范配置指令SCRIPT_FILENAME的用法，引入FastCGI变量传递配置；
- `mime.types`：MIME类型映射表，Nginx会根据服务端文件后缀名在映射关系中获取所属文件类型，将文件类型添加到HTTP消息头字段"`Content-Type`"中；
- `nginx.conf`：Nginx 默认的配置入口文件；
- `scgi_params`：Nginx 在配置SCGI代理服务时会根据`scgi_params`文件的配置向SCGI服务器传递变量；
- `uwsgi_params`：Nginx 在配置uWSGI代理服务时会根据`uwsgi_params`文件的配置向uWSGI服务器传递变量；
- `koi-utf`、`koi-win`、`win-utf`：这 3 个文件是KOI8-R编码转换的映射文件，因为Nginx的作者是俄罗斯人，在Unicode流行之前，KOI8-R 是使用最为广泛的俄语编码。

## 5.2 配置文件结构

### 5.2.1 配置指令

在配置文件中，由Nginx约定的内部固定字符串，Nginx官方文档中的英文单词为directive，本教程中则统一称为配置指令，简称指令。指令是Nginx中功能配置的最基本元素，Nginx的每个功能配置都是通过多个不同的指令组合来实现的；

### 5.2.2 配置指令值

每个配置指令都有对应的内容来表示该指令的控制参数，本教程中约定其对应的内容为配置指令值，简称指令值。指令值可以是字符串、数字或变量等多种类型；

### 5.2.3 配置指令语句

指令与指令值组合构成指令语句。一条指令语句可以包含多个配置指令值，在Nginx配置文件中，每条指令语句都要用`;`作为语句结束的标识符；

### 5.2.4 配置指令域

配置指令值有时会是由`{ }`括起来的指令语句集合，本教程中约定`{ }`括起来的部分为配置指令域，简称指令域。指令域既可以包含多个指令语句，也可以包含多个指令域；

### 5.2.5 配置全局域

配置文件`nginx.conf`中上层没有其他指令域的区域被称为配置全局域，简称全局域。

Nginx 的常见配置指令域如下表所示：

| 域名称   | 域类型 | 域说明                                                       |
| :------- | :----- | :----------------------------------------------------------- |
| main     | 全局域 | Nginx 的根级别指令区域。该区域的配置指令是全局有效的，该指令名为隐性显示，nginx.conf 的整个文件内容都写在该指令域中 |
| events   | 指令域 | Nginx 事件驱动相关的配置指令域                               |
| http     | 指令域 | Nginx HTTP 核心配置指令域，包含客户端完整 HTTP 请求过程中每个过程的处理方法的配置指令 |
| upstream | 指令域 | 用于定义被代理服务器组的指令区域，也称"上游服务器"           |
| server   | 指令域 | Nginx 用来定义服务 IP、绑定端口及服务相关的指令区域          |
| location | 指令域 | 对用户 URI 进行访问路由处理的指令区域                        |
| stream   | 指令域 | Nginx 对 TCP 协议实现代理的配置指令域                        |
| types    | 指令域 | 定义被请求文件扩展名与 MIME 类型映射表的指令区域             |
| if       | 指令域 | 按照选择条件判断为真时使用的配置指令域                       |

打开系统默认的`nginx.conf`文件，可以看到整个文件的结构如下：

```bash
#user  nobody;
worker_processes  1;                             # 只启动一个工作进程
events {
    worker_connections  1024;               # 每个工作进程的最大连接为1024
}
http {
    include       mime.types;                    # 引入MIME类型映射表文件
    default_type  application/octet-stream;   # 全局默认映射类型为application/octet-stream

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';
    #access_log  logs/access.log  main;
    sendfile        on;                             # 启用零复制机制
    keepalive_timeout  65;                  # 保持连接超时时间为65s
    server {
        listen       80;                              # 监听80端口的网络连接请求
        server_name  localhost;             # 虚拟主机名为localhost
        #charset koi8-r;
        #access_log  logs/host.access.log  main;
        location / {
            root   html;
            index  index.html index.htm;
        }
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
```

由上述配置文件可以看出，配置文件中的指令和指令值是以类似于`key-value`的形式书写的。写在配置文件全局域的指令是Nginx配置文件的核心指令，主要是对Nginx自身软件运行进行配置的指令。其中，`events`和`http`所包含的部分分别为事件指令域和`HTTP`指令域，指令域内的指令则明确约定了该区域内的指令的应用范围。

`server`指令域被包含于`http`指令域中，同时又包含了`location`指令域，各指令域中的共用范围逐层被上层指令域限定，可见各指令域匹配的顺序是由外到内的。`Nginx`的配置指令按照内部设定可以同时编写在不同指令域中，包含在最内层的指令将对外层同名指令进行指令值覆盖，并以最内层指令配置为最终生效配置。

编写Nginx配置文件时，为了便于维护，也会把一些指令或指令域写在外部文件中，再通过`include`指令引入`nginx.conf`主配置文件中。例如，配置文件中把写有types指令域的`mime.types`文件引用到`http`指令域中。此处使用的是`nginx.conf`文件的相对路径。

## 5.3 配置文件中的计量单位

在 Nginx 配置文件中有很多与容量、时间相关的指令值，Nginx 配置文件有如下规范。

1. 容量单位可以使用字节、千字节、兆字节或千兆字节，示例如下：

```bash
512
1k或1K
10m或10M
1g或10G
```

2. 时间的最小单位是毫秒，示例如下:

```bash
10ms  # 10毫秒
30s     # 30秒
2m      # 2分钟
5h      # 5小时
1h 30m  # 1小时30分
6d      # 6天
3w      # 3周
5M      # 5个月
2y      # 2年
```

## 5.4 配置文件中的哈希表

Nginx 使用哈希表加速对Nginx配置中常量的处理，如server中的主机名、types中的MIME类型映射表、请求头属性字段等数据集合。哈希表是通过关键码来快速访问常量对应值的数据存储结构，在通过哈希表获取数据的过程中，其内部实现通过相关函数将常量名转换为一个关键码来实现对应值的快速定位和读取。

由于数据的复杂性，会出现不同常量名转换的关键码是一样的情况，这就会导致读取对应值时发生冲突。为了解决这个问题，Nginx同时引入了哈希桶机制，就是把相同关键码的哈希键存在一个哈希桶定义的存储空间中，然后再进行二次计算来获取对应的值。

单个哈希桶的大小等于CPU缓存行大小的倍数。这样就可以通过减少内存访问的数量来加速在CPU中搜索哈希关键码的速度。如果哈希桶的大小等于CPU的缓存行的大小，在Nginx进行哈希关键码搜索期间，内存的访问次数最多是两次，一次是计算哈希桶的地址，另一次是在哈希桶内进行哈希关键码的搜索。

Linux系统下查看CPU缓存行的指令如下：

`cat /proc/cpuinfo |grep cache_alignment`

Nginx在每次启动或重新加载配置时会选择合适大小的最小初始化哈希表。哈希表的大小会随哈希桶数量的增加而不断调整，直到哈希桶总的大小达到哈希表设置的最大值。因此，在Nginx提示需要增加哈希表或哈希桶的大小时，要先调整哈希表的大小。

