package com.jiyuu.banking.audit.aspect;

import com.jiyuu.banking.audit.annotation.Auditable;
import com.jiyuu.banking.audit.service.AuditLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Aspect
@AllArgsConstructor
@Component
public class AuditAspect {
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String action = auditable.action();
        String entity = auditable.entity();
        Object result = null;
        String entityId = extractEntityId(joinPoint.getArgs());

        try {
            result = joinPoint.proceed();
            this.auditLogService.log(action, entity, entityId, null, result, "SUCCESS", null);
            return result;
        } catch (Exception e) {
            this.auditLogService.log(action, entity, entityId, null, null, "FAILURE", e.getMessage());
            throw e;
        }
    }

    private String extractEntityId(Object[] args) {
        if (args == null || args.length == 0) return null;

        for (Object arg : args) {
            if (arg instanceof Long || arg instanceof String) {
                return String.valueOf(arg);
            }
        }
        return null;
    }
}
