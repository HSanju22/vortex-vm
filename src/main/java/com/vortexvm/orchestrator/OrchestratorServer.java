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

        // 🔹 Step 1: Create WorkerNodes
        WorkerNode w1 = new WorkerNode("localhost", 9091);
        WorkerNode w2 = new WorkerNode("localhost", 9092);

        // 🔹 Step 2: Create registry
        NodeRegistry registry = new NodeRegistry(Arrays.asList(w1, w2));

        // 🔹 Step 3: Print registry
        registry.printRegistry();

        // 🔹 Step 4: Create LoadBalancer
        LoadBalancer loadBalancer = new LoadBalancer(registry);

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            // 🔹 Step 6: Start log
            System.out.println("[Orchestrator] Started on port " + port);

            // 🔹 Step 7: Loop forever
            while (true) {

                // 🔹 Step 8: Accept client connection
                Socket clientSocket = serverSocket.accept();

                // 🔹 Step 9: Log
                System.out.println("[Orchestrator] Client connected: "
                        + clientSocket.getInetAddress());

                // 🔥 Step 10: Handle in virtual thread
                Thread.ofVirtual().start(() -> {

                    try (
                            InputStream clientIn = clientSocket.getInputStream();
                            OutputStream clientOut = clientSocket.getOutputStream()
                    ) {

                        // 🔹 Step 11: Receive packet from client
                        ExecutionPacket packet = NetworkSerializer.receivePacket(clientIn);

                        // 🔹 Step 12: Select worker
                        WorkerNode worker = loadBalancer.selectWorker();

                        // 🔹 Step 13: Log selected worker
                        System.out.println("[Orchestrator] Selected worker: " + worker);

                        // 🔹 Step 14–16: Connect to worker and forward
                        try (Socket workerSocket = new Socket(worker.getHost(), worker.getPort());
                             InputStream workerIn = workerSocket.getInputStream();
                             OutputStream workerOut = workerSocket.getOutputStream()) {

                            // 🔹 Step 15: Send packet to worker
                            NetworkSerializer.sendPacket(workerOut, packet);

                            // 🔹 Step 16: Receive result
                            ExecutionResult result = NetworkSerializer.receiveResult(workerIn);

                            // 🔹 Step 17: Send result back to client
                            NetworkSerializer.sendResult(clientOut, result);

                            // 🔹 Step 18: Log success
                            System.out.println("[Orchestrator] Forwarded result: " + result.getValue());

                        }

                    } catch (Exception e) {

                        // 🔥 Step 20: Critical failure handling
                        System.out.println("[Orchestrator] Error: " + e.getMessage());

                        try (OutputStream clientOut = clientSocket.getOutputStream()) {

                            ExecutionResult failed = new ExecutionResult(
                                    "unknown",
                                    null,
                                    false,
                                    e.getMessage(),
                                    System.currentTimeMillis()
                            );

                            NetworkSerializer.sendResult(clientOut, failed);

                        } catch (Exception ex) {
                            System.out.println("[Orchestrator] Failed to send error response: " + ex.getMessage());
                        }

                    } finally {
                        try {
                            // 🔹 Step 19: Close client socket
                            clientSocket.close();
                        } catch (Exception ignored) {
                        }
                    }
                });
            }

        } catch (Exception e) {
            System.out.println("[Orchestrator] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}