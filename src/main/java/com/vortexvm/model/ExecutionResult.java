package com.vortexvm.model;

public class ExecutionResult {
    
    private String requestId;
    private Object value;
    private boolean success;
    private String errorMessage;
    private long timestamp;

    // No-arg constructor
    public ExecutionResult() {

    }

    //All-arg constructor
    public ExecutionResult(String requestId,
                           Object value,
                           boolean success,
                           String errorMessage,
                           long timestamp) {
        this.requestId = requestId;
        this.value = value;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp; 
    }

    // Getters and setters

    
    /**
     * @return String return the requestId
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * @param requestId the requestId to set
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * @return Object return the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * @return boolean return the success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @param success the success to set
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * @return String return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return long return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // toString() for logging

    @Override
    public String toString() {
        return "ExecutionResult{" +
                "requestId='" + requestId + '\'' +
                ", value=" + value +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

}
