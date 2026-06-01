package com.opsconsole.auth;

import com.opsconsole.activity.ActivityFeedService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final AppUserRepository userRepository;
    private final ActivityFeedService activityFeedService;

    public LoginSuccessHandler(AppUserRepository userRepository, ActivityFeedService activityFeedService) {
        this.userRepository = userRepository;
        this.activityFeedService = activityFeedService;
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(false);
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        AppUser user = resolveUser(authentication);
        if (user != null) {
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
            activityFeedService.recordLogin(user);
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private static AppUser resolveUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OpsOidcUser oidcUser) {
            return oidcUser.getAppUser();
        }
        if (principal instanceof OpsUserPrincipal opsUser) {
            return opsUser.getUser();
        }
        return null;
    }
}
