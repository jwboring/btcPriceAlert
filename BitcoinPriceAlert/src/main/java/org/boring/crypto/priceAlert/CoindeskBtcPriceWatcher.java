package org.boring.crypto.priceAlert;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


@Component(value="coindeskBtcPriceWatcher")
@Scope (value="singleton")
public class CoindeskBtcPriceWatcher {
	
	private OkHttpClient client = new OkHttpClient();
	private List<PriceChangeListener> listeners = new ArrayList<PriceChangeListener>();
	
	/** In Milliseconds 60,000 = 60 sec or 1 min */
	@Value("${ckPriceEveryXms}") private long ckPriceEveryXms;
	
	@Value("${coindeskEndpoint}") private String coindeskEndpoint;

	
	public CoindeskBtcPriceWatcher() {
		super();
	}


	public void addListener(PriceChangeListener toAdd) {
        listeners.add(toAdd);
    }
	
	
	private void sendRequest(String endpoint, Callback callback) {
		Request request = new Request.Builder().url(endpoint).build();
		client.newCall(request).enqueue(callback);
	}

	
	
	public void checkBitcoinPrice() {
		
		sendRequest(coindeskEndpoint, new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String str = response.body().string();
				for (PriceChangeListener priceChangeListener : listeners) {
					priceChangeListener.priceChange(str);
				}
			}

			@Override
			public void onFailure(Call call, IOException ioe) {
				System.out.println("IOException, Failed to reach endpoint!");
			}
		});
	}
		

}













