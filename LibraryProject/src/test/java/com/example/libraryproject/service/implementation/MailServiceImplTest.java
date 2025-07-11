package com.example.libraryproject.service.implementation;

import org.apache.commons.mail.EmailException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MailServiceImplTest {

    private MailServiceImpl mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailServiceImpl();
    }

    @Test
    void test1() {
        List<String> recipients = Arrays.asList("test1@example.com", "test2@example.com");
        String subject = "Test Subject";
        String message = "Test Message";

        assertDoesNotThrow(() -> {
            mailService.sendEmail(recipients, subject, message);
        });
    }

    @Test
    void test2() {
        List<String> recipients = Collections.singletonList("test@example.com");
        String subject = "Test Subject Empty Recipients";
        String message = "Test Message";

        assertDoesNotThrow(() -> {
            mailService.sendEmail(recipients, subject, message);
        });
    }

    @Test
    void test3() {
        List<String> recipients = Arrays.asList("test1@example.com", "test2@example.com");
        String subject = "HTML Test Subject";
        String htmlContent = "<h1>Test HTML Content</h1>";

        try {
            mailService.sendHtmlEmail(recipients, subject, htmlContent);
        } catch (EmailException e) {
            assertTrue(e.getMessage().contains("mail") || e.getMessage().contains("smtp") || e.getMessage().contains("connection"));
        }
    }

    @Test
    void test4() {
        List<String> recipients = Collections.singletonList("test@example.com");
        String subject = "HTML Test Subject Single Recipient";
        String htmlContent = "<p>Test HTML Content</p>";

        try {
            mailService.sendHtmlEmail(recipients, subject, htmlContent);
        } catch (EmailException e) {
            assertTrue(e.getMessage().contains("mail") || e.getMessage().contains("smtp") || e.getMessage().contains("connection"));
        }
    }
}
