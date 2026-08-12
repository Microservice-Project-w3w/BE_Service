package com.equipmentrental.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "roles",
        uniqueConstraints = {@UniqueConstraint(name = "uq_roles_code", columnNames = "code")})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_system", nullable = false)
    private boolean systemRole;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<RolePermission> rolePermissions = new LinkedHashSet<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Role() {}

    public Role(String code, String name, String description, boolean systemRole, boolean active) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.systemRole = systemRole;
        this.active = active;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public boolean isActive() {
        return active;
    }

    public Set<RolePermission> getRolePermissions() {
        return Set.copyOf(rolePermissions);
    }

    public Set<Permission> getPermissions() {
        Set<Permission> permissions = new LinkedHashSet<>();
        for (RolePermission rolePermission : rolePermissions) {
            permissions.add(rolePermission.getPermission());
        }
        return permissions;
    }

    public void replaceRolePermissions(Set<RolePermission> permissions) {
        if (permissions == null) {
            rolePermissions.clear();
            return;
        }

        rolePermissions.removeIf(existing -> permissions.stream().noneMatch(np -> np.getPermission()
                .getCode()
                .equals(existing.getPermission().getCode())));

        for (RolePermission newPerm : permissions) {
            RolePermission existing = rolePermissions.stream()
                    .filter(e -> e.getPermission()
                            .getCode()
                            .equals(newPerm.getPermission().getCode()))
                    .findFirst()
                    .orElse(null);

            if (existing != null) {
                existing.setDataScope(newPerm.getDataScope());
            } else {
                newPerm.setRole(this);
                rolePermissions.add(newPerm);
            }
        }
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
