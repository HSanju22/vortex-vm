package com.vortexvm.worker;

import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class WorkerExecutor {

    private static final Map<String, Class<?>> PRIMITIVE_TYPES = new HashMap<>();

    static {
        PRIMITIVE_TYPES.put("int", int.class);
        PRIMITIVE_TYPES.put("long", long.class);
        PRIMITIVE_TYPES.put("double", double.class);
        PRIMITIVE_TYPES.put("float", float.class);
        PRIMITIVE_TYPES.put("boolean", boolean.class);
        PRIMITIVE_TYPES.put("byte", byte.class);
        PRIMITIVE_TYPES.put("short", short.class);
        PRIMITIVE_TYPES.put("char", char.class);
    }

    public ExecutionResult execute(ExecutionPacket packet) {

        try {
            // Step 1: Log packet received
            System.out.println("[Worker] Received execution packet");

            // Step 2: Resolve class
            System.out.println("[Worker] Resolving class: " + packet.getClassName());
            Class<?> clazz = Class.forName(packet.getClassName());

            // Step 3: Resolve parameter types
            String[] paramTypeNames = packet.getParamTypes();
            Class<?>[] paramTypes = new Class<?>[paramTypeNames.length];

            for (int i = 0; i < paramTypeNames.length; i++) {
                String typeName = paramTypeNames[i];
                if (PRIMITIVE_TYPES.containsKey(typeName)) {
                    paramTypes[i] = PRIMITIVE_TYPES.get(typeName);
                } else {
                    paramTypes[i] = Class.forName(typeName);
                }
            }

            // Step 4: Resolve method
            Method method = clazz.getMethod(packet.getMethodName(), paramTypes);

            // Step 5: Create instance
            Object instance = clazz.getDeclaredConstructor().newInstance();

            // Step 6: Invoke method
            System.out.println("[Worker] Invoking method: " + packet.getMethodName()
                    + " with args: " + java.util.Arrays.toString(packet.getArgs()));

            Object result = method.invoke(instance, packet.getArgs());

            // Step 7: Build success result
            System.out.println("[Worker] Execution complete. Result: " + result);

            return new ExecutionResult(
                    packet.getRequestId(),
                    result,
                    true,
                    null,
                    System.currentTimeMillis()
            );

        } catch (Throwable e) {

            // Step 8: Handle failure
            System.out.println("[Worker] Execution failed: " + e.getMessage());

            return new ExecutionResult(
                    packet.getRequestId(),
                    null,
                    false,
                    e.getMessage(),
                    System.currentTimeMillis()
            );
        }
    }
}