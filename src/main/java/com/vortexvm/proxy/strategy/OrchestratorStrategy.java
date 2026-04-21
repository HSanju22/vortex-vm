package com.vortexvm.proxy.strategy;

import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;
import com.vortexvm.network.NetworkSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.UUID;

public class OrchestratorStrategy implements ExecutionStrategy {

    private final String orchestratorHost;
    private final int orchestratorPort;

    public OrchestratorStrategy(String orchestratorHost, int orchestratorPort) {
        this.orchestratorHost = orchestratorHost;
        this.orchestratorPort = orchestratorPort;
    }

    @Override
    public Object execute(Object target, Method method, Object[] args) throws Throwable {

        // 🔹 Step 1: Generate requestId
        String requestId = UUID.randomUUID().toString();

        // 🔹 Step 2: Build ExecutionPacket (same as before)
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

        // 🔹 Step 3: Connect to Orchestrator
        System.out.println("[Orchestrator] Connecting to orchestrator at "
                + orchestratorHost + ":" + orchestratorPort);

        // 🔹 Step 4: Open socket
        try (Socket socket = new Socket(orchestratorHost, orchestratorPort);
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            // 🔹 Step 6: Send packet
            System.out.println("[Orchestrator] Sending execution packet...");
            NetworkSerializer.sendPacket(out, packet);

            // 🔹 Step 7: Wait for result
            System.out.println("[Orchestrator] Waiting for result...");
            ExecutionResult result = NetworkSerializer.receiveResult(in);

            // 🔹 Step 8: Log result
            System.out.println("[Orchestrator] Result received: " + result.getValue());

            // 🔹 Step 9: Handle result
            if (result.isSuccess()) {
                return result.getValue();
            } else {
                throw new RuntimeException(result.getErrorMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException("Orchestrator communication failed: " + e.getMessage(), e);
        }
    }
}