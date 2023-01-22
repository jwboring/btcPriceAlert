package org.boring.crypto.priceAlert;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component(value="user")
@Scope (value="singleton")
public class User {

	// pa 2023026064
	// Shawn 8433436587
	private String[] userPhoneNumbers = new String[] {"8632559777", "8636481586", "2023026064", "8433436587"};

	public String[] getUserPhoneNumbers() {
		return userPhoneNumbers;
	}
	
	
	
	
}
