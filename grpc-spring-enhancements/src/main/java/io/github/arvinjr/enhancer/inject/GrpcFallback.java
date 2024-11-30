package io.github.arvinjr.enhancer.inject;

import java.lang.annotation.*;

/**
 * Used to annotate interfaces that require the default implementation.
 *
 * @author arvin
 * @date 2024/11/22
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GrpcFallback {

}
