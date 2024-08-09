package com.be.rebook.common.eureka.strategy.impl;

import java.util.List;
import java.util.Random;

import org.springframework.cloud.client.ServiceInstance;

import com.be.rebook.common.eureka.strategy.InstanceSelectionStrategy;

public class RandomStrategy implements InstanceSelectionStrategy {
    
    private final Random random = new Random();

    @Override
    public ServiceInstance selectInstance(List<ServiceInstance> instances) {
        int index = random.nextInt(instances.size());
        return instances.get(index);
    }
}
