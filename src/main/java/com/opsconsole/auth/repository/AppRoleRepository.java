package com.opsconsole.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import com.opsconsole.auth.domain.AppRole;
public interface AppRoleRepository extends JpaRepository<AppRole, Long> {

    Optional<AppRole> findByCode(String code);
}
