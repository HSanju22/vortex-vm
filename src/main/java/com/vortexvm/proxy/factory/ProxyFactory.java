package com.vortexvm.proxy.factory;

import com.vortexvm.proxy.handler.DistributedInvocationHandler;
import com.vortexvm.proxy.strategy.ExecutionStrategy;
import com.vortexvm.proxy.strategy.LocalExecutionStrategy;

import java.lang.reflect.Proxy;

public class ProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> interfaceClass, T target) {

        // Step 1: Validation guard
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException(
                interfaceClass.getName() + " is not an interface. " +
                "JDK Dynamic Proxy requires an interface.");
        }

        if (target == null) {
            throw new IllegalArgumentException(
                "Target object cannot be null");
        }

        // Step 2: Create execution strategy (Phase 1 = local)
        ExecutionStrategy strategy = new LocalExecutionStrategy();

        // Step 3: Create invocation handler
        DistributedInvocationHandler handler =
                new DistributedInvocationHandler(target, strategy);

        // Step 4: Resolve classloader safely
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = interfaceClass.getClassLoader();
        }

        // Step 5: Create and return proxy instance
        return (T) Proxy.newProxyInstance(
                cl,
                new Class<?>[]{interfaceClass},
                handler
        );
    }
}