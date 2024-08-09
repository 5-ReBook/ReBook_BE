package com.be.rebook.common.eureka.strategy.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.cloud.client.ServiceInstance;

import com.be.rebook.common.eureka.strategy.InstanceSelectionStrategy;

public class RoundRobinStrategy implements InstanceSelectionStrategy {
    private final AtomicInteger position = new AtomicInteger(0); // 여러개의 요청 스레드에 대한 동시성 문제를 해결하기 위해 AtomicInteger를 사용

    @Override
    public ServiceInstance selectInstance(List<ServiceInstance> instances) {
        int pos = Math.abs(position.getAndIncrement() % instances.size());
        return instances.get(pos);
    }
}
