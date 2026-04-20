package com.vortexvm.proxy.strategy;

import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;
import com.vortexvm.network.NetworkSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.UUID;

public class TcpRemoteStrategy implements ExecutionStrategy {

    private final String workerHost;
    private final int workerPort;

    public TcpRemoteStrategy(String workerHost, int workerPort) {
        this.workerHost = workerHost;
        this.workerPort = workerPort;
    }

    @Override
    public Object execute(Object target, Method method, Object[] args) throws Throwable {

        // 🔹 Step 1: Generate requestId
        String requestId = UUID.randomUUID().toString();

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

        // 🔹 Step 3: Log connection
        System.out.println("[TcpRemote] Connecting to worker at " 
                + workerHost + ":" + workerPort);

        // 🔹 Step 4: Open socket (auto-close)
        try (Socket socket = new Socket(workerHost, workerPort);
             InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            // 🔹 Step 6: Send packet
            System.out.println("[TcpRemote] Sending execution packet...");
            NetworkSerializer.sendPacket(out, packet);

            // 🔹 Step 7: Wait for response
            System.out.println("[TcpRemote] Waiting for result...");
            ExecutionResult result = NetworkSerializer.receiveResult(in);

            // 🔹 Step 8: Log result
            System.out.println("[TcpRemote] Result received: " + result.getValue());

            // 🔹 Step 9: Handle result
            if (result.isSuccess()) {
                return result.getValue();
            } else {
                throw new RuntimeException(result.getErrorMessage());
            }

        } catch (Exception e) {
            throw new RuntimeException("Network error: " + e.getMessage(), e);
        }
    }
}