package com.fortuneboot.customize.recurringBillLog;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author zhangchi118
 * @date 2025/7/10 09:29
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RecurringBillLog {

    /**
     * 日志描述
     */
    String value() default "";

}