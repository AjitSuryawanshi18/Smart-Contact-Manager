package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

import ch.qos.logback.classic.net.SMTPAppender;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {

		boolean f=false;
		
//		rest email code from here..

//		send email from -->
		String from = "ajitsuryawanshi1807@gmail.com";

//		variable for gmail
		String host = "smtp.gmail.com";

//		get the system properties 
		Properties properties = System.getProperties();

		System.out.println("properties :" + properties);

//		host set
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true"); // ssl--> secured socket layer
		properties.put("mail.smtp.auth", "true");

//		step 1 get the session object
		Session session = Session.getInstance(properties, new Authenticator() {

			@Override
			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication(from, "beygltxcvgcbnpdh");
				
//				this is password generated for mail send because google stopped less secure apps so we need to turn on 2 way verification of and have generate one random password nd have to use that password our default password not works here....
			}

		});

//		step 2 compose the message [text , multi media]

		MimeMessage mimeMessage = new MimeMessage(session);

		try {
//				Email from 
			mimeMessage.setFrom(from);
			
//			Email Recipient 
			mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
//			adding subject to the message of an email
			mimeMessage.setSubject(subject);
			
//			adding text to the mail message
			mimeMessage.setContent(message,"text/html");
			
//			step 3 send the mail using transport...
			
			Transport.send(mimeMessage);
			
			System.out.println("mail send succesfully....!!");
			
			f=true;

			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

}
