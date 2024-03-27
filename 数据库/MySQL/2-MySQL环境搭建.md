# 2 MySQL环境搭建

## 2.1 MySQL下载、安装、配置、卸载

这里不再描述，网上有很多教程😀



## 2.2 MySQL登录

### 2.2.1 服务的启动和停止

用`管理员身份`打开windows命令行：

```shell
# 启动MySQL
net start MySQL服务名

# 停止MySQL
net stop MySQL服务名
```

### 2.2.2 登陆与退出

用`管理员身份`打开windows命令行：

```shell
mysql -h 主机名 -P 端口号 -u 用户名 -p密码
```

**`-p`与密码之间==不能有空格==**

**退出登录：**

```shell
exit
或
quit
```



