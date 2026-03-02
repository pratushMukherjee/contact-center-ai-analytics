package com.zoom.ccai.observability;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * HTTP request tracing interceptor for observability.
 *
 * Adds a unique trace ID to every incoming request via MDC (Mapped Diagnostic Context).
 * This trace ID propagates through all log statements during request processing,
 * enabling end-to-end tracing of multi-agent query execution.
 *
 * Also logs request/response metadata for performance monitoring.
 */
@Component
public class TraceInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TraceInterceptor.class);
    private static final String TRACE_ID_KEY = "traceId";
    private static final String START_TIME_KEY = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID_KEY, traceId);
        request.setAttribute(START_TIME_KEY, System.currentTimeMillis());

        response.setHeader("X-Trace-Id", traceId);

        log.info("[{}] {} {} started", traceId, request.getMethod(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Long startTime = (Long) request.getAttribute(START_TIME_KEY);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;
        String traceId = MDC.get(TRACE_ID_KEY);

        log.info("[{}] {} {} completed in {}ms (status: {})",
                traceId, request.getMethod(), request.getRequestURI(),
                duration, response.getStatus());

        MDC.remove(TRACE_ID_KEY);
    }
}
