package com.equipmentrental.identity.repository;

import com.equipmentrental.identity.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role", "role.rolePermissions", "role.rolePermissions.permission"})
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role", "role.rolePermissions", "role.rolePermissions.permission"})
    Optional<User> findDetailedById(Long id);

    boolean existsByRoleId(Long roleId);
}
