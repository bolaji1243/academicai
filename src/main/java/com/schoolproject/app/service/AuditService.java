package com.schoolproject.app.service;

import com.schoolproject.app.entity.AuditEvent;
import com.schoolproject.app.entity.User;
import com.schoolproject.app.enums.AuditEventType;
import com.schoolproject.app.repository.AuditEventRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
public class AuditService {

    private final AuditEventRepository auditEventRepository;
    private final RequestMetadataService requestMetadataService;
    private final EntityManager entityManager;
    private final TransactionTemplate transactionTemplate;

    public AuditService(
            AuditEventRepository auditEventRepository,
            RequestMetadataService requestMetadataService,
            EntityManager entityManager,
            PlatformTransactionManager transactionManager
    ) {
        this.auditEventRepository = auditEventRepository;
        this.requestMetadataService = requestMetadataService;
        this.entityManager = entityManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    public void record(AuditEventType eventType, User user, String email, String details) {
        try {
            transactionTemplate.executeWithoutResult(status ->
                    auditEventRepository.save(AuditEvent.builder()
                            .eventType(eventType)
                            .user(resolveVisibleUser(user))
                            .email(trimToLength(email, 160))
                            .ipAddress(requestMetadataService.getIpAddress())
                            .userAgent(requestMetadataService.getUserAgent())
                            .details(trimToLength(details, 500))
                            .build())
            );
        } catch (Exception ex) {
            log.warn("Failed to record audit event {}", eventType, ex);
        }
    }

    private User resolveVisibleUser(User user) {
        if (user == null || user.getId() == null) {
            return null;
        }

        return entityManager.find(User.class, user.getId());
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
