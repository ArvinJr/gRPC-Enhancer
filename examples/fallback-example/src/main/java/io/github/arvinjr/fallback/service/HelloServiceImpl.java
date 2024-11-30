package io.github.arvinjr.fallback.service;

import io.github.arvinjr.enhancer.inject.GrpcFallback;
import io.github.arvinjr.grpc.HelloProto;
import io.github.arvinjr.grpc.HelloServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author arvin
 * @date 2024/11/22
 */
@Service
@GrpcFallback
@Slf4j
public class HelloServiceImpl implements HelloServiceGrpc.AsyncService {

	@Override
	public void hello(HelloProto.HelloRequest request, StreamObserver<HelloProto.HelloResponse> responseObserver) {
		log.info("默认回调收到：{}", request.getName());

		responseObserver.onNext(HelloProto.HelloResponse.newBuilder().setResult("默认回调响应").build());
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<HelloProto.HelloRequest> cs2s(StreamObserver<HelloProto.HelloResponse> responseObserver) {
		return new StreamObserver<HelloProto.HelloRequest>() {

			private final List<String> messages = new ArrayList<>();

			@Override
			public void onNext(HelloProto.HelloRequest value) {
				final String name = value.getName();
				log.info("默认回调收到：{}", name);
				messages.add(name);
			}

			@Override
			public void onError(Throwable t) {

			}

			@Override
			public void onCompleted() {
				responseObserver.onNext(HelloProto.HelloResponse
						.newBuilder()
						.setResult("默认回调收到了" + messages.size() + "条消息")
						.build());
				responseObserver.onCompleted();
			}
		};
	}
}
