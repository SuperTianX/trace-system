package com.steel.trace.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.steel.trace.mapper.OperationLogMapper;
import com.steel.trace.common.util.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        String username = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String) {
            username = (String) auth.getPrincipal();
        }

        String params = "";
        try {
            params = objectMapper.writeValueAsString(point.getArgs());
        } catch (Exception e) {
            params = "serialize-error";
        }

        Object result;
        int status = 0;
        String responseResult = "";
        try {
            result = point.proceed();
            status = 1;
            try {
                responseResult = objectMapper.writeValueAsString(result);
            } catch (Exception ignored) {}
        } catch (Exception e) {
            status = 0;
            responseResult = e.getMessage();
            throw e;
        } finally {
            long executeTime = System.currentTimeMillis() - startTime;
            try {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                com.steel.trace.entity.OperationLog logEntry = new com.steel.trace.entity.OperationLog();
                logEntry.setUsername(username);
                logEntry.setIp(request.getRemoteAddr());
                logEntry.setModule(operationLog.module());
                logEntry.setAction(operationLog.action());
                logEntry.setDescription(operationLog.description());
                logEntry.setRequestUrl(request.getRequestURI());
                logEntry.setRequestMethod(request.getMethod());
                logEntry.setRequestParams(params.length() > 2000 ? params.substring(0, 2000) : params);
                logEntry.setResponseResult(responseResult.length() > 2000 ? responseResult.substring(0, 2000) : responseResult);
                logEntry.setExecuteTime(executeTime);
                logEntry.setStatus(status);
                operationLogMapper.insert(logEntry);
            } catch (Exception e) {
                log.warn("操作日志记录失败", e);
            }
        }
        return result;
    }
}
