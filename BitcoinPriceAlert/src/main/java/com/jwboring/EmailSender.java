package com.jwboring;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailSender {

	private final String username = "jwboring@gmail.com";
	private Properties prop = new Properties();

	private boolean disabled = false;

	public EmailSender() {
		super();
		init();
	}

	public EmailSender(boolean disabled) {
		super();
		this.disabled = disabled;
		System.out.println("Send status="+disabled);
		init();
	}
	
	private void init() {
		prop.put("mail.smtp.host", "smtp.gmail.com");
		prop.put("mail.smtp.port", "587");
		prop.put("mail.smtp.auth", "true");
		prop.put("mail.smtp.starttls.enable", "true"); // TLS
	}

	public void send(String subject, String text) {
		String password = System.getProperty(BitcoinPriceAlert.EMAILSESSIONPASSWORD);
		Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("jwboring@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress
					.parse("jwboring@gmail.com, 8632559777@vtext.com, 2023026064@vtext.com, 8636481586@vtext.com"));
			if (!disabled) {
				message.setSubject(subject);
				message.setText(text);
				Transport.send(message);
			}

		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

}
// pa 2023026064