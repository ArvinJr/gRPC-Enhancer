package io.github.arvinjr.enhancer.util;

import io.grpc.ClientCall;
import io.grpc.internal.GrpcUtil;
import io.grpc.internal.SharedResourceHolder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * fallback工具类
 *
 * @author arvin
 * @date 2024/11/23
 */
@Slf4j
public class FallbackUtil {

	/**
	 * 异步消费响应流
	 *
	 * @param streamObserver 响应流
	 * @param onMessage {@link ClientCall.Listener#onMessage(Object)} 方法
	 * @param executor 线程池
	 * @param <T> 响应消息类型
	 */
	public static <T> CompletableFuture<Void> consumeResponseStreamObserver(EnhancementStreamObserver<T> streamObserver,
	                                                                        Consumer<T> onMessage,
	                                                                        Executor executor) {
		return CompletableFuture.runAsync(() -> {
			debugLog();
			T responseMessage = null;
			try {
				while ((responseMessage = streamObserver.take()) != null) {
					onMessage.accept(responseMessage);
				}
			} catch (InterruptedException | RuntimeException e) {
				throw new RuntimeException(e);
			}
		}, executor);
	}

	/**
	 * 将增强用 StreamObserver 流转换成目标流
	 * @param source 源流
	 * @param target 目标流
	 * @param executor 线程池
	 * @param <T> 消息类型
	 */
	public static <T> CompletableFuture<Void> transformStreamObserver(EnhancementStreamObserver<T> source,
	                                                                  StreamObserver<T> target,
	                                                                  Executor executor) {
		return CompletableFuture.runAsync(() -> {
			debugLog();
			T message = null;
			try {
				while ((message = source.take()) != null) {
					target.onNext(message);
				}
				target.onCompleted();
			} catch (RuntimeException e) {
				target.onError(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}, executor);
	}

	/**
	 * 获取gRPC默认线程池
	 * @return gRPC线程池
	 */
	public static Executor getGrpcDefaultExecutor() {
		final SharedResourceHolder.Resource<Executor> sharedChannelExecutor = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
		return SharedResourceHolder.get(sharedChannelExecutor);
	}

	/**
	 * 打印当前线程名称
	 * 仅 debug 模式启用
	 */
	private static void debugLog() {
		if (log.isDebugEnabled()) {
			log.debug("当前线程为：{}", Thread.currentThread().getName());
		}
	}

}
