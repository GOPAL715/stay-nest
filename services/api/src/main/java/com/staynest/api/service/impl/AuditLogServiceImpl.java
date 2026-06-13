package com.staynest.api.service.impl;

import com.staynest.api.entity.AuditLog;
import com.staynest.api.entity.User;
import com.staynest.api.repository.AuditLogRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void record(UUID actorId, String action, String entityType, UUID entityId,
                       Map<String, Object> beforeState, Map<String, Object> afterState, String ipAddress) {
        User actor = actorId != null
                ? userRepository.findById(actorId).orElse(null)
                : null;

        AuditLog entry = AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .beforeState(beforeState)
                .afterState(afterState)
                .ipAddress(ipAddress)
                .build();

        auditLogRepository.save(entry);
        log.debug("Audit log recorded: {} {} on {} {}", action, entityType, entityId, actorId);
    }
}
