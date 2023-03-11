package com.chat.config;

import com.chat.model.Profile;
import com.chat.model.Role;
import com.chat.model.RoleName;
import com.chat.model.User;
import com.chat.repository.RoleRepository;
import com.chat.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements ApplicationRunner {
    public final String CREATEDB = "create";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    public DataLoader(RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder,
                      UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (args != null) {
            if (args.getOptionNames().contains(CREATEDB)) {
                initialDb();
            }
        }
    }

    private void initialDb() {
        if (roleRepository.count() == 0) {
            Set<Role> roles = new HashSet<>();
            for (RoleName roleName : RoleName.values()) {
                Role role = new Role();
                role.setName(roleName);
                roles.add(role);
            }
            roleRepository.saveAll(roles);

            User user = new User();
            user.setUsername("admin");
            user.setActive(true);
            user.setPassword(passwordEncoder.encode("admin"));

            Role roleAdmin = roles.stream().filter(x -> x.getName() == RoleName.ADMIN)
                    .findAny().get();
            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(roleAdmin);
            user.setRoles(adminRoles);

            Profile profile = Profile.builder()
                    .name("Admin Admin")
                    .user(user)
                    .build();
            user.setProfile(profile);
            userRepository.save(user);
        }
    }
}
