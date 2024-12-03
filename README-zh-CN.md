# gRPC Spring Enhancements

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.arvinjr/grpc-spring-enhancements)](https://central.sonatype.com/artifact/io.github.arvinjr/grpc-spring-enhancements/overview)
[![javadoc](https://javadoc.io/badge2/io.github.arvinjr/grpc-spring-enhancements/javadoc.svg)](https://javadoc.io/doc/io.github.arvinjr/grpc-spring-enhancements)
[![GitHub License](https://img.shields.io/github/license/ArvinJr/grpc-enhancer)](LICENSE.txt)
![GitHub Tag](https://img.shields.io/github/v/tag/ArvinJr/grpc-enhancer)

README: [English](README.md) | [中文](README-zh-CN.md)

文档：[English](https://arvinjr.github.io/gRPC-Enhancer/en) | [中文](https://arvinjr.github.io/gRPC-Enhancer/zh-CN)

## 特性
- 使用 `@GrpcFallback` 注解可以实现自动注入和配置客户端默认实现
- 无需单独配置即可开始使用

### 用法

使用以下命令添加 Maven 依赖项：

```xml
<dependency>
    <groupId>io.github.arvinjr</groupId>
    <artifactId>grpc-spring-enhancements</artifactId>
    <version>1.0.0</version>
</dependency>
```

使用 Gradle 添加依赖：

```gradle
dependencies {
  implementation 'io.github.arvinjr:grpc-spring-enhancements:1.0.0'
}
```

在客户端创建默认实现并注入 `Spring IOC` 容器，使用 `@GrpcFallback` 注解使该插件将Bean注入容器。

```java
@Service
@GrpcFallback
public class MyServiceFallbackImpl implements MyService.AsyncService {
	
}
```

## 示例项目

在 [这里](examples) 可以查看更多关于该项目的示例。
