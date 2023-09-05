# space-gateway

[English](./README.md) | 简体中文

## 项目简介

space-gateway 用于请求路由、加密解密、身份验证和授权、账户管理、消息推送、视频点播服务文件获取。

该应用具有自定义的 HTTP 根路径：/space。 API 地址为：<https://localhost:8080/space/q/swagger-ui/>

## 模块介绍设计

- gateway: 请求路由、加密解密、身份验证和授权
- account: 账户管理
- push: 消息推送依赖
- vod: 视频点播服务文件获取依赖 [ao-space/space-media-vod](https://github.com/ao-space/space-media-vod)

## 构建与运行

### 在 dev 模式下运行应用程序

您可以使用以下命令在 dev 模式下运行应用程序，从而启用实时编码：

注意：在 dev 模式下运行应用程序需要 docker 环境，如果没有安装 docker 则需要在 application.yml 下配置 redis 和 postgresql 连接参数。

```shell
./mvnw compile quarkus:dev
```

注意: Quarkus 现在附带了 Dev UI，仅在 dev 模式下可用，访问地址为 <http://localhost:8080/q/dev/。>

### 打包并运行应用程序

可以使用以下命令打包应用程序：

```shell
./mvnw package
```

它将在 `target/quarkus-app/` 目录中生成 `quarkus-run.jar` 文件。请注意，它不是 `über-jar`，因为依赖项被复制到 `target/quarkus-app/lib/` 目录中。

如果要构建 `über-jar`，请执行以下命令：

```shell
./mvnw package -Dquarkus.package.type=uber-jar
```

现在，可以使用 `java -jar target/quarkus-app/quarkus-run.jar` 运行应用程序。

### 创建本机可执行文件和 Docker 镜像

您可以使用以下命令创建本机可执行文件：

```shell
./mvnw package -Pnative
```

或者，如果您没有安装 GraalVM，则可以在容器中运行本机可执行文件构建，使用以下命令：

```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

然后，您可以使用 `./target/space-gateway-1.0.0-SNAPSHOT-runner` 执行本机可执行文件。

创建本机 Docker 镜像：

```shell
docker build -f src/main/docker/Dockerfile.native -t native/space-gateway .
```

并运行上述本机镜像：

```text
docker run -i --rm -p 8080:8080 native/space-gateway
```

如果您想了解有关构建本机可执行文件的更多信息，请参阅 <https://quarkus.io/guides/maven-tooling.html> 。

### 和傲空间其它服务一起运行

参考 [build-and-deploy](https://github.com/ao-space/ao.space/blob/dev/docs/build-and-deploy_CN.md)。

## 注意事项

## 贡献指南

我们非常欢迎对本项目进行贡献。以下是一些指导原则和建议，希望能够帮助您参与到项目中来。

[贡献指南](https://github.com/ao-space/ao.space/blob/dev/docs/cn/contribution-guidelines.md)

## 联系我们

- 邮箱：<developer@ao.space>
- [官方网站](https://ao.space)
- [讨论组](https://slack.ao.space)

## 感谢您的贡献

最后，感谢您对本项目的贡献。我们欢迎各种形式的贡献，包括但不限于代码贡献、问题报告、功能请求、文档编写等。我们相信在您的帮助下，本项目会变得更加完善和强大。
