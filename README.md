# swagger-demo

Spring Boot 3 整合 Springdoc OpenAPI / Swagger UI 的可运行教程项目。依据 `SpringBoot3整合Swagger接口文档详细教程.docx` 落地，代码、配置、文档均已在本机实测通过。项目不依赖数据库，启动即可验证。

## 1、项目简介

- 通过 `springdoc-openapi-starter-webmvc-ui` 自动生成 OpenAPI 3 描述，并使用内置 Swagger UI 在线查看、调试 REST 接口。
- 用户管理模块提供列表、详情、新增、删除、搜索、文件上传、profile 共 7 个接口；订单管理模块提供列表、详情 2 个接口，用于演示文档分组与枚举 Schema。
- 覆盖 `@Tag`、`@Operation`、`@Parameter`、`@Schema`、`@ApiResponses`、`@SecurityScheme`、`@SecurityRequirement`、`GroupedOpenApi` 等典型注解与配置。
- 数据保存在内存仓储中，CRUD 行为自洽（新增后可查、删除后查不到），便于直接调试。

## 2、技术栈及版本

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| JDK | 17.0.12 | Spring Boot 3.x 最低要求 |
| Spring Boot | 3.5.16 | 3.5.x 最终 OSS 版本（2026-06-25 发布） |
| springdoc-openapi | 2.10.0 | 官方 v2 当前版本，Release Notes 明确「Spring Boot 3.5.x line (built on Spring Boot 3.5.16)」 |
| Swagger UI | 5.34.0 | 由 springdoc 2.10.0 传递引入 |
| swagger-annotations-jakarta | 2.2.55 | 依赖树中仅此一个版本，无注解冲突 |
| Spring Web MVC | 随 Boot BOM | `spring-boot-starter-web` |
| 参数校验 | 随 Boot BOM | `spring-boot-starter-validation` |
| Maven | 3.9.3 | 构建工具 |

版本选型依据：Spring Boot 3.5.x 与 springdoc 2.10.0 的对应关系来自 springdoc 官网 v2 文档与 2.10.0 Release Notes；两者构建基线同为 3.5.16，属于同一兼容组合，不要只因为依赖能下载就认定兼容。

## 3、快速开始

### 3.1 环境要求

- JDK 17+、Maven 3.6+（本项目未使用 Wrapper，直接调用本机 Maven）
- 8080 端口未被占用（可在启动时用 `--server.port=xxxx` 覆盖）

### 3.2 构建

```bash
mvn -B -f D:/project/swagger-demo/pom.xml clean package
```

构建会执行 6 个集成测试，并生成可执行 jar：`target/swagger-demo-1.0.0.jar`。

### 3.3 启动

```bash
java -jar D:/project/swagger-demo/target/swagger-demo-1.0.0.jar
```

启动约 2 秒，控制台出现 `Started SwaggerDemoApplication` 即就绪。

### 3.4 验证地址

| 地址 | 说明 |
| --- | --- |
| http://localhost:8080/swagger-ui.html | Swagger UI 页面（右上角可切换分组、Authorize） |
| http://localhost:8080/v3/api-docs | OpenAPI 3 JSON（全量） |
| http://localhost:8080/v3/api-docs.yaml | OpenAPI 3 YAML |
| http://localhost:8080/v3/api-docs/%E7%94%A8%E6%88%B7%E6%A8%A1%E5%9D%97 | 用户模块分组文档 |
| http://localhost:8080/v3/api-docs/%E8%AE%A2%E5%8D%95%E6%A8%A1%E5%9D%97 | 订单模块分组文档 |
| http://localhost:8080/v3/api-docs/swagger-config | Swagger UI 读取的分组配置 |
| http://localhost:8080/api/users | 用户列表接口 |

### 3.5 生产环境关闭文档

```bash
java -jar target/swagger-demo-1.0.0.jar --spring.profiles.active=prod
```

`application-prod.yml` 将 `springdoc.api-docs.enabled` 与 `springdoc.swagger-ui.enabled` 置为 `false`，实测业务接口仍返回 200，而文档、UI 端点均返回 404。

### 3.6 一键端到端验证

先启动应用，再执行：

```bash
python tools/verify_endpoints.py
```

脚本覆盖 36 项断言（业务接口、异常分支、OpenAPI JSON/YAML、分组隔离、Swagger UI、favicon），输出 `TOTAL=36 PASS=36 FAIL=0`。

## 4、工程结构

```text
swagger-demo
├─ pom.xml
├─ README.md
├─ docs/
│  └─ SpringBoot3整合Swagger接口文档实战教程.md   # 公众号教程（最终稿）
├─ tools/
│  ├─ verify_endpoints.py                        # 端到端验证脚本
│  ├─ build.log / deps.log / app.log              # 构建、依赖树、运行日志
│  └─ app-prod.log                               # prod profile 验证日志
└─ src
   ├─ main/java/com/example/swagger/
   │  ├─ SwaggerDemoApplication.java
   │  ├─ common/
   │  │  ├─ ApiError.java                        # 统一错误响应体
   │  │  └─ GlobalExceptionHandler.java         # 全局异常处理
   │  ├─ config/
   │  │  ├─ OpenApiConfig.java                  # @OpenAPIDefinition 全局信息
   │  │  ├─ SwaggerSecurityConfig.java          # @SecurityScheme Bearer JWT
   │  │  └─ OpenApiGroupConfig.java             # GroupedOpenApi 分组
   │  ├─ controller/
   │  │  ├─ user/UserController.java            # 用户模块（7 个接口）
   │  │  └─ order/OrderController.java          # 订单模块（2 个接口）
   │  ├─ dto/
   │  │  ├─ UserCreateRequest.java
   │  │  ├─ UserResponse.java
   │  │  ├─ OrderResponse.java
   │  │  └─ OrderStatus.java                    # 枚举
   │  ├─ exception/ResourceNotFoundException.java
   │  └─ repository/UserRepository.java         # 线程安全内存仓储
   ├─ main/resources/
   │  ├─ application.yml
   │  └─ application-prod.yml
   └─ test/java/com/example/swagger/
      └─ SwaggerDemoApplicationTests.java        # 6 个集成测试
```

## 5、关键配置说明

### 5.1 application.yml 配置项

| 配置项 | 当前值 | 作用 |
| --- | --- | --- |
| `server.port` | 8080 | 应用端口 |
| `spring.servlet.multipart.*` | 5MB / 10MB | 文件上传大小上限 |
| `springdoc.api-docs.enabled` | true | 是否启用 OpenAPI 文档接口 |
| `springdoc.api-docs.path` | /v3/api-docs | JSON 文档地址 |
| `springdoc.swagger-ui.enabled` | true | 是否启用 Swagger UI |
| `springdoc.swagger-ui.path` | /swagger-ui.html | UI 页面入口 |
| `springdoc.swagger-ui.operations-sorter` | alpha | 接口按名称排序 |
| `springdoc.swagger-ui.tags-sorter` | alpha | 分组标签排序 |
| `springdoc.swagger-ui.display-request-duration` | true | 显示请求耗时 |
| `logging.charset.console` | UTF-8 | 避免 Windows 控制台中文乱码 |

### 5.2 保留方法参数名

`pom.xml` 显式配置 `maven-compiler-plugin` 的 `<parameters>true</parameters>`。构建日志显示 `javac [debug parameters release 17]`，确保 springdoc 能正确读取参数名；否则 Boot 3.2+ 的 Parameter Name Discovery 变化会导致文档里参数名缺失。

### 5.3 分组与全局扫描包（重要）

`application.yml` 中**故意注释掉了** `springdoc.packages-to-scan`。实测结论：当项目定义了 `GroupedOpenApi` Bean 时，全局 `packages-to-scan` 会与分组自身的 `packagesToScan` 叠加，导致两个分组都返回全部路径、分组隔离失效。

- 未启用全局配置时：`用户模块` 分组只含 5 个 `/api/users**` 路径，`订单模块` 只含 2 个 `/api/orders**` 路径。
- 若确实需要全局扫描包，请不要同时依赖分组的 `packagesToScan`，改用 `pathsToMatch` 按路径过滤。

## 6、接口清单

| 方法 | 路径 | 说明 | 成功 | 异常 |
| --- | --- | --- | --- | --- |
| GET | /api/users | 用户列表 | 200 | — |
| GET | /api/users/{id} | 按 ID 查询用户 | 200 | 404 / 400（ID 非数字） |
| POST | /api/users | 创建用户 | 201 | 400（校验失败 / JSON 非法） |
| DELETE | /api/users/{id} | 删除用户 | 204 | 404 |
| GET | /api/users/search | 按姓名模糊搜索 + 分页 | 200 | 400（分页参数非法） |
| POST | /api/users/upload | multipart 文件上传 | 200 | 400（缺 file / 空文件） |
| GET | /api/users/profile | 当前用户信息（声明 Bearer 认证） | 200 | — |
| GET | /api/orders | 订单列表，可按 status 筛选 | 200 | 400（枚举值非法） |
| GET | /api/orders/{orderNo} | 按订单号查询 | 200 | 404 |

创建用户请求体示例：

```json
{ "username": "王五", "email": "wangwu@example.com" }
```

成功响应（201）：

```json
{ "id": 1003, "username": "王五", "email": "wangwu@example.com" }
```

## 7、文档能力覆盖

| 能力 | 落地位置 | 关键注解 / 类 |
| --- | --- | --- |
| 全局文档信息 | `OpenApiConfig` | `@OpenAPIDefinition`、`@Info`、`@Contact` |
| 接口分组标签 | 两个 Controller | `@Tag` |
| 接口摘要与响应码 | Controller 方法 | `@Operation`、`@ApiResponse`、`@ApiResponses` |
| 参数说明 | 方法参数 | `@Parameter` |
| DTO / 字段 / 必填 | `dto` 包 | `@Schema`、`@NotBlank`、`@Size`、`@Email` |
| 文件上传 | `/api/users/upload` | `consumes = multipart/form-data` + `MultipartFile` |
| 枚举值 | `OrderStatus` | Java enum（springdoc 内联为 `enum` 列表） |
| Bearer JWT | `SwaggerSecurityConfig` + `/profile` | `@SecurityScheme`、`@SecurityRequirement` |
| 多模块分组 | `OpenApiGroupConfig` | `GroupedOpenApi` |

## 8、统一异常响应

成功响应保持业务 DTO 原结构（与教程文档示例一致），异常响应统一为 `ApiError`：

```json
{
  "code": 400,
  "message": "请求参数校验失败",
  "fieldErrors": { "username": "姓名不能为空", "email": "邮箱格式不正确" },
  "timestamp": 1763635200000
}
```

| 异常 | HTTP | 处理位置 |
| --- | --- | --- |
| `MethodArgumentNotValidException` | 400 | 输出字段级错误明细 |
| 参数类型不匹配 / 缺参 / JSON 不可读 / 非法业务参数 | 400 | 统一错误体 |
| `ResourceNotFoundException` | 404 | 用户、订单不存在 |
| `NoResourceFoundException`（如 `/favicon.ico`） | 404 | 避免兜底 500 和 ERROR 堆栈 |

## 9、踩坑备忘

1. **不要引入 Springfox**。Spring Boot 3 迁移到 Jakarta EE，Springfox 在 Boot 3 下易启动失败；只用 springdoc 2.x。
2. **不要混用多个 swagger-annotations 版本**。`mvn dependency:tree` 实测只有 `swagger-annotations-jakarta:2.2.55`。
3. **`@OpenAPIDefinition` / `@SecurityScheme` 所在类要加 `@Configuration`**，否则类上没有 Bean 定义时注解可能被 AOT/编译器优化掉。
4. **全局 `packages-to-scan` 与 `GroupedOpenApi.packagesToScan` 会叠加**，导致分组隔离失效（见 5.3）。
5. **springdoc 2.10.0 把 Java 枚举内联到字段属性**，不会生成独立的 `OrderStatus` Schema；断言文档时应检查 `OrderResponse.properties.status.enum`。
6. **显式处理 `NoResourceFoundException`**，否则 `/favicon.ico` 会落到兜底异常并打印 ERROR 堆栈。
7. `TestRestTemplate.getStatusCode()` 返回 `HttpStatusCode`，不能直接赋给 `HttpStatus`。

## 10、验证情况

| 项目 | 结果 |
| --- | --- |
| `mvn -B clean package` | BUILD SUCCESS，6 个集成测试 `Failures: 0, Errors: 0` |
| 依赖树 | springdoc 2.10.0 单一版本、swagger-ui 5.34.0、无 Springfox、无注解冲突 |
| 应用启动 | 1.979s，Tomcat 8080，springdoc 初始化 241ms |
| 端到端脚本 | `TOTAL=36 PASS=36 FAIL=0` |
| 启动日志 | 零 ERROR / 零 WARN |
| prod profile | 业务接口 200，文档与 UI 均 404 |

## 11、生产化建议

- 生产环境按环境关闭或限制 Swagger UI（本项目已提供 `prod` profile 示例），不要默认对公网开放。
- 文档中不要出现真实密码、密钥、Token、身份证号、手机号等敏感信息；`dev@example.com` 仅为演示。
- `Authorize` 只负责在调试请求中携带凭证，不替代后端 Spring Security 鉴权；本项目未引入 Spring Security，集成时须按策略放行 `/swagger-ui.html`、`/swagger-ui/**`、`/v3/api-docs/**`。
- 内存仓储仅用于演示，真实项目应替换为持久层，并补充事务、并发控制与权限校验。
- 对外 API 建议把 OpenAPI 文档纳入版本管理与发布流程，避免实现与文档长期不一致。
