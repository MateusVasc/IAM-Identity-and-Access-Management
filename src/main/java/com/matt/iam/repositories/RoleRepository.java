package com.matt.iam.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.matt.iam.entities.Role;
import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}
