package com.opsconsole.auth;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class NavAccessService {

    private final RoleTabAccessRepository tabAccessRepository;

    public NavAccessService(RoleTabAccessRepository tabAccessRepository) {
        this.tabAccessRepository = tabAccessRepository;
    }

    public boolean canAccess(AppUser user, AppTab tab) {
        if (user == null || !user.isEnabled()) {
            return false;
        }
        return tabAccessRepository.existsByRoleIdAndTabAndAllowedTrue(user.getRole().getId(), tab);
    }

    public Map<String, Boolean> navAccessMap(AppUser user) {
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (AppTab tab : AppTab.values()) {
            map.put(tab.id(), canAccess(user, tab));
        }
        return map;
    }

    public AppTab tabForPath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return AppTab.DASHBOARD;
        }
        for (AppTab tab : AppTab.values()) {
            if (tab.path().equals(path)) {
                return tab;
            }
        }
        return null;
    }
}
