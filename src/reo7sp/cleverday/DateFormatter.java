package reo7sp.cleverday;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by reo7sp on 9/28/13 at 5:25 PM
 */
public class DateFormatter {
	private static final DateFormatter INSTANCE = new DateFormatter();
	private final Map<String, DateFormat> cache = new HashMap<String, DateFormat>();
	private final Collection<CachedResult> resultCache = new HashSet<CachedResult>();
	private boolean is24h;
	private boolean h24Requested;

	private DateFormatter() {
	}

	/**
	 * Singleton method
	 *
	 * @return the instance
	 */
	public static DateFormatter getInstance() {
		return INSTANCE;
	}

	private DateFormat getDateFormat(String format) {
		synchronized (cache) {
			java.text.DateFormat result = cache.get(format);
			if (result == null) {
				result = new SimpleDateFormat(format);
				cache.put(format, result);
			}
			return result;
		}
	}

	public String format(String format, long time) {
		synchronized (resultCache) {
			CachedResult cachedResult = getCachedResult(format, time);
			if (cachedResult == null) {
				cachedResult = new CachedResult(format, time, getDateFormat(format).format(new Date(time)));
				if (resultCache.size() > 64) {
					resultCache.clear();
				}
				resultCache.add(cachedResult);
			}
			return cachedResult.result;
		}
	}

	public String format(Format format, long time) {
		return format(format.getFormat(), time);
	}

	private CachedResult getCachedResult(String format, long time) {
		for (CachedResult result : resultCache) {
			if (result.time == time && result.format.equals(format)) {
				return result;
			}
		}
		return null;
	}

	public boolean is24HourFormat() {
		if (!h24Requested) {
			is24h = android.text.format.DateFormat.is24HourFormat(Core.getContext());
			h24Requested = true;
		}
		return is24h;
	}

	public static enum Format {
		/**
		 * Time format HH:mm (10:30)
		 */
		HOUR_MINUTE("HH:mm"),

		/**
		 * Date format dd MMM (14 Jan)
		 */
		DAY_MONTH("dd MMM"),

		/**
		 * Date format E, dd MMM (Fri, 14 Jan)
		 */
		WEEKDAY_DAY_MONTH("E, dd MMM"),

		/**
		 * Date format E, dd MMM yyyy (Fri, 14 Jan 3001)
		 */
		WEEKDAY_DAY_MONTH_YEAR("E, dd MMM yyyy"),

		/**
		 * Date format dd MMM HH:mm:ss.SSS (14 Jan 10:30:11.358)
		 */
		DAY_MONTH_HOUR_MINUTE_SECOND_MILLISECOND("dd MMM HH:mm:ss.SSS");
		//
		private final String format;

		private Format(String format) {
			this.format = format;
		}

		public String getFormat() {
			switch (this) {
				case HOUR_MINUTE:
					return Core.getDateFormatter().is24HourFormat() ? "HH:mm" : "hh:mm a";

				default:
					return format;
			}
		}
	}

	private static class CachedResult {
		public final String format;
		public final long time;
		public final String result;

		private CachedResult(String format, long time, String result) {
			this.format = format;
			this.time = time;
			this.result = result;
		}
	}
}
