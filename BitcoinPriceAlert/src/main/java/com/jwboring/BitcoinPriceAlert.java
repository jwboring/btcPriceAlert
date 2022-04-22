package com.jwboring;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BitcoinPriceAlert {
	
	//java -jar BitcoinPriceAlert-jar-with-dependencies.jar
	
	//TODO Add sms notification
	// https://www.tech-recipes.com/internet/instant-messaging/sms_email_cingular_nextel_sprint_tmobile_verizon_virgin/

	public static final String WATCH_COMMAND = "WATCH";
	public static final String QUIT_COMMAND = "QUIT";
	public static final String LIST_COMMAND = "LIST";
	public static final long PERIOD = 60 * 1000; // 30 sec
	public static final String BITCOIN_PRICE_ENDPOINT = "https://api.coindesk.com/v1/bpi/currentprice.json";
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
		
		
		app.launchTimer();
		app.scanConsole();
	}

	public BitcoinPriceAlert(boolean disabled) {
		
		sender = new EmailSender(disabled);
		
		pricesToWatch = new ArrayList<Price>(0);
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("40000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("40400.00").floatValue()));
		
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("39000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("41000.00").floatValue()));
		
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("38000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("42000.00").floatValue()));
		
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("43000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("44000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("45000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("46000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("47000.00").floatValue()));
		pricesToWatch.add(new Price(Float.valueOf("39880.00").floatValue(), Float.valueOf("48000.00").floatValue()));
		
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
			sender.send("Starting BTC Price Alert", emailMsg);
			firstTime = false;
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
			}

		}
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
