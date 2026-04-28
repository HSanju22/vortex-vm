package com.vortexvm.byteBuddy;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import com.vortexvm.proxy.handler.DistributedInvocationHandler;
import com.vortexvm.proxy.strategy.ExecutionStrategy;

import java.lang.reflect.Modifier;

public class ByteBuddyProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> targetClass,
                                    ExecutionStrategy localStrategy,
                                    ExecutionStrategy remoteStrategy) {
        try {
            // Step 1: Reject interfaces
            if (targetClass.isInterface()) {
                throw new IllegalArgumentException(
                        "Use ProxyFactory for interfaces. " +
                        "ByteBuddyProxyFactory is for concrete classes.");
            }

            // Step 2: Reject final classes
            if (Modifier.isFinal(targetClass.getModifiers())) {
                throw new IllegalArgumentException(
                        "Cannot subclass final class: " + targetClass.getName());
            }

            // Step 3: Create real target instance
            T targetInstance = targetClass.getDeclaredConstructor().newInstance();

            // Step 4: Create handler with both strategies
            DistributedInvocationHandler handler =
                    new DistributedInvocationHandler(targetInstance, localStrategy, remoteStrategy);

            // Step 5: Build ByteBuddy proxy class
            Class<? extends T> proxyClass = new ByteBuddy()
                    .subclass(targetClass)
                    .method(ElementMatchers.any())
                    .intercept(InvocationHandlerAdapter.of(handler))
                    .make()
                    .load(targetClass.getClassLoader())
                    .getLoaded();

            // Step 6: Instantiate and return proxy
            return proxyClass.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to create ByteBuddy proxy: " + e.getMessage(), e);
        }
    }
}