package org.boring.crypto.priceAlert;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public enum Type {
	
	
	UP() {
		@Override
		public boolean reached(float current, float target) {
			return target < current;
		}

		@Override
		public String msg(float current, float target) {
			String now = DT_FORMAT.format(ZonedDateTime.now());
			String msg = "ALERT: BTC has rised beyond ".concat(Float.valueOf(target).toString().concat(" with price : ").concat(Float.valueOf(current).toString().concat(" on ").concat(now)));
			return msg;
		}
	},
	DOWN() {
		@Override
		public boolean reached(float current, float target) {
			return current < target;
		}

		@Override
		public String msg(float current, float target) {
			String now = DT_FORMAT.format(ZonedDateTime.now());
			String msg = "ALERT: BTC has fallen below ".concat(Float.valueOf(target).toString().concat(" with price : ").concat(Float.valueOf(current).toString().concat(" on ").concat(now)));
			return msg;
		}
	};

	public DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a z");
	
	public abstract boolean reached(float current, float target);

	public abstract String msg(float current, float target);
	
	
}
