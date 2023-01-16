package org.boring.crypto.priceAlert;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


//@Component(value="price")
//@Scope (value="singleton")
public class Price {
	
	
	
	public static DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy 'at' hh:mm a z");

	enum Type {
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
		DOWN {
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

		public abstract boolean reached(float current, float target);

		public abstract String msg(float current, float target);
	}

	
	public float target;
	public Type type;

	public Price(float current, float target) {
		this.target = target;
		type = Float.compare(current, target) < 0 ? Type.UP : Type.DOWN;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(target);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Price other = (Price) obj;
		if (Float.floatToIntBits(target) != Float.floatToIntBits(other.target))
			return false;
		return true;
	}

}