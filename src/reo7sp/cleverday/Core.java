package reo7sp.cleverday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import java.util.Random;

import reo7sp.cleverday.data.DataCenter;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.service.NotificationService;
import reo7sp.cleverday.ui.TimeLinesLeader;
import reo7sp.cleverday.ui.activity.MainActivity;
import reo7sp.cleverday.utils.DateUtils;

public class Core {
	public static final String VERSION = "0.3.1+";
	private static final Random RANDOM = new Random();
	private static final ActionQueue SYNC_ACTION_QUEUE = new SyncActionQueue();
	private static final ActionQueue ASYNC_ACTION_QUEUE = new AsyncActionQueue();
	private static final Paint PAINT = new Paint();
	private static final DateFormatter DATE_FORMATTER = DateFormatter.getInstance();
	private static Vibrator vibrator;
	private static MainActivity mainActivity;
	private static Context context;
	private static SharedPreferences preferences;
	private static long creationTime = System.currentTimeMillis();
	private static boolean isBuilt;

	private Core() {
	}

	/**
	 * @return builder
	 */
	public static Builder startBuilding() {
		return new Builder();
	}

	/**
	 * @return the random
	 */
	public static Random getRandom() {
		return RANDOM;
	}

	/**
	 * @return the creation time
	 */
	public static long getCreationTime() {
		return creationTime;
	}

	/**
	 * @return the vibrator
	 */
	public static Vibrator getVibrator() {
		return vibrator;
	}

	/**
	 * @return the main activity
	 */
	public static MainActivity getMainActivity() {
		return mainActivity;
	}

	/**
	 * @return the data center
	 */
	public static DataCenter getDataCenter() {
		return DataCenter.getInstance();
	}

	/**
	 * @return the context
	 */
	public static Context getContext() {
		return context;
	}

	/**
	 * @return the preferences
	 */
	public static SharedPreferences getPreferences() {
		return preferences;
	}

	/**
	 * @return the time lines leader
	 */
	public static TimeLinesLeader getTimeLinesLeader() {
		return TimeLinesLeader.getInstance();
	}

	/**
	 * @return true if core has been ever built
	 */
	public static boolean isBuilt() {
		return isBuilt;
	}

	/**
	 * @return the paint
	 */
	public static Paint getPaint() {
		return PAINT;
	}

	/**
	 * @return the sync queue
	 */
	public static ActionQueue getSyncActionQueue() {
		return SYNC_ACTION_QUEUE;
	}

	/**
	 * @return the async queue
	 */
	public static ActionQueue getAsyncActionQueue() {
		return ASYNC_ACTION_QUEUE;
	}

	/**
	 * @return the date format factory
	 */
	public static DateFormatter getDateFormatter() {
		return DATE_FORMATTER;
	}

	public static class Builder {
		private MainActivity mainActivity;
		private Context context;

		private Builder() {
		}

		/**
		 * @param mainActivity main activity to set
		 * @return builder to allow chain invokes
		 */
		public Builder setMainActivity(MainActivity mainActivity) {
			this.mainActivity = mainActivity;
			return this;
		}

		/**
		 * @param context context to set
		 * @return builder to allow chain invokes
		 */
		public Builder setContext(Context context) {
			this.context = context;
			return this;
		}

		/**
		 * Builds core
		 */
		public void build() {
			synchronized (Core.class) {
				// context
				if (context == null && mainActivity != null && Core.context == null) {
					context = mainActivity.getApplicationContext();
				}
				if (context == null && Core.context == null) {
					return;
				}
				if (Core.context == null) {
					Core.context = context;
				}

				// log
				Log.i("Core", "Building core...");
				if (mainActivity != null) {
					Log.i("Core", "Core is building with main activity instance!");
				}

				// main activity
				if (mainActivity != null) {
					Core.mainActivity = mainActivity;
				}

				// UEH logger
				Log.initUEHLogger();

				// other
				if (!isBuilt) {
					Core.preferences = PreferenceManager.getDefaultSharedPreferences(context);
					Core.vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
					context.startService(new Intent(context, NotificationService.class));
				}

				// handling day change
				if (!DateUtils.isInOneDay(System.currentTimeMillis(), Core.getCreationTime())) {
					if (Core.mainActivity != null) {
						Core.mainActivity.recreate();
					}
				}

				// done!
				Log.i("Core", "Built!");
				creationTime = System.currentTimeMillis();
				isBuilt = true;
			}
		}
	}
}
