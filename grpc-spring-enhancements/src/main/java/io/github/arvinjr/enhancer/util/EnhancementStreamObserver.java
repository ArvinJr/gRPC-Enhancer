package io.github.arvinjr.enhancer.util;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * {@link StreamObserver} for enhancements.
 *
 * @author arvin
 * @date 2024/11/23
 */
@Slf4j
public class EnhancementStreamObserver<T> implements StreamObserver<T> {

	/**
	 * Wrapper for gRPC message.
	 *
	 * @param <T> Message type.
	 */
	static class WrappedMessage<T> {
		/**
		 * Message
		 */
		private final T value;
		/**
		 * Is an end marker
		 */
		private final boolean isPoisonPill;

		public WrappedMessage(T value) {
			this.value = value;
			this.isPoisonPill = false;
		}

		private WrappedMessage(boolean isPoisonPill) {
			this.value = null;
			this.isPoisonPill = isPoisonPill;
		}

		/**
		 * Create an end marker
		 *
		 * @return The end marker
		 * @param <T> Message type.
		 */
		public static <T> WrappedMessage<T> poisonPill() {
			return new WrappedMessage<>(true);
		}

		/**
		 * Is an end marker
		 */
		public boolean isPoisonPill() {
			return isPoisonPill;
		}

	}

	/**
	 * Message queue
	 */
	private final LinkedBlockingQueue<WrappedMessage<T>> queue;
	/**
	 * Mark for {@link StreamObserver#onCompleted()}
	 */
	private volatile boolean isCompleted = false;
	/**
	 * Mark for {@link StreamObserver#onError(Throwable)}
	 */
	private volatile boolean hasError = false;
	/**
	 * Error message
	 */
	private volatile Throwable error;

	public EnhancementStreamObserver(int capacity) {
		this.queue  = new LinkedBlockingQueue<>(capacity);
	}

	public EnhancementStreamObserver() {
		this(100);
	}

	@Override
	public void onNext(T value) {
		try {
			if (!isCompleted && !hasError) {
				queue.put(new WrappedMessage<>(value));
			} else {
				log.warn("Attempting to write after an exception or receiving the termination marker: {}", value.toString());
				this.onError(new RuntimeException("Attempting to write after an exception or receiving the termination marker: " + value));
			}
		} catch (InterruptedException e) {
			log.error("Exception while enqueueing message {}!", value.toString(), e);
			this.onError(new RuntimeException("Exception while enqueueing message " + value + "!", e));
		}
	}

	@Override
	public void onError(Throwable t) {
		hasError = true;
		error = t;
		queue.offer(WrappedMessage.poisonPill());
	}

	@Override
	public void onCompleted() {
		isCompleted = true;
		queue.offer(WrappedMessage.poisonPill());
	}

	/**
	 * Get message
	 *
	 * @return message
	 * @throws InterruptedException
	 * @throws RuntimeException
	 */
	public T take() throws InterruptedException {
		final WrappedMessage<T> message = queue.take();

		if (message.isPoisonPill()) {
			if (hasError) {
				log.warn("EnhancementStreamObserver received error!");
				throw new RuntimeException("EnhancementStreamObserver received error!", error);
			}
			return null;
		}

		return message.value;
	}

	/**
	 * Clear message queue.
	 * Used for GC optimization.
	 */
	public void clear() {
		queue.clear();
	}
}
