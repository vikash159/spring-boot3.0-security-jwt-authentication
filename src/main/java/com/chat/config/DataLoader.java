package com.chat.config;

import com.chat.model.AppUser;
import com.chat.model.Role;
import com.chat.model.RoleName;
import com.chat.model.User;
import com.chat.repository.RoleRepository;
import com.chat.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
            Role role = new Role();
            role.setName(RoleName.ADMIN);
            roles.add(role);

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


            AppUser userDetails = new AppUser(user);
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            userRepository.save(user);
        }
    }
}
