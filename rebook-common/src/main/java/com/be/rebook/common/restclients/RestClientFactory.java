package com.be.rebook.common.restclients;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.be.rebook.common.eureka.strategy.InstanceSelectionStrategy;
import com.be.rebook.common.eureka.strategy.impl.RoundRobinStrategy;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class RestClientFactory {

    private final DiscoveryClient discoveryClient;

    /**
     * 인스턴스 목록 중에서 선택된 인스턴스를 이용하여 AuthServiceRestClient 인터페이스를 구현한 클라이언트를 생성한다.
     * 
     * @param instanceList              인스턴스 목록
     * @param instanceSelectionStrategy 인스턴스 선택 전략 (기본값: RoundRobinStrategy)
     * @return AuthServiceRestClient 인터페이스를 구현한 클라이언트
     */
    public AuthServiceRestClient createAuthServiceRestClient(
            InstanceSelectionStrategy instanceSelectionStrategy) {
        HttpServiceProxyFactory factory = makeFactory("rebook-auth", instanceSelectionStrategy);

        return factory.createClient(AuthServiceRestClient.class);
    }

    /**
     * 인스턴스 목록 중에서 선택된 인스턴스를 이용하여 AIServiceRestClient 인터페이스를 구현한 클라이언트를 생성한다.
     * 
     * @param instanceList              인스턴스 목록
     * @param instanceSelectionStrategy 인스턴스 선택 전략 (기본값: RoundRobinStrategy)
     * @return AIServiceRestClient 인터페이스를 구현한 클라이언트
     */
    public AIServiceRestClient createAIServiceRestClient(
            InstanceSelectionStrategy instanceSelectionStrategy) {
        HttpServiceProxyFactory factory = makeFactory("rebook-ai", instanceSelectionStrategy);

        return factory.createClient(AIServiceRestClient.class);
    }

    /**
     * 인스턴스 목록 중에서 선택된 인스턴스를 이용하여 MemberServiceRestClient 인터페이스를 구현한 클라이언트를 생성한다.
     * 
     * @param instanceList              인스턴스 목록
     * @param instanceSelectionStrategy 인스턴스 선택 전략 (기본값: RoundRobinStrategy)
     * @return MemberServiceRestClient 인터페이스를 구현한 클라이언트
     */
    public MemberServiceRestClient createMemberServiceRestClient(
            InstanceSelectionStrategy instanceSelectionStrategy) {
        HttpServiceProxyFactory factory = makeFactory("rebook-members", instanceSelectionStrategy);

        return factory.createClient(MemberServiceRestClient.class);
    }

    private HttpServiceProxyFactory makeFactory(String serviceId,
            InstanceSelectionStrategy instanceSelectionStrategy) {
        List<ServiceInstance> instanceList = discoveryClient.getInstances(serviceId);
        instanceSelectionStrategy = instanceSelectionStrategy == null ? new RoundRobinStrategy()
                : instanceSelectionStrategy;

        ServiceInstance instance = instanceSelectionStrategy.selectInstance(instanceList);

        RestClient restClient = RestClient.create(instance.getUri().toString());
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        return factory;
    }
}
