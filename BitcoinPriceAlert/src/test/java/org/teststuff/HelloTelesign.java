package org.teststuff;

import com.telesign.MessagingClient;
import com.telesign.RestClient;

public class HelloTelesign {
	public static void main(String[] args) {

		String customerId = "9D30DFFF-DE99-42FD-B69A-96C76FB38613";
		String apiKey = "mzeBxlr9CUIHrXynk0oiutbhoaoBXvb+pitkx21pyFyZ9sbNqBkhcyMjrAOLmwOG+Ev3tFRWFX00/+aYeXhCJg==";
		String phoneNumber = "18632559777";
		String message = "You're scheduled for a dentist appointment at 2:30PM.";
		String messageType = "ARN";

		try {
			MessagingClient messagingClient = new MessagingClient(customerId, apiKey);
			RestClient.TelesignResponse telesignResponse = messagingClient.message(phoneNumber, message, messageType,
					null);
			System.out.println("Your reference id is: " + telesignResponse.json.get("reference_id"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
