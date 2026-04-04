package com.vortexvm;

import com.vortexvm.proxy.factory.ProxyFactory;
import com.vortexvm.service.MathService;
import com.vortexvm.service.MathServiceImpl;

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
    }
}
