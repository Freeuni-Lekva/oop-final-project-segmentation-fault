package com.example.libraryproject.model.dto;

import com.example.libraryproject.model.enums.Role;

public record RegistrationRequest(
        String username,
        String password,
        Role role
) {
}
