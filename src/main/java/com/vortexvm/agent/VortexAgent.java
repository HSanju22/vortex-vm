package com.vortexvm.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class VortexAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[VortexAgent] Agent attached to JVM");

        inst.addTransformer(new ClassFileTransformer() {

            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String className,
                    Class<?> classBeingRedefined,
                    ProtectionDomain protectionDomain,
                    byte[] classfileBuffer) {

                // Skip null, JDK internal and non-project classes
                if (className == null
                        || className.startsWith("java/")
                        || className.startsWith("sun/")
                        || className.startsWith("jdk/")
                        || className.startsWith("com/fasterxml")
                        || className.startsWith("net/bytebuddy")) {
                    return null;
                }

                // Only inspect our project classes
                if (!className.startsWith("com/vortexvm")) {
                    return null;
                }

                String normalClassName = className.replace("/", ".");

                // Check for @Distributed annotation string in raw bytecode
                // The annotation descriptor appears as a string in the .class file
                String bytecodeStr = new String(classfileBuffer);
                if (bytecodeStr.contains("Distributed")) {
                    System.out.println("[VortexAgent] Detected @Distributed in class: "
                            + normalClassName);
                }

                return null; // leave bytecode unchanged
            }
        });
    }
}