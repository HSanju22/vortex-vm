package com.vortexvm.orchestrator;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {

    private final NodeRegistry registry;

    // 🔥 Thread-safe counter
    private final AtomicInteger counter = new AtomicInteger(0);

    // 🔹 Constructor
    public LoadBalancer(NodeRegistry registry) {
        this.registry = registry;
    }

    // 🔹 Select worker using Round Robin
    public WorkerNode selectWorker() {

        List<WorkerNode> aliveWorkers = registry.getAliveWorkers();

        // ❌ No workers available
        if (aliveWorkers.isEmpty()) {
            throw new RuntimeException("No alive workers available");
        }

        // 🔥 Round Robin logic
        int index = counter.getAndIncrement() % aliveWorkers.size();

        WorkerNode selected = aliveWorkers.get(index);

        // 🔹 Log selection
        System.out.println("[LoadBalancer] Selected worker: " + selected);

        return selected;
    }
}