package org.boring.crypto.priceAlert;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


//			ARCHIVE!

@Component(value="btcMinWatcher")
@Scope (value="singleton")
public class BtcMinWatcher {
	
	public DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a z");
	
	private OkHttpClient client = new OkHttpClient();
	
	private List<PriceChangeListener> listeners = new ArrayList<PriceChangeListener>();
	
	private Timer timer;
	
	public void launchTimer(long ckPriceEveryXms, String endpoint) {
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				sendRequest(endpoint, new Callback() {

					@Override
					public void onResponse(Call call, Response response) throws IOException {
						String str = response.body().string();
						for (PriceChangeListener x : listeners) {
							x.priceChange(str);
						}
					}

					@Override
					public void onFailure(Call call, IOException ioe) {
						System.out.println("IOException, Failed to reach endpoint!");
					}
				});
			}
		}, 0, ckPriceEveryXms);
	}

	public void cancelTimer() {
		if (timer != null) {
			timer.cancel();
		}
	}
	
	public void addListener(PriceChangeListener toAdd) {
        listeners.add(toAdd);
    }
	
	
	private void sendRequest(String endpoint, Callback callback) {
		Request request = new Request.Builder().url(endpoint).build();
		client.newCall(request).enqueue(callback);
	}

	

}
