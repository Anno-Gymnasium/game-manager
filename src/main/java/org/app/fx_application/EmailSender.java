package org.app.fx_application;

import java.io.IOException;
import java.util.Properties;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Authenticator;

public class EmailSender {
    public static void sendEmail(String to, String subject, String text) throws MessagingException {
        Properties mailCredentials = new Properties();
        try {
            mailCredentials.load(EmailSender.class.getClassLoader().getResourceAsStream("mail_credentials.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Fehler beim Laden von der Sendenden Mail-Adresse und deren Passwort", e);
        }

        String from = mailCredentials.getProperty("mail.from");
        String password = mailCredentials.getProperty("mail.password");
        String host = "smtp.office365.com";

        Properties smtpProperties = new Properties();
        smtpProperties.put("mail.smtp.host", host);
        smtpProperties.put("mail.smtp.port", "587");
        smtpProperties.put("mail.smtp.auth", "true");
        smtpProperties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(smtpProperties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        message.setSubject(subject);
        message.setText(text);
        Transport.send(message);
    }
}