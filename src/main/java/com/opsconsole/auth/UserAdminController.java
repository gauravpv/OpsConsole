package com.opsconsole.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class UserAdminController {

    private final RoleAdminService roleAdminService;

    public UserAdminController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        List<AppRole> roles = roleAdminService.allRoles();
        Map<Long, Map<String, Boolean>> roleTabFlags = new LinkedHashMap<>();
        for (AppRole role : roles) {
            Map<String, Boolean> flags = new LinkedHashMap<>();
            for (AppTab tab : AppTab.values()) {
                flags.put(tab.id(), false);
            }
            for (RoleAdminService.RoleTabAccessView entry : roleAdminService.tabMatrixForRole(role.getId())) {
                flags.put(entry.tab().id(), entry.allowed());
            }
            roleTabFlags.put(role.getId(), flags);
        }

        model.addAttribute("activeNav", "users");
        model.addAttribute("users", roleAdminService.allUsers());
        model.addAttribute("roles", roles);
        model.addAttribute("roleTabFlags", roleTabFlags);
        model.addAttribute("allTabs", AppTab.values());
        return "user-management";
    }
}
