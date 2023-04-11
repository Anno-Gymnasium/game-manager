package org.app.fx_application;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

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
            throw new RuntimeException("Fehler beim Laden von der sendenden Mail-Adresse und deren Passwort", e);
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

        session.setDebug(true);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        String whiteText = createWhiteText();
        message.setContent("<h2>" + text + "</h2><p style='color: white;'>" + "\n".repeat(30) + whiteText + "</p>", "text/html");
        message.setSubject(subject);
        Transport.send(message);
    }
    private static String createWhiteText() {
        // Erstelle zufälligen String der Länge 30-50 mit zufälligen Buchstaben und Leerzeichen
        Random random = new Random();
        int length = random.nextInt(20, 51);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (random.nextBoolean()) {
                sb.append((char) (random.nextInt(26) + 'a'));
            } else {
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}