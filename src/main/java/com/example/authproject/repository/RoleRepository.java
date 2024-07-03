package com.example.authproject.repository;

import com.example.authproject.entity.Role;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role, Long> {
    Optional<Role> findByAuthority(int authority);
}
