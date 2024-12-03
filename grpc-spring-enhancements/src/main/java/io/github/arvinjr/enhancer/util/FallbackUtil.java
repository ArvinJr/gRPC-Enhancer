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
 * Utils for gRPC fallback
 *
 * @author arvin
 * @date 2024/11/23
 */
@Slf4j
public class FallbackUtil {

	/**
	 * Asynchronously consume the response stream.
	 *
	 * @param streamObserver Response stream.
	 * @param onMessage {@link ClientCall.Listener#onMessage(Object)} method.
	 * @param executor Thread pool.
	 * @param <T> Type of response message.
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
	 * Convert the {@link EnhancementStreamObserver} to the target stream.
	 *
	 * @param source Source stream.
	 * @param target Target stream.
	 * @param executor Thread pool.
	 * @param <T> Type of message.
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
	 * Get the default thread pool of gRPC.
	 *
	 * @return gRPC thread pool.
	 */
	public static Executor getGrpcDefaultExecutor() {
		final SharedResourceHolder.Resource<Executor> sharedChannelExecutor = GrpcUtil.SHARED_CHANNEL_EXECUTOR;
		return SharedResourceHolder.get(sharedChannelExecutor);
	}

	/**
	 * Printout the current thread name.
	 * Only printout when debug level is enabled.
	 */
	private static void debugLog() {
		if (log.isDebugEnabled()) {
			log.debug("Current thread is: {}", Thread.currentThread().getName());
		}
	}

}
