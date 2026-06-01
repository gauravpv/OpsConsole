package com.opsconsole.auth;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@ControllerAdvice
public class AuthModelAdvice {

    private final NavAccessService navAccessService;

    public AuthModelAdvice(NavAccessService navAccessService) {
        this.navAccessService = navAccessService;
    }

    @ModelAttribute("navAccess")
    public Map<String, Boolean> navAccess() {
        AppUser user = CurrentUser.userOrNull();
        if (user == null) {
            return null;
        }
        return navAccessService.navAccessMap(user);
    }

    @ModelAttribute("currentUser")
    public AppUser currentUser() {
        return CurrentUser.userOrNull();
    }
}
