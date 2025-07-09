package com.example.libraryproject.service.implementation;

import org.apache.commons.mail.*;

import java.util.List;

import static com.example.libraryproject.configuration.ApplicationProperties.*;

public class MailServiceImpl {

    public void sendEmail(List<String> recipients, String subject, String message) throws EmailException {
        SimpleEmail email = new SimpleEmail();
        configureEmail(email);
        email.setSubject(subject);
        email.setMsg(message);
        for (String to : recipients) {
            email.addTo(to);
        }
        email.send();
    }

    public void sendHtmlEmail(List<String> recipients, String subject, String htmlContent) throws EmailException {
        HtmlEmail email = new HtmlEmail();
        configureEmail(email);
        email.setSubject(subject);
        email.setHtmlMsg(htmlContent);
        email.setTextMsg("Your email client does not support HTML.");
        for (String to : recipients) {
            email.addTo(to);
        }
        email.send();
    }

    private void configureEmail(org.apache.commons.mail.Email email) throws EmailException {
        email.setHostName(SMTP_HOST);
        email.setSmtpPort(SMTP_PORT);
        email.setAuthenticator(new DefaultAuthenticator(EMAIL_ADDRESS, EMAIL_PASSWORD));
        email.setSSLOnConnect(true);
        email.setFrom(EMAIL_ADDRESS);
    }
}
