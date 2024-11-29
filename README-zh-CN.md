# gRPC-Spring-Enhancements

README: [English](README.md) | [中文](README-zh-CN.md)

## 特性
- 使用 `@GrpcFallback` 注解可以实现自动注入和配置客户端默认实现
- 无需单独配置即可开始使用

### 用法

使用以下命令添加 Maven 依赖项：

````xml
<dependency>
    <groupId>com.github.arvinjr</groupId>
    <artifactId>grpc-spring-enhancements</artifactId>
    <version>1.0.0</version>
</dependency>

````

使用 Gradle 添加依赖：

````gradle
dependencies {
  implementation 'com.github.arvinjr:grpc-spring-enhancements:1.0.0'
}
````

在客户端创建默认实现并注入 `Spring IOC` 容器，使用 `@GrpcFallback` 注解使该插件将Bean注入容器。

```java
@Service
@GrpcFallback
public class MyServiceFallbackImpl implements MyService.AsyncService {
	
}
```

## 示例项目

在 [这里](examples) 可以查看更多关于该项目的示例。
