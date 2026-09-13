package io.github.absketches.doodlemini.dto;

import io.github.absketches.doodlemini.entity.UserAccount;

public record UserResponse(Long id, String name, String email) {

    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
