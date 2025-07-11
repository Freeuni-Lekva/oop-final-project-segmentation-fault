package com.example.libraryproject.service.implementation;

import com.example.libraryproject.configuration.ApplicationProperties;
import com.example.libraryproject.model.dto.ActivationRequest;
import com.example.libraryproject.model.dto.ActivationResult;
import com.example.libraryproject.model.entity.AccountActivation;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.AccountActivationRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.AccountActivationService;
import com.example.libraryproject.service.MailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.example.libraryproject.configuration.ApplicationProperties.ACTIVATION_BASE_URL;

@RequiredArgsConstructor
public class AccountActivationServiceImpl implements AccountActivationService {

    private static final Logger logger = LoggerFactory.getLogger(AccountActivationServiceImpl.class);
    
    private final AccountActivationRepository accountActivationRepository;
    private final UserRepository userRepository;
    private final MailService mailService;


    @Override
    public boolean createActivation(User user, String activationBaseUrl) {
        logger.info("Creating activation for user: {}", user.getUsername());
        
        try {
            // Check if user already has a pending activation
            Optional<AccountActivation> existingActivation = accountActivationRepository.findByUser(user);
            if (existingActivation.isPresent()) {
                logger.info("User {} already has pending activation, updating expiration", user.getUsername());
                AccountActivation activation = existingActivation.get();
                activation.setExpirationDate(LocalDateTime.now().plusDays(1));
                activation.setToken(UUID.randomUUID());
                accountActivationRepository.update(activation);
                return sendActivationEmail(activation, activationBaseUrl);
            }

            AccountActivation activation = new AccountActivation(
                    user.getMail(),
                    user
            );
            
            accountActivationRepository.save(activation);
            logger.info("Activation token created for user: {}", user.getUsername());
            
            return sendActivationEmail(activation, activationBaseUrl);
            
        } catch (Exception e) {
            logger.error("Failed to create activation for user {}: {}", user.getUsername(), e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean createActivation(User user, HttpServletRequest request) {
        // Build activation URL dynamically from request
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();
        
        String activationBaseUrl = scheme + "://" + serverName + 
            (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + 
            contextPath + "/activate";
            
        return createActivation(user, activationBaseUrl);
    }

    @Override
    public ActivationResult activateAccount(UUID token) {
        logger.info("Attempting to activate account with token: {}", token);
        
        try {
            Optional<AccountActivation> activationOpt = accountActivationRepository.findByTokenAndNotExpired(token);
            
            if (activationOpt.isEmpty()) {
                logger.warn("Invalid or expired activation token: {}", token);
                return new ActivationResult(false, "Invalid or expired activation token", null);
            }
            
            AccountActivation activation = activationOpt.get();
            User user = activation.getUser();
            logger.info("Found valid activation for user: {} (current status: {})", user.getUsername(), user.getStatus());
            
            // Check if user is already active
            if (user.getStatus() == UserStatus.ACTIVE) {
                logger.warn("User {} is already active", user.getUsername());
                return new ActivationResult(false, "Account is already activated", user.getUsername());
            }
            
            // Update user status to ACTIVE
            user.setStatus(UserStatus.ACTIVE);
            userRepository.update(user);
            
            // Mark activation as completed
            activation.setActivated(true);
            accountActivationRepository.update(activation);
            
            logger.info("Account activated successfully for user: {}", user.getUsername());
            return new ActivationResult(true, "Account activated successfully", user.getUsername());
            
        } catch (Exception e) {
            logger.error("Failed to activate account with token {}: {}", token, e.getMessage(), e);
            return new ActivationResult(false, "Failed to activate account due to server error", null);
        }
    }

    @Override
    public ActivationResult activateAccount(String tokenString) {
        try {
            UUID token = UUID.fromString(tokenString);
            return activateAccount(token);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid token format: {}", tokenString);
            return new ActivationResult(false, "Invalid token format", null);
        }
    }

    @Override
    public boolean resendActivationEmail(ActivationRequest request) {
        logger.info("Resending activation email for: {}", request.email());
        
        try {
            // Find user by email or username
            Optional<User> userOpt = request.email() != null ? 
                    userRepository.findByMail(request.email()) : 
                    userRepository.findByUsername(request.username());
            
            if (userOpt.isEmpty()) {
                logger.warn("User not found for resend request: {}", request);
                return false;
            }
            
            User user = userOpt.get();
            
            // Check if user is already active
            if (user.getStatus() == UserStatus.ACTIVE) {
                logger.warn("User {} is already active", user.getUsername());
                return false;
            }
            
            // Find or create activation
            Optional<AccountActivation> activationOpt = accountActivationRepository.findByUser(user);
            
            if (activationOpt.isEmpty()) {
                // Create new activation if none exists
                // Use fallback URL for resend since we don't have request context
                return createActivation(user, ACTIVATION_BASE_URL + "/activate");
            }
            
            AccountActivation activation = activationOpt.get();
            
            // Update expiration date and generate new token
            activation.setExpirationDate(LocalDateTime.now().plusDays(1));
            activation.setToken(UUID.randomUUID());
            activation.setActivated(false);
            accountActivationRepository.update(activation);
            
            // Use fallback URL for resend since we don't have request context
            return sendActivationEmail(activation, ACTIVATION_BASE_URL + "/activate");
            
        } catch (Exception e) {
            logger.error("Failed to resend activation email: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean resendActivationEmail(ActivationRequest activationRequest, HttpServletRequest httpRequest) {
        logger.info("Resending activation email for: {}", activationRequest.email());
        
        try {
            // Find user by email or username
            Optional<User> userOpt = activationRequest.email() != null ? 
                    userRepository.findByMail(activationRequest.email()) : 
                    userRepository.findByUsername(activationRequest.username());
            
            if (userOpt.isEmpty()) {
                logger.warn("User not found for resend request: {}", activationRequest);
                return false;
            }
            
            User user = userOpt.get();
            
            // Check if user is already active
            if (user.getStatus() == UserStatus.ACTIVE) {
                logger.warn("User {} is already active", user.getUsername());
                return false;
            }
            
            // Find or create activation
            Optional<AccountActivation> activationOpt = accountActivationRepository.findByUser(user);
            
            if (activationOpt.isEmpty()) {
                // Create new activation if none exists using dynamic URL
                return createActivation(user, httpRequest);
            }
            
            AccountActivation activation = activationOpt.get();
            
            // Update expiration date and generate new token
            activation.setExpirationDate(LocalDateTime.now().plusDays(1));
            activation.setToken(UUID.randomUUID());
            activation.setActivated(false);
            accountActivationRepository.update(activation);
            
            // Build dynamic URL
            String scheme = httpRequest.getScheme();
            String serverName = httpRequest.getServerName();
            int serverPort = httpRequest.getServerPort();
            String contextPath = httpRequest.getContextPath();
            
            String activationBaseUrl = scheme + "://" + serverName + 
                (serverPort != 80 && serverPort != 443 ? ":" + serverPort : "") + 
                contextPath + "/activate";
            
            return sendActivationEmail(activation, activationBaseUrl);
            
        } catch (Exception e) {
            logger.error("Failed to resend activation email: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasPendingActivation(User user) {
        return accountActivationRepository.findByUser(user).isPresent();
    }



    @Override
    public boolean isTokenValid(UUID token) {
        return accountActivationRepository.findByTokenAndNotExpired(token).isPresent();
    }

    /**
     * Sends activation email to user
     */
    private boolean sendActivationEmail(AccountActivation activation, String activationBaseUrl) {
        try {
            User user = activation.getUser();
            String subject = "Activate Your Library Account";
            
            String activationLink = activationBaseUrl + "?token=" + activation.getToken();
            
            String htmlContent = String.format("""
                    <html>
                    <body>
                        <h2>Welcome to the Library, %s!</h2>
                        <p>Thank you for registering with our library system.</p>
                        <p>To activate your account, please click the link below:</p>
                        <p><a href="%s" style="background-color: #4CAF50; color: white; padding: 15px 32px; text-decoration: none; display: inline-block; border-radius: 4px;">Activate Account</a></p>
                        <p>Or copy and paste this link into your browser:</p>
                        <p>%s</p>
                        <p>This activation link will expire in 24 hours.</p>
                        <p>If you did not register for this account, please ignore this email.</p>
                        <br>
                        <p>Best regards,<br>Library Team</p>
                    </body>
                    </html>
                    """, user.getUsername(), activationLink, activationLink);
            
            mailService.sendHtmlEmail(
                    List.of(user.getMail()),
                    subject,
                    htmlContent
            );
            
            logger.info("Activation email sent successfully to: {}", user.getMail());
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send activation email to {}: {}", activation.getEmail(), e.getMessage(), e);
            return false;
        }
    }
}
