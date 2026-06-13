package com.staynest.api.service;

import com.staynest.api.dto.response.UserResponse;
import com.staynest.api.entity.User;
import com.staynest.api.enums.UserRole;
import com.staynest.api.enums.UserStatus;
import com.staynest.api.exception.BusinessRuleException;
import com.staynest.api.exception.ResourceNotFoundException;
import com.staynest.api.mapper.UserMapper;
import com.staynest.api.repository.UserRepository;
import com.staynest.api.service.impl.AdminUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User guest() {
        return User.builder()
                .email("guest@example.com")
                .passwordHash("hash")
                .firstName("Alice")
                .lastName("Guest")
                .role(UserRole.GUEST)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();
    }

    @Test
    void listUsers_returnsMappedPage() {
        User user = guest();
        given(userRepository.findAll(any(org.springframework.data.domain.Pageable.class))).willReturn(new PageImpl<>(List.of(user)));
        given(userMapper.toUserResponse(user)).willReturn(
                UserResponse.builder().email("guest@example.com").build());

        Page<UserResponse> result = adminUserService.listUsers(PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateUserStatus_deactivatesUser() {
        UUID userId = UUID.randomUUID();
        User user = guest();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);
        given(userMapper.toUserResponse(user)).willReturn(
                UserResponse.builder().status(UserStatus.INACTIVE).build());

        UserResponse result = adminUserService.updateUserStatus(userId, UserStatus.INACTIVE, UUID.randomUUID());

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
        assertThat(result.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }

    @Test
    void updateUserStatus_deletedViaStatusEndpoint_throwsBusinessRuleException() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.of(guest()));

        assertThrows(BusinessRuleException.class,
                () -> adminUserService.updateUserStatus(userId, UserStatus.DELETED, UUID.randomUUID()));
    }

    @Test
    void updateUserRole_changesRole() {
        UUID userId = UUID.randomUUID();
        User user = guest();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);
        given(userMapper.toUserResponse(user)).willReturn(
                UserResponse.builder().role(UserRole.HOST).build());

        adminUserService.updateUserRole(userId, UserRole.HOST, UUID.randomUUID());

        assertThat(user.getRole()).isEqualTo(UserRole.HOST);
    }

    @Test
    void softDeleteUser_marksDeleted() {
        UUID userId = UUID.randomUUID();
        User user = guest();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(user)).willReturn(user);

        adminUserService.softDeleteUser(userId, UUID.randomUUID());

        assertThat(user.getStatus()).isEqualTo(UserStatus.DELETED);
    }

    @Test
    void softDeleteUser_superAdmin_throwsBusinessRuleException() {
        UUID userId = UUID.randomUUID();
        User admin = User.builder()
                .email("admin@staynest.com")
                .passwordHash("hash")
                .firstName("Super")
                .lastName("Admin")
                .role(UserRole.SUPER_ADMIN)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .failedLoginAttempts(0)
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(admin));

        assertThrows(BusinessRuleException.class,
                () -> adminUserService.softDeleteUser(userId, UUID.randomUUID()));
    }

    @Test
    void getUserById_notFound_throwsResourceNotFoundException() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminUserService.getUserById(userId));
    }
}
