package ru.reimu.server.commons.jdbc.annotiation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @Author: Tomonori
 * @Date: 2019/11/8 12:04
 * @Desc: 多数据源切换注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

    @AliasFor(attribute = "key")
    String value() default "default";

    @AliasFor(attribute = "value")
    String key() default "default";
}
