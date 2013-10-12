package reo7sp.cleverday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import java.util.Random;

import reo7sp.cleverday.data.DataCenter;
import reo7sp.cleverday.data.GoogleCalendarStorage;
import reo7sp.cleverday.data.HistoryStorage;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.service.NotificationService;
import reo7sp.cleverday.ui.activity.MainActivity;
import reo7sp.cleverday.ui.timeline.TimeLinesLeader;
import reo7sp.cleverday.utils.DateUtils;

public class Core {
	public static final String VERSION = "0.4";
	private static final Random random = new Random();
	private static final ActionQueue syncActionQueue = new SyncActionQueue();
	private static final ActionQueue asyncActionQueue = new AsyncActionQueue();
	private static final Paint paint = new Paint();
	private static final DateFormatter dateFormatter = DateFormatter.getInstance();
	private static Vibrator vibrator;
	private static MainActivity mainActivity;
	private static Context context;
	private static SharedPreferences preferences;
	private static long creationTime = System.currentTimeMillis();
	private static boolean isBuilt;
	private static boolean isNetOn;

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
		return random;
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
	 * @return the paint
	 */
	public static Paint getPaint() {
		return paint;
	}

	/**
	 * @return the receive queue
	 */
	public static ActionQueue getSyncActionQueue() {
		return syncActionQueue;
	}

	/**
	 * @return the async queue
	 */
	public static ActionQueue getAsyncActionQueue() {
		return asyncActionQueue;
	}

	/**
	 * @return the history storage
	 */
	public static HistoryStorage getHistoryStorage() {
		return HistoryStorage.getInstance();
	}

	/**
	 * @return the google calendar storage
	 */
	public static GoogleCalendarStorage getGoogleCalendarStorage() {
		return GoogleCalendarStorage.getInstance();
	}

	/**
	 * @return true if net is on and this type of net wasn't forbidden
	 */
	public static boolean isNetOn() {
		return isNetOn;
	}

	/**
	 * @param netOn true if net is on and this type of net wasn't forbidden
	 */
	static void setNetOn(boolean netOn) {
		isNetOn = netOn;
	}

	/**
	 * @return the date format factory
	 */
	public static DateFormatter getDateFormatter() {
		return dateFormatter;
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
				if (context == null) {
					if (Core.context == null) {
						return;
					}
					context = Core.context;
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

				// invalidating net status
				Core.getSyncActionQueue().addAction(new Runnable() {
					@Override
					public void run() {
						NetWatcher.invalidate(context);
					}
				});

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
