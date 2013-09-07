package reo7sp.cleverday.log;

/**
 * Created by reo7sp on 8/12/13 at 12:00 PM
 */
public enum LogLevel {
	VERBOSE(android.util.Log.VERBOSE),
	DEBUG(android.util.Log.DEBUG),
	INFO(android.util.Log.INFO),
	WARNING(android.util.Log.WARN),
	ERROR(android.util.Log.ERROR),
	ASSERT(android.util.Log.ASSERT),;

	private final int value;

	private LogLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
