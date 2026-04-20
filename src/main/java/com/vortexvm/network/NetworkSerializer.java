package com.vortexvm.network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vortexvm.model.ExecutionPacket;
import com.vortexvm.model.ExecutionResult;

import java.io.*;

public class NetworkSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    //  Send ExecutionPacket
    public static void sendPacket(OutputStream out, ExecutionPacket packet) throws Exception {

        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(packet);

        DataOutputStream dos = new DataOutputStream(out);

        // Step 1: write length (4 bytes)
        dos.writeInt(bytes.length);

        // Step 2: write actual data
        dos.write(bytes);

        dos.flush();
    }

    // 🔹 Receive ExecutionPacket
    public static ExecutionPacket receivePacket(InputStream in) throws Exception {

        DataInputStream dis = new DataInputStream(in);

        // Step 1: read length
        int length = dis.readInt();

        // Step 2: read exact bytes
        byte[] bytes = new byte[length];
        dis.readFully(bytes);

        // Step 3: convert to object
        return OBJECT_MAPPER.readValue(bytes, ExecutionPacket.class);
    }

    // 🔹 Send ExecutionResult
    public static void sendResult(OutputStream out, ExecutionResult result) throws Exception {

        byte[] bytes = OBJECT_MAPPER.writeValueAsBytes(result);

        DataOutputStream dos = new DataOutputStream(out);

        dos.writeInt(bytes.length);
        dos.write(bytes);

        dos.flush();
    }

    // 🔹 Receive ExecutionResult
    public static ExecutionResult receiveResult(InputStream in) throws Exception {

        DataInputStream dis = new DataInputStream(in);

        int length = dis.readInt();

        byte[] bytes = new byte[length];
        dis.readFully(bytes);

        return OBJECT_MAPPER.readValue(bytes, ExecutionResult.class);
    }
}