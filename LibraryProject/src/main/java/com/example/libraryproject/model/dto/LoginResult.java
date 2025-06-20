package com.example.libraryproject.model.dto;

import com.example.libraryproject.model.enums.Role;

public record LoginResult(
        String username,
        Role role) {}
