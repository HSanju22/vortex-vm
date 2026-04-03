package com.vortexvm.proxy.strategy;

import java.lang.reflect.Method;

public class LocalExecutionStrategy implements ExecutionStrategy {
    
    @Override
    public Object execute(Object target,Method method, Object[] args) throws Throwable {
        return method.invoke(target, args);
    }
}
