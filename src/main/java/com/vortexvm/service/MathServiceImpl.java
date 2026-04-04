package com.vortexvm.service;

import com.vortexvm.annotation.Distributed;

public class MathServiceImpl implements MathService{
    
    @Distributed
    @Override
    public int add(int a, int b){
        return a+b;
    }

    @Override
    public int multiply(int a, int b){
        return a*b;
    }

    @Distributed
    @Override
    public int randomNumber(){
        return 42;//only for testing
    }
}
