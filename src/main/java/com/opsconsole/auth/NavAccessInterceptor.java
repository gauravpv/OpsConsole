package com.opsconsole.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class NavAccessInterceptor implements HandlerInterceptor {

    private final NavAccessService navAccessService;

    public NavAccessInterceptor(NavAccessService navAccessService) {
        this.navAccessService = navAccessService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();
        if (path.startsWith("/api/health") || path.startsWith("/actuator")) {
            return true;
        }

        AppTab tab = navAccessService.tabForPath(path);
        if (tab == null) {
            return true;
        }

        AppUser user = CurrentUser.userOrNull();
        if (user == null) {
            return true;
        }

        if (!navAccessService.canAccess(user, tab)) {
            String redirect = navAccessService.canAccess(user, AppTab.DASHBOARD) ? "/?denied=1" : "/login?error";
            response.sendRedirect(redirect);
            return false;
        }
        return true;
    }
}
