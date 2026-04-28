package com.vortexvm;

import com.vortexvm.proxy.factory.ProxyFactory;
import com.vortexvm.service.MathService;
import com.vortexvm.service.MathServiceImpl;

import com.vortexvm.byteBuddy.ByteBuddyProxyFactory;
import com.vortexvm.proxy.strategy.ExecutionStrategy;
import com.vortexvm.proxy.strategy.LocalExecutionStrategy;
import com.vortexvm.proxy.strategy.OrchestratorStrategy;

public class Main {
    
    public static void main(String[] args){

        //creating proxy
        MathService service = ProxyFactory.createProxy(
            MathService.class,
            new MathServiceImpl()
        );

        //test 1: annotated method

        System.out.println("=== Test 1: add() ===");
        int addResult = service.add(5, 10);
        System.out.println("Result: " + addResult);

        
        // Test 2: Non-annotated method
        
        System.out.println("\n=== Test 2: multiply() ===");
        int multiplyResult = service.multiply(4, 3);
        System.out.println("Result: " + multiplyResult);

        
        // Test 3: Zero-argument method
        
        System.out.println("\n=== Test 3: randomNumber() ===");
        int random = service.randomNumber();
        System.out.println("Result: " + random);


        System.out.println("\n=== Phase 6: ByteBuddy Proxy (No Interface) ===");

// ByteBuddy works on concrete class directly — no interface needed
ExecutionStrategy local = new LocalExecutionStrategy();
ExecutionStrategy remote = new OrchestratorStrategy("localhost", 9090);

MathServiceImpl directService = ByteBuddyProxyFactory.createProxy(
        MathServiceImpl.class, local, remote);

int bbResult = directService.add(20, 30);
System.out.println("ByteBuddy Result: " + bbResult);

int bbMultiply = directService.multiply(6, 7);
System.out.println("ByteBuddy Multiply (local): " + bbMultiply);
    }
}
