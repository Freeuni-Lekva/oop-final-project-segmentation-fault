package com.example.libraryproject.service.implementation;

import com.example.libraryproject.model.dto.ActivationRequest;
import com.example.libraryproject.model.dto.ActivationResult;
import com.example.libraryproject.model.entity.AccountActivation;
import com.example.libraryproject.model.entity.User;
import com.example.libraryproject.model.enums.UserStatus;
import com.example.libraryproject.repository.AccountActivationRepository;
import com.example.libraryproject.repository.UserRepository;
import com.example.libraryproject.service.MailService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountActivationServiceImplTest {

    private AccountActivationRepository activationRepository;
    private UserRepository userRepository;
    private MailService mailService;
    private AccountActivationServiceImpl activationService;

    @BeforeEach
    void setUp() {
        activationRepository = mock(AccountActivationRepository.class);
        userRepository = mock(UserRepository.class);
        mailService = mock(MailService.class);
        activationService = new AccountActivationServiceImpl(activationRepository, userRepository, mailService);
    }

    @Test
    void testCreateActivation_NewUser_Success() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        user.setMail("test@example.com");

        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.createActivation(user, "http://localhost/activate");

        assertTrue(result);
        verify(activationRepository).save(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("test@example.com")), anyString(), anyString());
    }

    @Test
    void testCreateActivation_ExistingActivation_UpdatesToken() throws Exception {
        User user = new User();
        user.setUsername("existinguser");
        user.setMail("existing@example.com");

        AccountActivation existing = new AccountActivation("existing@example.com", user);
        when(activationRepository.findByUser(user)).thenReturn(Optional.of(existing));
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.createActivation(user, "http://localhost/activate");

        assertTrue(result);
        verify(activationRepository).update(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("existing@example.com")), anyString(), anyString());
    }

    @Test
    void testCreateActivation_WithHttpServletRequest() throws Exception {
        User user = new User();
        user.setUsername("httpuser");
        user.setMail("http@example.com");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("https");
        when(request.getServerName()).thenReturn("example.com");
        when(request.getServerPort()).thenReturn(443);
        when(request.getContextPath()).thenReturn("/library");

        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.createActivation(user, request);

        assertTrue(result);
        verify(activationRepository).save(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("http@example.com")), anyString(), anyString());
    }

    @Test
    void testCreateActivation_WithHttpServletRequest_NonStandardPort() throws Exception {
        User user = new User();
        user.setUsername("portuser");
        user.setMail("port@example.com");

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getScheme()).thenReturn("http");
        when(request.getServerName()).thenReturn("localhost");
        when(request.getServerPort()).thenReturn(8080);
        when(request.getContextPath()).thenReturn("/library");

        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.createActivation(user, request);

        assertTrue(result);
        verify(activationRepository).save(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("port@example.com")), anyString(), anyString());
    }

    @Test
    void testCreateActivation_FailsToSendEmail() throws Exception {
        User user = new User();
        user.setUsername("failuser");
        user.setMail("fail@example.com");

        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());
        doThrow(new Exception("Email sending failed")).when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.createActivation(user, "http://localhost/activate");

        assertFalse(result);
        verify(activationRepository).save(any(AccountActivation.class));
    }

    @Test
    void testActivateAccount_Success() {
        UUID token = UUID.randomUUID();
        User user = new User();
        user.setUsername("testuser");
        user.setStatus(UserStatus.INACTIVE);
        AccountActivation activation = new AccountActivation("test@example.com", user);
        activation.setToken(token);

        when(activationRepository.findByTokenAndNotExpired(token)).thenReturn(Optional.of(activation));

        ActivationResult result = activationService.activateAccount(token);

        assertTrue(result.success());
        verify(userRepository).update(user);
        verify(activationRepository).update(activation);
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        assertTrue(activation.isActivated());
        assertEquals("Account activated successfully", result.message());
        assertEquals("testuser", result.username());
    }

    @Test
    void testActivateAccount_TokenExpired() {
        UUID token = UUID.randomUUID();
        when(activationRepository.findByTokenAndNotExpired(token)).thenReturn(Optional.empty());

        ActivationResult result = activationService.activateAccount(token);

        assertFalse(result.success());
        assertEquals("Invalid or expired activation token", result.message());
        assertNull(result.username());
    }

    @Test
    void testActivateAccount_AlreadyActivated() {
        UUID token = UUID.randomUUID();
        User user = new User();
        user.setUsername("alreadyactive");
        user.setStatus(UserStatus.ACTIVE);
        AccountActivation activation = new AccountActivation("already@example.com", user);
        activation.setToken(token);

        when(activationRepository.findByTokenAndNotExpired(token)).thenReturn(Optional.of(activation));

        ActivationResult result = activationService.activateAccount(token);

        assertFalse(result.success());
        assertEquals("Account is already activated", result.message());
        assertEquals("alreadyactive", result.username());
    }

    @Test
    void testActivateAccount_Exception() {
        UUID token = UUID.randomUUID();
        User user = new User();
        user.setUsername("exceptionuser");
        user.setStatus(UserStatus.INACTIVE);
        AccountActivation activation = new AccountActivation("exception@example.com", user);
        activation.setToken(token);

        when(activationRepository.findByTokenAndNotExpired(token)).thenReturn(Optional.of(activation));
        doThrow(new RuntimeException("Database error")).when(userRepository).update(user);

        ActivationResult result = activationService.activateAccount(token);

        assertFalse(result.success());
        assertEquals("Failed to activate account due to server error", result.message());
        assertNull(result.username());
    }

    @Test
    void testActivateAccount_StringToken_Success() {
        UUID token = UUID.randomUUID();
        String tokenString = token.toString();
        User user = new User();
        user.setUsername("stringtokenuser");
        user.setStatus(UserStatus.INACTIVE);
        AccountActivation activation = new AccountActivation("stringtoken@example.com", user);
        activation.setToken(token);

        when(activationRepository.findByTokenAndNotExpired(token)).thenReturn(Optional.of(activation));

        ActivationResult result = activationService.activateAccount(tokenString);

        assertTrue(result.success());
        verify(userRepository).update(user);
        verify(activationRepository).update(activation);
    }

    @Test
    void testActivateAccount_InvalidStringToken() {
        String invalidToken = "not-a-uuid";

        ActivationResult result = activationService.activateAccount(invalidToken);

        assertFalse(result.success());
        assertEquals("Invalid token format", result.message());
        assertNull(result.username());
    }

    @Test
    void testResendActivationEmail_NewActivation() throws Exception {
        User user = new User();
        user.setUsername("resend");
        user.setMail("resend@example.com");
        user.setStatus(UserStatus.INACTIVE);

        ActivationRequest request = new ActivationRequest(null, "resend");

        when(userRepository.findByUsername("resend")).thenReturn(Optional.of(user));
        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.resendActivationEmail(request);

        assertTrue(result);
        verify(activationRepository).save(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("resend@example.com")), anyString(), anyString());
    }

    @Test
    void testResendActivationEmail_ExistingActivation() throws Exception {
        User user = new User();
        user.setUsername("resend2");
        user.setMail("resend2@example.com");
        user.setStatus(UserStatus.INACTIVE);
        AccountActivation activation = new AccountActivation("resend2@example.com", user);

        ActivationRequest request = new ActivationRequest("resend2@example.com", null);

        when(userRepository.findByMail("resend2@example.com")).thenReturn(Optional.of(user));
        when(activationRepository.findByUser(user)).thenReturn(Optional.of(activation));
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.resendActivationEmail(request);

        assertTrue(result);
        verify(activationRepository).update(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("resend2@example.com")), anyString(), anyString());
    }

    @Test
    void testResendActivationEmail_UserNotFound() {
        ActivationRequest request = new ActivationRequest("notfound@example.com", null);
        when(userRepository.findByMail("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = activationService.resendActivationEmail(request);

        assertFalse(result);
        verify(userRepository).findByMail("notfound@example.com");
        verifyNoInteractions(mailService);
    }

    @Test
    void testResendActivationEmail_UserAlreadyActive() {
        User user = new User();
        user.setUsername("active");
        user.setMail("active@example.com");
        user.setStatus(UserStatus.ACTIVE);

        ActivationRequest request = new ActivationRequest("active@example.com", null);
        when(userRepository.findByMail("active@example.com")).thenReturn(Optional.of(user));

        boolean result = activationService.resendActivationEmail(request);

        assertFalse(result);
        verify(userRepository).findByMail("active@example.com");
        verifyNoInteractions(mailService);
    }

    @Test
    void testResendActivationEmail_WithHttpServletRequest() throws Exception {
        User user = new User();
        user.setUsername("httpresend");
        user.setMail("httpresend@example.com");
        user.setStatus(UserStatus.INACTIVE);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getScheme()).thenReturn("https");
        when(httpRequest.getServerName()).thenReturn("example.com");
        when(httpRequest.getServerPort()).thenReturn(443);
        when(httpRequest.getContextPath()).thenReturn("/library");

        ActivationRequest activationRequest = new ActivationRequest(null, "httpresend");

        when(userRepository.findByUsername("httpresend")).thenReturn(Optional.of(user));
        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.resendActivationEmail(activationRequest, httpRequest);

        assertTrue(result);
        verify(activationRepository).save(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("httpresend@example.com")), anyString(), anyString());
    }

    @Test
    void testResendActivationEmail_WithHttpServletRequest_ExistingActivation() throws Exception {
        User user = new User();
        user.setUsername("httpresend2");
        user.setMail("httpresend2@example.com");
        user.setStatus(UserStatus.INACTIVE);
        AccountActivation activation = new AccountActivation("httpresend2@example.com", user);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getScheme()).thenReturn("http");
        when(httpRequest.getServerName()).thenReturn("localhost");
        when(httpRequest.getServerPort()).thenReturn(8080);
        when(httpRequest.getContextPath()).thenReturn("/library");

        ActivationRequest activationRequest = new ActivationRequest("httpresend2@example.com", null);

        when(userRepository.findByMail("httpresend2@example.com")).thenReturn(Optional.of(user));
        when(activationRepository.findByUser(user)).thenReturn(Optional.of(activation));
        doNothing().when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.resendActivationEmail(activationRequest, httpRequest);

        assertTrue(result);
        verify(activationRepository).update(any(AccountActivation.class));
        verify(mailService).sendHtmlEmail(eq(List.of("httpresend2@example.com")), anyString(), anyString());
    }

    @Test
    void testResendActivationEmail_WithHttpServletRequest_UserNotFound() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        ActivationRequest activationRequest = new ActivationRequest("notfound@example.com", null);
        
        when(userRepository.findByMail("notfound@example.com")).thenReturn(Optional.empty());

        boolean result = activationService.resendActivationEmail(activationRequest, httpRequest);

        assertFalse(result);
        verify(userRepository).findByMail("notfound@example.com");
        verifyNoInteractions(mailService);
    }

    @Test
    void testResendActivationEmail_WithHttpServletRequest_Exception() throws Exception {
        User user = new User();
        user.setUsername("exception");
        user.setMail("exception@example.com");
        user.setStatus(UserStatus.INACTIVE);

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getScheme()).thenReturn("https");
        when(httpRequest.getServerName()).thenReturn("example.com");
        when(httpRequest.getServerPort()).thenReturn(443);
        when(httpRequest.getContextPath()).thenReturn("/library");

        ActivationRequest activationRequest = new ActivationRequest(null, "exception");

        when(userRepository.findByUsername("exception")).thenReturn(Optional.of(user));
        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());
        doThrow(new Exception("Email sending failed")).when(mailService).sendHtmlEmail(anyList(), anyString(), anyString());

        boolean result = activationService.resendActivationEmail(activationRequest, httpRequest);

        assertFalse(result);
    }

    @Test
    void testHasPendingActivation_True() {
        User user = new User();
        when(activationRepository.findByUser(user)).thenReturn(Optional.of(new AccountActivation("mail", user)));

        assertTrue(activationService.hasPendingActivation(user));
    }

    @Test
    void testHasPendingActivation_False() {
        User user = new User();
        when(activationRepository.findByUser(user)).thenReturn(Optional.empty());

        assertFalse(activationService.hasPendingActivation(user));
    }

    @Test
    void testIsTokenValid_True() {
        UUID token = UUID.randomUUID();
        when(activationRepository.findByTokenAndNotExpired(token)).thenReturn(Optional.of(new AccountActivation()));

        assertTrue(activationService.isTokenValid(token));
    }

    @Test
    void testIsTokenValid_False() {
        UUID token = UUID.randomUUID();
        when(activationRepository.findByTokenAndNotExpired(token)).thenReturn(Optional.empty());

        assertFalse(activationService.isTokenValid(token));
    }
}
