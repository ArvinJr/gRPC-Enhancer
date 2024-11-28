package com.github.arvinjr.enhancer;

import com.github.arvinjr.enhancer.inject.GrpcFallback;
import com.github.arvinjr.enhancer.interceptor.FallbackInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.autoconfigure.GrpcClientAutoConfiguration;
import net.devh.boot.grpc.client.inject.StubTransformer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * grp-enhancer自动配置
 *
 * @author arvin
 * @date 2024/11/27
 */
@Configuration
@AutoConfigureAfter(GrpcClientAutoConfiguration.class)
@ComponentScan(basePackages = "com.github.arvinjr.enhancer")
@Slf4j
@RequiredArgsConstructor
public class GrpcEnhancerAutoConfiguration {

	private final ApplicationContext applicationContext;

	/**
	 * 为所有stub注入已存在的默认实现
	 */
	@Bean
	public StubTransformer fallbackStubTransformer() {
		return ((name, stub) -> {
			if (log.isDebugEnabled()) {
				log.debug("channel name: {}, stub: {}", name, stub);
			}

			final String stubName = stub.getClass().getSimpleName();
			final Class<?> grpcServiceClass = stub.getClass().getEnclosingClass();

			try {
				// 获取目标stub的AsyncService接口
				final Class<?> targetInterface = Arrays.stream(grpcServiceClass.getClasses())
						.filter(clazz -> "AsyncService".equals(clazz.getSimpleName()))
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("找不到" + stubName + "对应的AsyncService接口"));

				// 获取默认实现类
				final List<?> fallbackBeans = applicationContext.getBeansOfType(targetInterface)
						.values()
						.stream()
						.filter(service -> service.getClass().getDeclaredAnnotation(GrpcFallback.class) != null)
						.collect(Collectors.toList());

				if (!fallbackBeans.isEmpty()) {
					final Object fallbackBean = fallbackBeans.get(0);
					if (fallbackBeans.size() > 1) {
						log.warn("存在多个 {} 的默认实现，将使用第一个 {}", grpcServiceClass.getSimpleName(), fallbackBean.getClass().getSimpleName());
					}
					log.info("为 {} 注入fallbackBean:{}", stubName, fallbackBean);
					return stub.withInterceptors(new FallbackInterceptor(fallbackBean));
				} else {
					log.warn("未找到 {} 对应的默认实现，将使用默认stub", grpcServiceClass.getSimpleName());
					return stub;
				}
			} catch (Exception e) {
				log.error("解析 {} 的默认实现时出现异常：", stubName, e);
				throw new RuntimeException("解析" + stubName + "的默认实现时出现异常", e);
			}
		});
	}
}
