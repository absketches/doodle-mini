package io.github.absketches.doodlemini.controller;

import io.github.absketches.doodlemini.dto.CreateUserRequest;
import io.github.absketches.doodlemini.dto.UserResponse;
import io.github.absketches.doodlemini.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @PostMapping
    ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = UserResponse.from(users.create(request.name(), request.email()));
        return ResponseEntity.created(URI.create("/users/" + response.id())).body(response);
    }

    @GetMapping("/{userId}")
    UserResponse get(@PathVariable Long userId) {
        return UserResponse.from(users.get(userId));
    }
}
