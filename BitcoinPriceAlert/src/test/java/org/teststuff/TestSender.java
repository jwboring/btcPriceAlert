package org.teststuff;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import org.junit.jupiter.api.Test;

class TestSender {

	@Test
	void test() {
		
		LocalDateTime localDT = LocalDateTime.now();
		System.out.println(DateTimeFormatter.ofPattern("MM/dd/yyyy kk:mm:ss a").format(localDT));
		System.out.println(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(localDT));
		
		
		 DateTimeFormatter formatter6 = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a z");
		 System.out.println(formatter6.format(ZonedDateTime.now()));
//		 "ZonedDateTime", formatter6.format(ZonedDateTime.now()));
	}

}
