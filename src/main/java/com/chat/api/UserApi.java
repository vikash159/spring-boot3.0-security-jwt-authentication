package com.chat.api;

import com.chat.model.Profile;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class UserApi {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtTokenProvider tokenProvider;

    public UserApi(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginPayload loginPayload) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginPayload.getUsername(), loginPayload.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);
        LoginResult result = LoginResult.builder().success(true).message("User Logged in successfully").body(Converter.convertTo(user)).token(jwt).build();
        return ResponseEntity.ok(result);
    }

    @Transactional
    @PostMapping(value = "/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody SignUpPayload payload) {
        log.info("creating user {}", payload.getUsername());

        User user = User.builder().username(payload.getUsername()).password(payload.getPassword()).build();

        Profile profile = Profile.builder().name(payload.getName()).user(user).build();
        user.setProfile(profile);

        User currentUser = userService.registerUser(user, RoleName.USER);

        //generate token and return user with token
        String jwt = tokenProvider.generateToken(payload.getUsername(), RoleName.USER);
        LoginResult result = LoginResult.builder().success(true).message("User registered successfully").body(Converter.convertTo(currentUser)).token(jwt).build();
        return ResponseEntity.ok(result);
    }

    @GetMapping(value = "/users")
    public ResponseEntity<?> findAll(Pageable pageable, @AuthenticationPrincipal User currentUser) {
        log.info("retrieving all users");
        Page<User> page = userService.findAllByIdNotIn(List.of(currentUser.getId()), pageable);
        List<UserDto> userSummaries = page.getContent().stream().map(Converter::convertTo).collect(Collectors.toList());
        return ResponseEntity.ok(UserResult.builder().body(userSummaries).page(com.chat.payload.Page.toPage(page)).success(true).build());
    }
}
