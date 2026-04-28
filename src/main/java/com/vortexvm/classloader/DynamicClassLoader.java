package com.vortexvm.classloader;

public class DynamicClassLoader extends ClassLoader {

    // 🔹 Constructor with parent delegation
    public DynamicClassLoader(ClassLoader parent) {
        super(parent);
    }

    // 🔹 Expose defineClass (normally protected)
    public Class<?> defineClass(String name, byte[] bytes) {
        return super.defineClass(name, bytes, 0, bytes.length);
    }
}