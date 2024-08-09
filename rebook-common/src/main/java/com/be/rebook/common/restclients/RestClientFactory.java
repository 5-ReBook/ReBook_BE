package com.be.rebook.common.restclients;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.be.rebook.common.eureka.strategy.InstanceSelectionStrategy;
import com.be.rebook.common.eureka.strategy.impl.RoundRobinStrategy;

public class RestClientFactory {

    /**
     * 인스턴스 목록 중에서 선택된 인스턴스를 이용하여 AuthServiceRestClient 인터페이스를 구현한 클라이언트를 생성한다.
     * @param instanceList 인스턴스 목록
     * @param instanceSelectionStrategy 인스턴스 선택 전략 (기본값: RoundRobinStrategy)
     * @return AuthServiceRestClient 인터페이스를 구현한 클라이언트
     */
    public static AuthServiceRestClient createAuthServiceRestClient(List<ServiceInstance> instanceList, InstanceSelectionStrategy instanceSelectionStrategy) {

        instanceSelectionStrategy = instanceSelectionStrategy == null ? new RoundRobinStrategy() : instanceSelectionStrategy;
        ServiceInstance instance = instanceSelectionStrategy.selectInstance(instanceList);

        RestClient restClient = RestClient.create(instance.getUri().toString());
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory.createClient(AuthServiceRestClient.class);
    }
}
