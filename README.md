分布式票务系统（对标 12306）
高并发购票｜正确性与体验并重｜工程化落地
Java 17 · Spring Cloud Alibaba · Nacos/Seata · Redis · Gateway · Vue3 + AntD


## 分布式票务系统（对标12306）

一套面向“高并发购票场景”的微服务项目，完整覆盖购票核心链路与工程化实践。目标：在高并发下同时保证可用性、正确性与良好体验。

- 模块与端口
  - gateway 网关: 8000
  - member 会员服务: 8001
  - business 业务服务: 8002
  - batch 调度任务: 8003
  - web 会员前端: 9000
  - admin 控台前端: 9001
- 服务治理与配置：Spring Cloud Alibaba + Nacos（namespace: train）
- 通信/网关：OpenFeign、Spring Cloud Gateway
### 架构示意
```mermaid
graph TD
  subgraph Client[前端]
    Web[Web 会员端 (9000)]
    Admin[Admin 控台 (9001)]
  end
  Web -->|HTTP| Gateway[API Gateway (8000)]
  Admin -->|HTTP| Gateway

  Gateway --> Member[Member 服务 (8001)]
  Gateway --> Business[Business 服务 (8002)]
  Gateway --> Batch[Batch 服务 (8003)]

  subgraph Infra[基础设施]
    Nacos[Nacos 注册/配置 (namespace: train)]
    Redis[(Redis 缓存)]
    MySQL[(MySQL 数据库)]
    Sentinel[Sentinel Dashboard]
    Seata[Seata Server]
    RocketMQ[RocketMQ]
  end

  Member --> MySQL
  Business --> MySQL
  Batch --> MySQL

  Member --> Redis
  Business --> Redis
  Batch --> Redis

  Member -. 注册/配置 .-> Nacos
  Business -. 注册/配置 .-> Nacos
  Batch -. 注册/配置 .-> Nacos
  Gateway -. 服务发现 .-> Nacos

  Business -. 限流 .-> Sentinel
  Batch -. 限流 .-> Sentinel

  Business -. 事务协调 .-> Seata
  Member -. 事务协调 .-> Seata
  Batch -. 事务协调 .-> Seata

  Business -. 异步 .-> RocketMQ
  RocketMQ -. 消费 .-> Batch
```

---


- 存储与缓存：MyBatis + MySQL、Redis
- 稳定性增强（可选）：Sentinel（规则可持久化到 Nacos）
- 分布式事务：Seata（Nacos 配置模式）
- 消息（可选）：RocketMQ

---

### 核心亮点（HR 一眼可见）
- 高并发防护与提速
  - CDN 加速静态资源
  - 分布式缓存，覆盖缓存击穿/穿透/雪崩场景
  - 前后端“双层验证码”削峰与防机器人
  - 限流 + 快速失败，降低无效请求占用
  - 令牌发放 + 令牌大闸（代码已实现，默认关闭，可按需开启）
### 快速开始（超短版 · 复制即跑）
- 启动中间件：Nacos(namespace=train)、Seata、MySQL、Redis
- 后端（四个终端）：
```
mvn -pl member -am spring-boot:run
mvn -pl business -am spring-boot:run
mvn -pl batch -am spring-boot:run
mvn -pl gateway -am spring-boot:run
```
- 前端：
```
cd web && npm install && npm run web-dev
```
- 访问入口：
  - 会员前端：http://localhost:9000
  - 网关入口：http://localhost:8000

> 完整指引与 Nacos 配置样例见下文“快速开始（标准模式）”。

---


- 正确性保障
  - 分布式锁防止超卖，只售真实可用库存
  - 异步削峰 + 排队机制，快速反馈“无票/排队中”
  - 分布式事务：集成 Seata（可开启全局事务）；默认以异步 + 分布式锁保障一致性
  - 工程与协作：三端分离（web/admin/服务），职责清晰，资源隔离
  - 自制代码生成器：单表 CRUD（含前端页面）可分钟级生成，提升迭代效率


---

### 技术栈
- 后端：Java 17、Spring Boot 3、Spring Cloud Gateway、Spring Cloud Alibaba（Nacos/Sentinel）、OpenFeign、MyBatis、Redis、Seata
- 前端：Vue 3、Ant Design Vue、Vue Router、Vuex、Axios
### 环境参数（简）
- JDK 17、Spring Boot 3.0.x、Spring Cloud Alibaba 2022.x（Nacos/Sentinel/Feign/Gateway）
- MySQL 8、Redis 6、Seata 1.5+

---


- 构建：Maven、Node.js（建议 16/18 LTS）

---

### 快速开始（标准模式：Nacos/Seata）- Windows/PowerShell
前置：JDK 17+、Maven 3.8+、Node 16/18、MySQL 8+、Redis 6+、Nacos 2.x、Seata 1.5+

1) 启动 Nacos（命名空间使用 train）
- PowerShell 示例（按你的安装路径）：
```
Set-Location "D:\middleware\nacos\bin"
./startup.cmd -m standalone
```
- 控制台：http://127.0.0.1:8848

2) 启动 Seata（接入 Nacos 配置）
- 请按你的安装方式启动 Seata，并确保已注册/读取到 Nacos 的命名空间 train。

3) 在 Nacos 中创建各服务 dev 配置（DataId 为 yml）
- 命名空间：train
- DataId：member-dev.yml、business-dev.yml、batch-dev.yml（建议 Group: DEFAULT_GROUP）
- 示例（请替换为你的本地/测试环境）：
```
spring:
  datasource:
    url: jdbc:mysql://<mysql-host>:3306/<db-name>?characterEncoding=UTF8&serverTimezone=Asia/Shanghai
    username: <user>
    password: <password>
  data:
    redis:
      host: <redis-host>
      port: 6379
      password: <redis-pass>
```

4) 启动后端服务（建议顺序：member -> business -> batch -> gateway）
- 在项目根目录分别执行（每个服务一个终端）：
```
mvn -pl member -am spring-boot:run
mvn -pl business -am spring-boot:run
mvn -pl batch -am spring-boot:run
mvn -pl gateway -am spring-boot:run
```

5) 启动前端
- 会员端（web，端口 9000）：
```
cd web
npm install
npm run web-dev
```
- 控台端（admin，端口 9001）：
```
cd admin
npm install
npm run admin-dev
```

6) 访问入口
- 会员前端：http://localhost:9000
- 网关（统一后端入口）：http://localhost:8000

> 说明：Sentinel/RocketMQ 为可选组件，不影响最小可运行路径。

---

### 目录结构（简化）
- common：通用模块/工具
- member：会员域服务（8001）
- business：购票与业务编排（8002）
- batch：调度/异步任务（8003）
- gateway：API 网关（8000）
- web：会员前端（Vue3 + AntD，9000）
- admin：控台前端（Vue3 + AntD，9001）
- generator：MyBatis 代码生成

---

### 截图（建议放 2–3 张）
请在 docs/screenshots 下添加图片，并在此引用：
- docs/screenshots/01-登录-注册.png
- docs/screenshots/02-查询余票-选座.png
- docs/screenshots/03-我的车票-座位图.png

---

### License
MIT License

