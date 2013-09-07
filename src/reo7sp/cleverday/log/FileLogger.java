package reo7sp.cleverday.log;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import reo7sp.cleverday.utils.DateUtils;

/**
 * Created by reo7sp on 8/12/13 at 12:03 PM
 */
public class FileLogger extends Thread {
	private final Queue<String> queue = new ConcurrentLinkedQueue<String>();
	private File file;
	private PrintWriter writer;

	@SuppressWarnings("ResultOfMethodCallIgnored")
	FileLogger() throws IOException {
		// file
		if (!canWrite()) {
			throw new IOException("Can't write to external storage");
		}
		file = new File(Environment.getExternalStorageDirectory(), "CleverDay.log");
		if (!file.exists() && !file.createNewFile()) {
			throw new IOException("Can't create file in external storage");
		}
		writer = new PrintWriter(new FileOutputStream(file, true), true);
		if (file.length() > 262144) { // 262144 = 256 KB
			file.delete();
			file.createNewFile();
		}

		// queue
		for (int i = 0; i < 3; i++) {
			queue.add("");
		}

		// thread
		start();
	}

	public File getFile() {
		return file;
	}

	public void log(String tag, String message, LogLevel level) {
		String date = DateUtils.FORMAT_DAY_MONTH_HOUR_MINUTE_SECOND_MILLISECOND.format(new Date());
		queue.add("[" + date + "] [" + level + "] [" + tag + "] " + message);
	}

	@Override
	public void run() {
		try {
			String s;
			while (!isInterrupted()) {
				sleep(1000);
				while ((s = queue.poll()) != null) {
					writer.println(s);
				}
			}
		} catch (InterruptedException ignored) {
		}
	}

	private boolean canWrite() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
}
