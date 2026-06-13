package com.staynest.api.service;

import java.util.Map;
import java.util.UUID;

public interface AuditLogService {

    void record(UUID actorId, String action, String entityType, UUID entityId,
                Map<String, Object> beforeState, Map<String, Object> afterState, String ipAddress);
}
