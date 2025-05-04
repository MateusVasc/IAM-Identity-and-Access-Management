package com.matt.iam.runners;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.matt.iam.entities.Permission;
import com.matt.iam.entities.Role;
import com.matt.iam.repositories.PermissionRepository;
import com.matt.iam.repositories.RoleRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DummyRoleInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        // Define permissões
        String[] permissionNames = {"READ_PRIVILEGES", "WRITE_PRIVILEGES"};

        // Cria permissões se não existirem
        Set<Permission> permissions = Arrays.stream(permissionNames)
            .map(name -> permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(null, name))))
            .collect(Collectors.toSet());

        // Cria role USER se não existir
        roleRepository.findByName("USER").ifPresentOrElse(
            r -> {}, // já existe, não faz nada
            () -> {
                Role userRole = new Role();
                userRole.setName("USER");
                userRole.setPermissions(permissions);
                roleRepository.save(userRole);
            }
        );
    }

}
