package com.vortexvm.proxy.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexvm.annotation.Distributed;
import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;
import com.vortexvm.worker.WorkerExecutor;

import java.lang.reflect.Method;
import java.util.UUID;

public class SimulatedRemoteStrategy implements ExecutionStrategy {

    private final WorkerExecutor workerExecutor;

    // 🔥 ObjectMapper should be static (thread-safe, reusable)
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public SimulatedRemoteStrategy(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    @Override
    public Object execute(Object target, Method method, Object[] args) throws Throwable {

        // 🔹 Step 1: Generate requestId (always system-controlled)
        String requestId = UUID.randomUUID().toString();

        System.out.println("[SimulatedRemote] Building execution packet...");
        System.out.println("[SimulatedRemote] Method: " + method.getName());

        // 🔹 Step 2: Build ExecutionPacket

        Class<?>[] paramClasses = method.getParameterTypes();
        String[] paramTypes = new String[paramClasses.length];

        for (int i = 0; i < paramClasses.length; i++) {
            paramTypes[i] = paramClasses[i].getName();
        }

        ExecutionPacket packet = new ExecutionPacket(
                requestId,
                target.getClass().getName(),
                method.getName(),
                paramTypes,
                args != null ? args : new Object[]{},
                System.currentTimeMillis()
        );

        // 🔹 Step 3: Serialize to JSON
        String json = OBJECT_MAPPER.writeValueAsString(packet);
        System.out.println("[SimulatedRemote] Packet: " + json);

        // 🔹 Step 4: Simulate network latency
        System.out.println("[SimulatedRemote] Simulating network transmission...");
        Thread.sleep(100);

        // 🔹 Step 5: Deserialize (simulate worker receiving)
        ExecutionPacket receivedPacket =
                OBJECT_MAPPER.readValue(json, ExecutionPacket.class);

        // 🔹 Step 6: Execute on worker
        ExecutionResult result = workerExecutor.execute(receivedPacket);

        // 🔹 Step 7: Handle result
        if (result.isSuccess()) {
            return result.getValue();
        } else {
            throw new RuntimeException(result.getErrorMessage());
        }
    }
}