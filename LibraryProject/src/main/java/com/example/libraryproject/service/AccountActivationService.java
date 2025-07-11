package com.example.libraryproject.service;

import com.example.libraryproject.model.dto.ActivationRequest;
import com.example.libraryproject.model.dto.ActivationResult;
import com.example.libraryproject.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

public interface AccountActivationService {

    /**
     * Creates an activation token for a user and sends an activation email
     * @param user The user to create activation for
     * @param activationBaseUrl The base URL for activation links
     * @return true if activation email was sent successfully
     */
    boolean createActivation(User user, String activationBaseUrl);

    /**
     * Creates an activation token for a user using the request to build the activation URL dynamically
     * @param user The user to create activation for
     * @param request The HTTP request to extract context path from
     * @return true if activation email was sent successfully
     */
    boolean createActivation(User user, HttpServletRequest request);

    /**
     * Activates a user account using the provided token
     * @param token The activation token
     * @return ActivationResult containing success status and message
     */
    ActivationResult activateAccount(UUID token);

    /**
     * Activates a user account using string token (for web requests)
     * @param tokenString The activation token as string
     * @return ActivationResult containing success status and message
     */
    ActivationResult activateAccount(String tokenString);

    /**
     * Resends activation email for a user
     * @param request The activation request containing email/username
     * @return true if email was sent successfully
     */
    boolean resendActivationEmail(ActivationRequest request);

    /**
     * Resends activation email for a user using dynamic URL from HTTP request
     * @param activationRequest The activation request containing email/username
     * @param httpRequest The HTTP request to extract context path from
     * @return true if email was sent successfully
     */
    boolean resendActivationEmail(ActivationRequest activationRequest, HttpServletRequest httpRequest);

    /**
     * Checks if a user has a pending activation
     * @param user The user to check
     * @return true if user has pending activation
     */
    boolean hasPendingActivation(User user);

    /**
     * Checks if an activation token is valid and not expired
     * @param token The token to validate
     * @return true if token is valid
     */
    boolean isTokenValid(UUID token);
} 