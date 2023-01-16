package org.boring.crypto.priceAlert;

import java.util.Properties;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Starter {
	
	private static ApplicationContext appContext;
	
	public static void main(String[] args) {
		
		Properties sysProps = System.getProperties();
        System.out.println("JRE Vendor: "+ sysProps.getProperty("java.vendor"));
        System.out.println("JRE Version: "+ sysProps.getProperty("java.version"));
        System.out.println("Classpath:\n%s" + sysProps.getProperty("java.class.path").replaceAll(";", "\n"));
        
		appContext = new ClassPathXmlApplicationContext("appContext.xml");
		appContext.getBean("bitcoinPriceAlert");

	}

}
