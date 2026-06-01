package com.opsconsole.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByAzureAdId(String azureAdId);

    Optional<AppUser> findByEmailIgnoreCase(String email);

    List<AppUser> findAllByOrderByDisplayNameAsc();

    long countByRole_Code(String roleCode);
}
