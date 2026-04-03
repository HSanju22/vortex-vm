package com.vortexvm.proxy.strategy;

import java.lang.reflect.Method;

public interface ExecutionStrategy {
    
    Object execute(Object target, Method method, Object[] args) throws Throwable;
    
}
