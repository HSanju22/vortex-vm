package com.vortexvm.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)


public @interface Distributed{

    String node() default "auto";

    int timeoutMs() default 3000;
    
    String RequestId() default "";
    
    boolean async() default false;
}