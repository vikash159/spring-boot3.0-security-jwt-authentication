package com.chat.service;

import com.chat.exception.ResourceNotFoundException;
import com.chat.exception.UsernameAlreadyExistsException;
import com.chat.model.AppUser;
import com.chat.model.Role;
import com.chat.model.RoleName;
import com.chat.model.User;
import com.chat.repository.RoleRepository;
import com.chat.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;
    private RoleRepository roleRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public Page<User> findAll(Pageable pageable) {
        log.info("retrieving all users");
        return userRepository.findAll(pageable);
    }

    public Optional<User> findByUsername(String username) {
        log.info("retrieving user {}", username);
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        log.info("retrieving user {}", id);
        return userRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("user id %s not found", id)));
    }

    public User registerUser(User user, RoleName roleName) {
        log.info("registering user {}", user.getUsername());

        if (userRepository.existsByUsername(user.getUsername())) {
            log.warn("username {} already exists.", user.getUsername());

            throw new UsernameAlreadyExistsException(
                    String.format("username %s already exists", user.getUsername()));
        }
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(roleRepository.findByName(roleName));
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return savedUser;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username)
                .map(AppUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }
}
