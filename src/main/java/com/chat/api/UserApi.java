package com.chat.api;

import com.chat.model.RoleName;
import com.chat.model.User;
import com.chat.payload.*;
import com.chat.service.JwtTokenProvider;
import com.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UserApi {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider tokenProvider;

    public UserApi(UserService userService,
                   AuthenticationManager authenticationManager,
                   JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginPayload loginPayload) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginPayload.getUsername(),
                        loginPayload.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @Transactional
    @PostMapping(value = "/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignUpPayload payload) {
        log.info("creating user {}", payload.getUsername());

        User user = User
                .builder()
                .username(payload.getUsername())
                .password(payload.getPassword())
                .build();

        userService.registerUser(user, RoleName.USER);

        //generate token and return user with token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        payload.getUsername(),
                        payload.getPassword()
                )
        );
        String jwt = tokenProvider.generateToken(authentication);
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        LoginResult result = LoginResult.builder()
                .success(true)
                .message("User registered successfully")
                .body(convertTo(user))
                .token(jwt)
                .build();
        return ResponseEntity.created(location).body(result);
    }

    @GetMapping(value = "/users")
    public ResponseEntity<?> findAll(Pageable pageable) {
        log.info("retrieving all users");
        Page<User> page = userService.findAll(pageable);
        List<UserDto> userSummaries = page.getContent().stream()
                .map(this::convertTo).collect(Collectors.toList());
        return ResponseEntity.ok(UserResult.builder()
                .body(userSummaries)
                .success(true).build());
    }

    private UserDto convertTo(User user) {
        return UserDto
                .builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRoles().stream().map(x -> x.getName().name()).collect(Collectors.joining(",")))
                .build();
    }
}
