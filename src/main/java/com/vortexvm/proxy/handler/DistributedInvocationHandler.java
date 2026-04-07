package com.vortexvm.proxy.handler;

import com.vortexvm.annotation.Distributed;
import com.vortexvm.proxy.strategy.ExecutionStrategy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class DistributedInvocationHandler implements InvocationHandler {

    private final Object target;
    private final ExecutionStrategy localStrategy;
    private final ExecutionStrategy remoteStrategy;

    public DistributedInvocationHandler(Object target,
                                        ExecutionStrategy localStrategy,
                                        ExecutionStrategy remoteStrategy) {
        this.target = target;
        this.localStrategy = localStrategy;
        this.remoteStrategy = remoteStrategy;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // Step 1: Resolve method on actual implementation class
        Method targetMethod;
        try {
            targetMethod = target.getClass()
                    .getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Implementation class " + target.getClass().getName() +
                    " is missing method: " + method.getName(), e);
        }

        // Step 2: Check if @Distributed is present
        boolean isDistributed = targetMethod.isAnnotationPresent(Distributed.class);

        if (isDistributed) {

            // Step 3: Log method detection
            System.out.println("[Interceptor] Distributed method detected: " + method.getName());

            // Step 4: Log arguments safely
            if (args != null && args.length > 0) {
                System.out.print("[Interceptor] Arguments: ");
                for (Object arg : args) {
                    System.out.print(arg + " ");
                }
                System.out.println();
            } else {
                System.out.println("[Interceptor] No arguments passed");
            }
        }

        // Step 5: Route to correct strategy based on annotation
        if (isDistributed) {
            return remoteStrategy.execute(target, targetMethod, args); // distributed path
        } else {
            return localStrategy.execute(target, targetMethod, args);  // local path
        }
    }
}