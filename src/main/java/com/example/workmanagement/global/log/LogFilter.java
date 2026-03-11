package com.example.workmanagement.global.log;

import com.example.workmanagement.global.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class LogFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long startTimestamp = System.currentTimeMillis();
        String exactUrl = request.getRequestURL().toString();

        try {
            MDC.put("http_method", request.getMethod());
            filterChain.doFilter(request, response);
        } finally {
            long elapsedTimestamp = System.currentTimeMillis() - startTimestamp;
            MDC.put("http_status", String.valueOf(response.getStatus()));
            MDC.put("elapsed_time_ms", String.valueOf(elapsedTimestamp));

            ErrorCode errorCode = (ErrorCode) request.getAttribute(LogConstants.ATTR_NAME_ERR_CODE);
            String errorMessage = (String) request.getAttribute(LogConstants.ATTR_NAME_ERR_MSG);
            Exception thrown = (Exception) request.getAttribute(LogConstants.ATTR_NAME_CAUGHT_EXCEPTION);

            HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(response.getStatus());
            if (httpStatusCode.is2xxSuccessful() || httpStatusCode.is3xxRedirection()) {
                log.info("[SUCCESS] HTTP_RESPONSE: exactUrl={}", exactUrl);
            } else if (httpStatusCode.is4xxClientError()) {
                log.warn("[WARNING] HTTP_RESPONSE: exactUrl={}, errCode={}, errMessage={}", exactUrl, errorCode, errorMessage, thrown);
            } else { // 5xx
                log.error("[ERROR] HTTP_RESPONSE: exactUrl={}, errCode={}, errMessage={}", exactUrl, errorCode, errorMessage, thrown);
            }

            MDC.clear(); // 누수 방지를 위해
        }
    }
}
