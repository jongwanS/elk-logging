package com.jw.study.elklogging.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor //@RequiredArgsConstructor는 final 필드 또는 @NonNull 필드만 생성자 주입 대상
public class LoggingFilter implements Filter {

    private final ObjectMapper objectMapper;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpServletRequest) {
            // 요청을 CachedBodyHttpServletRequest로 래핑
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpServletRequest);

            // URL, 메서드 및 요청 바디 로깅
            String url = wrappedRequest.getRequestURI();
            String method = wrappedRequest.getMethod();
            String body = wrappedRequest.getReader().lines().reduce("", String::concat);

//            log.info("Incoming Request: URL={}, Method={}, Body={}", url, method, body);

            // traceId 설정
            MDC.put("traceId", UUID.randomUUID().toString());

            // 로그 구조화
            Map<String, Object> logMap = new LinkedHashMap<>();
            logMap.put("traceId", MDC.get("traceId"));
            logMap.put("url", url);
            logMap.put("method", method);
            logMap.put("body", body);

            log.info(objectMapper.writeValueAsString(logMap));

            chain.doFilter(wrappedRequest, response);

            MDC.clear(); // 꼭 클리어해줘야 다음 요청에 traceId 섞이지 않음


            // 래핑된 요청 객체를 다음 필터 체인으로 전달
            chain.doFilter(wrappedRequest, response);
        } else {
            // HttpServletRequest가 아닌 경우 그대로 전달
            chain.doFilter(request, response);
        }
    }
}