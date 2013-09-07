package reo7sp.cleverday.log;

import java.io.File;
import java.io.IOException;

/**
 * Created by reo7sp on 8/12/13 at 11:57 AM
 */
public class Log {
	private static final Log instance = new Log();
	private FileLogger fileLogger;

	private Log() {
		// file logger
		try {
			fileLogger = new FileLogger();
		} catch (IOException err) {
			writeToLog("Logging", "Can't log to file! " + err, LogLevel.WARNING);
			err.printStackTrace();
		}

		// done!
		writeToLog("Logging", "Logger started!", LogLevel.INFO);
	}

	public static void initUEHLogger() {
		Thread.setDefaultUncaughtExceptionHandler(UEHLogger.getInstance());
	}

	public static File getLogFile() {
		return instance.fileLogger.getFile();
	}

	public static void log(String tag, String message, LogLevel level) {
		instance.writeToLog(tag, message, level);
	}

	public static void verbose(String tag, String message) {
		log(tag, message, LogLevel.VERBOSE);
	}

	public static void debug(String tag, String message) {
		log(tag, message, LogLevel.DEBUG);
	}

	public static void info(String tag, String message) {
		log(tag, message, LogLevel.INFO);
	}

	public static void warning(String tag, String message) {
		log(tag, message, LogLevel.WARNING);
	}

	public static void error(String tag, String message) {
		log(tag, message, LogLevel.ERROR);
	}

	public static void l(String tag, String message, LogLevel level) {
		log(tag, message, level);
	}

	public static void v(String tag, String message) {
		log(tag, message, LogLevel.VERBOSE);
	}

	public static void d(String tag, String message) {
		log(tag, message, LogLevel.DEBUG);
	}

	public static void i(String tag, String message) {
		log(tag, message, LogLevel.INFO);
	}

	public static void w(String tag, String message) {
		log(tag, message, LogLevel.WARNING);
	}

	public static void e(String tag, String message) {
		log(tag, message, LogLevel.ERROR);
	}

	public static void wtf(String tag, String message) {
		log(tag, message, LogLevel.ASSERT);
	}

	public static void err(String tag, String message, Throwable throwable) {
		err(tag, message, throwable, false);
	}

	public static void err(String tag, String message, Throwable throwable, boolean onlyToFile) {
		StringBuilder builder = new StringBuilder();
		builder.append(message).append("\n");
		builder.append("    ").append(throwable).append("\n");
		for (StackTraceElement element : throwable.getStackTrace()) {
			builder.append("        at ").append(element.toString()).append("\n");
		}
		if (throwable.getCause() != null) {
			builder.append("    Caused by ").append(throwable.getCause()).append("\n");
			for (StackTraceElement element : throwable.getCause().getStackTrace()) {
				builder.append("        at ").append(element.toString()).append("\n");
			}
		}
		Log.e(tag, builder.toString());
	}

	public synchronized void writeToLog(String tag, String message, LogLevel level) {
		android.util.Log.println(level.getValue(), tag, message);
		if (fileLogger != null) {
			fileLogger.log(tag, message, level);
		}
	}
}
