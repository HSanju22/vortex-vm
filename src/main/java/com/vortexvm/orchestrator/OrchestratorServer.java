package com.vortexvm.orchestrator;

import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;
import com.vortexvm.network.NetworkSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class OrchestratorServer {

    public static void main(String[] args) {

        int port = 9090;

        // Step 1: Create WorkerNodes
        WorkerNode w1 = new WorkerNode("localhost", 9091);
        WorkerNode w2 = new WorkerNode("localhost", 9092);

        // Step 2: Create registry
        NodeRegistry registry = new NodeRegistry(Arrays.asList(w1, w2));

        // Step 3: Print registry
        registry.printRegistry();

        // Step 4: Create LoadBalancer
        LoadBalancer loadBalancer = new LoadBalancer(registry);

        // Step 5: Start heartbeat BEFORE server loop
        HeartbeatMonitor heartbeat = new HeartbeatMonitor(registry, 5000);
        heartbeat.start();
        System.out.println("[Orchestrator] Heartbeat monitor started");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("[Orchestrator] Started on port " + port);

            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("[Orchestrator] Client connected: "
                        + clientSocket.getInetAddress());

                Thread.ofVirtual().start(() -> {

                    try (
                        InputStream clientIn = clientSocket.getInputStream();
                        OutputStream clientOut = clientSocket.getOutputStream()
                    ) {
                        // Receive packet from client
                        ExecutionPacket packet = 
                                NetworkSerializer.receivePacket(clientIn);

                        // Retry loop
                        int maxRetries = 3;

                        for (int attempt = 1; attempt <= maxRetries; attempt++) {
                            try {
                                WorkerNode worker = loadBalancer.selectWorker();
                                System.out.println("[Orchestrator] Attempt "
                                        + attempt + " → selected worker: " + worker);

                                try (Socket workerSocket = new Socket(
                                             worker.getHost(), worker.getPort());
                                     InputStream workerIn = 
                                             workerSocket.getInputStream();
                                     OutputStream workerOut = 
                                             workerSocket.getOutputStream()) {

                                    NetworkSerializer.sendPacket(workerOut, packet);
                                    ExecutionResult result = 
                                            NetworkSerializer.receiveResult(workerIn);
                                    NetworkSerializer.sendResult(clientOut, result);

                                    System.out.println("[Orchestrator] Forwarded"
                                            + " result: " + result.getValue());
                                    break; // success — exit retry loop
                                }

                            } catch (Exception e) {
                                System.out.println("[Orchestrator] Attempt "
                                        + attempt + " failed: " + e.getMessage());

                                if (attempt == maxRetries) {
                                    ExecutionResult failed = new ExecutionResult(
                                            "unknown", null, false,
                                            "All " + maxRetries 
                                            + " attempts failed: " + e.getMessage(),
                                            System.currentTimeMillis()
                                    );
                                    NetworkSerializer.sendResult(clientOut, failed);
                                }
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("[Orchestrator] Error: " 
                                + e.getMessage());
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (Exception ignored) {}
                    }
                });
            }

        } catch (Exception e) {
            System.out.println("[Orchestrator] Failed to start: " 
                    + e.getMessage());
            e.printStackTrace();
        }
    }
}