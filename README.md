# gRPC Spring Enhancements

[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.arvinjr/grpc-spring-enhancements)](https://central.sonatype.com/artifact/io.github.arvinjr/grpc-spring-enhancements/overview)
[![javadoc](https://javadoc.io/badge2/io.github.arvinjr/grpc-spring-enhancements/javadoc.svg)](https://javadoc.io/doc/io.github.arvinjr/grpc-spring-enhancements)
[![GitHub License](https://img.shields.io/github/license/ArvinJr/grpc-enhancer)](LICENSE.txt)
![GitHub Tag](https://img.shields.io/github/v/tag/ArvinJr/grpc-enhancer)

README: [English](README.md) | [中文](README-zh-CN.md)

Documentation: [English](https://arvinjr.github.io/gRPC-Enhancer/en) | [中文](https://arvinjr.github.io/gRPC-Enhancer/zh-CN)

## Features
- Using the `@GrpcFallback` annotation enables automatic injection and configuration of client default implementations.
- Ready to use without additional configuration.

### Usage
To add a dependency using Maven, use the following:
```xml
<dependency>
    <groupId>io.github.arvinjr</groupId>
    <artifactId>grpc-spring-enhancements</artifactId>
    <version>1.0.0</version>
</dependency>
```

To add a dependency using Gradle:
```gradle
dependencies {
  implementation 'io.github.arvinjr:grpc-spring-enhancements:1.0.0'
}
```

Create a default implementation on the client and inject it into the `Spring IOC` container. Use the `@GrpcFallback` annotation to allow the plugin to inject the Bean into the container.
```java
@Service
@GrpcFallback
public class MyServiceFallbackImpl implements MyService.AsyncService {
	
}
```

## Example-Projects
Read more about our example projects [here](examples).