package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.LoginRequest;
import com.example.libraryproject.model.dto.LoginResult;
import com.example.libraryproject.model.dto.RegistrationRequest;

public interface AuthorizationService {

    void register(RegistrationRequest request);

    LoginResult login(LoginRequest request);

    boolean checkBookkeeper(String username);

    boolean checkUser(String username);

}
