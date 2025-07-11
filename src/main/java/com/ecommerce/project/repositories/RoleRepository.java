package com.ecommerce.project.repositories;

import com.ecommerce.project.models.AppRole;
import com.ecommerce.project.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(AppRole appRole);
}
