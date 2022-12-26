package com.chat.repository;


import com.chat.model.Role;
import com.chat.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByName(RoleName name);
}
