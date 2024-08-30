package com.be.rebook.common.argumentresolver.auth;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.be.rebook.common.config.BaseResponse;
import com.be.rebook.common.exception.BaseException;
import com.be.rebook.common.restclients.AuthServiceRestClient;
import com.be.rebook.common.restclients.RestClientFactory;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthArgumentResolver implements HandlerMethodArgumentResolver {

    private final RestClientFactory restClientFactory;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterAnnotation(Auth.class) != null &&
                parameter.getParameterType().equals(MemberLoginInfo.class);
    }

    // TODO: 리팩토링 필요
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws BaseException {
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        try {
            String authorizationHeader = webRequest.getHeader("Authorization");

            AuthServiceRestClient authServiceRestClient = restClientFactory.createAuthServiceRestClient(
                    null);

            ResponseEntity<BaseResponse<MemberLoginInfo>> res;
            try {
                res = authServiceRestClient.authenticate(authorizationHeader);
            } catch (Exception e) {
                // 예외 발생 시 즉시 응답 작성 후 null 반환
                handleUnauthorizedResponse(response, "인증에 실패했습니다.");
                return null;
            }

            if (res.getStatusCode().is2xxSuccessful()) {
                BaseResponse<MemberLoginInfo> baseResponse = res.getBody();

                if (baseResponse != null && baseResponse.getResult() != null) {
                    MemberLoginInfo memberLoginInfo = baseResponse.getResult();
                    System.out.println("AuthArgumentResolver.resolveArgument() memberLoginInfo: " + memberLoginInfo);
                    return memberLoginInfo;
                } else {
                    handleUnauthorizedResponse(response, "응답에서 MemberLoginInfo를 가져올 수 없습니다.");
                    return null;
                }
            } else {
                handleUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleUnauthorizedResponse(response, "서버 내부 오류");
            return null;
        }
    }

    // FIXME: 응답이 두번 Write되어 에러가 발생함. 우선 응답은 되긴 함.
    private void handleUnauthorizedResponse(HttpServletResponse response, String message) {
        try {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"error\": \"" + message + "\"}");
            response.getWriter().flush();
            response.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
