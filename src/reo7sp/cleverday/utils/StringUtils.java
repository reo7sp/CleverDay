package reo7sp.cleverday.utils;

/**
 * Created by reo7sp on 8/5/13 at 11:24 PM
 */
public class StringUtils {
	private StringUtils() {
	}

	/**
	 * Makes first character of specified string upper cased.
	 * If method takes in arguments string "hello, world!", it will return "Hello, world!"
	 *
	 * @return string with upper cased first character
	 */
	public static String makeFirstCharUpperCased(String string) {
		return string.substring(0, 1).toUpperCase() + string.substring(1);
	}
}
