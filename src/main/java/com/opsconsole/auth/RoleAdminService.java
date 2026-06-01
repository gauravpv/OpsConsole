package com.opsconsole.auth;

import com.opsconsole.activity.ActivityFeedService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RoleAdminService {

    private final AppRoleRepository roleRepository;
    private final RoleTabAccessRepository tabAccessRepository;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityFeedService activityFeedService;

    public RoleAdminService(
            AppRoleRepository roleRepository,
            RoleTabAccessRepository tabAccessRepository,
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ActivityFeedService activityFeedService
    ) {
        this.roleRepository = roleRepository;
        this.tabAccessRepository = tabAccessRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityFeedService = activityFeedService;
    }

    @Transactional(readOnly = true)
    public List<AppUser> allUsers() {
        return userRepository.findAllByOrderByDisplayNameAsc();
    }

    @Transactional(readOnly = true)
    public List<AppRole> allRoles() {
        return roleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<RoleTabAccessView> tabMatrixForRole(Long roleId) {
        return tabAccessRepository.findByRoleIdOrderByTabAsc(roleId).stream()
                .map(rta -> new RoleTabAccessView(rta.getTab(), rta.isAllowed()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<Long, Map<String, Boolean>> tabMatrixForAllRoles() {
        Map<Long, Map<String, Boolean>> result = new LinkedHashMap<>();
        for (AppRole role : roleRepository.findAll()) {
            Map<String, Boolean> flags = new LinkedHashMap<>();
            for (AppTab tab : AppTab.values()) {
                flags.put(tab.id(), false);
            }
            result.put(role.getId(), flags);
        }
        for (RoleTabAccess access : tabAccessRepository.findAll()) {
            Map<String, Boolean> flags = result.get(access.getRole().getId());
            if (flags != null) {
                flags.put(access.getTab().id(), access.isAllowed());
            }
        }
        return result;
    }

    @Transactional
    public void updateUserRole(Long userId, String roleCode, AppUser actor) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String previousRoleName = user.getRole().getName();
        AppRole role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
        activityFeedService.recordUserRoleChanged(actor, user, previousRoleName, role.getName());
    }

    @Transactional
    public void updateUserEnabled(Long userId, boolean enabled, AppUser actor) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        boolean wasEnabled = user.isEnabled();
        user.setEnabled(enabled);
        userRepository.save(user);
        if (wasEnabled != enabled) {
            activityFeedService.recordUserStatusChanged(actor, user, enabled);
        }
    }

    @Transactional
    public AppUser createUser(CreateUserRequest request) {
        String email = normalizeEmail(request.email());
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email is already in use");
        }

        String azureAdId = resolveAzureAdId(request.azureAdId(), email);
        if (userRepository.findByAzureAdId(azureAdId).isPresent()) {
            throw new IllegalArgumentException("Azure AD ID is already in use");
        }

        String displayName = request.displayName() == null ? "" : request.displayName().trim();
        if (!StringUtils.hasText(displayName)) {
            throw new IllegalArgumentException("Display name is required");
        }

        String password = request.password();
        if (!StringUtils.hasText(password) || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        AppRole role = roleRepository.findByCode(request.roleCode())
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        AppUser user = new AppUser(
                azureAdId,
                email,
                displayName,
                role,
                passwordEncoder.encode(password)
        );
        if (StringUtils.hasText(request.jobTitle())) {
            user.setJobTitle(request.jobTitle().trim());
        }
        user.setEnabled(request.enabled() == null || request.enabled());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, Long actorUserId) {
        if (userId.equals(actorUserId)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (AuthDataInitializer.CODE_ADMINISTRATOR.equals(user.getRole().getCode())) {
            long adminCount = userRepository.countByRole_Code(AuthDataInitializer.CODE_ADMINISTRATOR);
            if (adminCount <= 1) {
                throw new IllegalArgumentException("Cannot delete the last Administrator account");
            }
        }

        userRepository.delete(user);
    }

    @Transactional
    public void updateRoleTabs(Long roleId, Map<String, Boolean> tabAccess, AppUser actor) {
        AppRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));

        Map<AppTab, RoleTabAccess> existing = new LinkedHashMap<>();
        for (RoleTabAccess entry : tabAccessRepository.findByRoleIdOrderByTabAsc(roleId)) {
            existing.put(entry.getTab(), entry);
        }

        List<RoleTabAccess> toSave = new ArrayList<>();
        for (AppTab tab : AppTab.values()) {
            boolean allowed = Boolean.TRUE.equals(tabAccess.get(tab.id()));
            RoleTabAccess entry = existing.get(tab);
            if (entry == null) {
                toSave.add(new RoleTabAccess(role, tab, allowed));
            } else {
                entry.setAllowed(allowed);
                toSave.add(entry);
            }
        }
        tabAccessRepository.saveAll(toSave);
        activityFeedService.recordRoleTabsChanged(actor, role);
    }

    public record RoleTabAccessView(AppTab tab, boolean allowed) {
    }

    public record RoleTabsUpdateRequest(Map<String, Boolean> tabs) {
    }

    public record UserRoleUpdateRequest(String roleCode) {
    }

    public record UserStatusUpdateRequest(boolean enabled) {
    }

    public record CreateUserRequest(
            String displayName,
            String email,
            String azureAdId,
            String roleCode,
            String password,
            String jobTitle,
            Boolean enabled
    ) {
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String resolveAzureAdId(String azureAdId, String email) {
        if (StringUtils.hasText(azureAdId)) {
            return azureAdId.trim();
        }
        return "local-" + UUID.randomUUID();
    }
}
