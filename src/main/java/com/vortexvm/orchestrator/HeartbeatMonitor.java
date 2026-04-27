package com.vortexvm.orchestrator;

import java.net.Socket;

public class HeartbeatMonitor {

    private final NodeRegistry registry;
    private final int intervalMs;

    // 🔹 Constructor
    public HeartbeatMonitor(NodeRegistry registry, int intervalMs) {
        this.registry = registry;
        this.intervalMs = intervalMs;
    }

    // 🔹 Start background heartbeat thread
    public void start() {

        Runnable task = () -> {
            while (true) {
                try {
                    // 🔹 Sleep between heartbeats
                    Thread.sleep(intervalMs);

                    // 🔹 Ping all workers (ALIVE + DEAD)
                    for (WorkerNode worker : registry.getAllWorkers()) {
                        pingWorker(worker);
                    }

                } catch (InterruptedException e) {
                    System.out.println("[Heartbeat] Interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        };

        // 🔥 Virtual daemon thread
        Thread t = Thread.ofVirtual().unstarted(task);
        t.setDaemon(true);
        t.start();
    }

    // 🔹 Ping a single worker
    private void pingWorker(WorkerNode worker) {

        try {
            // Try opening socket (connectivity check)
            Socket socket = new Socket(worker.getHost(), worker.getPort());
            socket.close();

            // If it was DEAD → recovered
            if (worker.getStatus() == NodeStatus.DEAD) {
                worker.setStatus(NodeStatus.ALIVE);
                System.out.println("[Heartbeat] Worker recovered: " + worker);
            }

            // If already ALIVE → do nothing (avoid log spam)

        } catch (Exception e) {

            // If it was ALIVE → now dead
            if (worker.getStatus() == NodeStatus.ALIVE) {
                worker.setStatus(NodeStatus.DEAD);
                System.out.println("[Heartbeat] Worker DEAD: " + worker);
            }

            // If already DEAD → do nothing
        }
    }
}