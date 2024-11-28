package com.github.arvinjr.fallback.controller;

import com.github.arvinjr.grpc.HelloProto;
import com.github.arvinjr.grpc.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author arvin
 * @date 2024/11/27
 */
@RestController
@Slf4j
public class HelloController {

	@GrpcClient(value = "GLOBAL")
	private HelloServiceGrpc.HelloServiceBlockingStub stub;

	@GrpcClient(value = "GLOBAL")
	private HelloServiceGrpc.HelloServiceStub asyncStub;

	@RequestMapping("/test1")
	public String test1(String name) {

		log.info("hello收到：{}", name);
		final HelloProto.HelloResponse helloResponse = stub.hello(HelloProto.HelloRequest.newBuilder().setName(name).build());
		return "服务端响应: " + helloResponse.getResult();
	}

	@RequestMapping("/test2")
	public String test2() throws InterruptedException {
		final HelloProto.HelloResponse[] response = new HelloProto.HelloResponse[1];
		final StreamObserver<HelloProto.HelloRequest> request = asyncStub.cs2s(new StreamObserver<HelloProto.HelloResponse>() {
			@Override
			public void onNext(HelloProto.HelloResponse value) {
				log.info("cs2s收到：{}", value.getAllFields());
				response[0] = value;
			}

			@Override
			public void onError(Throwable t) {

			}

			@Override
			public void onCompleted() {
				log.info("onCompleted");
			}
		});
		for (int i = 0; i < 10; i++) {
			final HelloProto.HelloRequest helloRequest = HelloProto.HelloRequest.newBuilder()
					.setName("cs2s test send " + i)
					.build();

			request.onNext(helloRequest);
			Thread.sleep(1000);
		}
		request.onCompleted();
		Thread.sleep(3000);
		final String result = response[0].getResult();
		return result;
	}

}
