package com.vortexvm.orchestrator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NodeRegistry {

    // Thread-safe list
    private final List<WorkerNode> nodes = new CopyOnWriteArrayList<>();

    // Constructor
    public NodeRegistry(List<WorkerNode> initialNodes) {
        if (initialNodes != null) {
            nodes.addAll(initialNodes);
        }
    }

    // Returns only ALIVE workers
    public List<WorkerNode> getAliveWorkers() {
        return nodes.stream()
                .filter(node -> node.getStatus() == NodeStatus.ALIVE)
                .toList();
    }

    // Register new worker
    public void register(WorkerNode node) {
        nodes.add(node);
        System.out.println("[NodeRegistry] Registered worker: " + node);
    }

    // Print all workers
    public void printRegistry() {
        System.out.println("[NodeRegistry] Current workers:");
        for (WorkerNode node : nodes) {
            System.out.println("  - " + node);
        }
    }
}