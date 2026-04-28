package com.vortexvm.worker;

import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;
import com.vortexvm.network.NetworkSerializer;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WorkerServer {

    public static void main(String[] args) {

        int port = args.length > 0 ? Integer.parseInt(args[0]) : 9091;
        WorkerExecutor workerExecutor = new WorkerExecutor();

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("[WorkerServer] Started on port " + port);
            System.out.println("[WorkerServer] Listening for connections...");

            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("[WorkerServer] Client connected: "
                        + clientSocket.getInetAddress());

                Thread.ofVirtual().start(() -> {

                    try (
                        InputStream in = clientSocket.getInputStream();
                        OutputStream out = clientSocket.getOutputStream()
                    ) {
                        ExecutionPacket packet = NetworkSerializer.receivePacket(in);
                        ExecutionResult result = workerExecutor.execute(packet);
                        NetworkSerializer.sendResult(out, result);
                        System.out.println("[WorkerServer] Result sent back to client");

                    } catch (EOFException e) {
                        // Heartbeat probe — connection opened and closed immediately
                        // Normal behavior — ignore silently

                    } catch (Exception e) {
                        // Real unexpected error — log it
                        System.out.println("[WorkerServer] Error: " + e.getMessage());
                        e.printStackTrace();

                    } finally {
                        try {
                            clientSocket.close();
                        } catch (Exception ignored) {}
                    }
                });
            }

        } catch (Exception e) {
            System.out.println("[WorkerServer] Failed to start: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 