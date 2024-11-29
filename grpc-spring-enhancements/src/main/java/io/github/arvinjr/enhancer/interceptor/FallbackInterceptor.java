package io.github.arvinjr.enhancer.interceptor;

import io.github.arvinjr.enhancer.util.EnhancementStreamObserver;
import io.github.arvinjr.enhancer.util.FallbackUtil;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * gRPC fallback拦截器
 *
 * @author arvin
 * @date 2024/11/23
 */
@Slf4j
@AllArgsConstructor
public class FallbackInterceptor implements ClientInterceptor {

	// 默认实现实例
	private Object fallbackInstance;

	@Override
	public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method,
	                                                           CallOptions callOptions,
	                                                           Channel next) {

		return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
			private final EnhancementStreamObserver<ReqT> requestStreamObserver = new EnhancementStreamObserver<>();

			@Override
			public void sendMessage(ReqT message) {
				requestStreamObserver.onNext(message);
				super.sendMessage(message);
			}

			@Override
			public void halfClose() {
				requestStreamObserver.onCompleted();
				super.halfClose();
			}

			@Override
			public void cancel(@Nullable String message, @Nullable Throwable cause) {
				requestStreamObserver.onError(cause);
				super.cancel(message, cause);
			}

			@Override
			public void start(Listener<RespT> responseListener, Metadata headers) {
				super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
					@Override
					public void onClose(Status status, Metadata trailers) {
						if (!status.isOk() && !status.equals(Status.CANCELLED)) {
							log.warn("{} 请求出现异常开始调用默认实现", method.getFullMethodName());

							// 在proto文件中的方法名
							final String methodName = method.getBareMethodName();

							final Method[] fallbackMethods = fallbackInstance.getClass().getDeclaredMethods();
							final Method targetMethod = Arrays.stream(fallbackMethods)
									.filter(impelementedMethod -> impelementedMethod.getName().equalsIgnoreCase(methodName))
									.findFirst()
									.orElse(null);

							if (Objects.isNull(targetMethod)) {
								log.warn("{} 默认实现未找到，将使用gRPC默认处理！", method.getFullMethodName());
								super.onClose(status, trailers);
								return;
							}

							final int parameterCount = targetMethod.getParameterCount();
							CompletableFuture<Void> transformed = CompletableFuture.completedFuture(null);
							final EnhancementStreamObserver<RespT> respStreamObserver = new EnhancementStreamObserver<>();
							try {
								// 调用目标方法获取默认返回
								if (parameterCount == 2) {
									// 客户端非流
									final ReqT requestParam = requestStreamObserver.take();
									targetMethod.invoke(fallbackInstance, requestParam, respStreamObserver);
								} else {
									// 客户端流
									StreamObserver<ReqT> requestStreamObserver4write = (StreamObserver<ReqT>) targetMethod.invoke(fallbackInstance, respStreamObserver);
									transformed = FallbackUtil.transformStreamObserver(requestStreamObserver,
											requestStreamObserver4write,
											FallbackUtil.getGrpcDefaultExecutor());
								}
								final CompletableFuture<Void> consumed = FallbackUtil.consumeResponseStreamObserver(respStreamObserver,
										super::onMessage,
										FallbackUtil.getGrpcDefaultExecutor());

								transformed.thenCompose(x -> consumed).join();
								super.onClose(Status.OK, trailers);
								return;
							} catch (InterruptedException |
							         RuntimeException |
							         InvocationTargetException |
							         IllegalAccessException e) {
								log.error("默认实现调用失败，将使用gRPC默认处理！", e);
							}
						}

						requestStreamObserver.clear();
						super.onClose(status, trailers);
					}
				}, headers);
			}
		};
	}
}
