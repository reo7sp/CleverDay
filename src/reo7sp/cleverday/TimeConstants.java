package reo7sp.cleverday;

/**
 * Created by reo7sp on 8/1/13 at 2:57 PM
 */
public interface TimeConstants {
	/**
	 * Second in milliseconds
	 */
	public static final long SECOND = 1000; // 1000 milliseconds

	/**
	 * Minute in milliseconds
	 */
	public static final long MINUTE = SECOND * 60; // 60000 milliseconds

	/**
	 * Hour in milliseconds
	 */
	public static final long HOUR = MINUTE * 60; // 3600000 milliseconds

	/**
	 * Half of hour (30 minutes) in milliseconds
	 */
	public static final long HALF_OF_HOUR = MINUTE * 30; // 1800000 milliseconds

	/**
	 * Quarter of hour (15 minutes) in milliseconds
	 */
	public static final long QUARTER_OF_HOUR = MINUTE * 15; // 900000 milliseconds

	/**
	 * Day in milliseconds
	 */
	public static final long DAY = HOUR * 24; // 86400000 milliseconds
}
