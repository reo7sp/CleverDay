package reo7sp.cleverday.log;

/**
 * Created by reo7sp on 8/12/13 at 1:56 PM
 */
public class UEHLogger implements Thread.UncaughtExceptionHandler {
	private static UEHLogger instance;
	private final Thread.UncaughtExceptionHandler defaultUEH;

	private UEHLogger() {
		this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		Log.err("UEH", "Uncaught exception from " + thread.getName(), ex);
		defaultUEH.uncaughtException(thread, ex);
	}

	/**
	 * Singleton method with lazy initialization
	 *
	 * @return the instance
	 */
	public static UEHLogger getInstance() {
		if (instance == null) {
			synchronized (UEHLogger.class) {
				if (instance == null) {
					instance = new UEHLogger();
				}
			}
		}
		return instance;
	}
}
