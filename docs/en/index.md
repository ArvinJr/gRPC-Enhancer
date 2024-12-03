# gRPC Spring Enhancements Documentation

`gRPC Spring Enhancements` is an extension toolkit based on `grpc-spring`, currently implementing a functionality similar to the `fallback` feature in `Spring Cloud OpenFeign`.

## Quick Start
### Table of Contents
- [Project Setup](#project-setup)
- [Creating the gRPC-Service Definitions](#creating-the-grpc-service-definitions)
- [Define the default implementation for the client](#define-the-default-implementation-for-the-client)
- [Access the client](#access-the-client)
- [Example Projects](#example-projects)

### Project Setup
#### Maven
```xml
    <dependencies>
        <dependency>
            <groupId>net.devh</groupId>
            <artifactId>grpc-client-spring-boot-starter</artifactId>
        </dependency>
    
        <dependency>
            <groupId>io.github.arvinjr</groupId>
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
    compile('io.github.arvinjr:grpc-spring-enhancements')
    compile('my-example:my-grpc-interface')
}

buildscript {
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
    }
}
```
Complete the `channel` configuration by referring to the [grpc-spring configuration](https://grpc-ecosystem.github.io/grpc-spring/zh-CN/client/configuration.html).

### Creating the gRPC-Service Definitions

Place your protobuf definitions `.proto` files in `src/main/proto`. For writing protobuf files please refer to the official [protobuf docs](https://developers.google.com/protocol-buffers/docs/proto3).

Your `.proto` files will look similar to the example below:
```proto
syntax = "proto3";

package io.github.arvinjr.example;

option java_multiple_files = true;
option java_package = "io.github.arvinjr.examples.lib";
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
```
The configured maven/gradle protobuf plugins will then use invoke the
[`protoc`](https://mvnrepository.com/artifact/com.google.protobuf/protoc) compiler with the
[`protoc-gen-grpc-java`](https://mvnrepository.com/artifact/io.grpc/protoc-gen-grpc-java) plugin and generate the data
classes, grpc service `ImplBase`s and `Stub`s. Please note that other plugins such as
[reactive-grpc](https://github.com/salesforce/reactive-grpc) might generate additional/alternative classes that you have
to use instead. However, they can be used in a similar fashion.

### Define the default implementation for the client
Create an implementation of `MyService.AsyncService` and inject it into the `Spring IOC` container.
```java
import example.HelloReply;
import example.HelloRequest;
import example.MyServiceGrpc;

import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import io.github.arvinjr.enhancer.inject.GrpcFallback;

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

### Access the client
The process is consistent with [grpc-spring](https://grpc-ecosystem.github.io/grpc-spring/zh-CN/client/getting-started.html#%E8%AE%BF%E9%97%AE%E5%AE%A2%E6%88%B7%E7%AB%AF). You can directly inject the `Stub` using `@GrpcClient` or `@GrpcClientBean`. During `Spring Boot` initialization, `gRPC-Spring-Enhancements` will automatically inject the default implementation annotated with the `@GrpcFallback` annotation into the relevant `Stub`.
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

### Example Projects
Read more about our example projects [here](https://github.com/ArvinJr/gRPC-Enhancer/tree/master/examples).
