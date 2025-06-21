package com.example.libraryproject.model.dto;
import com.example.libraryproject.model.entity.User;

public record UserDTO(
        String username,
        String status) {
    public static UserDTO convertUser(User user) {
        return new UserDTO(user.getUsername(), user.getStatus().toString());
    }
}
