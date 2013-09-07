package reo7sp.cleverday.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.TimeConstants;

@SuppressLint("SimpleDateFormat")
public class DateUtils {
	private DateUtils() {
	}

	/**
	 * Time format HH:mm (10:30)
	 */
	public static final SimpleDateFormat FORMAT_HOUR_MINUTE = new SimpleDateFormat("HH:mm");

	/**
	 * Date format dd MMM (14 Jan)
	 */
	public static final SimpleDateFormat FORMAT_DAY_MONTH = new SimpleDateFormat("dd MMM");

	/**
	 * Date format E, dd MMM (Fri, 14 Jan)
	 */
	public static final SimpleDateFormat FORMAT_WEEKDAY_DAY_MONTH = new SimpleDateFormat("E, dd MMM");

	/**
	 * Date format E, dd MMM yyyy (Fri, 14 Jan 3001)
	 */
	public static final SimpleDateFormat FORMAT_WEEKDAY_DAY_MONTH_YEAR = new SimpleDateFormat("E, dd MMM yyyy");

	/**
	 * Date format dd MMM HH:mm:ss.SSS (14 Jan 10:30:11.358)
	 */
	public static final SimpleDateFormat FORMAT_DAY_MONTH_HOUR_MINUTE_SECOND_MILLISECOND = new SimpleDateFormat("dd MMM HH:mm:ss.SSS");

	/**
	 * Transforms UTC time to local time
	 *
	 * @param localTime local time
	 * @return UTC time
	 */
	public static long getUtcTime(long localTime) {
		return getUtcTime(localTime, getTimeZoneOffset());
	}

	/**
	 * Transforms UTC time to local time
	 *
	 * @param utcTime UTC time
	 * @return local time
	 */
	public static long getLocalTime(long utcTime) {
		return getLocalTime(utcTime, getTimeZoneOffset());
	}

	/**
	 * Transforms UTC time to local time
	 *
	 * @param localTime local time
	 * @param zone      time zone
	 * @return UTC time
	 */
	public static long getUtcTime(long localTime, TimeZone zone) {
		return getUtcTime(localTime, getTimeZoneOffset(zone));
	}

	/**
	 * Transforms UTC time to local time
	 *
	 * @param utcTime UTC time
	 * @param zone    time zone
	 * @return local time
	 */
	public static long getLocalTime(long utcTime, TimeZone zone) {
		return utcTime + TimeConstants.HOUR * getTimeZoneOffset(zone);
	}

	/**
	 * Transforms UTC time to local time
	 *
	 * @param localTime      local time
	 * @param timeZoneOffset time zone offset in milliseconds
	 * @return UTC time
	 */
	public static long getUtcTime(long localTime, int timeZoneOffset) {
		return localTime - timeZoneOffset;
	}

	/**
	 * Transforms UTC time to local time
	 *
	 * @param utcTime        UTC time
	 * @param timeZoneOffset time zone offset in milliseconds
	 * @return local time
	 */
	public static long getLocalTime(long utcTime, int timeZoneOffset) {
		return utcTime + timeZoneOffset;
	}

	/**
	 * Checks if all args are in one day
	 *
	 * @param first  first timestamp in milliseconds
	 * @param second second timestamp in milliseconds
	 * @param other  other timestamps in milliseconds
	 * @return true if all args are in one day
	 */
	public static boolean isInOneDay(long first, long second, long... other) {
		long min = first, max = second;
		for (long time : other) {
			min = Math.min(time, min);
			max = Math.max(time, max);
		}
		min = Math.min(first, min);
		max = Math.max(second, max);

		return getFromTime(min, Calendar.DAY_OF_YEAR) == getFromTime(max, Calendar.DAY_OF_YEAR);
	}

	/**
	 * Gets {@link Calendar} field from time
	 *
	 * @param time  time
	 * @param field {@link Calendar} field
	 * @return {@link Calendar} field
	 */
	@SuppressWarnings("MagicConstant")
	public static int getFromTime(long time, int field) {
		return getCalendarInstance(time).get(field);
	}

	/**
	 * Gets {@link Calendar} instance from time
	 *
	 * @param time time
	 * @return {@link Calendar} instance
	 */
	public static Calendar getCalendarInstance(long time) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		return calendar;
	}

	/**
	 * Gets time zone offset in milliseconds from default time zone
	 *
	 * @return time zone offset in milliseconds from default time zone
	 */
	public static int getTimeZoneOffset() {
		return getTimeZoneOffset(TimeZone.getDefault());
	}

	/**
	 * Gets time zone offset in milliseconds from time zone
	 *
	 * @param zone time zone
	 * @return time zone offset in milliseconds from time zone
	 */
	public static int getTimeZoneOffset(TimeZone zone) {
		return zone.getOffset(Core.getCreationTime());
	}

	/**
	 * Trims day to 0 hours 0 minutes 0 seconds 0 milliseconds
	 *
	 * @param time time to trim
	 * @return trimmed time
	 */
	public static long trimToDay(long time) {
		Calendar calendar = getCalendarInstance(time);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis();
	}
}
