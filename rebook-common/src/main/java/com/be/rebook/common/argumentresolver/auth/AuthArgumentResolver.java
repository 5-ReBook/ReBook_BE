package com.be.rebook.common.argumentresolver.auth;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.be.rebook.common.restclients.AuthServiceRestClient;
import com.be.rebook.common.restclients.RestClientFactory;

@Component
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final DiscoveryClient discoveryClient;
    private final RestClient restClient;

    public AuthArgumentResolver(DiscoveryClient discoveryClient, RestClient.Builder restClientBuilder) {
        this.discoveryClient = discoveryClient;
        restClient = restClientBuilder.build();
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(Auth.class) != null &&
                parameter.getParameterType().equals(MemberLoginInfo.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        String authorizationHeader = webRequest.getHeader("Authorization");

        List<ServiceInstance> instanceList = discoveryClient.getInstances("rebook-auth");
        AuthServiceRestClient authServiceRestClient = RestClientFactory.createAuthServiceRestClient(instanceList, null);

        return authServiceRestClient.authenticate(authorizationHeader);
    }
}
