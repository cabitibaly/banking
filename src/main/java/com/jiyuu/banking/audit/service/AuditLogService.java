package com.jiyuu.banking.audit.service;

import com.jiyuu.banking.audit.entity.AuditLog;
import com.jiyuu.banking.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@AllArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(
            String action,
            String entity,
            String idEntity,
            Object oldValue,
            Object newValue,
            String status,
            String errorMessage
    ) {
        try {
            String performer = this.extractCurrentUsername();
            String ip = this.extractIpAddress();

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntity(entity);
            auditLog.setIdEntity(idEntity);
            auditLog.setPerformedBy(performer);
            auditLog.setIpAddress(ip);
            auditLog.setOldValue(oldValue == null ? null : objectMapper.writeValueAsString(oldValue));
            auditLog.setNewValue(newValue == null ? null : objectMapper.writeValueAsString(newValue));
            auditLog.setStatus(status);
            auditLog.setErrorMessage(errorMessage);

            this.auditLogRepository.save(auditLog);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) return "anonymous";
        return authentication.getName();
    }

    private String extractIpAddress() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (requestAttributes == null) return "unknown";

            HttpServletRequest request = requestAttributes.getRequest();
            String ipAddress = request.getHeader("X-Forwarded-For");
            return StringUtils.hasText(ipAddress) ? ipAddress.split(",")[0] : request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }

}
