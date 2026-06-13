package com.staynest.api.service;

import com.staynest.api.dto.request.ModerationActionRequest;
import com.staynest.api.dto.request.SubmitHostApplicationRequest;
import com.staynest.api.dto.response.HostApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HostApplicationService {

    HostApplicationResponse submitApplication(UUID userId, SubmitHostApplicationRequest request);

    Page<HostApplicationResponse> listApplications(Pageable pageable);

    HostApplicationResponse getApplicationById(UUID applicationId);

    HostApplicationResponse approveApplication(UUID applicationId, UUID adminId);

    HostApplicationResponse rejectApplication(UUID applicationId, ModerationActionRequest request, UUID adminId);
}
