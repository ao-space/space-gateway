# space-gateway

English | [简体中文](./README_cn.md)

## Project Introduction

space-gateway is used for request routing, encryption and decryption, authentication and authorization, account management, video on demand service.

This application has a customized http root path: `/space`. And for api: <https://localhost:8080/space/q/swagger-ui/>

## Module Design Introduction

- gateway: Request routing, encryption and decryption, identity verification and authorization
- account: Account management
- push: Message push dependency
- vod: Video on demand service file acquisition dependency [ao-space/space-media-vod](https://github.com/ao-space/space-media-vod)

## Build and Run

### Running the application in dev mode

You can run your application in dev mode that enables live coding using:

Note: The docker environment is required to run the application in dev mode. If docker is not installed,
redis and postgresql connection parameters need to be configured under application.yml.

```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

### Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

### Creating a native executable and Docker image

You can create a native executable using:

```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/space-gateway-1.0.0-SNAPSHOT-runner`

Create native docker image:

```shell script
docker build -f src/main/docker/Dockerfile.native -t native/space-gateway .
```

And run above native image:

```shell script
docker run -i --rm -p 8080:8080 native/space-gateway
```

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling.html>.

### Combined with other services in AO.space

Refer to [build-and-deploy](https://github.com/ao-space/ao.space/blob/dev/docs/build-and-deploy.md).

## Notes

## Contribution Guidelines

Contributions to this project are very welcome. Here are some guidelines and suggestions to help you get involved in the project.

[Contribution Guidelines](https://github.com/ao-space/ao.space/blob/dev/docs/en/contribution-guidelines.md)

## Contact us

- Email: <developer@ao.space>
- [Official Website](https://ao.space)
- [Discussion group](https://slack.ao.space)

## Thanks for your contribution

Finally, thank you for your contribution to this project. We welcome contributions in all forms, including but not limited to code contributions, issue reports, feature requests, documentation writing, etc. We believe that with your help, this project will become more perfect and stronger.
