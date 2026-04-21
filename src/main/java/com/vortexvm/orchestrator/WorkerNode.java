package com.vortexvm.orchestrator;

public class WorkerNode {

    private final String host;
    private final int port;

    private NodeStatus status;
    private int activeJobs;

    // 🔹 Constructor (host & port only)
    public WorkerNode(String host, int port) {
        this.host = host;
        this.port = port;
        this.status = NodeStatus.ALIVE; // default
        this.activeJobs = 0;            // default
    }

    // 🔹 Getters

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public int getActiveJobs() {
        return activeJobs;
    }

    // 🔹 Setters (only for mutable fields)

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public void setActiveJobs(int activeJobs) {
        this.activeJobs = activeJobs;
    }

    // 🔹 toString()

    @Override
    public String toString() {
        return host + ":" + port +
                " [" + status + "] (" + activeJobs + " jobs)";
    }
}