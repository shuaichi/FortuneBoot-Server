
<p align="center">
      <img src="https://img.shields.io/badge/Release-V1.0.8-green.svg" alt="Downloads">
      <img src="https://img.shields.io/badge/JDK-21+-green.svg" alt="Build Status">
  <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/Spring%20Boot-3.3-blue.svg" alt="Downloads">
   <a target="_blank" href="https://bladex.vip">
   <img src="https://img.shields.io/badge/Author-弛神降临-ff69b4.svg" alt="Downloads">
 </a>
 <a target="_blank" href="https://bladex.vip">
   <img src="https://img.shields.io/badge/Copyright%20-@FortuneBoot-%23ff3f59.svg" alt="Downloads">
 </a>
 </p>  
<p align="center">

[//]: # (暂时注释logo)
[//]: # (<img alt="logo" height="200" src="">)
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">FortuneBoot V1.0.8 </h1>
<h4 align="center">基于AgileBoot开发的记账管理软件</h4>
<p align="center">
</p>

## ⚡平台简介⚡

FortuneBoot是一套开源的记账管理系统，毫无保留给个人及企业免费使用。本项目的目标是做一款精简可靠，代码风格优良，项目规范的财务中心。
适合个人、家庭及开店使用。也可作为供初学者学习使用的案例。

* 前端是基于优秀的开源项目[AgileBoot](https://github.com/valarchie/AgileBoot-Front-End)与[Pure-Admin](https://github.com/pure-admin/vue-pure-admin)开发而成。在此感谢 AgileBoot 和 Pure-Admin 作者。
* 后端是基于优秀的开源项目[AgileBoot](https://github.com/valarchie/AgileBoot-Back-End)开发而成。再次敢接AgileBoot作者。
* 记账思路是基于优秀开源项目[monenote](https://github.com/getmoneynote/moneynote-api)提供，在此感谢moneynote作者。
* ***有大量的单元测试，集成测试覆盖确保业务逻辑正确***。

> 有任何问题或者建议，可以在 _Issues_ 中提给作者。
>
> 您的Issue比Star更重要
>
> 如果觉得项目对您有帮助，可以来个Star ⭐


## 💥 在线体验 💥
演示地址：
- http://fortuneboot.com
- http://fortuneboot.cn
>  账号密码：admin/admin123


## 🌴 项目背景 🌴
日常消费后都有记账，每月复盘的习惯，之前用鲨鱼记账，由于记账软件有广告，且数据不在自己手里，所以本地化部署了9块记账。
但9块记账少一些我需求的功能，故而打算自己开发，又由于9块记账使用的框架不是我熟悉的框架，且代码风格和我的不一样，所以自己开发。

## ✨ 使用 ✨


### 开发环境

- JDK
- Mysql
- Redis
- Node.js

### 技术栈

| 技术             | 说明              | 版本                |
|----------------|-----------------|-------------------|
| `springboot`   | Java项目必备框架      | 3.3.6             |
| `druid`        | alibaba数据库连接池   | 1.2.21            |
| `springdoc`    | 文档生成            | 3.0.0             |
| `mybatis-plus` | 数据库框架           | 3.5.6             |
| `hutool`       | 国产工具包（简单易用）     | 5.8.27            |
| `mockito`      | 单元测试模拟          | 1.10.19           |
| `guava`        | 谷歌工具包（提供简易缓存实现） | 33.2.0-jre        |
| `junit`        | 单元测试            | 4.13.2            |
| `h2`           | 内存数据库           | 2.2.224           |
| `jackson`      | 比较安全的Json框架     | follow springboot |
| `Spring Task`  | 定时任务框架（适合小型项目）  | follow springboot |


### 启动说明

#### 前置准备： 下载前后端代码

```

```

#### 安装好Mysql和Redis


#### 后端启动
```
1. 生成所需的数据库表
找到后端项目根目录下的sql目录中的agileboot_xxxxx.sql脚本文件(取最新的sql文件)。 导入到你新建的数据库中。

2. 在admin模块底下，找到resource目录下的application-dev.yml文件
配置数据库以及Redis的 地址、端口、账号密码

3. 在根目录执行mvn install

4. 找到agileboot-admin模块中的AgileBootAdminApplication启动类，直接启动即可

5. 当出现以下字样即为启动成功
  ____   _                _                                                           __         _  _ 
 / ___| | |_  __ _  _ __ | |_   _   _  _ __    ___  _   _   ___  ___  ___  ___  ___  / _| _   _ | || |
 \___ \ | __|/ _` || '__|| __| | | | || '_ \  / __|| | | | / __|/ __|/ _ \/ __|/ __|| |_ | | | || || |
  ___) || |_| (_| || |   | |_  | |_| || |_) | \__ \| |_| || (__| (__|  __/\__ \\__ \|  _|| |_| || ||_|
 |____/  \__|\__,_||_|    \__|  \__,_|| .__/  |___/ \__,_| \___|\___|\___||___/|___/|_|   \__,_||_|(_)
                                      |_|                             

```

#### 前端启动
详细步骤请查看对应前端部分

```
1. pnpm install

2. pnpm run dev

3. 当出现以下字样时即为启动成功

vite v2.6.14 dev server running at:

> Local: http://127.0.0.1:80/

ready in 4376ms.

```


> 对于想要尝试全栈项目的前端人员，这边提供更简便的后端启动方式，无需配置Mysql和Redis直接启动
#### 无Mysql/Redis 后端启动
```
1. 找到agilboot-admin模块下的resource文件中的application.yml文件

2. 配置以下两个值
spring.profiles.active: basic,dev
改为
spring.profiles.active: basic,test

fortuneboot.embedded.mysql: false
fortuneboot.embedded.redis: false
改为
fortuneboot.embedded.mysql: true
fortuneboot.embedded.redis: true

请注意:高版本的MacOS系统，无法启动内置的Redis


3. fortuneboot-main模块中的FortuneBootApplication启动类，直接启动即可
```


## 🙊 系统内置功能 🙊
```
额外新增的功能，我们使用 🚀 标记。
重新实现的功能，我们使用 ⭐️ 标记。
```

|     | 功能         | 描述                                                                                 |
| --- | ------------ | ------------------------------------------------------------------------------------ |
|     | 用户管理     | 用户是系统操作者，该功能主要完成系统用户配置                                         |
|     | 菜单管理     | 配置系统菜单、操作权限、按钮权限标识等，本地缓存提供性能                             |
|     | 角色管理     | 角色菜单权限分配、设置角色按机构进行数据范围权限划分                                 |
|     | 参数管理     | 对系统动态配置常用参数                                                               |
|     | 通知公告     | 系统通知公告信息发布维护                                                             |
|     | 操作日志     | 系统正常操作日志记录和查询；系统异常信息日志记录和查询                               |
|     | 登录日志     | 系统登录日志记录查询包含登录异常                                                     |
|     | 在线用户     | 当前系统中活跃用户状态监控                                                           |
|     | 系统接口     | 根据业务代码自动生成相关的 api 接口文档                                              |
|     | 服务监控     | 监视当前系统 CPU、内存、磁盘、堆栈等相关信息                                         |
|     | 缓存监控     | 对系统的缓存信息查询，命令统计等                                                     |
|     | 连接池监视   | 监视当前系统数据库连接池状态，可进行分析 SQL 找出系统性能瓶颈                        |
| 🚀  | 分组管理     | 记账的一个单位组织，一个组下面包含记账的用户，账户，账本                             |
| 🚀  | 账户管理     | 账户在现实生活中代表我们存钱的地方                                                   |
| 🚀  | 账本管理     | 账本跟生活中的账簿类似,一个账本下面包含分类、标签、交易对象、账单                    |
| 🚀  | 分类管理     | 划分和归集交易，它代表了我们记账的整体框架，是统计报表最重要的维度，账单必须包含分类 |
| 🚀  | 标签管理     | 给账单加上特征，可以方便筛选出某一特征的账单                                         |
| 🚀  | 交易对象管理 | 映射现实生活中的收款或付款对象，例如：京东商城                                       |
| 🚀  | 账单管理     | 真实的交易账单                                                                       |


## 🐯 工程结构 🐯

``` 
fortuneboot

├── fortuneboot-common -- 精简基础工具模块
│
├── fortuneboot-rest -- http接口
│
├── fortuneboot-dao -- 数据库orm层
│
├── fortuneboot-infrastructure -- 基础设施模块（主要是配置和集成，不包含业务逻辑）
│
├── fortuneboot-support 业务逻辑
│
├── fortuneboot-main 程序启动入口
│
├── fortuneboot-domain -- 业务模块
├    ├── user -- 用户模块（举例）
├         ├── command -- 命令参数接收模型（命令）
├         ├── dto -- 返回数据类
├         ├── db -- DB操作类
├              ├── entity -- 实体类
├              ├── service -- DB Service
├              ├── mapper -- DB Dao
├         ├── model -- 领域模型类
├         ├── query -- 查询参数模型（查询）
│         ├────── UserApplicationService -- 应用服务（事务层，操作领域模型类完成业务逻辑）

```
--- 

## 🎅 技术文档 🎅
* 持续输出中



## 🌻 注意事项 🌻
- IDEA会自动将.properties文件的编码设置为ISO-8859-1,请在Settings > Editor > File Encodings > Properties Files > 设置为UTF-8
- 请导入统一的代码格式化模板（Google）: Settings > Editor > Code Style > Java > 设置按钮 > import schema > 选择项目根目录下的GoogleStyle.xml文件
- 如需要生成新的表，请使用CodeGenerator类进行生成。
    - 填入数据库地址，账号密码，库名。然后填入所需的表名执行代码即可。（大概看一下代码就知道怎么填啦）
    - 生成的类在infrastructure模块下的target/classes目录下
    - 不同的数据库keywordsHandler方法请填入对应不同数据库handler。（搜索keywordsHandler关键字）
- 注意：管理后台的后端启动类是FortuneBoot-main**main**FortuneBootApplication
- Swagger的API地址为 http://localhost:8080/v3/api-docs

## 🎬 FortuneBoot全栈交流群 🎬

QQ 群： [![加入QQ群](https://img.shields.io/badge/1009576058-blue.svg)](https://qm.qq.com/q/M2zyt7vxyG) 点击按钮入群。


如果觉得该项目对您有帮助，可以小额捐赠支持本项目演示网站服务器等费用~

[//]: # (捐赠图片)
[//]: # (<img alt="logo" height="200" src="">)
