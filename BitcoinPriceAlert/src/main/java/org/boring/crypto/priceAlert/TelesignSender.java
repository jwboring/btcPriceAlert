package org.boring.crypto.priceAlert;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.telesign.MessagingClient;
import com.telesign.RestClient;

@Component(value="telesignSender")
@Scope (value="singleton")
public class TelesignSender implements Sender {

	
	private String customerId = "9D30DFFF-DE99-42FD-B69A-96C76FB38613";
	private String apiKey = "mzeBxlr9CUIHrXynk0oiutbhoaoBXvb+pitkx21pyFyZ9sbNqBkhcyMjrAOLmwOG+Ev3tFRWFX00/+aYeXhCJg==";
	String phoneNumber = "18632559777";
//	String message = "You're scheduled for a dentist appointment at 2:30PM.";
	private String messageType = "ARN";
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public TelesignSender() {
		super();
	}

	@Override
	public void sendMsg(String toPhone, String subject, String message) {

		try {
			MessagingClient messagingClient = new MessagingClient(customerId, apiKey);
			RestClient.TelesignResponse telesignResponse = messagingClient.message(phoneNumber, message, messageType, null);
			System.out.println("Your reference id is: " + telesignResponse.json.get("reference_id"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
