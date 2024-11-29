package io.github.arvinjr.enhancer.inject;

import java.lang.annotation.*;

/**
 * 用于标注需要使用默认实现的接口
 *
 * @author arvin
 * @date 2024/11/22
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GrpcFallback {

}
