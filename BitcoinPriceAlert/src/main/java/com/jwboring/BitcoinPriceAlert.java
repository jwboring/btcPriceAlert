package com.jwboring;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BitcoinPriceAlert {
	
	public static final String WATCH_COMMAND = "WATCH";
	public static final String QUIT_COMMAND = "QUIT";
	public static final String LIST_COMMAND = "LIST";
	public static final long PERIOD = 60 * 1000; // 30 sec
	public static final String BITCOIN_PRICE_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
	public static final String PROPERTIESFILE = "BitcoinPriceAlert.properties";
	public static final String SECRETKEY = "secretKey";
	public static final String EMAILSESSIONPASSWORD= "emailSessionPassword";
	private OkHttpClient client = new OkHttpClient();
	private Timer timer;
	private List<Price> pricesToWatch;
	private float currentPrice;
	
	private boolean firstTime = true; 
	private EmailSender sender;

	public static void main(String[] args) {
		BitcoinPriceAlert app = null;
		
		if (!Arrays.asList(args).isEmpty() && args[0] != null) {
			app = new BitcoinPriceAlert(Boolean.valueOf(args[0]));
		}
		else {
			 app = new BitcoinPriceAlert(false);
		}
		
		app.loadProperites();
		app.launchTimer();
		app.scanConsole();
	}

	public BitcoinPriceAlert(boolean disabled) {
		
		sender = new EmailSender(disabled);
		
		pricesToWatch = new ArrayList<Price>(0);
		
	}
	
	private void loadProperites() {
		try (InputStream input = BitcoinPriceAlert.class.getClassLoader().getResourceAsStream(PROPERTIESFILE)) {

			StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();   
			encryptor.setPassword(System.getProperty(SECRETKEY)); // set via -D
			EncryptableProperties prop = new EncryptableProperties(encryptor);

            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }
            prop.load(input);
            System.setProperty("emailSessionPassword", prop.getProperty("emailSessionPassword"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

	}

	private void scanConsole() {
		String command = null;
		Scanner scanner = new Scanner(System.in);

		while (!QUIT_COMMAND.equals(command)) {
			String line = scanner.nextLine();
			line = line.toUpperCase();

			if (line.startsWith(QUIT_COMMAND)) {
				command = QUIT_COMMAND;
				System.out.println("Goodbye!");
			} else if (line.startsWith(LIST_COMMAND)) {
				command = LIST_COMMAND;
				listPricesToWatch();

			} else {
				String[] tmp = line.split(";");

				if (tmp.length != 2) {
					System.out.println("Bad entry: COMMAND;Price");
				} else {
					command = tmp[0];

					// use a switch for future commands
					switch (command) {
					case WATCH_COMMAND:
						Float price = null;

						try {
							price = Float.parseFloat(tmp[1]);
						} catch (Exception e) {
							price = null;
						}

						if (price == null) {
							System.out.println("Bad price");
						} else {
							Price priceObj = new Price(currentPrice, price);
							pricesToWatch.add(priceObj);
							System.out.println("Watch for BTC " + priceObj.type + " = " + price);
						}
						break;
					}

				}

			}
		}

		scanner.close();
		cancelTimer();
		System.exit(0);
	}

	private void listPricesToWatch() {
		if (pricesToWatch.isEmpty()) {
			System.out.println("No prices to watch");
		} else {
			System.out.println("Prices to watch:");

			for (Price price : pricesToWatch) {
				System.out.println("\t" + price.type + "\t" + price.target);
			}
		}

		System.out.println();
	}

	private void launchTimer() {
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				loadBitcoinPrice(new Callback() {

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						String str = response.body().string();
						parseBitcoinPrice(str);
					}

					@Override
					public void onFailure(Call call, IOException ioe) {

					}
				});
			}
		}, 0, PERIOD);
	}

	private void cancelTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}

	private void loadBitcoinPrice(Callback callback) {
		Request request = new Request.Builder().url(BITCOIN_PRICE_ENDPOINT).build();
		client.newCall(request).enqueue(callback);
	}

	private void parseBitcoinPrice(String str) {
		JSONObject jsonObject = new JSONObject(str);
		currentPrice = jsonObject.getJSONObject("bpi").getJSONObject("USD").getFloat("rate_float");
		
		String emailMsg = " | Current price = " + currentPrice + "\n";
		System.out.println(LocalDateTime.now() + emailMsg);
		if (firstTime) {
//			sender.send("Starting BTC Price Alert", emailMsg);						//disable here
			firstTime = false;
			adjustWatchlist(currentPrice);
			
		}

		// we check if one price is reached
		for (Iterator<Price> it = pricesToWatch.iterator(); it.hasNext();) {
			Price priceToWatch = it.next();

			if (priceToWatch.type.reached(currentPrice, priceToWatch.target)) {
				String message = priceToWatch.type.msg(currentPrice, priceToWatch.target);
				System.out.println(message);
				displayNotification("Bitcoin Watcher", message);
				// remove from list to watch
				it.remove();
//				if (priceToWatch.type==Price.Type.DOWN) 
//					adjust down
//				else
//					adjust up
					
				
				adjustWatchlist(currentPrice);
			}
		}
		
		
	}
	
	
	private void adjustWatchlist(float theCurrent) {
		
		pricesToWatch = new ArrayList<Price>(0);
		float d;
		Price down1;
		float u;
		Price up1;
		
		/** Down 1000, rounded to 1000 */
		d = Math.round((theCurrent - 1000)/1000)*1000;
		down1 = new Price(theCurrent, d);
		pricesToWatch.add(down1);
		
		/** Down 500, rounded to 100 */
		d = Math.round((theCurrent - 500)/100)*100;
		down1 = new Price(theCurrent, d);
		pricesToWatch.add(down1);
		
		
		/** up 1000, rounded to 1000 */
		u = Math.round((theCurrent + 1000)/1000)*1000;
		up1 = new Price(theCurrent, u);
		pricesToWatch.add(up1);
		
		/** up 500, rounded to 100 */
		u = Math.round((theCurrent + 500)/100)*100;
		up1 = new Price(theCurrent, u);
		pricesToWatch.add(up1);
		
	}
	

	public void displayNotification(String title, String message) {
		sender.send(title, message);
		if (SystemTray.isSupported()) {
			// Obtain only one instance of the SystemTray object
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
//			System.err.println("tray supported. " + tray.toString());

			TrayIcon trayIcon = new TrayIcon(image, "Bitcoin Watcher Notif");
			// Let the system resize the image if needed
			trayIcon.setImageAutoSize(true);
			// Set tooltip text for the tray icon
			trayIcon.setToolTip("Bitcoin Watcher");

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println(e);
			}

			trayIcon.displayMessage(title, message, MessageType.INFO);
		} else {
			System.err.println("System tray not supported!");
		}
	}

}


//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("27000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("28000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("29000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("30000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("31000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("32000.00").floatValue()));

//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("38000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("39000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("40000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("41000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("42000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("43000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("44000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("45000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("46000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("47000.00").floatValue()));
//pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("48000.00").floatValue()));