package com.github.arvinjr.enhancer.util;

import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * 增强用 StreamObserver
 *
 * @author arvin
 * @date 2024/11/23
 */
@Slf4j
public class EnhancementStreamObserver<T> implements StreamObserver<T> {

	/**
	 * 消息对象包装类
	 *
	 * @param <T> 封装的消息类型
	 */
	static class WrappedMessage<T> {
		// 消息
		private final T value;
		// 是否是结束标记
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
		 * 创建结束标记
		 *
		 * @return 结束标记
		 * @param <T> 封装的消息类型
		 */
		public static <T> WrappedMessage<T> poisonPill() {
			return new WrappedMessage<>(true);
		}

		/**
		 * 是否是结束标记
		 */
		public boolean isPoisonPill() {
			return isPoisonPill;
		}

	}

	// 消息队列
	private final LinkedBlockingQueue<WrappedMessage<T>> queue;
	// 是否已经结束
	private volatile boolean isCompleted = false;
	// 是否出现异常
	private volatile boolean hasError = false;
	// 异常信息
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
				log.warn("在出现异常或收到终止标记后仍尝试写入：{}", value.toString());
				this.onError(new RuntimeException("在出现异常或收到终止标记后仍尝试写入：" + value));
			}
		} catch (InterruptedException e) {
			log.error("消息 {} 入队异常!", value.toString(), e);
			this.onError(new RuntimeException("消息" + value + "入队异常!", e));
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
	 * 获取消息
	 *
	 * @return 消息
	 * @throws InterruptedException
	 * @throws RuntimeException
	 */
	public T take() throws InterruptedException {
		final WrappedMessage<T> message = queue.take();

		if (message.isPoisonPill()) {
			if (hasError) {
				log.warn("CustomStreamObserver接收到异常");
				throw new RuntimeException("CustomStreamObserver出现异常", error);
			}
			return null;
		}

		return message.value;
	}

	/**
	 * 清空消息队列
	 * 用于 GC 优化
	 */
	public void clear() {
		queue.clear();
	}
}
