package com.equipmentrental.identity.repository;

import com.equipmentrental.identity.entity.Role;
import com.equipmentrental.identity.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role", "role.rolePermissions", "role.rolePermissions.permission"})
    Optional<User> findByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"role", "role.rolePermissions", "role.rolePermissions.permission"})
    Optional<User> findDetailedById(Long id);

    boolean existsByRoleId(Long roleId);

    @Modifying
    @Transactional
    @Query("update User user set user.role = :replacementRole where user.role.code = :legacyRoleCode")
    int reassignRole(@Param("legacyRoleCode") String legacyRoleCode, @Param("replacementRole") Role replacementRole);
}
