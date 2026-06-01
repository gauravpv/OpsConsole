package com.opsconsole.auth;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static AppUser requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new IllegalStateException("Not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OpsOidcUser oidcUser) {
            return oidcUser.getAppUser();
        }
        if (principal instanceof OpsUserPrincipal opsUser) {
            return opsUser.getUser();
        }
        throw new IllegalStateException("Unknown principal type: " + principal.getClass().getName());
    }

    public static AppUser userOrNull() {
        try {
            return requireUser();
        } catch (IllegalStateException ex) {
            return null;
        }
    }
}
