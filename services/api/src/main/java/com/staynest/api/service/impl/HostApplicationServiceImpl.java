package com.staynest.api.service.impl;

import com.staynest.api.dto.request.ModerationActionRequest;
import com.staynest.api.dto.request.SubmitHostApplicationRequest;
import com.staynest.api.dto.response.HostApplicationResponse;
import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.HostApplication;
import com.staynest.api.entity.User;
import com.staynest.api.enums.HostApplicationStatus;
import com.staynest.api.enums.UserRole;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.UserMapper;
import com.staynest.api.repository.HostApplicationRepository;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.EmailService;
import com.staynest.api.service.HostApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostApplicationServiceImpl implements HostApplicationService {

    private final HostApplicationRepository hostApplicationRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public HostApplicationResponse submitApplication(UUID userId, SubmitHostApplicationRequest request) {
        if (hostApplicationRepository.existsByApplicantId(userId)) {
            throw new BusinessRuleException("You have already submitted a host application");
        }

        User applicant = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (applicant.getRole() == UserRole.HOST) {
            throw new BusinessRuleException("You are already a host");
        }

        HostApplication application = HostApplication.builder()
                .applicant(applicant)
                .status(HostApplicationStatus.PENDING)
                .motivation(request.getMotivation())
                .build();

        hostApplicationRepository.save(application);
        log.info("Host application submitted by user [{}]", userId);
        return toResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HostApplicationResponse> listApplications(Pageable pageable) {
        return hostApplicationRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public HostApplicationResponse getApplicationById(UUID applicationId) {
        HostApplication application = getOrThrow(applicationId);
        return toResponse(application);
    }

    @Override
    @Transactional
    public HostApplicationResponse approveApplication(UUID applicationId, UUID adminId) {
        HostApplication application = getOrThrow(applicationId);

        if (application.getStatus() != HostApplicationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING applications can be approved. Current: " + application.getStatus());
        }

        User reviewer = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        // Upgrade applicant role to HOST
        User applicant = application.getApplicant();
        applicant.updateRole(UserRole.HOST);
        userRepository.save(applicant);

        application.approve(reviewer);
        hostApplicationRepository.save(application);

        emailService.sendGenericEmail(
                applicant.getEmail(),
                "Congratulations! Your host application has been approved 🎉",
                "Hi " + applicant.getFirstName() + ",\n\nYour application to become a StayNest host has been approved!\n"
                + "You can now create and manage property listings. Welcome to the host community!\n\n— The StayNest Team"
        );

        log.info("Host application [{}] approved by admin [{}]", applicationId, adminId);
        return toResponse(application);
    }

    @Override
    @Transactional
    public HostApplicationResponse rejectApplication(UUID applicationId, ModerationActionRequest request, UUID adminId) {
        HostApplication application = getOrThrow(applicationId);

        if (application.getStatus() != HostApplicationStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING applications can be rejected. Current: " + application.getStatus());
        }

        User reviewer = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

        application.reject(reviewer, request.getReason());
        hostApplicationRepository.save(application);

        emailService.sendGenericEmail(
                application.getApplicant().getEmail(),
                "Update on your StayNest host application",
                "Hi " + application.getApplicant().getFirstName() + ",\n\nUnfortunately, your host application was not approved at this time.\n\n"
                + "Reason: " + request.getReason() + "\n\nYou may reapply in the future.\n\n— The StayNest Team"
        );

        log.info("Host application [{}] rejected by admin [{}]", applicationId, adminId);
        return toResponse(application);
    }

    private HostApplication getOrThrow(UUID applicationId) {
        return hostApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Host application not found with id: " + applicationId));
    }

    private HostApplicationResponse toResponse(HostApplication app) {
        UserResponse reviewedByResponse = app.getReviewedBy() != null
                ? userMapper.toUserResponse(app.getReviewedBy()) : null;

        return HostApplicationResponse.builder()
                .id(app.getId())
                .applicant(userMapper.toUserResponse(app.getApplicant()))
                .status(app.getStatus())
                .motivation(app.getMotivation())
                .reviewNotes(app.getReviewNotes())
                .reviewedAt(app.getReviewedAt())
                .reviewedBy(reviewedByResponse)
                .createdAt(app.getCreatedAt())
                .build();
    }
}
