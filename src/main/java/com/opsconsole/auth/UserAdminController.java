package com.opsconsole.auth;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class UserAdminController {

    private final RoleAdminService roleAdminService;

    public UserAdminController(RoleAdminService roleAdminService) {
        this.roleAdminService = roleAdminService;
    }

    @GetMapping("/users")
    public String userManagement(Model model) {
        model.addAttribute("activeNav", "users");
        model.addAttribute("users", roleAdminService.allUsers());
        model.addAttribute("roles", roleAdminService.allRoles());
        model.addAttribute("roleTabFlags", roleAdminService.tabMatrixForAllRoles());
        model.addAttribute("allTabs", AppTab.values());
        return "user-management";
    }
}
