package io.github.arvinjr.enhancer;

import io.github.arvinjr.enhancer.inject.GrpcFallback;
import io.github.arvinjr.enhancer.interceptor.FallbackInterceptor;
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
 * AutoConfiguration for gRPC Spring Enhancements.
 *
 * @author arvin
 * @date 2024/11/27
 */
@Configuration
@AutoConfigureAfter(GrpcClientAutoConfiguration.class)
@ComponentScan(basePackages = "io.github.arvinjr.enhancer")
@Slf4j
@RequiredArgsConstructor
public class GrpcEnhancerAutoConfiguration {

	private final ApplicationContext applicationContext;

	/**
	 * Inject existing default implementations for all grpc stubs.
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
				// Get the AsyncService interface of the target stub.
				final Class<?> targetInterface = Arrays.stream(grpcServiceClass.getClasses())
						.filter(clazz -> "AsyncService".equals(clazz.getSimpleName()))
						.findFirst()
						.orElseThrow(() -> new IllegalStateException("Cannot find the AsyncService interface corresponding to " + stubName));

				// Get the default implementation class.
				final List<?> fallbackBeans = applicationContext.getBeansOfType(targetInterface)
						.values()
						.stream()
						.filter(service -> service.getClass().getDeclaredAnnotation(GrpcFallback.class) != null)
						.collect(Collectors.toList());

				if (!fallbackBeans.isEmpty()) {
					final Object fallbackBean = fallbackBeans.get(0);
					if (fallbackBeans.size() > 1) {
						log.warn("Multiple default implementations of {} found, using the first one: {}", grpcServiceClass.getSimpleName(), fallbackBean.getClass().getSimpleName());
					}
					log.info("Injecting fallbackBean for {}: {}", stubName, fallbackBean);
					return stub.withInterceptors(new FallbackInterceptor(fallbackBean));
				} else {
					log.warn("No default implementation found for {}, using the default stub.", grpcServiceClass.getSimpleName());
					return stub;
				}
			} catch (Exception e) {
				log.error("An error occurred while parsing the default implementation for {}: ", stubName, e);
				throw new RuntimeException("An error occurred while parsing the default implementation for " + stubName, e);
			}
		});
	}
}
