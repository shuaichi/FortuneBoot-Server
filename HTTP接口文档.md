# FortuneBoot 后端 HTTP 接口文档

> 本文档由源码逐控制器扫描生成，覆盖 `fortuneboot-rest` 模块全部 25 个控制器接口。
> 内容包含：接口名称、HTTP 方法、完整 URL、入参（含来源与校验）、出参类型、权限约束、`@AccessLog` 业务类型等说明。
>
> **入参/出参对象的字段级明细见文末「附录：DTO 字段字典」**（按 common / fortune / system 三模块，逐类列出字段名、类型、是否必填、校验注解、含义），前端对接时可直接查阅。接口表中的入参/出参类名（如 `FortuneAccountAddCommand`、`FortuneBillVo`）均可在字典中找到对应字段表。

## 目录

### common（通用，3 个控制器）
- FileRest `/file` — 文件上传下载
- LoginRest（根路径）— 登录/注册/验证码/路由
- SpaForwardController — SPA 前端路由转发

### fortune（记账业务，14 个控制器）
- FortuneAccountRest `/fortune/account`
- FortuneBillRest `/fortune/bill`
- FortuneBookRest `/fortune/book/base`
- FortuneCategoryRest `/fortune/category`
- FortuneCurrencyRest `/fortune/currency`
- FortuneFileRest `/fortune/bill/file`
- FortuneFinanceOrderRest `/fortune/finance/order`
- FortuneGoodsKeeperRest `/fortune/goods/keeper`
- FortuneGroupRest `/fortune/group`
- FortuneIncludeRest `/fortune/include`
- FortuneMemberRest `/fortune/member`
- FortunePayeeRest `/fortune/payee`
- FortuneRecurringBillRest `/fortune/recurring/bill`
- FortuneTagRest `/fortune/tag`

### system（系统管理，8 个控制器）
- MonitorRest `/monitor`
- SysConfigRest `/system`
- SysLogsRest `/logs`
- SysMenuRest `/system/menus`
- SysNoticeRest `/system/notices`
- SysProfileRest `/system/user/profile`
- SysRoleRest `/system/role`
- SysUserRest `/system/users`

## 通用约定

- **响应包装**：绝大多数接口返回 `ResponseDTO<T>`，含 `code`/`msg`/`data`；文件流/Excel 导出接口直接写 `HttpServletResponse`。
- **分页**：`PageDTO<T>` 承载分页结果，Query 对象继承分页基类（page/pageSize）。
- **鉴权体系**：
  - 系统管理侧使用 `@permission.has('模块:资源:动作')` 功能权限。
  - 记账业务侧使用自定义 SpEL：`@fortune.groupVisitorPermission`（分组可见）、`groupActorPermission`（分组操作）、`groupOwnerPermission`（分组所有者）、`groupOwnerPermissionByRelationId`（按关系ID的所有者）、`bookVisitorPermission`（账本可见）、`bookActorPermission`（账本操作）、`billVisitorPermission`（账单可见）。
  - 标注"无"表示仅需登录态、无额外权限校验。
- **操作日志**：`@AccessLog(businessType=...)` 标注写操作的业务类型（ADD/MODIFY/DELETE/EXPORT/IMPORT/GRANT 等）。
- **HTTP 方法语义**：查询 GET、新增 POST、整体修改 PUT、状态切换/局部变更 PATCH、删除 DELETE。

---

# 一、common 通用模块


## 上传 API — `FileRest`

- **类型**：`@RestController`
- **基础路径**：`/file`
- **鉴权**：无自定义权限注解（受全局认证过滤器约束）

| 接口 | 方法 | URL | 入参 | 出参 | 约束/说明 |
|------|------|-----|------|------|-----------|
| 下载文件 | GET | `/file/download` | `String fileName`（Query），`HttpServletResponse`（框架注入） | `ResponseEntity<byte[]>`（文件字节流，`application/octet-stream`） | 需通过 `FileUploadUtils.isAllowDownload` 白名单校验；不允许下载时返回错误 JSON；异常时返回 null |
| 单个上传文件 | POST | `/file/upload` | `MultipartFile file`（multipart 表单） | `ResponseDTO<UploadDTO>` | file 为空抛 `UPLOAD_FILE_IS_EMPTY`；返回 url/fileName/newFileName/originalFilename |
| 多个上传文件 | POST | `/file/uploads` | `List<MultipartFile> files`（multipart 表单） | `ResponseDTO<List<UploadDTO>>` | files 为空抛 `UPLOAD_FILE_IS_EMPTY` |

> `UploadDTO` 字段：`url`（全路径）、`fileName`（相对路径）、`newFileName`（新文件名）、`originalFilename`（原始文件名）。
---

## 登录 API — `LoginRest`

- **类型**：`@RestController`
- **基础路径**：无（根路径 `/`）
- **鉴权**：多数为白名单接口（登录/验证码/注册等无需认证）；获取用户信息/路由需登录态

| 接口 | 方法 | URL | 入参 | 出参 | 约束/说明 |
|------|------|-----|------|------|-----------|
| 获取API版本号 | GET | `/getApiVersion` | 无 | `ResponseDTO<String>` | — |
| 获取ICP备案信息 | GET | `/getIcp` | 无 | `ResponseDTO<String>` | — |
| 首页 | GET | `/` | 无 | `ModelAndView`（forward:/index.html） | 转发到前端 index.html |
| 获取系统内置配置 | GET | `/getConfig` | 无 | `ResponseDTO<ConfigDTO>` | — |
| 获取RSA公钥 | GET | `/getRsaPublicKey` | 无 | `ResponseDTO<String>` | 用于登录密码加密 |
| 验证码 | GET | `/captchaImage` | 无 | `ResponseDTO<CaptchaDTO>` | 限流：@RateLimit 按 IP，10 秒内最多 10 次，内存缓存|
| 登录 | POST | `/login` | `LoginCommand`（@RequestBody） | `ResponseDTO<TokenDTO>` | 返回 token + 当前登录用户信息 |
| 获取当前登录用户信息 | GET | `/getLoginUserInfo` | 无 | `ResponseDTO<CurrentLoginUserDTO>` | 需登录态 |
| 获取用户菜单路由 | GET | `/getRouters` | 无 | `ResponseDTO<List<RouterDTO>>` | 动态生成路由，需登录态 |
| 检查用户名是否重复 | POST | `/{userName}/checkRepeat` | `String userName`（@PathVariable） | `ResponseDTO<Boolean>` | APP 注册时检查 |
| 获取允许注册的角色 | GET | `/getAllowRegisterRoles` | 无 | `ResponseDTO<List<RoleDTO>>` | 注册时必选 |
| 注册接口 | POST | `/register` | `AddUserCommand`（@RequestBody） | `ResponseDTO<Void>` | 受系统配置 REGISTER 开关控制，关闭时抛 `COMMON_UNSUPPORTED_OPERATION`；强制 source=REGISTER、status=ENABLE |
---

## SPA 路由转发 — `SpaForwardController`

- **类型**：`@Controller`（非 `@RestController`，唯一一个非 REST 控制器）
- **基础路径**：无
- **作用**：替代 nginx `try_files`，将非静态资源、非 API 的前端路由请求转发到 `index.html`，由 Vue Router 客户端处理

| 接口 | 方法 | URL | 入参 | 出参 | 约束/说明 |
|------|------|-----|------|------|-----------|
| 单级路由转发 | ALL（`@RequestMapping`） | `/{path:[^.]*}` | 路径变量（正则 `[^.]*` 排除带扩展名的静态资源） | `String`：`forward:/index.html` | 匹配如 `/dashboard`、`/login` |
| 多级路由转发 | ALL（`@RequestMapping`） | `/**/{path:[^.]*}` | 同上 | `String`：`forward:/index.html` | 匹配如 `/system/user/detail` |
---

# 二、fortune 记账业务模块

## 账户 API — `FortuneAccountRest`

- **类型**：`@RestController`
- **基础路径**：`/fortune/account`
- **鉴权**：`@fortune.groupVisitorPermission` / `groupActorPermission` / `bookActorPermission`

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询账户 | GET | `/fortune/account/getPage` | `FortuneAccountQuery`（@Valid，Query） | `ResponseDTO<PageDTO<FortuneAccountVo>>` | groupVisitor(#query.groupId) | — |
| 查询启用的账户 | GET | `/fortune/account/{groupId}/getEnableList` | `Long groupId`（@PathVariable，正数） | `ResponseDTO<List<FortuneAccountVo>>` | groupVisitor(#groupId) | — |
| 新增账户 | POST | `/fortune/account/add` | `FortuneAccountAddCommand`（@Valid @RequestBody） | `ResponseDTO<Void>` | groupActor(#addCommand.groupId) | @AccessLog ADD |
| 修改账户 | PUT | `/fortune/account/modify` | `FortuneAccountModifyCommand`（@Valid @RequestBody） | `ResponseDTO<Void>` | groupActor(#modifyCommand.groupId) | @AccessLog MODIFY |
| 余额调整 | PATCH | `/fortune/account/balanceAdjust` | `FortuneAccountAdjustCommand`（@Valid @RequestBody） | `ResponseDTO<Void>` | bookActor(#adjustCommand.bookId) | @AccessLog BALANCE_ADJUST |
| 移入回收站 | PATCH | `/fortune/account/{groupId}/{accountId}/moveToRecycleBin` | `Long groupId, Long accountId`（@PathVariable） | `ResponseDTO<Void>` | groupActor(#groupId) | @AccessLog |
| 删除账户 | DELETE | `/fortune/account/{groupId}/{accountId}/remove` | `Long groupId, Long accountId` | `ResponseDTO<Void>` | groupActor(#groupId) | @AccessLog |
| 移出回收站 | PATCH | `/fortune/account/{groupId}/{accountId}/putBack` | `Long groupId, Long accountId` | `ResponseDTO<Void>` | groupActor(#groupId) | @AccessLog |
| 可支出 | PATCH | `/fortune/account/{groupId}/{accountId}/canExpense` | `Long groupId, Long accountId` | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 不可支出 | PATCH | `/fortune/account/{groupId}/{accountId}/cannotExpense` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 可收入 | PATCH | `/fortune/account/{groupId}/{accountId}/canIncome` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 不可收入 | PATCH | `/fortune/account/{groupId}/{accountId}/cannotIncome` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 可转出 | PATCH | `/fortune/account/{groupId}/{accountId}/canTransferOut` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 不可转出 | PATCH | `/fortune/account/{groupId}/{accountId}/cannotTransferOut` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 可转入 | PATCH | `/fortune/account/{groupId}/{accountId}/canTransferIn` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 不可转入 | PATCH | `/fortune/account/{groupId}/{accountId}/cannotTransferIn` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 计入净资产 | PATCH | `/fortune/account/{groupId}/{accountId}/includeAccount` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 不计入净资产 | PATCH | `/fortune/account/{groupId}/{accountId}/excludeAccount` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 启用 | PATCH | `/fortune/account/{groupId}/{accountId}/enable` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
| 停用 | PATCH | `/fortune/account/{groupId}/{accountId}/disable` | 同上 | `ResponseDTO<Void>` | groupActor(#groupId) | — |
---

## 账单 API — `FortuneBillRest`

- **类型**：`@RestController`
- **基础路径**：`/fortune/bill`
- **鉴权**：`@fortune.bookVisitorPermission` / `bookActorPermission`

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询账单 | GET | `/fortune/bill/getPage` | `FortuneBillQuery`（@Valid，Query） | `ResponseDTO<PageDTO<FortuneBillVo>>` | bookVisitor(#query.bookId) | — |
| 新增账单 | POST | `/fortune/bill/add` | multipart：`data`=`FortuneBillAddCommand`（@Valid）、`files`=`List<MultipartFile>`（可选） | `ResponseDTO<Void>` | bookActor(#addCommand.bookId) | @AccessLog ADD；含附件上传 |
| 修改账单 | PUT | `/fortune/bill/modify` | multipart：`data`=`FortuneBillModifyCommand`（@Valid）、`files`（可选） | `ResponseDTO<Void>` | bookActor(#modifyCommand.bookId) | @AccessLog MODIFY |
| 删除账单 | DELETE | `/fortune/bill/{bookId}/{billId}/remove` | `Long bookId, Long billId`（@PathVariable，正数） | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog DELETE |
| 确认账单 | PATCH | `/fortune/bill/{bookId}/{billId}/confirm` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog OTHER |
| 取消确认账单 | PATCH | `/fortune/bill/{bookId}/{billId}/unConfirm` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog OTHER |
| 统计账单 | PATCH | `/fortune/bill/{bookId}/{billId}/include` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog INCLUDE |
| 取消统计账单 | PATCH | `/fortune/bill/{bookId}/{billId}/exclude` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog EXCLUDE |
| 账单列表导出 | GET | `/fortune/bill/excel` | `FortuneBillQuery`（@Valid），`HttpServletResponse` | 无（写入响应流，Excel） | bookVisitor(#query.bookId) | @AccessLog EXPORT；最大导出 10000 条 |
| 账单列表导入 | POST | `/fortune/bill/{bookId}/import` | `Long bookId`，multipart `file`=`MultipartFile` | `ResponseEntity`：成功→`ResponseDTO<FortuneBillImportResultVo>`；失败→错误 Excel 字节流 | bookActor(#bookId) | @AccessLog IMPORT |
| 账单导入模板下载 | GET | `/fortune/bill/{bookId}/excelTemplate` | `Long bookId` | `ResponseEntity<byte[]>`（Excel 模板） | bookVisitor(#bookId) | — |
---

## 账本 API — `FortuneBookRest`

- **类型**：`@RestController`
- **基础路径**：`/fortune/book/base`
- **鉴权**：`@fortune.groupVisitorPermission` / `groupActorPermission` / `bookVisitorPermission` / `bookActorPermission`

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询账本 | GET | `/fortune/book/base/getPage` | `FortuneBookQuery`（@Valid，Query） | `ResponseDTO<PageDTO<FortuneBookVo>>` | groupVisitor(#query.groupId) | — |
| 查询启用的账本 | GET | `/fortune/book/base/{groupId}/getEnableList` | `Long groupId`（@PathVariable，正数） | `ResponseDTO<List<FortuneBookVo>>` | groupVisitor(#groupId) | — |
| 根据账本id查询 | GET | `/fortune/book/base/{bookId}/getBookById` | `Long bookId` | `ResponseDTO<FortuneBookVo>` | bookVisitor(#bookId) | — |
| 新增账本 | POST | `/fortune/book/base/add` | `FortuneBookAddCommand`（@Valid @RequestBody） | `ResponseDTO<Void>` | groupActor(#bookAddCommand.groupId) | @AccessLog ADD |
| 修改账本 | PUT | `/fortune/book/base/modify` | `FortuneBookModifyCommand`（@Valid @RequestBody） | `ResponseDTO<Void>` | groupActor(#bookModifyCommand.groupId) | @AccessLog MODIFY |
| 删除账本 | DELETE | `/fortune/book/base/{bookId}/remove` | `Long bookId` | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog DELETE |
| 移到回收站 | PATCH | `/fortune/book/base/{bookId}/moveToRecycleBin` | `Long bookId` | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog |
| 移出回收站 | PATCH | `/fortune/book/base/{bookId}/putBack` | `Long bookId` | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog PUT_BACK |
| 设置为默认账本 | PATCH | `/fortune/book/base/{bookId}/setDefaultBook` | `Long bookId` | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog SET_DEFAULT |
| 通过分组id查询 | GET | `/fortune/book/base/{groupId}/getByGroupId` | `Long groupId` | `ResponseDTO<List<FortuneBookVo>>` | groupVisitor(#groupId) | — |
| 启用账本 | PATCH | `/fortune/book/base/{bookId}/enable` | `Long bookId` | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog ENABLE |
| 停用账本 | PATCH | `/fortune/book/base/{bookId}/disable` | `Long bookId` | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog DISABLE |
---

## 分类 API — `FortuneCategoryRest`

- **类型**：`@RestController`
- **基础路径**：`/fortune/category`
- **鉴权**：`@fortune.bookVisitorPermission` / `bookActorPermission`

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询分类 | GET | `/fortune/category/getPage` | `FortuneCategoryQuery`（@Valid，Query） | `ResponseDTO<PageDTO<FortuneCategoryVo>>` | bookVisitor(#query.bookId) | — |
| 查询分类（树形） | GET | `/fortune/category/getList` | `FortuneCategoryQuery`（@Valid） | `ResponseDTO<List<FortuneCategoryVo>>` | bookVisitor(#query.bookId) | TreeUtil 构建树 |
| 分页查询分类（平铺） | GET | `/fortune/category/getListPage` | `FortuneCategoryQuery`（@Valid） | `ResponseDTO<PageDTO<FortuneCategoryVo>>` | bookVisitor(#query.bookId) | — |
| 查询启用的分类 | GET | `/fortune/category/{bookId}/getEnableList` | `Long bookId`（@PathVariable），`Integer billType`（@RequestParam 可选） | `ResponseDTO<List<FortuneCategoryVo>>` | bookVisitor(#bookId) | 返回树形 |
| 新增分类 | POST | `/fortune/category/add` | `FortuneCategoryAddCommand`（@Valid @RequestBody） | `ResponseDTO<Void>` | bookActor(#addCommand.bookId) | @AccessLog ADD |
| 修改分类 | PUT | `/fortune/category/modify` | `FortuneCategoryModifyCommand`（@Valid @RequestBody） | `ResponseDTO<Void>` | bookActor(#modifyCommand.bookId) | @AccessLog MODIFY |
| 分类移入回收站 | PATCH | `/fortune/category/{bookId}/{categoryId}/moveToRecycleBin` | `Long bookId, Long categoryId`（@PathVariable，正数） | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog |
| 删除分类 | DELETE | `/fortune/category/{bookId}/{categoryId}/remove` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog DELETE |
| 分类移出回收站 | PATCH | `/fortune/category/{bookId}/{categoryId}/putBack` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog PUT_BACK |
| 启用分类 | PATCH | `/fortune/category/{bookId}/{categoryId}/enable` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog ENABLE |
| 停用分类 | PATCH | `/fortune/category/{bookId}/{categoryId}/disable` | 同上 | `ResponseDTO<Void>` | bookActor(#bookId) | @AccessLog DISABLE |
---

## 汇率 API — `FortuneCurrencyRest`

- **类型**：`@RestController`
- **基础路径**：`/fortune/currency`
- **鉴权**：无自定义权限注解

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 刷新汇率 | POST | `/fortune/currency/refresh` | 无 | `ResponseDTO<Void>` | 无 | 刷新汇率数据 |
| 查询汇率 | GET | `/fortune/currency` | `FortuneCurrencyQuery`（Query，含 base 基准币种） | `ResponseDTO<List<FortuneCurrencyVo>>` | 无 | 按 base 换算 |
---

## 账单附件 API — `FortuneFileRest`

- **类型**：`@RestController`
- **基础路径**：`/fortune/bill/file`
- **鉴权**：`@fortune.billVisitorPermission`

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 根据账单id查询文件 | GET | `/fortune/bill/file/{billId}/getByBillId` | `Long billId`（@PathVariable，正数） | `ResponseDTO<List<FortuneFileVo>>` | billVisitor(#billId) | — |
---

## 单据 API — `FortuneFinanceOrderRest`
- **类型**：@RestController
- **基础路径**：`/fortune/finance/order`
- **鉴权**：查询类 bookVisitorPermission，写操作 bookActorPermission

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询单据 | GET | `/fortune/finance/order/getPage` | FortuneFinanceOrderQuery（含 bookId） | ResponseDTO<PageDTO<FortuneFinanceOrderVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 查询使用中的单据 | GET | `/fortune/finance/order/{bookId}/getUsingFinanceOrderList` | @PathVariable bookId | ResponseDTO<List<FortuneFinanceOrderVo>> | bookVisitorPermission(#bookId) | - |
| 新增单据 | POST | `/fortune/finance/order/add` | @Valid FortuneFinanceOrderAddCommand（含 bookId） | ResponseDTO<Boolean> | bookActorPermission(#command.getBookId()) | @AccessLog ADD |
| 修改单据 | PUT | `/fortune/finance/order/modify` | @Valid FortuneFinanceOrderModifyCommand（含 bookId） | ResponseDTO<Boolean> | bookActorPermission(#command.getBookId()) | @AccessLog MODIFY |
| 删除单据 | DELETE | `/fortune/finance/order/{bookId}/{orderId}/remove` | @PathVariable bookId, orderId | ResponseDTO<Boolean> | bookActorPermission(#bookId) | @AccessLog MODIFY |
| 使用单据 | PATCH | `/fortune/finance/order/{bookId}/{orderId}/using` | @PathVariable bookId, orderId | ResponseDTO<Boolean> | bookActorPermission(#bookId) | @AccessLog USING_ORDER |
| 关闭单据 | PATCH | `/fortune/finance/order/{bookId}/{orderId}/close` | @PathVariable bookId, orderId | ResponseDTO<Boolean> | bookActorPermission(#bookId) | @AccessLog CLOSE_ORDER |
| 重开单据 | PATCH | `/fortune/finance/order/{bookId}/{orderId}/reopen` | @PathVariable bookId, orderId | ResponseDTO<Boolean> | bookActorPermission(#bookId) | @AccessLog REOPEN_ORDER |
---

## 归物 API — `FortuneGoodsKeeperRest`
- **类型**：@RestController
- **基础路径**：`/fortune/goods/keeper`
- **鉴权**：查询类 bookVisitorPermission，写操作 bookActorPermission

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询 | GET | `/fortune/goods/keeper/getPage` | FortuneGoodsKeeperQuery（含 bookId） | ResponseDTO<PageDTO<FortuneGoodsKeeperVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 归物统计 | GET | `/fortune/goods/keeper/{bookId}/getGoodsKeeperStatistics` | @PathVariable bookId | ResponseDTO<FortuneGoodsKeeperStatisticsVo> | bookVisitorPermission(#bookId) | - |
| 新增物品 | POST（multipart） | `/fortune/goods/keeper/add` | @RequestPart("data") @Valid FortuneGoodsKeeperAddCommand + @RequestPart("files", 可选) List<MultipartFile> | ResponseDTO<Void> | bookActorPermission(#addCommand.getBookId()) | @AccessLog ADD |
| 修改物品 | PUT（multipart） | `/fortune/goods/keeper/modify` | @RequestPart("data") @Valid FortuneGoodsKeeperModifyCommand + @RequestPart("files", 可选) List<MultipartFile> | ResponseDTO<Void> | bookActorPermission(#command.getBookId()) | @AccessLog MODIFY |
| 删除物品 | DELETE | `/fortune/goods/keeper/{bookId}/{keeperId}/remove` | @PathVariable bookId, keeperId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog DELETE |
---

## 分组 API — `FortuneGroupRest`
- **类型**：@RestController
- **基础路径**：`/fortune/group`
- **鉴权**：查询自身分组无权限；分组内操作 groupVisitor/groupActor；删除/邀请 groupOwner

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询我的分组 | GET | `/fortune/group/getPage` | @Valid FortuneGroupQuery | ResponseDTO<PageDTO<FortuneGroupVo>> | 无 | - |
| 查询我的启用分组 | GET | `/fortune/group/getEnableList` | 无 | ResponseDTO<List<FortuneGroupVo>> | 无 | - |
| 通过分组id查看分组 | GET | `/fortune/group/{groupId}/getByGroupId` | @PathVariable groupId | ResponseDTO<FortuneGroupVo> | groupVisitorPermission(#groupId) | - |
| 获取账本模板 | GET | `/fortune/group/getBookTemplate` | 无 | ResponseDTO<List<SelectOptionsVo>> | 无 | - |
| 通过id获取账本模板备注 | GET | `/fortune/group/{id}/getBookTemplateRemarkById` | @PathVariable id | ResponseDTO<String> | 无 | - |
| 获取货币模板 | GET | `/fortune/group/getCurrencyTemplate` | 无 | ResponseDTO<List<SelectOptionsVo>> | 无 | - |
| 查询用户的默认分组 | GET | `/fortune/group/getDefaultGroupId` | 无 | ResponseDTO<Long> | 无 | - |
| 新增分组 | POST | `/fortune/group/add` | @Valid @RequestBody FortuneGroupAddCommand | ResponseDTO<Void> | 无 | @AccessLog ADD |
| 修改分组 | PUT | `/fortune/group/modify` | @Valid @RequestBody FortuneGroupModifyCommand | ResponseDTO<Void> | groupActorPermission(#groupModifyCommand.groupId) | @AccessLog MODIFY |
| 删除分组 | DELETE | `/fortune/group/{groupId}/remove` | @PathVariable groupId | ResponseDTO<Void> | groupOwnerPermission(#groupId) | @AccessLog DELETE |
| 查询分组角色枚举 | GET | `/fortune/group/getRoleTypes` | 无 | ResponseDTO<Map<Integer,String>> | 无 | - |
| 邀请用户 | POST | `/fortune/group/inviteUser` | @RequestBody @Valid FortuneUserGroupRelationInviteCommand | ResponseDTO<Void> | groupOwnerPermission(#inviteCommand.getGroupId()) | @AccessLog GRANT |
| 删除用户 | DELETE | `/fortune/group/{relationId}/removeGroupUser` | @PathVariable relationId | ResponseDTO<Void> | groupOwnerPermissionByRelationId(#relationId) | @AccessLog DELETE |
| 查询分组用户 | GET | `/fortune/group/{groupId}/getGroupUser` | @PathVariable groupId | ResponseDTO<List<FortuneUserGroupRelationVo>> | groupVisitorPermission(#groupId) | - |
| 设置为默认分组 | PATCH | `/fortune/group/{groupId}/setDefaultGroup` | @PathVariable groupId | ResponseDTO<Void> | groupActorPermission(#groupId) | @AccessLog SET_DEFAULT |
| 启用分组 | PATCH | `/fortune/group/{groupId}/enable` | @PathVariable groupId | ResponseDTO<Void> | groupActorPermission(#groupId) | @AccessLog ENABLE |
| 停用分组 | PATCH | `/fortune/group/{groupId}/disable` | @PathVariable groupId | ResponseDTO<Void> | groupActorPermission(#groupId) | @AccessLog DISABLE |
---

## 统计 API — `FortuneIncludeRest`
- **类型**：@RestController
- **基础路径**：`/fortune/include`
- **鉴权**：账本维度 bookVisitorPermission，分组维度 groupVisitorPermission

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 统计支出收入 | GET | `/fortune/include/getBillStatistics` | FortuneBillQuery（含 bookId） | ResponseDTO<BillStatisticsVo> | bookVisitorPermission(#query.getBookId()) | - |
| 统计总资产 | GET | `/fortune/include/{groupId}/getTotalAssets` | @PathVariable groupId | ResponseDTO<List<FortunePieVo>> | groupVisitorPermission(#groupId) | - |
| 统计总负债 | GET | `/fortune/include/{groupId}/getTotalLiabilities` | @PathVariable groupId | ResponseDTO<List<FortunePieVo>> | groupVisitorPermission(#groupId) | - |
| 统计收入折线图 | GET | `/fortune/include/getIncomeTrends` | BillTrendsQuery（含 bookId，billType 强制 INCOME） | ResponseDTO<List<FortuneLineVo>> | bookVisitorPermission(#billTrendsQuery.getBookId()) | - |
| 统计支出折线图 | GET | `/fortune/include/getExpenseTrends` | BillTrendsQuery（含 bookId，billType 强制 EXPENSE） | ResponseDTO<List<FortuneLineVo>> | bookVisitorPermission(#billTrendsQuery.getBookId()) | - |
| 统计资产负债 | GET | `/fortune/include/{groupId}/getFortuneAssetsLiabilities` | @PathVariable groupId | ResponseDTO<FortuneAssetsLiabilitiesVo> | groupVisitorPermission(#groupId) | - |
| 统计支出分类 | GET | `/fortune/include/getCategoryExpense` | @Valid CategoryIncludeQuery（含 bookId） | ResponseDTO<List<FortunePieVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 统计收入分类 | GET | `/fortune/include/getCategoryIncome` | @Valid CategoryIncludeQuery（含 bookId） | ResponseDTO<List<FortunePieVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 统计支出标签 | GET | `/fortune/include/getTagExpense` | @Valid TagIncludeQuery（含 bookId） | ResponseDTO<List<FortuneBarVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 统计收入标签 | GET | `/fortune/include/getTagIncome` | @Valid TagIncludeQuery（含 bookId） | ResponseDTO<List<FortuneBarVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 统计支出交易对象 | GET | `/fortune/include/getPayeeExpense` | @Valid PayeeIncludeQuery（含 bookId） | ResponseDTO<List<FortunePieVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 统计收入交易对象 | GET | `/fortune/include/getPayeeIncome` | @Valid PayeeIncludeQuery（含 bookId） | ResponseDTO<List<FortunePieVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 获取首页金额显示设置 | GET | `/fortune/include/getDisplayConfig` | 无 | ResponseDTO<String> | 无 | - |
| 首页聚合看板 | GET | `/fortune/include/dashboard` | DashboardQuery（bookId、periodType、startDate、endDate） | ResponseDTO<DashboardVo> | bookVisitorPermission(#query.getBookId()) | groupId 从账本反查，避免跨组读取资产 |
| 查询日期统计 | GET | `/fortune/include/getDateInclude` | BillIncludeQuery（含 bookId、startDate、endDate） | ResponseDTO<List<FortuneLineVo>> | bookVisitorPermission(#query.getBookId()) | 每日支出，服务层补零 |
| 成员维度统计 | GET | `/fortune/include/getMemberInclude` | BillIncludeQuery（含 bookId、billType） | ResponseDTO<List<FortuneBarVo>> | bookVisitorPermission(#query.getBookId()) | 多成员账单每个成员计全额 |
| 借贷总览 | GET | `/fortune/include/{bookId}/getLoanOverview` | @PathVariable bookId | ResponseDTO<LoanOverviewVo> | bookVisitorPermission(#bookId) | 按 payee 计算待收/待还净额 |
| 收支对比 | GET | `/fortune/include/getBillCompare` | BillCompareQuery | ResponseDTO<List<BillCompareVo>> | bookVisitorPermission(#query.getBookId()) | compareType=1按月，2按年 |
| 收支排行 | GET | `/fortune/include/getBillRank` | BillRankQuery（topN默认10） | ResponseDTO<List<FortuneBarVo>> | bookVisitorPermission(#query.getBookId()) | SQL层排序并限制TopN |
| 日历热力图 | GET | `/fortune/include/getCalendarHeatmap` | CalendarHeatmapQuery（year 1900-2100） | ResponseDTO<List<HeatmapVo>> | bookVisitorPermission(#query.getBookId()) | 返回全年日期，缺失补0 |
| 账户维度统计 | GET | `/fortune/include/getAccountInclude` | BillIncludeQuery | ResponseDTO<List<AccountIncludeVo>> | bookVisitorPermission(#query.getBookId()) | 统计资金从/进哪个账户最多 |
| 账单类型分布 | GET | `/fortune/include/getBillTypeDistribution` | BillIncludeQuery | ResponseDTO<List<BillTypeDistributionVo>> | bookVisitorPermission(#query.getBookId()) | 按 BillTypeEnum 聚合 |
| 账户类型资产分布 | GET | `/fortune/include/{groupId}/getAssetsByAccountType` | @PathVariable groupId | ResponseDTO<List<AccountTypeAssetsVo>> | groupVisitorPermission(#groupId) | 按账户类型汇总资产/负债 |
| 信用卡额度看板 | GET | `/fortune/include/{groupId}/getCreditCardOverview` | @PathVariable groupId | ResponseDTO<List<CreditCardVo>> | groupVisitorPermission(#groupId) | usedAmount=负余额绝对值 |
| 净资产趋势 | GET | `/fortune/include/{groupId}/netAssetsTrend` | NetAssetsTrendQuery(periodType=3/4) | ResponseDTO<List<FortuneLineVo>> | groupVisitorPermission(#groupId) | 基于 fortune_balance_snapshot |
| 账户余额趋势 | GET | `/fortune/include/getAccountBalanceTrend` | AccountBalanceTrendQuery | ResponseDTO<List<FortuneLineVo>> | accountVisitorPermission(#query.getAccountId()) | 基于 fortune_balance_snapshot |
| 理财收益统计 | GET | `/fortune/include/{bookId}/getFinanceProfit` | FinanceProfitQuery | ResponseDTO<List<FinanceProfitVo>> | bookVisitorPermission(#bookId) | 统计关闭的借出/借入单收益 |
| 统计口径说明 | GET | `/fortune/include/getIncludePolicy` | 无 | ResponseDTO<IncludePolicyVo> | 无 | 默认转账/借贷不计入支出，包含未确认账单 |
| 导出统计报表 | GET | `/fortune/include/exportReport` | ReportExportQuery | Excel文件流 | bookVisitorPermission(#query.getBookId()) | 单Sheet汇总导出 |
---

## 成员 API — `FortuneMemberRest`
- **类型**：@RestController
- **基础路径**：`/fortune/member`
- **鉴权**：查询类 bookVisitorPermission，写操作 bookActorPermission

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询成员 | GET | `/fortune/member/getPage` | @Valid FortuneMemberQuery（含 bookId） | ResponseDTO<PageDTO<FortuneMemberVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 查询启用的成员列表 | GET | `/fortune/member/{bookId}/getEnableList` | @PathVariable bookId | ResponseDTO<List<FortuneMemberVo>> | bookVisitorPermission(#bookId) | - |
| 新增成员 | POST | `/fortune/member/add` | @Valid @RequestBody FortuneMemberAddCommand（含 bookId） | ResponseDTO<Void> | bookActorPermission(#addCommand.getBookId()) | @AccessLog ADD |
| 修改成员 | PUT | `/fortune/member/modify` | @RequestBody @Valid FortuneMemberModifyCommand（含 bookId） | ResponseDTO<Void> | bookActorPermission(#modifyCommand.getBookId()) | @AccessLog MODIFY |
| 成员移入回收站 | PATCH | `/fortune/member/{bookId}/{memberId}/moveToRecycleBin` | @PathVariable bookId, memberId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog MOVE_TO_RECYCLE_BIN |
| 成员移出回收站 | PATCH | `/fortune/member/{bookId}/{memberId}/putBack` | @PathVariable bookId, memberId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog PUT_BACK |
| 删除成员 | DELETE | `/fortune/member/{bookId}/{memberId}/remove` | @PathVariable bookId, memberId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog DELETE |
| 启用成员 | PATCH | `/fortune/member/{bookId}/{memberId}/enable` | @PathVariable bookId, memberId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog ENABLE |
| 停用成员 | PATCH | `/fortune/member/{bookId}/{memberId}/disable` | @PathVariable bookId, memberId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog DISABLE |
---

## 交易对象 API — `FortunePayeeRest`
- **类型**：@RestController
- **基础路径**：`/fortune/payee`
- **鉴权**：查询类 bookVisitorPermission，写操作 bookActorPermission

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 查询交易对象 | GET | `/fortune/payee/getPage` | @Valid FortunePayeeQuery（含 bookId） | ResponseDTO<PageDTO<FortunePayeeVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 查询启用的交易对象 | GET | `/fortune/payee/{bookId}/getEnableList` | @PathVariable bookId + @RequestParam(可选) billType | ResponseDTO<List<FortunePayeeVo>> | bookVisitorPermission(#bookId) | - |
| 新增交易对象 | POST | `/fortune/payee/add` | @Valid @RequestBody FortunePayeeAddCommand（含 bookId） | ResponseDTO<Void> | bookActorPermission(#addCommand.getBookId()) | @AccessLog ADD |
| 修改交易对象 | PUT | `/fortune/payee/modify` | @RequestBody @Valid FortunePayeeModifyCommand（含 bookId） | ResponseDTO<Void> | bookActorPermission(#modifyCommand.getBookId()) | @AccessLog MODIFY |
| 交易对象移入回收站 | PATCH | `/fortune/payee/{bookId}/{payeeId}/moveToRecycleBin` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog MOVE_TO_RECYCLE_BIN |
| 删除交易对象 | DELETE | `/fortune/payee/{bookId}/{payeeId}/remove` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog DELETE |
| 交易对象移出回收站 | PATCH | `/fortune/payee/{bookId}/{payeeId}/putBack` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog PUT_BACK |
| 交易对象可支出 | PATCH | `/fortune/payee/{bookId}/{payeeId}/canExpense` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_EXPENSE |
| 交易对象不可支出 | PATCH | `/fortune/payee/{bookId}/{payeeId}/cannotExpense` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_NOT_EXPENSE |
| 交易对象可收入 | PATCH | `/fortune/payee/{bookId}/{payeeId}/canIncome` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_INCOME |
| 交易对象不可收入 | PATCH | `/fortune/payee/{bookId}/{payeeId}/cannotIncome` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_NOT_INCOME |
| 启用交易对象 | PATCH | `/fortune/payee/{bookId}/{payeeId}/enable` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog ENABLE |
| 停用交易对象 | PATCH | `/fortune/payee/{bookId}/{payeeId}/disable` | @PathVariable bookId, payeeId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog DISABLE |
---

## 周期记账 API — `FortuneRecurringBillRest`
- **类型**：@RestController
- **基础路径**：`/fortune/recurring/bill`
- **鉴权**：查询/启停 bookVisitorPermission，增删改 bookActorPermission

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 查询周期记账规则 | GET | `/fortune/recurring/bill/getRulePage` | @Valid FortuneRecurringBillRuleQuery（含 bookId） | ResponseDTO<PageDTO<FortuneRecurringBillRuleVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 查询补偿策略 | GET | `/fortune/recurring/bill/getRecoveryStrategy` | 无 | ResponseDTO<List<SelectOptionsVo>> | 无 | - |
| 校验Cron表达式是否合法 | POST | `/fortune/recurring/bill/checkCronExpression` | cronExpression（表单参数） | ResponseDTO<Boolean> | 无 | - |
| 新增周期记账规则 | POST | `/fortune/recurring/bill/addRule` | @RequestBody @Valid FortuneRecurringBillRuleAddCommand（含 bookId） | ResponseDTO<Boolean> | bookActorPermission(#command.getBookId()) | @AccessLog ADD |
| 修改周期记账规则 | PUT | `/fortune/recurring/bill/modifyRule` | @RequestBody @Valid FortuneRecurringBillRuleModifyCommand（含 bookId） | ResponseDTO<Boolean> | bookActorPermission(#command.getBookId()) | @AccessLog MODIFY |
| 删除周期记账规则 | DELETE | `/fortune/recurring/bill/{bookId}/{ruleId}/removeRule` | @PathVariable bookId, ruleId | ResponseDTO<Boolean> | bookActorPermission(#bookId) | @AccessLog DELETE |
| 查询周期记账执行情况 | GET | `/fortune/recurring/bill/{bookId}/{ruleId}/getLogByRuleId` | @PathVariable bookId, ruleId | ResponseDTO<List<FortuneRecurringBillLogVo>> | bookVisitorPermission(#bookId) | - |
| 启用周期记账 | PATCH | `/fortune/recurring/bill/{bookId}/{ruleId}/enableRule` | @PathVariable bookId, ruleId | ResponseDTO<Boolean> | bookVisitorPermission(#bookId) | - |
| 禁用周期记账 | PATCH | `/fortune/recurring/bill/{bookId}/{ruleId}/disableRule` | @PathVariable bookId, ruleId | ResponseDTO<Boolean> | bookVisitorPermission(#bookId) | - |
---

## 标签 API — `FortuneTagRest`
- **类型**：@RestController
- **基础路径**：`/fortune/tag`
- **鉴权**：查询类 bookVisitorPermission，写操作 bookActorPermission

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 分页查询标签 | GET | `/fortune/tag/getPage` | @Valid FortuneTagQuery（含 bookId） | ResponseDTO<PageDTO<FortuneTagVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 查询标签（树形） | GET | `/fortune/tag/getList` | @Valid FortuneTagQuery（含 bookId） | ResponseDTO<List<FortuneTagVo>> | bookVisitorPermission(#query.getBookId()) | TreeUtil.buildForest 树形 |
| 平铺查询标签 | GET | `/fortune/tag/getListPage` | @Valid FortuneTagQuery（含 bookId） | ResponseDTO<PageDTO<FortuneTagVo>> | bookVisitorPermission(#query.getBookId()) | - |
| 查询启用的标签 | GET | `/fortune/tag/{bookId}/getEnableList` | @PathVariable bookId + @RequestParam(可选) billType | ResponseDTO<List<FortuneTagVo>> | bookVisitorPermission(#bookId) | 树形 |
| 新增标签 | POST | `/fortune/tag/add` | @Valid @RequestBody FortuneTagAddCommand（含 bookId） | ResponseDTO<Void> | bookActorPermission(#addCommand.getBookId()) | @AccessLog ADD |
| 修改标签 | PUT | `/fortune/tag/modify` | @Valid @RequestBody FortuneTagModifyCommand（含 bookId） | ResponseDTO<Void> | bookActorPermission(#modifyCommand.getBookId()) | @AccessLog MODIFY |
| 标签移入回收站 | PATCH | `/fortune/tag/{bookId}/{tagId}/moveToRecycleBin` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog MOVE_TO_RECYCLE_BIN |
| 删除标签 | DELETE | `/fortune/tag/{bookId}/{tagId}/remove` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog DELETE |
| 标签移出回收站 | PATCH | `/fortune/tag/{bookId}/{tagId}/putBack` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog PUT_BACK |
| 标签可支出 | PATCH | `/fortune/tag/{bookId}/{tagId}/canExpense` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_EXPENSE |
| 标签不可支出 | PATCH | `/fortune/tag/{bookId}/{tagId}/cannotExpense` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_NOT_EXPENSE |
| 标签可收入 | PATCH | `/fortune/tag/{bookId}/{tagId}/canIncome` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_INCOME |
| 标签不可收入 | PATCH | `/fortune/tag/{bookId}/{tagId}/cannotIncome` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_NOT_INCOME |
| 标签可转账 | PATCH | `/fortune/tag/{bookId}/{tagId}/canTransfer` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_TRANSFER |
| 标签不可转账 | PATCH | `/fortune/tag/{bookId}/{tagId}/cannotTransfer` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog CAN_NOT_TRANSFER |
| 标签启用 | PATCH | `/fortune/tag/{bookId}/{tagId}/enable` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog ENABLE |
| 标签停用 | PATCH | `/fortune/tag/{bookId}/{tagId}/disable` | @PathVariable bookId, tagId | ResponseDTO<Void> | bookActorPermission(#bookId) | @AccessLog DISABLE |
---

# 三、system 系统管理模块

## 监控 API — `MonitorRest`
- **类型**：@RestController
- **基础路径**：`/monitor`
- **鉴权**：均基于 `@permission.has('...')` 功能权限

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 服务器信息 | GET | `/monitor/serverInfo` | 无 | ResponseDTO&lt;ServerInfo&gt; | monitor:server:list | 服务器CPU/内存/JVM/磁盘等信息 |
| 在线用户列表 | GET | `/monitor/onlineUsers` | ipAddress(String,Query,可选)、username(String,Query,可选) | ResponseDTO&lt;PageDTO&lt;OnlineUserDTO&gt;&gt; | monitor:online:list | 按 IP/用户名过滤在线用户 |
| 强退用户 | DELETE | `/monitor/onlineUser/{tokenId}` | tokenId(String,Path) | ResponseDTO&lt;Void&gt; | monitor:online:forceLogout | @AccessLog: FORCE_LOGOUT |

---

## 参数配置 API — `SysConfigRest`
- **类型**：@RestController
- **基础路径**：`/system`
- **鉴权**：均基于 `@permission.has('system:config:*')` 功能权限；类级 @Validated

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 参数列表 | GET | `/system/configs` | query(ConfigQuery,分页查询) | ResponseDTO&lt;PageDTO&lt;ConfigDTO&gt;&gt; | system:config:list | 分页获取配置参数列表 |
| 配置信息 | GET | `/system/config/{configId}` | configId(Long,Path,@NotNull @Positive) | ResponseDTO&lt;ConfigDTO&gt; | system:config:query | 配置详情 |
| 配置修改 | PUT | `/system/config/{configId}` | configId(Long,Path,@NotNull @Positive)、config(ConfigUpdateCommand,Body) | ResponseDTO&lt;Void&gt; | system:config:edit | @AccessLog: MODIFY |
| 刷新配置缓存 | DELETE | `/system/configs/cache` | 无 | ResponseDTO&lt;Void&gt; | system:config:remove | @AccessLog: CLEAN，清空配置缓存 |
| 参数设置枚举列表 | GET | `/system/config/getSystemConfigOptions` | 无 | ResponseDTO&lt;List&lt;configKeyDTO&gt;&gt; | system:config:list | 配置参数枚举列表 |
| 新增参数 | POST | `/system/config/addSystemConfig` | configAddCommand(ConfigAddCommand,Body) | ResponseDTO&lt;Void&gt; | system:config:add | @AccessLog: ADD |
| 删除参数 | DELETE | `/system/config/{configId}` | configId(Long,Path,@NotNull) | ResponseDTO&lt;Void&gt; | system:config:remove | @AccessLog: DELETE |

---

## 系统日志 API — `SysLogsRest`
- **类型**：@RestController
- **基础路径**：`/logs`
- **鉴权**：均基于 `@permission.has('monitor:...')` 功能权限；类级 @Validated

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 登录日志列表 | GET | `/logs/loginLogs` | query(LoginLogQuery,分页查询) | ResponseDTO&lt;PageDTO&lt;LoginLogDTO&gt;&gt; | monitor:logininfor:list | 分页登录日志 |
| 登录日志导出 | GET | `/logs/loginLogs/excel` | query(LoginLogQuery)、response(HttpServletResponse) | Excel文件流(void) | monitor:logininfor:export | @AccessLog: EXPORT，直写响应流 |
| 删除登录日志 | DELETE | `/logs/loginLogs` | ids(List&lt;Long&gt;,Query,@NotNull @NotEmpty) | ResponseDTO&lt;Void&gt; | monitor:logininfor:remove | @AccessLog: DELETE，批量删除 |
| 操作日志列表 | GET | `/logs/operationLogs` | query(OperationLogQuery,分页查询) | ResponseDTO&lt;PageDTO&lt;OperationLogDTO&gt;&gt; | monitor:operlog:list | 分页操作日志 |
| 操作日志导出 | GET | `/logs/operationLogs/excel` | query(OperationLogQuery)、response(HttpServletResponse) | Excel文件流(void) | monitor:operlog:export | @AccessLog: EXPORT，直写响应流 |
| 删除操作日志 | DELETE | `/logs/operationLogs` | operationIds(List&lt;Long&gt;,Query) | ResponseDTO&lt;Void&gt; | monitor:operlog:remove | @AccessLog: DELETE，批量删除 |

---

## 菜单 API — `SysMenuRest`
- **类型**：@RestController
- **基础路径**：`/system/menus`
- **鉴权**：均基于 `@permission.has('system:menu:*')` 功能权限；类级 @Validated

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 菜单列表 | GET | `/system/menus` | menuQuery(MenuQuery,查询) | ResponseDTO&lt;List&lt;MenuDTO&gt;&gt; | system:menu:list | 菜单列表 |
| 菜单详情 | GET | `/system/menus/{menuId}` | menuId(Long,Path,@NotNull @PositiveOrZero) | ResponseDTO&lt;MenuDetailDTO&gt; | system:menu:query | 菜单详情 |
| 菜单列表（树级） | GET | `/system/menus/dropdown` | 无（取当前登录用户） | ResponseDTO&lt;List&lt;Tree&lt;Long&gt;&gt;&gt; | 无（登录即可） | 菜单树级下拉框 |
| 添加菜单 | POST | `/system/menus` | addCommand(AddMenuCommand,Body) | ResponseDTO&lt;Void&gt; | system:menu:add | @AccessLog: ADD |
| 编辑菜单 | PUT | `/system/menus/{menuId}` | menuId(Long,Path)、updateCommand(UpdateMenuCommand,Body) | ResponseDTO&lt;Void&gt; | system:menu:edit | @AccessLog: MODIFY |
| 删除菜单 | DELETE | `/system/menus/{menuId}` | menuId(Long,Path) | ResponseDTO&lt;Void&gt; | system:menu:remove | @AccessLog: DELETE |

---

## 通知公告 API — `SysNoticeRest`
- **类型**：@RestController
- **基础路径**：`/system/notices`
- **鉴权**：均基于 `@permission.has('system:notice:*')` 功能权限；类级 @Validated

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 公告列表 | GET | `/system/notices` | query(NoticeQuery,分页查询) | ResponseDTO&lt;PageDTO&lt;NoticeDTO&gt;&gt; | system:notice:list | 分页公告列表 |
| 公告列表（从库） | GET | `/system/notices/database/slave` | query(NoticeQuery,分页查询) | ResponseDTO&lt;PageDTO&lt;NoticeDTO&gt;&gt; | system:notice:list | 演示主从库读取 |
| 公告详情 | GET | `/system/notices/{noticeId}` | noticeId(Long,Path,@NotNull @Positive) | ResponseDTO&lt;NoticeDTO&gt; | system:notice:query | 公告详情 |
| 添加公告 | POST | `/system/notices` | addCommand(NoticeAddCommand,Body) | ResponseDTO&lt;Void&gt; | system:notice:add | @AccessLog: ADD；@Unrepeatable(60s,SYSTEM_USER) 防重复提交 |
| 修改公告 | PUT | `/system/notices/{noticeId}` | noticeId(Long,Path)、updateCommand(NoticeUpdateCommand,Body) | ResponseDTO&lt;Void&gt; | system:notice:edit | @AccessLog: MODIFY |
| 删除公告 | DELETE | `/system/notices` | noticeIds(List&lt;Integer&gt;,Query) | ResponseDTO&lt;Void&gt; | system:notice:remove | @AccessLog: DELETE，批量删除 |

---

## 个人中心 API — `SysProfileRest`
- **类型**：@RestController
- **基础路径**：`/system/user/profile`
- **鉴权**：无功能权限注解，登录即可（均取当前登录用户 userId）

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 获取个人信息 | GET | `/system/user/profile` | 无（取当前登录用户） | ResponseDTO&lt;UserProfileDTO&gt; | 无（登录即可） | 个人信息 |
| 修改个人信息 | PUT | `/system/user/profile` | command(UpdateProfileCommand,Body，userId 由后端注入) | ResponseDTO&lt;Void&gt; | 无（登录即可） | @AccessLog: MODIFY |
| 重置个人密码 | PUT | `/system/user/profile/password` | command(UpdateUserPasswordCommand,Body，userId 由后端注入) | ResponseDTO&lt;Void&gt; | 无（登录即可） | @AccessLog: MODIFY |
| 修改个人头像 | POST | `/system/user/profile/avatar` | avatarfile(MultipartFile,multipart) | ResponseDTO&lt;UploadFileDTO&gt; | 无（登录即可） | @AccessLog: MODIFY，multipart 头像上传，空文件抛 USER_UPLOAD_FILE_FAILED |

---

## 角色 API — `SysRoleRest`
- **类型**：@RestController
- **基础路径**：`/system/role`
- **鉴权**：均基于 `@permission.has('system:role:*')` 功能权限；类级 @Validated

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 角色列表 | GET | `/system/role/list` | query(RoleQuery,分页查询) | ResponseDTO&lt;PageDTO&lt;RoleDTO&gt;&gt; | system:role:list | 分页角色列表 |
| 角色列表导出 | POST | `/system/role/export` | query(RoleQuery)、response(HttpServletResponse) | Excel文件流(void) | system:role:export | @AccessLog: EXPORT，直写响应流 |
| 角色详情 | GET | `/system/role/{roleId}` | roleId(Long,Path,@NotNull) | ResponseDTO&lt;RoleDTO&gt; | system:role:query | 角色详情 |
| 添加角色 | POST | `/system/role` | addCommand(AddRoleCommand,Body) | ResponseDTO&lt;Void&gt; | system:role:add | @AccessLog: ADD |
| 删除角色 | DELETE | `/system/role/{roleId}` | roleIds(List&lt;Long&gt;,Path) | ResponseDTO&lt;Void&gt; | system:role:remove | @AccessLog: DELETE，批量删除 |
| 修改角色 | PUT | `/system/role` | updateCommand(UpdateRoleCommand,Body,@Validated) | ResponseDTO&lt;Void&gt; | system:role:edit | @AccessLog: MODIFY |
| 修改角色数据权限 | PUT | `/system/role/{roleId}/dataScope` | roleId(Long,Path)、command(UpdateDataScopeCommand,Body) | ResponseDTO&lt;Void&gt; | system:role:edit | @AccessLog: MODIFY |
| 修改角色状态 | PUT | `/system/role/{roleId}/status` | roleId(Long,Path)、command(UpdateStatusCommand,Body) | ResponseDTO&lt;Void&gt; | system:role:edit | @AccessLog: MODIFY |
| 已关联该角色的用户列表 | GET | `/system/role/{roleId}/allocated/list` | roleId(Long,Path)、query(AllocatedRoleQuery,分页) | ResponseDTO&lt;PageDTO&lt;UserDTO&gt;&gt; | system:role:list | 已分配用户 |
| 未关联该角色的用户列表 | GET | `/system/role/{roleId}/unallocated/list` | roleId(Long,Path)、query(UnallocatedRoleQuery,分页) | ResponseDTO&lt;PageDTO&lt;UserDTO&gt;&gt; | system:role:list | 未分配用户 |
| 批量解除角色和用户关联 | DELETE | `/system/role/users/{userIds}/grant/bulk` | userIds(List&lt;Long&gt;,Path) | ResponseDTO&lt;Void&gt; | system:role:edit | @AccessLog: GRANT，批量取消授权 |
| 批量添加用户和角色关联 | POST | `/system/role/{roleId}/users/{userIds}/grant/bulk` | roleId(Long,Path)、userIds(List&lt;Long&gt;,Path) | ResponseDTO&lt;Void&gt; | system:role:edit | @AccessLog: GRANT，批量授权 |

---

## 用户 API — `SysUserRest`
- **类型**：@RestController
- **基础路径**：`/system/users`
- **鉴权**：`@permission.has('system:user:*')` 功能权限，部分叠加数据权限 `@dataScope.checkUserId/checkUserIds`

| 接口 | 方法 | URL | 入参 | 出参 | 权限 | 说明 |
|------|------|-----|------|------|------|------|
| 用户列表 | GET | `/system/users` | query(SearchUserQuery&lt;SearchUserDO&gt;,分页查询) | ResponseDTO&lt;PageDTO&lt;UserDTO&gt;&gt; | system:user:list | 分页用户列表 |
| 用户列表导出 | GET | `/system/users/excel` | query(SearchUserQuery)、response(HttpServletResponse) | Excel文件流(void) | system:user:export | @AccessLog: EXPORT，直写响应流 |
| 用户列表导入 | POST | `/system/users/excel` | file(MultipartFile,multipart) | ResponseDTO&lt;Void&gt; | system:user:import | @AccessLog: IMPORT，source=ADMIN_IMPORT |
| 用户导入模板下载 | GET | `/system/users/excelTemplate` | response(HttpServletResponse) | Excel模板流(void) | 无（登录即可） | 下载导入模板 |
| 用户详情 | GET | `/system/users/{userId}` | userId(Long,Path,可选) | ResponseDTO&lt;UserDetailDTO&gt; | system:user:query | 用户详情 |
| 新增用户 | POST | `/system/users` | command(AddUserCommand,Body,@Validated(AddValidation)) | ResponseDTO&lt;Void&gt; | system:user:add | @AccessLog: ADD，source=ADMIN_ADD |
| 修改用户 | PUT | `/system/users/{userId}` | command(UpdateUserCommand,Body,@Validated) | ResponseDTO&lt;Void&gt; | system:user:edit + @dataScope.checkUserId(#command.userId) | @AccessLog: MODIFY |
| 删除用户 | DELETE | `/system/users/{userIds}` | userIds(List&lt;Long&gt;,Path) | ResponseDTO&lt;Void&gt; | system:user:remove + @dataScope.checkUserIds(#userIds) | @AccessLog: DELETE，批量删除 |
| 重置用户密码 | PUT | `/system/users/{userId}/password` | userId(Long,Path)、command(ResetPasswordCommand,Body) | ResponseDTO&lt;Void&gt; | system:user:resetPwd + @dataScope.checkUserId(#userId) | @AccessLog: MODIFY |
| 修改用户状态 | PUT | `/system/users/{userId}/status` | userId(Long,Path)、command(ChangeStatusCommand,Body) | ResponseDTO&lt;Void&gt; | system:user:edit + @dataScope.checkUserId(#command.userId) | @AccessLog: MODIFY |

---



---

# 附录：DTO 字段字典

> 下列为接口入参/出参对象的字段级明细，按 common / fortune / system 三模块组织。接口表中出现的类名可在此查阅字段。
>
> **字典导航**：
> - [A. common 模块字段字典](#a-common-模块-dto-字段明细)
> - [B. fortune 记账模块字段字典](#b-fortune-记账模块-dto-字段明细)
> - [C. system 系统模块字段字典](#c-system-模块-dto-字段明细)

## A. common 模块 DTO 字段明细

> 本文档从后端 Java 源码逐字段提取，涵盖 common 模块（登录 / 上传 / 角色 / 配置 / 菜单路由 / 用户注册导入）相关的入参、出参 DTO。
> 校验注解基于 `jakarta.validation`，「必填」列以校验注解或业务语义判定；无校验注解的出参字段一律标为「-」。

---

## 一、用户 / 注册 / 导入

### AddUserCommand（新增/注册/导入用户 入参）

> 登录注册与用户导入共用；`password` 仅在新增分组（`AddValidation.class`）下必填。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| username | String | 是 | @NotBlank(用户名不能为空) | 用户名 |
| nickname | String | 是 | @NotBlank(昵称不能为空) | 昵称 |
| email | String | 否 | - | 邮件 |
| phoneNumber | String | 否 | - | 电话号码 |
| sex | Integer | 否 | - | 性别 |
| avatar | String | 否 | - | 头像 |
| password | String | 新增时必填 | @NotBlank(密码不能为空，groups=AddValidation.class) | 密码，仅新增分组校验必填 |
| status | Integer | 否 | - | 状态 |
| roleId | Long | 是 | @NotNull(角色不能为空) | 角色ID |
| remark | String | 否 | - | 备注 |
| source | Integer | 否 | - | 来源 |

### UserDTO（用户信息 出参）

> 作为 `CurrentLoginUserDTO.userInfo` 及用户列表返回；均为出参字段，无校验注解。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | - | - | 用户ID |
| roleId | Long | - | - | 角色ID |
| roleName | String | - | - | 角色名称（由 roleId 查缓存填充） |
| username | String | - | - | 用户名 |
| nickname | String | - | - | 用户昵称 |
| userType | Integer | - | - | 用户类型 |
| email | String | - | - | 邮件 |
| phoneNumber | String | - | - | 号码 |
| sex | Integer | - | - | 性别 |
| avatar | String | - | - | 用户头像 |
| status | Integer | - | - | 状态 |
| loginIp | String | - | - | 最近登录 IP |
| loginDate | Date | - | - | 登录时间 |
| creatorId | Long | - | - | 创建者ID |
| creatorName | String | - | - | 创建者（由 creatorId 查缓存填充） |
| createTime | LocalDateTime | - | - | 创建时间 |
| updaterId | Long | - | - | 修改者ID |
| updaterName | String | - | - | 修改者 |
| updateTime | LocalDateTime | - | - | 修改时间 |
| remark | String | - | - | 备注 |

---

## 二、登录相关

### LoginCommand（登录 /login 入参）

> 源码内无校验注解，「必填」按业务语义标注（用户名、密码为登录必需）。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| username | String | 是（业务） | - | 用户名 |
| password | String | 是（业务） | - | 用户密码 |
| captchaCode | String | 否 | - | 验证码（验证码开启时需填） |
| captchaCodeKey | String | 否 | - | 验证码唯一标识（验证码开启时需填） |

### TokenDTO（登录 /login 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| token | String | - | - | 登录令牌 |
| currentUser | CurrentLoginUserDTO | - | - | 当前登录用户信息，见下表 |

### CurrentLoginUserDTO（/getLoginUserInfo 出参 & TokenDTO.currentUser）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userInfo | UserDTO | - | - | 用户基本信息，见「UserDTO」表 |
| roleKey | String | - | - | 角色标识 |
| permissions | Set&lt;String&gt; | - | - | 权限标识集合 |

### CaptchaDTO（/captchaImage 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| isCaptchaOn | Boolean | - | - | 是否开启验证码 |
| captchaCodeKey | String | - | - | 验证码唯一标识（回填至 LoginCommand.captchaCodeKey） |
| captchaCodeImg | String | - | - | 验证码图片（Base64） |

---

## 三、文件上传

### UploadDTO（/file/upload 出参）

> 使用 `@Builder` 构建；均为出参字段。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| url | String | - | - | 文件访问地址 |
| fileName | String | - | - | 文件名 |
| newFileName | String | - | - | 服务器新文件名 |
| originalFilename | String | - | - | 原始文件名 |

### UploadFileDTO（个人中心头像上传出参）

> `SysProfileRest` 头像上传接口 `POST /system/user/profile/avatar` 的出参。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| imgUrl | String | - | - | 头像访问地址 |

---

## 四、角色

### RoleDTO（角色信息 入参/出参）

> 无校验注解。`selectedMenuList` 用于角色-菜单关联提交。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleId | Long | - | - | 角色ID |
| roleName | String | - | - | 角色名称 |
| roleKey | String | - | - | 角色标识 |
| roleSort | Integer | - | - | 角色排序 |
| status | Integer | - | - | 角色状态 |
| remark | String | - | - | 备注 |
| createTime | LocalDateTime | - | - | 创建时间 |
| dataScope | Integer | - | - | 数据范围 |
| allowRegister | Boolean | - | - | 是否允许注册（源码 Excel 名标为「数据范围」，疑为笔误，字段义为允许注册） |
| isAdmin | Boolean | - | - | 是否是超级管理员 |
| selectedMenuList | List&lt;Long&gt; | - | - | 选中的菜单ID列表 |

---

## 五、系统配置

### ConfigDTO（配置信息 入参/出参）

> `@Schema(name="ConfigDTO", description="配置信息")`；无校验注解。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| configId | String | - | - | 配置ID（由 Long 转字符串） |
| configName | String | - | - | 配置名称 |
| configKey | String | - | - | 配置键名 |
| configValue | String | - | - | 配置键值 |
| configOptions | List&lt;String&gt; | - | - | 配置可选项列表 |
| isAllowChange | Integer | - | - | 是否允许修改 |
| isAllowChangeStr | String | - | - | 是否允许修改（描述文本） |
| remark | String | - | - | 备注 |
| createTime | LocalDateTime | - | - | 创建时间 |

---

## 六、菜单动态路由

> `RouterDTO` / `MetaDTO` 均加了 `@JsonInclude(Include.NON_NULL)`，为 null 的字段不会出现在 JSON 中（配合 Vue 动态路由渲染）。

### RouterDTO（动态路由信息 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| name | String | - | - | 路由名字，前端要求唯一，最好与组件 Name 一致 |
| path | String | - | - | 路由路径地址 |
| redirect | String | - | - | 路由重定向 |
| component | String | - | - | 组件地址 |
| rank | Integer | - | - | 一级菜单排序值（仅支持一级菜单） |
| meta | MetaDTO | - | - | 其他元信息，见下表 |
| children | List&lt;RouterDTO&gt; | - | - | 子路由（递归结构） |

### MetaDTO（路由显示信息 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| title | String | - | - | 菜单名称（兼容国际化/非国际化） |
| icon | String | - | - | 菜单图标 |
| showLink | Boolean | - | - | 是否显示该菜单 |
| showParent | Boolean | - | - | 是否显示父级菜单 |
| roles | List&lt;String&gt; | - | - | 页面级别权限设置 |
| auths | List&lt;String&gt; | - | - | 按钮级别权限设置 |
| frameSrc | String | - | - | 需要内嵌的 iframe 链接地址 |
| isFrameSrcInternal | Boolean | - | - | 是否是内部页面（frameSrc 嵌入时前端做特殊处理） |
| rank | Integer | - | - | 菜单排序，值越高越靠后（仅顶级路由） |
| extraIcon | ExtraIconDTO | - | - | 菜单名称右侧的额外图标，见下表 |
| keepAlive | Boolean | - | - | 是否缓存该路由页面 |
| frameLoading | Boolean | - | - | 内嵌 iframe 页面是否开启首次加载动画 |
| transition | TransitionDTO | - | - | 页面加载动画，见下表 |
| hiddenTag | Boolean | - | - | 当前菜单是否禁止添加到标签页 |
| dynamicLevel | Integer | - | - | 标签页显示的最大数量 |

### ExtraIconDTO（额外图标）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| svg | boolean | - | - | 是否是 svg |
| name | String | - | - | iconfont 名称（目前仅支持 iconfont） |

### TransitionDTO（页面动画）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| name | String | - | - | 当前页面动画（第一种模式，如 "fade"） |
| enterTransition | String | - | - | 进场动画（第二种模式，如 "animate__fadeInLeft"） |
| leaveTransition | String | - | - | 离场动画（第二种模式，如 "animate__fadeOutRight"） |


---

## B. fortune 记账模块 DTO 字段明细

> 本文档由后端源码（`fortuneboot-domain` 模块的 command / query / vo 包）逐字段提取，供前端开发对接使用。
>
> 说明：
> - **必填** 列依据校验注解（`@NotNull` / `@NotBlank` / `@Positive` 等）判定；无校验注解者标为「否（可选）」。查询类的过滤条件多数为可选（不传即不过滤）。
> - `@Positive` 表示必须为正数，`@Size(max=n)` 表示字符串最大长度，`@DecimalMin` 表示金额下限，`@Max` 表示数值上限。
> - **ModifyCommand** 均继承对应的 **AddCommand**，仅额外增加主键字段，父类字段一并列出。
> - 所有分页 **Query** 继承自 `AbstractLambdaPageQuery`，其通用分页/排序/时间范围字段见文末「附录 A：Query 公共父类字段」。
> - 金额类型 `BigDecimal`、日期 `LocalDate`（`yyyy-MM-dd`）、日期时间 `LocalDateTime`（`yyyy-MM-dd HH:mm:ss`）。

---

## 账户 Account

### FortuneAccountAddCommand（新增账户入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| cardNo | String | 否 | - | 卡号 |
| accountName | String | 是 | @NotBlank；@Size(max=50) | 账户名称 |
| balance | BigDecimal | 否 | - | 余额 |
| billDay | LocalDate | 否 | - | 账单日 |
| repayDay | LocalDate | 否 | - | 还款日 |
| canExpense | Boolean | 否 | - | 可支出 |
| canIncome | Boolean | 否 | - | 可收入 |
| canTransferOut | Boolean | 否 | - | 可转出 |
| canTransferIn | Boolean | 否 | - | 可转入 |
| creditLimit | BigDecimal | 否 | - | 信用额度 |
| currencyCode | String | 否 | - | 币种 |
| enable | Boolean | 否 | - | 是否启用 |
| include | Boolean | 否 | - | 是否计入净资产 |
| apr | BigDecimal | 否 | - | 利率 |
| initialBalance | BigDecimal | 否 | - | 期初余额 |
| accountType | Integer | 是 | @NotNull；@Positive | 账户类型（见 AccountTypeEnum） |
| groupId | Long | 是 | @NotNull；@Positive | 分组 id |
| sort | Integer | 否 | - | 排序 |
| remark | String | 否 | @Size(max=512) | 备注 |

### FortuneAccountModifyCommand（修改账户入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| accountId | Long | 是 | @NotNull；@Positive | 账户 id |
| *（继承 FortuneAccountAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneAccountAdjustCommand（余额调整入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull | 账本 id |
| accountId | Long | 是 | @NotNull | 账户 id |
| title | String | 否 | - | 标题 |
| balance | BigDecimal | 是 | @NotNull | 金额（调整后金额） |
| tradeTime | LocalDateTime | 是 | @NotNull | 发生时间（yyyy-MM-dd HH:mm:ss） |
| remark | String | 否 | - | 备注 |

### FortuneAccountQuery（账户查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| groupId | Long | 是 | @NotNull；@Positive | 分组 id |
| accountType | Long | 否 | @Positive | 账户类型 |
| accountName | String | 否 | - | 账户名称（模糊） |
| lowerBalance | BigDecimal | 否 | - | 余额下限 |
| limitBalance | BigDecimal | 否 | - | 余额上限 |
| currencyCode | String | 否 | - | 币种 |
| include | Boolean | 否 | - | 是否计入净资产 |
| canExpense | Boolean | 否 | - | 可支出 |
| canIncome | Boolean | 否 | - | 可收入 |
| canTransferOut | Boolean | 否 | - | 可转出 |
| canTransferIn | Boolean | 否 | - | 可转入 |
| enable | Boolean | 否 | - | 是否启用 |
| recycleBin | Boolean | 否 | - | 回收站（实际作为过滤条件，建议传） |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneAccountVo（账户出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| accountId | Long | 账户 id |
| cardNo | String | 卡号 |
| accountName | String | 账户名称 |
| balance | BigDecimal | 余额 |
| billDay | LocalDate | 账单日 |
| repayDay | LocalDate | 还款日 |
| canExpense | Boolean | 可支出 |
| canIncome | Boolean | 可收入 |
| canTransferOut | Boolean | 可转出 |
| canTransferIn | Boolean | 可转入 |
| creditLimit | BigDecimal | 信用额度 |
| currencyCode | String | 币种 |
| enable | Boolean | 是否启用 |
| include | Boolean | 是否计入净资产 |
| apr | BigDecimal | 利率 |
| initialBalance | BigDecimal | 期初余额 |
| accountType | Integer | 账户类型（见 AccountTypeEnum） |
| groupId | Long | 分组 id |
| sort | Integer | 排序 |
| remark | String | 备注 |

---

## 账单 Bill

### FortuneBillAddCommand（新增账单入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| title | String | 是 | @NotBlank；@Size(max=50) | 标题 |
| tradeTime | LocalDateTime | 否 | - | 交易时间（yyyy-MM-dd HH:mm:ss） |
| accountId | Long | 否 | - | 账户 id |
| orderId | Long | 否 | - | 单据 ID |
| categoryAmountPair | List\<CategoryAmountDTO\> | 是 | @NotNull | 分类和金额列表（见 CategoryAmountDTO） |
| amount | BigDecimal | 否 | - | 金额（后端从 categoryAmountPair 累加计算，前端传值被忽略） |
| convertedAmount | BigDecimal | 否 | - | 汇率转换后金额（后端按汇率重算，前端传值被忽略） |
| payeeId | Long | 否 | - | 交易对象 id |
| billType | Integer | 否 | - | 流水类型（见 BillTypeEnum） |
| toAccountId | Long | 否 | - | 转账目标账户 id |
| confirm | Boolean | 否 | - | 是否确认 |
| include | Boolean | 否 | - | 是否统计 |
| remark | String | 否 | @Size(max=512) | 备注 |
| tagIdList | List\<Long\> | 否 | - | 标签 id 列表 |
| memberIdList | List\<Long\> | 否 | - | 成员 id 列表 |
| fileList | List\<MultipartFile\> | 否 | - | 附件（multipart 上传） |
| extras | List\<FortuneBillExtraAddCommand\> | 否 | - | 附加费用列表（手续费/优惠，见下表） |

### FortuneBillExtraAddCommand（账单附加费用入参：手续费/优惠）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| billId | Long | 否 | - | 关联账单 id；新增账单时可空，主表保存后回填 |
| extraType | Integer | 是 | @NotNull；@Positive | 附加类型：1-手续费，2-优惠 |
| amount | BigDecimal | 是 | @NotNull | 金额（>0） |
| accountSide | Integer | 是 | @NotNull；@Positive | 账户方向：1-转出账户(from)，2-转入账户(to) |
| categoryId | Long | 否 | - | 关联分类 id（可空） |
| remark | String | 否 | @Size(max=255) | 备注 |

### CategoryAmountDTO（分类金额对，账单入参子对象）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| categoryId | Long | 是 | @NotNull | 分类 ID |
| amount | BigDecimal | 是 | @NotNull | 金额 |

### FortuneBillModifyCommand（修改账单入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| billId | Long | 是 | @NotNull；@Positive | 账单 id |
| *（继承 FortuneBillAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneBillQuery（账单查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| accountId | Integer | 否 | - | 账户 id（同时匹配转出/转入账户） |
| billType | Integer | 否 | - | 账单类型 |
| title | String | 否 | - | 标题（模糊） |
| tradeTimeStartTime | String | 否 | - | 交易开始时间（yyyy-MM-dd） |
| tradeTimeEndTime | String | 否 | - | 交易结束时间（yyyy-MM-dd） |
| amountMin | BigDecimal | 否 | - | 最小金额 |
| amountMax | BigDecimal | 否 | - | 最大金额 |
| orderId | Long | 否 | - | 单据 ID |
| categoryIds | List\<Integer\> | 否 | - | 分类 id 列表 |
| tagIds | List\<Integer\> | 否 | - | 标签 id 列表 |
| payeeId | Integer | 否 | - | 交易对象 id |
| confirm | Boolean | 否 | - | 是否确认 |
| include | Boolean | 否 | - | 是否统计 |
| fileInclude | Boolean | 否 | - | 是否有文件（待开发） |
| remark | String | 否 | - | 备注（模糊） |
| orderColumn | String | 否 | - | 排序字段（支持 tradeTime / convertedAmount，默认 tradeTime） |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneBillVo（账单出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| billId | Long | 账单 id |
| bookId | Long | 账本 id |
| bookName | String | 账本名称 |
| title | String | 标题 |
| tradeTime | LocalDateTime | 交易时间 |
| accountId | Long | 账户 id |
| orderId | Long | 单据 ID |
| accountName | String | 账户名称 |
| amount | BigDecimal | 金额 |
| currencyCode | String | 币种 |
| toCurrencyCode | String | 被转入账户金额币种 |
| convertedAmount | BigDecimal | 汇率转换后的金额 |
| payeeId | Long | 交易对象 id |
| payeeName | String | 交易对象名称 |
| billType | Integer | 流水类型（见 BillTypeEnum） |
| toAccountId | Long | 转账目标账户 id |
| toAccountName | String | 转账目标账户名称 |
| confirm | Boolean | 是否确认 |
| include | Boolean | 是否统计 |
| remark | String | 备注 |
| categoryAmountPair | List\<BillCategoryAmountVo\> | 分类金额列表（见下表） |
| tagList | List\<FortuneTagVo\> | 标签列表 |
| memberList | List\<FortuneMemberVo\> | 成员列表 |
| hasFile | Boolean | 是否存在附件（默认 false） |
| extras | List\<FortuneBillExtraVo\> | 附加费用（手续费/优惠） |

### BillCategoryAmountVo（账单-分类金额出参子对象）
| 字段 | 类型 | 说明 |
|------|------|------|
| categoryId | Long | 分类 id |
| categoryName | String | 分类名称 |
| amount | BigDecimal | 金额 |

### FortuneBillExtraVo（账单附加费用出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| extraId | Long | 主键 |
| billId | Long | 关联账单 id |
| extraType | Integer | 附加类型：1-手续费，2-优惠 |
| amount | BigDecimal | 金额 |
| accountSide | Integer | 账户方向：1-转出账户(from)，2-转入账户(to) |
| categoryId | Long | 关联分类 id（可空） |
| remark | String | 备注 |

### FortuneBillDownloadVo（账单导出出参，用于 Excel 导出）
| 字段 | 类型 | Excel 列名 | 说明 |
|------|------|------|------|
| billId | Long | 账单ID | 账单 id |
| bookName | String | 账本 | 账本名称 |
| title | String | 标题 | 标题 |
| tradeTime | LocalDateTime | 交易时间 | 交易时间 |
| accountName | String | 账户 | 账户名称（转账时格式为「转出账户 -> 转入账户」） |
| currencyCode | String | 币种 | 币种 |
| billTypeDesc | String | 流水类型 | 流水类型描述（见 BillTypeEnum） |
| convertedAmount | BigDecimal | 金额 | 汇率转换后的金额 |
| payeeName | String | 交易对象 | 交易对象名称 |
| categories | String | 分类 | 分类名称（多个以「， 」拼接） |
| tags | String | 标签 | 标签名称（多个以「， 」拼接） |
| confirm | String | 是否确认 | 是/否 |
| include | String | 是否统计 | 是/否 |
| hasFileDesc | String | 包含附件 | 是/否 |
| remark | String | 备注 | 备注 |

### FortuneBillImportExcelVo（账单导入模板/入参行，Excel 单行）
| 字段 | 类型 | Excel 列名 | 说明 |
|------|------|------|------|
| title | String | 标题（必填，50字以内） | 标题 |
| tradeTime | LocalDateTime | 交易时间（必填，yyyy-MM-dd HH:mm:ss） | 交易时间 |
| billType | String | 流水类型（必填，支出/收入/转账/垫付/报销） | 流水类型（中文） |
| account | String | 账户（转账必填，其他可空） | 账户名称 |
| toAccount | String | 转入账户（转账必填） | 转入账户名称 |
| orderId | Long | 单据ID（垫付/报销必填） | 单据 id |
| payee | String | 交易对象（选填，填名称） | 交易对象名称 |
| categoryAmounts | String | 分类金额（支出/收入/垫付/报销必填，分类:金额；分类:金额） | 分类金额串 |
| amount | BigDecimal | 金额（转账必填，其他可空） | 金额 |
| tags | String | 标签（选填，标签1；标签2） | 标签串 |
| members | String | 成员（选填，成员1；成员2） | 成员串 |
| confirm | String | 是否确认（选填，是/否/true/false/1/0） | 是否确认 |
| include | String | 是否统计（选填，是/否/true/false/1/0） | 是否统计 |
| remark | String | 备注（选填，512字以内） | 备注 |
| extras | String | 附加费用（选填，仅支出/转账，类型:金额:账户方向:分类:备注） | 附加费用串 |
| files | String | 附件（暂不支持，请留空） | 附件（暂不支持） |

### FortuneBillImportErrorVo（账单导入错误行出参，Excel 单行）
| 字段 | 类型 | Excel 列名 | 说明 |
|------|------|------|------|
| *（前 16 字段同 FortuneBillImportExcelVo）* | - | - | 见上表 |
| errorReason | String | 错误原因 | 该行导入失败的原因 |

### FortuneBillImportResultVo（账单导入结果出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| totalCount | Integer | 总条数 |
| successCount | Integer | 成功条数 |
| failCount | Integer | 失败条数 |
| billIdList | List\<Long\> | 成功导入的账单 id 列表 |
| message | String | 结果提示信息 |

---

## 账本 Book

### FortuneBookAddCommand（新增账本入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| groupId | Long | 是 | @NotNull；@Positive | 分组 id |
| bookName | String | 是 | @NotBlank；@Size(max=50) | 账本名称 |
| defaultCurrency | String | 是 | @NotBlank | 默认币种 |
| defaultExpenseAccountId | Long | 否 | - | 默认支出账户 ID |
| defaultIncomeAccountId | Long | 否 | - | 默认收入账户 ID |
| defaultTransferOutAccountId | Long | 否 | - | 默认转出账户 ID |
| defaultTransferInAccountId | Long | 否 | - | 默认转入账户 ID |
| sort | Integer | 否 | - | 顺序 |
| remark | String | 否 | @Size(max=512) | 备注 |
| bookTemplate | Long | 否 | - | 账本模板 |
| enable | Boolean | 否 | - | 是否启用 |

### FortuneBookModifyCommand（修改账本入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| *（继承 FortuneBookAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneBookQuery（账本查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| groupId | Long | 是 | @NotNull；@Positive | 分组 id |
| bookName | String | 否 | - | 账本名称（模糊） |
| enable | Boolean | 否 | - | 是否启用 |
| recycleBin | Boolean | 是 | @NotNull | 是否回收站 |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneBookVo（账本出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| bookId | Long | 账本 id |
| groupId | Long | 分组 id |
| bookName | String | 账本名称 |
| defaultCurrency | String | 默认币种 |
| defaultExpenseAccountId | Long | 默认支出账户 id |
| defaultExpenseAccountName | String | 默认支出账户名称 |
| defaultIncomeAccountId | Long | 默认收入账户 id |
| defaultIncomeAccountName | String | 默认收入账户名称 |
| defaultTransferOutAccountId | Long | 默认转出账户 id |
| defaultTransferOutAccountName | String | 默认转出账户名称 |
| defaultTransferInAccountId | Long | 默认转入账户 id |
| defaultTransferInAccountName | String | 默认转入账户名称 |
| sort | Integer | 顺序 |
| remark | String | 备注 |
| enable | Boolean | 是否启用 |

---

## 分类 Category

### FortuneCategoryAddCommand（新增分类入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| categoryName | String | 是 | @NotBlank；@Size(max=50) | 分类名称 |
| categoryType | Integer | 是 | @NotNull；@Positive | 分类类型（见 CategoryTypeEnum） |
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| parentId | Long | 否 | - | 父级 id |
| sort | Integer | 否 | - | 顺序 |
| remark | String | 否 | @Size(max=512) | 备注 |
| enable | Boolean | 否 | - | 是否启用 |

### FortuneCategoryModifyCommand（修改分类入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| categoryId | Long | 是 | @NotNull；@Positive | 分类 id |
| *（继承 FortuneCategoryAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneCategoryRelationAddCommand（分类-账单关系入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| billId | Long | 是 | @NotNull；@Positive | 账单 id |
| categoryId | Long | 是 | @NotNull；@Positive | 分类 id |
| amount | BigDecimal | 是 | @NotNull | 金额 |

### FortuneCategoryQuery（分类查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| categoryName | String | 否 | - | 分类名称（模糊，条件查询时生效） |
| enable | Boolean | 否 | - | 是否启用 |
| categoryType | Integer | 否 | - | 分类类型（见 CategoryTypeEnum） |
| recycleBin | Boolean | 是 | @NotNull | 是否回收站 |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneCategoryVo（分类出参，树形结构，继承 AbstractTreeNode）
| 字段 | 类型 | 说明 |
|------|------|------|
| categoryId | Long | 分类 id（同时作为树节点 id） |
| categoryName | String | 分类名称 |
| categoryType | Integer | 分类类型（见 CategoryTypeEnum） |
| bookId | Long | 账本 id |
| parentId | Long | 父级 id |
| sort | Integer | 顺序 |
| remark | String | 备注 |
| enable | Boolean | 是否启用 |
| recycleBin | Boolean | 回收站 |
| children | List\<AbstractTreeNode\> | 子分类列表（树形结构，见附录 B） |

---

## 汇率 Currency

> 汇率模块无 Add/Modify Command。

### FortuneCurrencyQuery（汇率查询入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| target | String | 否 | - | 目标币种 |
| base | String | 否 | - | 基准币种 |

### FortuneCurrencyVo（汇率出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| currencyName | String | 汇率名称（币种名称） |
| rate | BigDecimal | 汇率 |
| baseCurrency | String | 基准币种（为空时默认 USD） |

---

## 单据 FinanceOrder

### FortuneFinanceOrderAddCommand（新增单据入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| title | String | 是 | @NotBlank | 标题 |
| type | Integer | 是 | @NotNull | 类型（见 FinanceOrderTypeEnum） |
| remark | String | 否 | - | 备注 |

### FortuneFinanceOrderModifyCommand（修改单据入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| orderId | Long | 是 | @NotNull；@Positive | 单据 id |
| *（继承 FortuneFinanceOrderAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneFinanceOrderQuery（单据查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotBlank | 账本 id |
| title | String | 否 | - | 标题（模糊） |
| type | Integer | 否 | - | 类型 |
| status | Integer | 否 | - | 状态 |
| remark | String | 否 | - | 备注（模糊） |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneFinanceOrderVo（单据出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| orderId | Long | 单据 id |
| bookId | Long | 账本 id |
| title | String | 标题 |
| type | Integer | 类型（见 FinanceOrderTypeEnum） |
| typeDesc | String | 类型描述 |
| outAmount | BigDecimal | 支出金额 |
| inAmount | BigDecimal | 转入金额 |
| status | Integer | 状态（见 FinanceOrderStatusEnum） |
| statusDesc | String | 状态描述 |
| submitTime | LocalDateTime | 单据提交时间 |
| closeTime | LocalDateTime | 单据关闭时间 |
| remark | String | 备注 |

---

## 归物 GoodsKeeper

### FortuneGoodsKeeperAddCommand（新增物品入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| goodsName | String | 是 | @NotBlank；@Size(max=50) | 物品名称 |
| categoryId | Long | 否 | - | 分类 id |
| tagId | Long | 否 | - | 标签 id |
| price | BigDecimal | 是 | @NotNull | 价格 |
| purchaseDate | LocalDate | 是 | @NotNull | 购买日期（yyyy-MM-dd） |
| warrantyDate | LocalDate | 否 | - | 保修日期（yyyy-MM-dd） |
| useByTimes | Boolean | 否 | - | 按次使用 |
| usageNum | Long | 否 | - | 使用次数 |
| status | Integer | 是 | @NotNull；@Positive | 状态（见 GoodsKeeperStatusEnum） |
| retiredDate | LocalDate | 否 | - | 退役日期（yyyy-MM-dd） |
| soldPrice | BigDecimal | 否 | - | 出售价格 |
| remark | String | 否 | @Size(max=512) | 备注 |
| fileList | List\<MultipartFile\> | 否 | - | 附件 |

### FortuneGoodsKeeperModifyCommand（修改物品入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| goodsKeeperId | Long | 是 | @NotNull；@Positive | 物品 id |
| *（继承 FortuneGoodsKeeperAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneGoodsKeeperQuery（归物查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| goodsName | String | 否 | - | 物品名称（模糊） |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneGoodsKeeperVo（归物出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| goodsKeeperId | Long | 物品 id |
| bookId | Long | 账本 id |
| goodsName | String | 物品名称 |
| categoryId | Long | 分类 id |
| categoryName | String | 分类名称 |
| tagId | Long | 标签 id |
| tagName | String | 标签名称 |
| price | BigDecimal | 价格 |
| purchaseDate | LocalDate | 购买日期 |
| holdingTime | Long | 持有时间（天，后端计算） |
| warrantyDate | LocalDate | 保修日期 |
| isOverWarranty | String | 是否过保（已过保/未过保/空） |
| useByTimes | Boolean | 按次使用 |
| useByTimesDesc | String | 按次使用描述（是/否） |
| usageNum | Long | 使用次数 |
| status | Integer | 状态（见 GoodsKeeperStatusEnum） |
| statusDesc | String | 状态描述 |
| retiredDate | LocalDate | 退役时间 |
| soldPrice | BigDecimal | 出售价格 |
| remark | String | 备注 |
| fileList | List\<MultipartFile\> | 附件 |
| dailyAverageCost | BigDecimal | 日均成本（后端计算） |

### FortuneGoodsKeeperStatisticsVo（归物统计出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| allPrice | BigDecimal | 全部物品的成本 |
| allDailyPrice | BigDecimal | 全部物品日均成本 |
| activePrice | BigDecimal | 在役物品成本 |
| activeDailyPrice | BigDecimal | 在役物品日均成本 |

---

## 分组 Group

### FortuneGroupAddCommand（新增分组入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| groupName | String | 是 | @NotBlank；@Size(max=50) | 分组名称 |
| defaultCurrency | String | 是 | @NotBlank | 默认币种 |
| enable | Boolean | 否 | - | 是否启用 |
| bookTemplate | Long | 否 | - | 账本模板 |
| remark | String | 否 | @Size(max=512) | 备注 |

### FortuneGroupModifyCommand（修改分组入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| groupId | Long | 是 | @NotNull；@Positive | 分组 id |
| defaultBookId | Long | 否 | - | 默认账本 |
| *（继承 FortuneGroupAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneGroupQuery（分组查询入参，继承分页父类）
> 无自定义过滤字段，仅继承分页父类字段（见附录 A）。查询返回当前用户可访问的全部分组。

### FortuneGroupVo（分组出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| groupId | Long | 主键 |
| groupName | String | 分组名称 |
| defaultCurrency | String | 默认币种 |
| roleTypeDesc | String | 权限名称（当前用户在该分组的角色描述） |
| enable | Boolean | 是否启用 |
| defaultBookId | Long | 默认账本 id |
| defaultBookName | String | 默认账本名称 |
| remark | String | 备注 |

---

## 成员 Member

### FortuneMemberAddCommand（新增成员入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| memberName | String | 是 | @NotBlank；@Size(max=50) | 成员名称 |
| enable | Boolean | 否 | - | 是否启用 |
| sort | Integer | 否 | - | 排序 |
| remark | String | 否 | @Size(max=512) | 备注 |

### FortuneMemberModifyCommand（修改成员入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| memberId | Long | 是 | @NotNull；@Positive | 成员 id |
| *（继承 FortuneMemberAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneMemberRelationAddCommand（成员-账单关系入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| billId | Long | 是 | @NotNull；@Positive | 账单 id |
| memberId | Long | 是 | @NotNull；@Positive | 成员 id |

### FortuneMemberQuery（成员查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| memberName | String | 否 | - | 成员名称（模糊） |
| enable | Boolean | 否 | - | 是否启用 |
| recycleBin | Boolean | 是 | @NotNull | 是否回收站 |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneMemberVo（成员出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| memberId | Long | 成员 id |
| bookId | Long | 账本 id |
| memberName | String | 成员名称 |
| enable | Boolean | 是否启用 |
| sort | Integer | 排序 |
| remark | String | 备注 |

---

## 交易对象 Payee

### FortunePayeeAddCommand（新增交易对象入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| payeeName | String | 是 | @NotBlank | 交易对象名称 |
| sort | Integer | 否 | - | 顺序 |
| canExpense | Boolean | 否 | - | 可支出 |
| canIncome | Boolean | 否 | - | 可收入 |
| enable | Boolean | 否 | - | 是否启用 |
| remark | String | 否 | @Size(max=512) | 备注 |

### FortunePayeeModifyCommand（修改交易对象入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| payeeId | Long | 是 | @NotNull；@Positive | 交易对象 id |
| *（继承 FortunePayeeAddCommand 全部字段）* | - | - | - | 见上表 |

### FortunePayeeQuery（交易对象查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| recycleBin | Boolean | 是 | @NotNull | 是否回收站 |
| payeeName | String | 否 | - | 交易对象名称（模糊） |
| enable | Boolean | 否 | - | 是否启用 |
| canExpense | Boolean | 否 | - | 可支出 |
| canIncome | Boolean | 否 | - | 可收入 |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortunePayeeVo（交易对象出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| payeeId | Long | 交易对象 id |
| bookId | Long | 账本 id |
| payeeName | String | 交易对象名称 |
| sort | Integer | 顺序 |
| canExpense | Boolean | 可支出 |
| canIncome | Boolean | 可收入 |
| enable | Boolean | 是否启用 |
| remark | String | 备注 |

---

## 周期记账 RecurringBill

### FortuneRecurringBillRuleAddCommand（新增周期记账规则入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| ruleName | String | 是 | @NotBlank；@Size(max=100) | 规则名称 |
| cronExpression | String | 是 | @NotBlank | cron 表达式 |
| enable | Boolean | 否 | - | 启用状态 |
| billRequest | FortuneBillAddCommand | 是 | @NotNull | 账单数据（见账单 AddCommand） |
| startDate | LocalDate | 否 | - | 开始日期 |
| endDate | LocalDate | 否 | - | 结束日期 |
| maxExecutions | Long | 否 | - | 最大执行次数 |
| executedCount | Long | 否 | - | 已执行次数 |
| lastExecutedTime | LocalDateTime | 否 | - | 最后一次执行时间 |
| nextExecutionTime | LocalDateTime | 否 | - | 下次执行时间 |
| lastRecoveryCheck | LocalDateTime | 否 | - | 上次补偿时间 |
| recoveryStrategy | Integer | 否 | - | 补偿策略 |
| maxRecoveryCount | Long | 否 | - | 最大补偿次数 |
| remark | String | 否 | @Size(max=1024) | 备注 |

### FortuneRecurringBillRuleModifyCommand（修改周期记账规则入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| ruleId | Long | 否 | - | 规则 id |
| *（继承 FortuneRecurringBillRuleAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneRecurringBillRuleQuery（周期记账规则查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 否 | - | 账本 id |
| ruleName | String | 否 | - | 规则名称（模糊） |
| cronExpression | String | 否 | - | cron 表达式（模糊） |
| enable | Boolean | 否 | - | 启用状态 |
| remark | String | 否 | - | 备注（模糊） |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneRecurringBillRuleVo（周期记账规则出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| bookId | Long | 账本 id |
| ruleId | Long | 规则 id |
| ruleName | String | 规则名称 |
| cronExpression | String | cron 表达式 |
| enable | Boolean | 启用状态 |
| billRequest | String | 请求参数（账单模板 JSON 字符串） |
| startDate | LocalDate | 开始日期 |
| endDate | LocalDate | 结束日期 |
| maxExecutions | Long | 最大执行次数 |
| executedCount | Long | 已执行次数 |
| lastExecutedTime | LocalDateTime | 最后一次执行时间 |
| nextExecutionTime | LocalDateTime | 下次执行时间 |
| lastRecoveryCheck | LocalDateTime | 上次补偿时间 |
| recoveryStrategy | Integer | 补偿策略 |
| maxRecoveryCount | Long | 最大补偿次数 |
| remark | String | 备注 |

### FortuneRecurringBillLogVo（周期记账执行日志出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| logId | Long | 日志 id |
| ruleId | Long | 规则 id |
| executionTime | LocalDateTime | 执行时间 |
| status | Integer | 状态 |
| billId | Long | 生成的账单 id |
| errorMsg | String | 错误信息 |
| executionDuration | Long | 执行耗时（毫秒） |

---

## 标签 Tag

### FortuneTagAddCommand（新增标签入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| tagName | String | 是 | @NotBlank；@Size(max=50) | 标签名称 |
| parentId | Long | 否 | - | 父级 ID |
| canExpense | Boolean | 否 | - | 可支出 |
| canIncome | Boolean | 否 | - | 可收入 |
| canTransfer | Boolean | 否 | - | 可转账 |
| enable | Boolean | 否 | - | 是否启用 |
| sort | Integer | 否 | - | 顺序 |
| remark | String | 否 | @Size(max=512) | 备注 |

### FortuneTagModifyCommand（修改标签入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| tagId | Long | 是 | @NotNull；@Positive | 标签 id |
| *（继承 FortuneTagAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneTagRelationAddCommand（标签-账单关系入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| billId | Long | 是 | @NotNull；@Positive | 账单 id |
| tagId | Long | 是 | @NotNull；@Positive | 标签 id |

### FortuneTagQuery（标签查询入参，继承分页父类）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| tagName | String | 否 | - | 标签名称（模糊，条件查询时生效） |
| canExpense | Boolean | 否 | - | 可支出（条件查询时生效） |
| canIncome | Boolean | 否 | - | 可收入（条件查询时生效） |
| canTransfer | Boolean | 否 | - | 可转账（条件查询时生效） |
| enable | Boolean | 否 | - | 是否启用（条件查询时生效） |
| recycleBin | Boolean | 是 | @NotNull | 是否回收站 |
| *（继承分页父类字段）* | - | - | - | 见附录 A |

### FortuneTagVo（标签出参，树形结构，继承 AbstractTreeNode）
| 字段 | 类型 | 说明 |
|------|------|------|
| tagId | Long | 标签 id（同时作为树节点 id） |
| tagName | String | 标签名称 |
| bookId | Long | 账本 id |
| parentId | Long | 父级 ID |
| canExpense | Boolean | 可支出 |
| canIncome | Boolean | 可收入 |
| canTransfer | Boolean | 可转账 |
| enable | Boolean | 是否启用 |
| sort | Integer | 顺序 |
| remark | String | 备注 |
| children | List\<AbstractTreeNode\> | 子标签列表（树形结构，见附录 B） |

---

## 用户分组关系 UserGroupRelation

### FortuneUserGroupRelationAddCommand（新增用户分组关系入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleType | Integer | 是 | @NotNull | 权限类型（见 RoleTypeEnum） |
| groupId | Long | 是 | @NotNull | 分组 id |
| userId | Long | 是 | @NotNull | 用户 id |

### FortuneUserGroupRelationModifyCommand（修改用户分组关系入参，继承 AddCommand）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userGroupRelationId | Long | 是 | @NotNull；@Positive | 关系主键 |
| *（继承 FortuneUserGroupRelationAddCommand 全部字段）* | - | - | - | 见上表 |

### FortuneUserGroupRelationInviteCommand（邀请用户入组入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleType | Integer | 是 | @NotNull | 权限类型（见 RoleTypeEnum） |
| groupId | Long | 是 | @NotNull | 分组 id |
| username | String | 是 | @NotBlank | 用户名（按用户名邀请） |

### FortuneUserGroupRelationVo（用户分组关系出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| userGroupRelationId | Long | 关系主键 |
| roleType | Integer | 角色（见 RoleTypeEnum） |
| roleTypeDesc | String | 角色描述 |
| groupId | Long | 分组 id |
| username | String | 用户姓名 |
| nickname | String | 昵称 |

---

## 文件 File（账单附件）

### FortuneFileAddCommand（新增文件入参）
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| fileId | Long | 否 | - | 文件 id |
| billId | Long | 否 | - | 账单 id |
| contentType | String | 否 | - | 媒体类型 |
| fileData | byte[] | 否 | - | 文件数据 |
| size | Long | 否 | - | 大小 |
| originalName | String | 否 | - | 原始名称 |

### FortuneFileVo（文件出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| fileId | Long | 文件 id |
| contentType | String | 文件类型 |
| fileData | byte[] | 文件数据 |
| size | Long | 大小 |
| originalName | String | 原始名称 |
| billId | Long | 账单 id |

---

## 统计与图表 Vo

### BillStatisticsVo（账单收支统计出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| income | BigDecimal | 收入 |
| expense | BigDecimal | 支出 |
| surplus | BigDecimal | 结余 |

### FortuneAssetsLiabilitiesVo（资产负债出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| totalAssets | BigDecimal | 资产 |
| totalLiabilities | BigDecimal | 负债 |
| netAssets | BigDecimal | 净资产 |

### FortunePieVo（饼图出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| value | BigDecimal | 值 |
| name | String | 名称 |
| percent | BigDecimal | 百分比 |

### FortuneLineVo（折线图出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 名称 |
| value | BigDecimal | 值 |

### FortuneBarVo（柱状图出参）
| 字段 | 类型 | 说明 |
|------|------|------|
| name | String | 名称 |
| value | BigDecimal | 值 |

### SelectOptionsVo（下拉选择器出参，通用）
> 用于周期记账补偿策略等下拉选项返回；`@NoArgsConstructor`/`@AllArgsConstructor`。
| 字段 | 类型 | 说明 |
|------|------|------|
| value | Long | 选项值 |
| label | String | 选项显示文本 |

### BillTrendsQuery（收支趋势查询入参）
> 统计接口 `FortuneIncludeRest` 使用；**不继承分页父类**。
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull；@Positive | 账本 id |
| timeGranularity | Integer | 否 | - | 时间维度（粒度） |
| timePoint | LocalDateTime | 否 | - | 时间点（yyyy-MM-dd HH:mm:ss） |
| billType | Integer | 否 | - | 账单类型 |

### CategoryIncludeQuery（分类统计查询入参）
> 统计接口 `FortuneIncludeRest` 使用；**不继承分页父类**。
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull(账本不能为空)；@Positive(账本ID必须是正数) | 账本 id |
| startDate | LocalDate | 否 | - | 开始日期（yyyy-MM-dd） |
| endDate | LocalDate | 否 | - | 结束日期（yyyy-MM-dd） |
| title | String | 否 | - | 标题 |
| categoryIds | List\<Long\> | 否 | - | 分类 id 列表 |
| tagIds | List\<Long\> | 否 | - | 标签 id 列表 |
| payeeIds | List\<Long\> | 否 | - | 交易对象 id 列表 |
| accountIds | List\<Long\> | 否 | - | 账户 id 列表 |

### TagIncludeQuery（标签统计查询入参）
> 统计接口 `FortuneIncludeRest` 使用；字段与 CategoryIncludeQuery 完全一致；**不继承分页父类**。
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull(账本不能为空)；@Positive(账本ID必须是正数) | 账本 id |
| startDate | LocalDate | 否 | - | 开始日期（yyyy-MM-dd） |
| endDate | LocalDate | 否 | - | 结束日期（yyyy-MM-dd） |
| title | String | 否 | - | 标题 |
| categoryIds | List\<Long\> | 否 | - | 分类 id 列表 |
| tagIds | List\<Long\> | 否 | - | 标签 id 列表 |
| payeeIds | List\<Long\> | 否 | - | 交易对象 id 列表 |
| accountIds | List\<Long\> | 否 | - | 账户 id 列表 |

### PayeeIncludeQuery（交易对象统计查询入参）
> 统计接口 `FortuneIncludeRest` 使用；字段与 CategoryIncludeQuery 完全一致；**不继承分页父类**。
| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| bookId | Long | 是 | @NotNull(账本不能为空)；@Positive(账本ID必须是正数) | 账本 id |
| startDate | LocalDate | 否 | - | 开始日期（yyyy-MM-dd） |
| endDate | LocalDate | 否 | - | 结束日期（yyyy-MM-dd） |
| title | String | 否 | - | 标题 |
| categoryIds | List\<Long\> | 否 | - | 分类 id 列表 |
| tagIds | List\<Long\> | 否 | - | 标签 id 列表 |
| payeeIds | List\<Long\> | 否 | - | 交易对象 id 列表 |
| accountIds | List\<Long\> | 否 | - | 账户 id 列表 |

---

## 附录 A：Query 公共父类字段

除 `FortuneCurrencyQuery`（不继承分页父类）外，所有 fortune 查询类均继承 `AbstractLambdaPageQuery`（其又继承 `AbstractLambdaQuery`），公共字段如下：

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| pageNum | Integer | 否 | @Max(2147483647) | 分页页数，默认 1 |
| pageSize | Integer | 否 | @Max(500) | 单页大小，默认 10，最大 500 |
| orderDirection | String | 否 | - | 排序方向：`ascending`（升序）/ `descending`（降序） |
| beginTime | Date | 否 | - | 时间范围-起始（yyyy-MM-dd，需查询类重写时间字段才生效） |
| endTime | Date | 否 | - | 时间范围-结束（yyyy-MM-dd，需查询类重写时间字段才生效） |

> 注：`FortuneBillQuery` 覆盖了默认排序逻辑，使用自身的 `orderColumn` + `orderDirection` 控制排序，并固定追加 createTime 倒序。

## 附录 B：树节点公共父类字段（AbstractTreeNode）

`FortuneCategoryVo`、`FortuneTagVo` 继承 `AbstractTreeNode`，用于返回树形结构，公共字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| children | List\<AbstractTreeNode\> | 子节点列表（元素类型为对应的 Vo，如分类树中为 FortuneCategoryVo） |

> 树节点的 id 由各 Vo 重写 `getId()` 提供（分类为 categoryId、标签为 tagId）。


---

## C. system 模块 DTO 字段明细

> 本文档从后端 Java 源码逐字段提取，涵盖 system 系统管理模块（配置 / 日志 / 菜单 / 公告 / 角色 / 用户 / 个人中心 / 监控）相关的入参（Command）、查询入参（Query）、出参（DTO）。
> 校验注解基于 `jakarta.validation`；「必填」列以校验注解或业务语义判定；无校验注解的出参字段一律标为「-」。
> 所有查询类（Query）继承自 `AbstractQuery` / `AbstractPageQuery`，其公共字段见文末「附录：查询基类公共字段」。

---

## 一、配置管理

### ConfigAddCommand（参数新增 入参）

> 无字段级校验注解，均为可选（业务层可能另行校验）。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| configName | String | 否 | - | 配置名称 |
| configKey | String | 否 | - | 配置 key |
| configValue | String | 否 | - | 配置值 |
| configOptions | String | 否 | - | 配置可选项（JSON 字符串） |
| isAllowChange | Boolean | 否 | - | 是否允许更改 |
| remark | String | 否 | - | 备注 |

### ConfigUpdateCommand（参数修改 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| configId | Long | 是 | @NotNull @Positive | 配置ID（正数） |
| configValue | String | 是 | @NotNull @NotEmpty | 配置值 |

### ConfigQuery（配置查询 入参）

> 继承 `AbstractPageQuery`，含分页与时间范围公共字段（见附录）；`timeRangeColumn` 固定为 `create_time`。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| configName | String | 否 | - | 配置名称（模糊匹配） |
| configKey | String | 否 | - | 配置 key（精确匹配） |
| isAllowChange | Boolean | 否 | - | 是否允许更改配置 |

### ConfigDTO（配置信息 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| configId | String | - | - | 配置ID（Long 转字符串） |
| configName | String | - | - | 配置名称 |
| configKey | String | - | - | 配置 key |
| configValue | String | - | - | 配置值 |
| configOptions | List\<String\> | - | - | 配置可选项列表（由 JSON 解析） |
| isAllowChange | Integer | - | - | 是否允许更改（数值） |
| isAllowChangeStr | String | - | - | 是否允许更改（枚举描述文本） |
| remark | String | - | - | 备注 |
| createTime | LocalDateTime | - | - | 创建时间 |

### configKeyDTO（参数枚举信息 出参）

> 由 `ConfigKeyEnum` 枚举拷贝生成，用于返回内置配置项元数据。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| value | String | - | - | 配置 key 值 |
| description | String | - | - | 配置描述 |
| option | String | - | - | 可选项 |
| defaultValue | String | - | - | 默认值 |
| isAllowChange | Boolean | - | - | 是否允许更改 |
| remark | String | - | - | 备注 |

---

## 二、日志管理

### LoginLogQuery（登录日志查询 入参）

> 继承 `AbstractPageQuery`（见附录）。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| ipAddress | String | 否 | - | IP 地址（模糊匹配） |
| status | String | 否 | - | 状态（精确匹配） |
| username | String | 否 | - | 用户名（模糊匹配） |

### OperationLogQuery（操作日志查询 入参）

> 继承 `AbstractPageQuery`（见附录）；`timeRangeColumn` 固定为 `operation_time`。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| businessType | String | 否 | - | 业务类型（模糊匹配） |
| status | String | 否 | - | 状态（精确匹配） |
| username | String | 否 | - | 用户名（模糊匹配） |
| requestModule | String | 否 | - | 请求模块（模糊匹配） |

### LoginLogDTO（登录日志 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| logId | String | - | - | 日志ID（Long 转字符串） |
| username | String | - | - | 用户名 |
| ipAddress | String | - | - | IP 地址 |
| loginLocation | String | - | - | 登录地点 |
| operationSystem | String | - | - | 操作系统 |
| browser | String | - | - | 浏览器 |
| status | Integer | - | - | 状态（数值） |
| statusStr | String | - | - | 状态（枚举描述文本） |
| msg | String | - | - | 描述信息 |
| loginTime | Date | - | - | 登录时间 |

### OperationLogDTO（操作日志 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| operationId | Long | - | - | 操作ID |
| businessType | Integer | - | - | 业务类型（数值） |
| businessTypeStr | String | - | - | 业务类型（枚举描述文本） |
| requestMethod | String | - | - | 请求方式（枚举描述文本） |
| requestModule | String | - | - | 请求模块 |
| requestUrl | String | - | - | 请求 URL |
| calledMethod | String | - | - | 调用方法 |
| operatorType | Integer | - | - | 操作人类型（数值） |
| operatorTypeStr | String | - | - | 操作人类型（枚举描述文本） |
| userId | Long | - | - | 用户ID |
| username | String | - | - | 用户名 |
| operatorIp | String | - | - | 操作人 IP |
| operatorLocation | String | - | - | 操作人地点 |
| operationParam | String | - | - | 操作参数 |
| operationResult | String | - | - | 操作结果 |
| status | Integer | - | - | 状态（数值） |
| statusStr | String | - | - | 状态（枚举描述文本） |
| errorStack | String | - | - | 错误堆栈 |
| operationTime | Date | - | - | 操作时间 |

---

## 三、菜单管理

### AddMenuCommand（新增菜单 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| parentId | Long | 否 | - | 父菜单ID |
| menuName | String | 是 | @NotBlank(菜单名称不能为空) @Size(max=50) | 菜单名称 |
| routerName | String | 否 | - | 路由名称（必须唯一） |
| path | String | 否 | @Size(max=200) | 路由地址 |
| status | Integer | 否 | - | 状态 |
| menuType | Integer | 否 | - | 菜单类型 |
| isButton | Boolean | 否 | - | 是否为按钮 |
| permission | String | 否 | @Size(max=100) | 权限标识 |
| meta | MetaDTO | 否 | - | 路由显示信息（结构见下方 MetaDTO） |

### UpdateMenuCommand（修改菜单 入参）

> 继承 `AddMenuCommand`，含其全部字段。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| menuId | Long | 是 | @NotNull | 菜单ID |
| *(继承 AddMenuCommand 全部字段)* | - | - | - | 见上表 |

### MenuQuery（菜单查询 入参）

> 继承 `AbstractQuery`（**非分页**，无 pageNum/pageSize），含排序与时间范围公共字段（见附录）；固定按 `parent_id` 降序。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| isButton | Boolean | 否 | - | 是否为按钮（精确匹配） |

### MetaDTO（路由显示信息 出参/入参子对象）

> 既用于 AddMenuCommand 入参，也用于 MenuDTO/MenuDetailDTO 出参。标注 `@JsonInclude(NON_NULL)`，null 字段不序列化。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| title | String | - | - | 菜单名称（支持国际化） |
| icon | String | - | - | 菜单图标 |
| showLink | Boolean | - | - | 是否显示该菜单 |
| showParent | Boolean | - | - | 是否显示父级菜单 |
| roles | List\<String\> | - | - | 页面级别权限设置 |
| auths | List\<String\> | - | - | 按钮级别权限设置 |
| frameSrc | String | - | - | 需内嵌的 iframe 链接地址 |
| isFrameSrcInternal | Boolean | - | - | iframe 是否为内部页面 |
| rank | Integer | - | - | 菜单排序（仅顶级路由，值越高越靠后） |
| extraIcon | ExtraIconDTO | - | - | 菜单名称右侧额外图标 |
| keepAlive | Boolean | - | - | 是否缓存该路由页面 |
| frameLoading | Boolean | - | - | 内嵌 iframe 是否开启首次加载动画 |
| transition | TransitionDTO | - | - | 页面加载动画配置 |
| hiddenTag | Boolean | - | - | 是否禁止添加到标签页 |
| dynamicLevel | Integer | - | - | 标签页最大显示数量 |

### MenuDTO（菜单信息 出参）

> 使用 `id`/`parentId` 便于前端构建树结构。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| id | Long | - | - | 菜单ID |
| parentId | Long | - | - | 父菜单ID |
| menuName | String | - | - | 菜单名称 |
| routerName | String | - | - | 路由名称 |
| path | String | - | - | 路由地址 |
| rank | Integer | - | - | 排序（取自 meta.rank） |
| menuType | Integer | - | - | 菜单类型（按钮时为 0） |
| menuTypeStr | String | - | - | 菜单类型（枚举描述文本） |
| isButton | Boolean | - | - | 是否为按钮 |
| status | Integer | - | - | 状态（数值） |
| statusStr | String | - | - | 状态（枚举描述文本） |
| createTime | LocalDateTime | - | - | 创建时间 |
| icon | String | - | - | 图标（取自 meta.icon） |

### MenuDetailDTO（菜单详情 出参）

> 继承 `MenuDTO`，含其全部字段，额外增加权限标识与完整 meta。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| permission | String | - | - | 权限标识 |
| meta | MetaDTO | - | - | 完整路由显示信息（结构见 MetaDTO） |
| *(继承 MenuDTO 全部字段)* | - | - | - | 见 MenuDTO 表 |

---

## 四、公告管理

### NoticeAddCommand（新增公告 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| noticeTitle | String | 是 | @NotBlank(公告标题不能为空) @Size(max=50) | 公告标题 |
| noticeType | String | 否 | - | 公告类型 |
| noticeContent | String | 是 | @NotBlank | 公告内容（支持富文本，跳过 XSS 过滤） |
| status | String | 否 | - | 状态 |

### NoticeUpdateCommand（修改公告 入参）

> 继承 `NoticeAddCommand`，含其全部字段。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| noticeId | Long | 是 | @NotNull @Positive | 公告ID（正数） |
| *(继承 NoticeAddCommand 全部字段)* | - | - | - | 见上表 |

### NoticeQuery（公告查询 入参）

> 继承 `AbstractPageQuery`（见附录）。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| noticeType | String | 否 | - | 公告类型（精确匹配） |
| noticeTitle | String | 否 | - | 公告标题（模糊匹配） |
| creatorName | String | 否 | - | 创建者名称（模糊匹配） |

### NoticeDTO（公告信息 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| noticeId | String | - | - | 公告ID（Long 转字符串） |
| noticeTitle | String | - | - | 公告标题 |
| noticeType | Integer | - | - | 公告类型（数值） |
| noticeContent | String | - | - | 公告内容 |
| status | Integer | - | - | 状态（数值） |
| createTime | LocalDateTime | - | - | 创建时间 |
| creatorName | String | - | - | 创建者用户名（由缓存填充） |

---

## 五、角色管理

### AddRoleCommand（新增角色 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleName | String | 是 | @NotBlank(角色名称不能为空) @Size(max=30) | 角色名称 |
| roleKey | String | 是 | @NotBlank(权限字符不能为空) @Size(max=100) | 角色权限标识 |
| roleSort | Integer | 是 | @NotNull(显示顺序不能为空) | 角色排序 |
| dataScope | String | 否 | - | 数据范围 |
| status | String | 否 | @PositiveOrZero | 状态（非负） |
| menuIds | List\<Long\> | 是 | @NotNull | 关联菜单ID列表 |
| allowRegister | Boolean | 是 | @NotNull | 是否允许注册 |
| remark | String | 否 | - | 备注 |

### UpdateRoleCommand（修改角色 入参）

> 继承 `AddRoleCommand`，含其全部字段。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleId | Long | 是 | @NotNull @PositiveOrZero | 角色ID（非负） |
| *(继承 AddRoleCommand 全部字段)* | - | - | - | 见上表 |

### UpdateDataScopeCommand（修改数据范围 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleId | Long | 是 | @NotNull @Positive | 角色ID（正数） |
| dataScope | Integer | 否 | - | 数据范围 |

### UpdateStatusCommand（修改角色状态 入参）

> 无字段级校验注解。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleId | Long | 否 | - | 角色ID |
| status | Integer | 否 | - | 状态 |

### RoleQuery（角色查询 入参）

> 继承 `AbstractPageQuery`（见附录）。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleName | String | 否 | - | 角色名称（模糊匹配） |
| roleKey | String | 否 | - | 角色标识（精确匹配） |
| status | String | 否 | - | 状态（精确匹配） |

### AllocatedRoleQuery（已分配角色用户查询 入参）

> 继承 `AbstractPageQuery`（见附录）；查询某角色下已分配的用户。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleId | Long | 否 | - | 角色ID |
| username | String | 否 | - | 用户名（模糊匹配） |
| phoneNumber | String | 否 | - | 电话号码（模糊匹配） |

### UnallocatedRoleQuery（未分配角色用户查询 入参）

> 继承 `AbstractPageQuery`（见附录）；查询未分配给某角色的用户。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleId | Long | 否 | - | 角色ID |
| username | String | 否 | - | 用户名（模糊匹配） |
| phoneNumber | String | 否 | - | 电话号码（模糊匹配） |

### RoleDTO（角色信息 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| roleId | Long | - | - | 角色ID |
| roleName | String | - | - | 角色名称 |
| roleKey | String | - | - | 角色标识 |
| roleSort | Integer | - | - | 角色排序 |
| status | Integer | - | - | 角色状态 |
| remark | String | - | - | 备注 |
| createTime | LocalDateTime | - | - | 创建时间 |
| dataScope | Integer | - | - | 数据范围 |
| allowRegister | Boolean | - | - | 是否允许注册 |
| isAdmin | Boolean | - | - | 是否为超级管理员 |
| selectedMenuList | List\<Long\> | - | - | 已选菜单ID列表 |

---

## 六、用户管理

### AddUserCommand（新增用户 入参）

> `password` 仅在新增分组（`AddValidation.class`）下必填。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| username | String | 是 | @NotBlank(用户名不能为空) | 用户名 |
| nickname | String | 是 | @NotBlank(昵称不能为空) | 昵称 |
| email | String | 否 | - | 邮件 |
| phoneNumber | String | 否 | - | 电话号码 |
| sex | Integer | 否 | - | 性别 |
| avatar | String | 否 | - | 头像 |
| password | String | 新增时必填 | @NotBlank(密码不能为空，groups=AddValidation.class) | 密码 |
| status | Integer | 否 | - | 状态 |
| roleId | Long | 是 | @NotNull(角色不能为空) | 角色ID |
| remark | String | 否 | - | 备注 |
| source | Integer | 否 | - | 来源 |

### UpdateUserCommand（修改用户 入参）

> 继承 `AddUserCommand`，含其全部字段。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID |
| *(继承 AddUserCommand 全部字段)* | - | - | - | 见上表 |

### ChangeStatusCommand（修改用户状态 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID |
| status | String | 否 | - | 状态 |

### ResetPasswordCommand（重置密码 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID |
| password | String | 否 | - | 新密码 |

### SearchUserQuery\<T\>（用户搜索查询 入参，泛型可复用）

> 继承 `AbstractPageQuery<T>`（见附录）；`timeRangeColumn` 固定为 `u.create_time`。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID（精确匹配） |
| username | String | 否 | - | 用户名（模糊匹配） |
| status | Integer | 否 | - | 状态（精确匹配） |
| phoneNumber | String | 否 | - | 电话号码（模糊匹配） |

### UserDTO（用户信息 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | - | - | 用户ID |
| roleId | Long | - | - | 角色ID |
| roleName | String | - | - | 角色名称（由 roleId 查缓存填充） |
| username | String | - | - | 用户名 |
| nickname | String | - | - | 用户昵称 |
| userType | Integer | - | - | 用户类型 |
| email | String | - | - | 邮件 |
| phoneNumber | String | - | - | 电话号码 |
| sex | Integer | - | - | 性别 |
| avatar | String | - | - | 用户头像 |
| status | Integer | - | - | 状态 |
| loginIp | String | - | - | 最后登录 IP |
| loginDate | Date | - | - | 最后登录时间 |
| creatorId | Long | - | - | 创建者ID |
| creatorName | String | - | - | 创建者用户名（由缓存填充） |
| createTime | LocalDateTime | - | - | 创建时间 |
| updaterId | Long | - | - | 修改者ID |
| updaterName | String | - | - | 修改者用户名 |
| updateTime | LocalDateTime | - | - | 修改时间 |
| remark | String | - | - | 备注 |

### UserDetailDTO（用户详情 出参）

> 用于用户编辑页，返回用户信息 + 全部可选角色 + 已分配角色 + 权限集合。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| user | UserDTO | - | - | 用户信息（结构见 UserDTO） |
| roleOptions | List\<RoleDTO\> | - | - | 所有可选角色列表（结构见 RoleDTO） |
| roleId | Long | - | - | 当前已分配角色ID |
| permissions | Set\<String\> | - | - | 权限标识集合 |

---

## 七、个人中心

### UpdateProfileCommand（修改个人资料 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID |
| sex | Integer | 否 | - | 性别 |
| nickname | String | 否 | - | 昵称 |
| phoneNumber | String | 否 | - | 电话号码 |
| email | String | 否 | - | 邮件 |

### UpdateUserPasswordCommand（修改个人密码 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID |
| newPassword | String | 否 | - | 新密码 |
| oldPassword | String | 否 | - | 旧密码 |

### UpdateUserAvatarCommand（修改个人头像 入参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| userId | Long | 否 | - | 用户ID |
| avatar | String | 否 | - | 头像 |

### UserProfileDTO（个人中心信息 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| user | UserDTO | - | - | 用户信息（结构见 UserDTO） |
| roleName | String | - | - | 角色名称 |

---

## 八、系统监控

### OnlineUserDTO（在线用户 出参）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| tokenId | String | - | - | 会话编号 |
| username | String | - | - | 用户名称 |
| ipAddress | String | - | - | 登录 IP 地址 |
| loginLocation | String | - | - | 登录地址 |
| browser | String | - | - | 浏览器类型 |
| operationSystem | String | - | - | 操作系统 |
| loginTime | Long | - | - | 登录时间（时间戳） |

### ServerInfo（服务器监控信息 出参）

> 服务器状态监控的顶层出参，聚合 CPU / 内存 / JVM / 系统 / 磁盘 五类子信息。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| cpuInfo | CpuInfo | - | - | CPU 相关信息 |
| memoryInfo | MemoryInfo | - | - | 内存相关信息 |
| jvmInfo | JvmInfo | - | - | JVM 相关信息 |
| systemInfo | SystemInfo | - | - | 服务器系统相关信息 |
| diskInfos | List\<DiskInfo\> | - | - | 磁盘信息列表 |

#### CpuInfo（CPU 信息 子对象）

> 各使用率通过 getter 换算为百分比返回。

| 字段 | 类型 | 说明 |
|------|------|------|
| cpuNum | int | 核心数 |
| total | double | CPU 总使用率（%，getter 换算） |
| sys | double | 系统使用率（%） |
| used | double | 用户使用率（%） |
| wait | double | 当前等待率（%） |
| free | double | 当前空闲率（%） |

#### MemoryInfo（内存信息 子对象）

> total/used/free getter 换算为 GB；额外提供 usage 使用率（%）。

| 字段 | 类型 | 说明 |
|------|------|------|
| total | double | 内存总量（GB） |
| used | double | 已用内存（GB） |
| free | double | 剩余内存（GB） |
| usage | double | 内存使用率（%，仅 getter，无字段） |

#### JvmInfo（JVM 信息 子对象）

> total/max/free/used getter 换算为 MB；name/startTime/runTime/inputArgs 等为纯 getter 计算值（无字段）。

| 字段 | 类型 | 说明 |
|------|------|------|
| total | double | JVM 占用内存总数（MB） |
| max | double | JVM 最大可用内存（MB） |
| free | double | JVM 空闲内存（MB） |
| used | double | JVM 已用内存（MB，getter 计算） |
| usage | double | JVM 内存使用率（%，getter 计算） |
| version | String | JDK 版本 |
| home | String | JDK 路径（Native 模式返回提示文本） |
| name | String | JVM 名称（getter 计算） |
| startTime | String | JDK 启动时间（getter 计算） |
| runTime | String | JDK 运行时长（getter 计算） |
| inputArgs | String | 运行参数（getter 计算） |

#### SystemInfo（系统信息 子对象）

| 字段 | 类型 | 说明 |
|------|------|------|
| computerName | String | 服务器名称 |
| computerIp | String | 服务器 IP |
| userDir | String | 项目路径 |
| osName | String | 操作系统名称 |
| osArch | String | 系统架构 |

#### DiskInfo（磁盘信息 子对象）

| 字段 | 类型 | 说明 |
|------|------|------|
| dirName | String | 盘符路径 |
| sysTypeName | String | 盘符类型 |
| typeName | String | 文件类型 |
| total | String | 总大小（已格式化，如 "100.0 GB"） |
| free | String | 剩余大小（已格式化） |
| used | String | 已使用量（已格式化） |
| usage | double | 使用率（%） |

---

## 附录：查询基类公共字段

所有 `*Query` 类均继承下列基类，前端在调用列表 / 分页接口时可附带这些公共参数。

### AbstractQuery\<T\>（所有查询基类，排序 + 时间范围）

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| orderColumn | String | 否 | - | 排序字段（仅允许字母/数字/下划线，防注入） |
| orderDirection | String | 否 | - | 排序方向：`ascending` 升序 / `descending` 降序 |
| timeRangeColumn | String | 否 | - | 时间范围筛选列（多由后端固定设置） |
| beginTime | Date | 否 | - | 起始时间，格式 `yyyy-MM-dd` |
| endTime | Date | 否 | - | 结束时间，格式 `yyyy-MM-dd` |

### AbstractPageQuery\<T\>（分页查询基类，继承 AbstractQuery）

> 除上表字段外额外提供分页参数。`MenuQuery` 直接继承 `AbstractQuery`，**不含**下列分页字段。

| 字段 | 类型 | 必填 | 校验 | 说明 |
|------|------|------|------|------|
| pageNum | Integer | 否 | @Max(200) | 页码，默认 1，最大 200 |
| pageSize | Integer | 否 | @Max(500) | 每页大小，默认 10，最大 500 |
