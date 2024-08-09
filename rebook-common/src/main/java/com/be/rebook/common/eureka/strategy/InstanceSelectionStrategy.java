package com.be.rebook.common.eureka.strategy;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;

public interface InstanceSelectionStrategy {
    ServiceInstance selectInstance(List<ServiceInstance> instances);
}
