package com.jw.study.elklogging.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter implements Filter {

    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest(httpRequest);
        CachedBodyHttpServletResponse wrappedResponse = new CachedBodyHttpServletResponse(httpResponse);

        String method = wrappedRequest.getMethod();
        String url = wrappedRequest.getRequestURI();
        String requestBody = wrappedRequest.getReader().lines().reduce("", String::concat);

        setTraceId();

        long startTime = System.currentTimeMillis();
        try {
            logRequest(method, url, requestBody);

            chain.doFilter(wrappedRequest, wrappedResponse); // chain 호출

            String responseBody = new String(wrappedResponse.getCachedBody(), response.getCharacterEncoding());
            long duration = System.currentTimeMillis() - startTime;

            logResponse(method, url, wrappedResponse.getStatus(), responseBody, duration);

            wrappedResponse.copyBodyToResponse(); // 응답 복사
        } finally {
            clearTraceId(); // 꼭 클리어
        }
    }

    private void setTraceId() {
        MDC.put("traceId", UUID.randomUUID().toString());
    }

    private void clearTraceId() {
        MDC.clear();
    }

    private void logRequest(String method, String url, String body) {
        log.info("REQUEST: {} {}", method, url,
                keyValue("traceId", MDC.get("traceId")),
                keyValue("url", url),
                keyValue("method", method),
                keyValue("body", body)
        );
    }

    private void logResponse(String method, String url, int status, String body, long duration) {
        log.info("RESPONSE: {} {} - status: {}", method, url, status,
                keyValue("traceId", MDC.get("traceId")),
                keyValue("status", status),
                keyValue("body", body),
                keyValue("duration", duration)
        );
    }
}