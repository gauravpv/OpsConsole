package com.opsconsole.auth.dto;

import com.opsconsole.auth.domain.AppUser;

import java.time.Instant;

public record UserDetailResponse(
        Long id,
        String email,
        String displayName,
        String jobTitle,
        String roleCode,
        String roleName,
        boolean enabled,
        Instant createdAt,
        Instant lastLoginAt,
        String azureAdId,
        boolean hasLocalPassword
) {
    public static UserDetailResponse from(AppUser user) {
        return new UserDetailResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getJobTitle(),
                user.getRole().getCode(),
                user.getRole().getName(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getLastLoginAt(),
                user.getAzureAdId(),
                user.hasLocalPassword()
        );
    }
}
