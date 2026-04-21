package com.vortexvm.worker;

import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;
import com.vortexvm.network.NetworkSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerServer {

    public static void main(String[] args) {

        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9091;
        WorkerExecutor workerExecutor = new WorkerExecutor();

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            // Step 2: Start log
            System.out.println("[WorkerServer] Started on port " + port);
            System.out.println("[WorkerServer] Listening for connections...");

            // Step 3: Infinite loop
            while (true) {

                // Step 4: Accept connection
                Socket clientSocket = serverSocket.accept();

                // Step 5: Log client connection
                System.out.println("[WorkerServer] Client connected: "
                        + clientSocket.getInetAddress());

                // Step 6: Handle in virtual thread
                Thread.ofVirtual().start(() -> {

                    try (
                            InputStream in = clientSocket.getInputStream();
                            OutputStream out = clientSocket.getOutputStream()
                    ) {

                        // Step 9: Receive ExecutionPacket
                        ExecutionPacket packet = NetworkSerializer.receivePacket(in);

                        // Step 10: Execute
                        ExecutionResult result = workerExecutor.execute(packet);

                        // Step 11: Send result
                        NetworkSerializer.sendResult(out, result);

                        System.out.println("[WorkerServer] Result sent back to client");

                    } catch (Exception e) {

                        // Step 13: Error handling
                        System.out.println("[WorkerServer] Error: " + e.getMessage());
                        e.printStackTrace();

                    } finally {
                        try {
                            // Step 12: Close socket
                            clientSocket.close();
                        } catch (Exception ignored) {
                        }
                    }
                });
            }

        } catch (Exception e) {
            System.out.println("[WorkerServer] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
}