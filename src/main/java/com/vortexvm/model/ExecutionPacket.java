package com.vortexvm.model;

public class ExecutionPacket {
    
    private String requestId;
    private String className;
    private String methodName;
    private String[] paramTypes;
    private Object[] args;
    private long timestamp;

    //no-arg constructor - required by jackson
    public ExecutionPacket(){

    }

    //All-arg constructor
    public ExecutionPacket(String requestId,
                           String className,
                           String methodName,
                           String[] paramTypes,
                           Object[] args,
                           long timestamp) {
        this.requestId = requestId;
        this.className = className;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.args = args;
        this.timestamp = timestamp;
    }

    //getters and setters
    
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(String[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // toString() for logging

    @Override
    public String toString() {
        return "ExecutionPacket{"+
                "requestId='" + requestId + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + java.util.Arrays.toString(paramTypes) +
                ", args=" + java.util.Arrays.toString(args) +
                ", timestamp=" + timestamp +
                '}';
    }

}
