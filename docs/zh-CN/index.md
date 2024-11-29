# gRPC-Spring-Enhancements 文档

gRPC-Spring-Enhancements 是一个基于 [grpc-spring](https://github.com/grpc-ecosystem/grpc-spring) 的增强工具包，当前已实现类似 [Spring Cloud OpenFeign fallback](https://docs.spring.io/spring-cloud-openfeign/reference/spring-cloud-openfeign.html#spring-cloud-feign-circuitbreaker-fallback) 功能。

## 快速开始
### 目录
- [项目初始化](#项目初始化)
- [创建 gRPC 服务定义](#创建 gRPC 服务定义)
- [定义客户端默认实现](#定义客户端默认实现)
- [访问客户端](#访问客户端)
- [示例项目](#示例项目)

### 项目初始化
#### Maven
```xml
    <dependencies>
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-client-spring-boot-starter</artifactId>
        </dependency>
    
        <dependency>
            <groupId>com.github.arvinjr</groupId>
            <artifactId>grpc-spring-enhancements</artifactId>
        </dependency>

        <dependency>
            <groupId>example</groupId>
            <artifactId>my-grpc-interface</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
```

#### Gradle
```groovy
apply plugin: 'org.springframework.boot'

dependencies {
    compile('org.springframework.boot:spring-boot-starter')
    compile('net.devh:grpc-client-spring-boot-starter')
    compile('com.github.arvinjr:grpc-spring-enhancements')
    compile('my-example:my-grpc-interface')
}

buildscript {
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
}
```
参考 [grpc-spring 的配置](https://grpc-ecosystem.github.io/grpc-spring/zh-CN/client/configuration.html) 完成 `channel` 配置。

### 创建 gRPC 服务定义

将您的 protobuf 定义/`.proto`文件放入`src/main/proto`。 有关编写 protobuf 文件的信息，请参阅官方的 [protobuf 文档](https://developers.google.com/protocol-buffers/docs/proto3)。

您的 `.proto` 文件跟如下的示例类似：

````proto
syntax = "proto3";

package com.github.arvinjr.example;

option java_multiple_files = true;
option java_package = "com.github.arvinjr.examples.lib";
option java_outer_classname = "HelloWorldProto";

// The greeting service definition.
service MyService {
    // Sends a greeting
    rpc SayHello (HelloRequest) returns (HelloReply) {
    }
}

// The request message containing the user's name.
message HelloRequest {
    string name = 1;
}

// The response message containing the greetings
message HelloReply {
    string message = 1;
}
````

配置 maven/gradle protobuf 插件使其调用 [`protoc`](https://mvnrepository.com/artifact/com.google.protobuf/protoc) 编译器，并使用 [`protoc-gen-grpc-java`](https://mvnrepository.com/artifact/io.grpc/protoc-gen-grpc-java) 插件并生成数据类、grpc 服务类 `ImplicBase`s 和 `Stub`。 请注意，其他插件，如 [reactive-grpc](https://github.com/salesforce/reactive-grpc) 可能会生成其他额外 / 替代类。 然而，它们也可以同样的方式使用。

### 定义客户端默认实现
创建一个 `Myservice.AsyncService` 实现并注入 `Spring IOC` 容器。
```java
import example.HelloReply;
import example.HelloRequest;
import example.MyServiceGrpc;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import com.github.arvinjr.enhancer.inject.GrpcFallback;

@Service
@GrpcFallback
public class MyServiceFallbackImpl implements MyService.AsyncService {

	@Override
	public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
		HelloReply reply = HelloReply.newBuilder()
				.setMessage("Fallback Hello ==> " + request.getName())
				.build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}
	
}
```

### 访问客户端
流程与 [grpc-spring](https://grpc-ecosystem.github.io/grpc-spring/zh-CN/client/getting-started.html#%E8%AE%BF%E9%97%AE%E5%AE%A2%E6%88%B7%E7%AB%AF) 一致，直接通过 `@GrpcClient` 或 `@GrpcClientBean` 的方式注入 `Stub` 即可，在 `Spring Boot` 初始化时 `gRPC-Spring-Enhancements` 会自动将标注了 `@GrpcFallback` 注解的默认实现自动注入相关 `Stub` 中。
```java
import example.HelloRequest;
import example.MyServiceGrpc.MyServiceBlockingStub;

import net.devh.boot.grpc.client.inject.GrpcClient;

import org.springframework.stereotype.Service;

@Service
public class FoobarService {

    @GrpcClient("myService")
    private MyServiceBlockingStub myServiceStub;

    public String receiveGreeting(String name) {
        HelloRequest request = HelloRequest.newBuilder()
                .setName(name)
                .build();
        return myServiceStub.sayHello(request).getMessage();
    }

}
```

### 示例项目
在 [这里](https://github.com/ArvinJr/gRPC-Enhancer/tree/master/examples) 可以查看更多关于该项目的示例。