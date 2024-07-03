package com.example.authproject.repository;

import com.example.authproject.entity.Role;
import com.example.authproject.enums.RoleEnum;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByAuthority(RoleEnum authority);
}
