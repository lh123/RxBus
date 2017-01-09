package com.lh.rxbuslibrary.annotation;

import com.lh.rxbuslibrary.event.EventThread;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by home on 2017/1/9.
 * 订阅注解类
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subscribe {
    int tag() default 0;
    EventThread scheduler() default EventThread.CURRENT;
}