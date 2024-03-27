# 4 Nginx配置简述

## 4.1 环境配置

用vim打开`/etc/profile`文件：

```shell
vim /etc/profile
```

在文件末尾添加：

```bash
export PATH=$PATH:/usr/local/nginx/sbin
```

保存退出，刷新：

```shell
source /etc/profile
```

## 4.2 命令行参数

启动Nginx：

```shell
> nginx
```

命令语法格式：

```shell
nginx [-?hvVtTq] [-s signal] [-c filename] [-p prefix] [-g directives]
```

- -v 参数：显示 Nginx 执行文件的版本信息；
- -V 参数：显示 Nginx 执行文件的版本信息和编译配置参数；
- -t 参数：进行配置文件语法检查，测试配置文件的有效性；
- -T 参数：进行配置文件语法检查，测试配置文件的有效性，同时输出所有有效配置内容；
- -q 参数：在测试配置文件有效性时，不输出非错误信息；
- -s 参数：发送信号给 Nginx 主进程，信号可以为以下 4 个；
  - stop：快速关闭；
  - quit：正常关闭；
  - reopen：重新打开日志文件；
  - reload：重新加载配置文件，启动一个加载新配置文件的 Worker Process，正常关闭一个加载旧配置文件的 Worker Process；
- -p 参数：指定Nginx的执行目录，默认为configure时的安装目录，通常为`/usr/local/nginx`；
- -c 参数：指定`nginx.conf`文件的位置，默认为`conf/nginx.conf`；
- -g 参数：外部指定配置文件中的全局指令；

示例如下：

```bash
nginx -t               # 执行配置文件检测
nginx -t -q            # 执行配置文件检测，且只输出错误信息
nginx -s stop          # 快速停止Nginx
nginx -s quit          # 正常关闭Nginx
nginx -s reopen        # 重新打开日志文件
nginx -s reload        # 重新加载配置文件
nginx -p /usr/local/newnginx            # 指定Nginx的执行目录
nginx -c /etc/nginx/nginx.conf          # 指定nginx.conf文件的位置
# 外部指定pid和worker_processes配置指令参数
nginx -g "pid /var/run/nginx.pid; worker_processes 'sysctl -n hw.ncpu';"
```

