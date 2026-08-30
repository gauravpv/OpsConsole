package com.opsconsole.auth.dto;

import com.opsconsole.auth.domain.UserActivityAction;
import com.opsconsole.auth.domain.UserActivityLog;

import java.time.Instant;

public record UserActivityLogView(
        Long id,
        UserActivityAction action,
        String actionLabel,
        String detail,
        String actorDisplayName,
        Instant occurredAt
) {
    public static UserActivityLogView from(UserActivityLog log) {
        return new UserActivityLogView(
                log.getId(),
                log.getAction(),
                labelFor(log.getAction()),
                log.getDetail(),
                log.getActorDisplayName(),
                log.getCreatedAt()
        );
    }

    private static String labelFor(UserActivityAction action) {
        return switch (action) {
            case LOGIN -> "Login";
            case USER_CREATED -> "Account created";
            case USER_DELETED -> "Account deleted";
            case ROLE_CHANGED -> "Role changed";
            case STATUS_CHANGED -> "Status changed";
            case PROFILE_UPDATED -> "Profile updated";
        };
    }
}
