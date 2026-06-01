package com.opsconsole.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RoleAdminServiceTest {

    @Autowired
    private RoleAdminService roleAdminService;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void createUser_persistsWithPassword() {
        AppUser created = roleAdminService.createUser(new RoleAdminService.CreateUserRequest(
                "New Tester",
                "new-tester@opsconsole.local",
                null,
                AuthDataInitializer.CODE_TESTER,
                "TestPass@1",
                "QA",
                true
        ));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getEmail()).isEqualTo("new-tester@opsconsole.local");
        assertThat(created.hasLocalPassword()).isTrue();
        assertThat(created.getJobTitle()).isEqualTo("QA");
    }

    @Test
    void createUser_rejectsDuplicateEmail() {
        roleAdminService.createUser(new RoleAdminService.CreateUserRequest(
                "Dup",
                "dup@opsconsole.local",
                null,
                AuthDataInitializer.CODE_MONITORING,
                "TestPass@1",
                null,
                true
        ));

        assertThatThrownBy(() -> roleAdminService.createUser(new RoleAdminService.CreateUserRequest(
                "Dup 2",
                "dup@opsconsole.local",
                null,
                AuthDataInitializer.CODE_MONITORING,
                "TestPass@2",
                null,
                true
        ))).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email");
    }

    @Test
    void deleteUser_removesAccount() {
        AppUser created = roleAdminService.createUser(new RoleAdminService.CreateUserRequest(
                "To Delete",
                "delete-me@opsconsole.local",
                "local-delete-test",
                AuthDataInitializer.CODE_MONITORING,
                "TestPass@1",
                null,
                true
        ));
        AppUser admin = userRepository.findByAzureAdId("dev-admin").orElseThrow();

        roleAdminService.deleteUser(created.getId(), admin.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void deleteUser_cannotDeleteSelf() {
        AppUser admin = userRepository.findByAzureAdId("dev-admin").orElseThrow();

        assertThatThrownBy(() -> roleAdminService.deleteUser(admin.getId(), admin.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("own account");
    }
}
