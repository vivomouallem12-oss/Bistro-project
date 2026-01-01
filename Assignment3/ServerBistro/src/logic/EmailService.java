package logic;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import jakarta.activation.*;


public class EmailService {

    private static final String FROM_EMAIL = "bistro.customer.service@gmail.com";
    private static final String APP_PASSWORD = "mnxl uaeu hayf ztax";
    
    
    public static void sendReminderEmail(String toEmail) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject("Reservation Reminder");
            message.setText("Hello.\nWe wanted to remind you that you have a reservation in 2 hours.\nSee you soon!");


            Transport.send(message);
            System.out.println("[EMAIL] Reminder sent sent to " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("[EMAIL] Failed to send reminder email");
        }
    }
    
    
    
    

    public static void sendConfirmationEmail(String toEmail, int confirmationCode) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject("Your Reservation Confirmation");
            message.setText(
                "Thank you for your reservation!\n\n" +
                "Your confirmation code is: " + confirmationCode + "\n\n" +
                "Please keep this code to view or cancel your reservation."
            );
            Transport.send(message);
            System.out.println("[EMAIL] Confirmation code sent to " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("[EMAIL] Failed to send code email");
        }
    }
    
    
    
    public static void LostCodeEmail(String toEmail, int confirmationCode) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject("Confirmation Code");
            message.setText(
                "Thank you for your reservation!\n\n" +
                "Your confirmation code is: " + confirmationCode + "\n\n" +
                "Please keep this code to view or cancel your reservation."
            );
            Transport.send(message);
            System.out.println("[EMAIL] Confirmation code sent to " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("[EMAIL] Failed to send email");
        }
    }
    
    public static void main(String[] args) {
		sendReminderEmail("mhmadda25@gmail.com");
	}
 }
