package reo7sp.cleverday.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.DataInvalidateListener;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.activity.EditBlockActivity;
import reo7sp.cleverday.ui.activity.MainActivity;
import reo7sp.cleverday.ui.activity.PostponeActivity;
import reo7sp.cleverday.utils.AndroidUtils;

public class NotificationService extends Service implements DataInvalidateListener {
	private static final int MAIN_NOTIFICATION_ID = 1;
	private static boolean invalidated = true;
	private final Thread updateThread = new UpdateThread();
	private NotificationManager notificationManager;
	private NotificationCompat.Builder mainNotificationBuilder;
	private PendingIntent pendingAppOpenIntent;
	private TimeBlock current, next, later;

	@Override
	public void onCreate() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mainNotificationBuilder = new NotificationCompat.Builder(this);

		Core.startBuilding()
				.setContext(getApplicationContext())
				.build();

		// main notification intent
		Intent appOpenIntent = new Intent(this, MainActivity.class);
		appOpenIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(MainActivity.class);
		stackBuilder.addNextIntent(appOpenIntent);
		pendingAppOpenIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mainNotificationBuilder.setContentIntent(pendingAppOpenIntent);

		// appearance and other
		mainNotificationBuilder.setSmallIcon(R.drawable.ic_for_bar_light);
		mainNotificationBuilder.setPriority(Notification.PRIORITY_MIN);
		mainNotificationBuilder.setWhen(0);
		mainNotificationBuilder.setOngoing(true);
		Intent addBlockIntent = new Intent(this, EditBlockActivity.class);
		addBlockIntent.putExtra("create", true);
		mainNotificationBuilder.addAction(R.drawable.ic_new_light, getResources().getString(R.string.add), PendingIntent.getActivity(this, 0, addBlockIntent, PendingIntent.FLAG_CANCEL_CURRENT));

		// listener
		Core.getDataCenter().registerDataInvalidateListener(this);

		// thread
		updateThread.start();
	}

	@Override
	public void onDestroy() {
		updateThread.interrupt();
		Core.getDataCenter().unregisterDataInvalidateListener(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDataInvalidate() {
		invalidated = true;
	}

	private void findNextTimeBlocks() {
		current = null;
		next = null;
		later = null;

		long now = System.currentTimeMillis();
		int i = 0;
		for (TimeBlock block : Core.getDataCenter().getTimeBlocks()) {
			if (i >= 3) {
				break;
			} else if (block.getEnd() < now) {
				continue;
			} else if (i == 0 && !(block.getStart() < now && block.getEnd() > now)) {
				i++;
			}

			switch (i) {
				case 0:
					current = block;
					break;
				case 1:
					next = block;
					break;
				case 2:
					later = block;
					break;
			}
		}
	}

	private void updateMainNotification() {
		if (!AndroidUtils.isNotificationShown()) {
			notificationManager.cancel(MAIN_NOTIFICATION_ID);
			return;
		}
		if (!invalidated) {
			return;
		}

		if (current == null) {
			if (next == null) {
				mainNotificationBuilder.setContentTitle(getResources().getString(R.string.no_plan));
				mainNotificationBuilder.setContentText("");
			} else {
				mainNotificationBuilder.setContentTitle(getResources().getString(R.string.next) + ": " + next);
				if (later == null) {
					mainNotificationBuilder.setContentText("");
				} else {
					mainNotificationBuilder.setContentText(getResources().getString(R.string.later) + ": " + later);
				}
			}
		} else {
			mainNotificationBuilder.setContentTitle(getResources().getString(R.string.now) + ": " + current);
			if (next == null) {
				mainNotificationBuilder.setContentText("");
			} else {
				mainNotificationBuilder.setContentText(getResources().getString(R.string.next) + ": " + next);
			}
		}
		notificationManager.notify(MAIN_NOTIFICATION_ID, mainNotificationBuilder.build());
		invalidated = false;
	}

	private void updateAlarmNotification() {
		if (current != null && current.hasReminder()) {
			// notification
			NotificationCompat.Builder alarmNotificationBuilder = new NotificationCompat.Builder(NotificationService.this);
			alarmNotificationBuilder.setContentTitle("" + current);
			alarmNotificationBuilder.setTicker("" + current);
			alarmNotificationBuilder.setContentText(current.getNotes() == null ? "" : current.getNotes());
			alarmNotificationBuilder.setWhen(current.getStart());
			alarmNotificationBuilder.setSmallIcon(R.drawable.ic_alarm_status_bar);
			alarmNotificationBuilder.setPriority(Notification.PRIORITY_MAX);
			alarmNotificationBuilder.setAutoCancel(true);
			alarmNotificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
			alarmNotificationBuilder.setVibrate(new long[] {0, 350, 150, 150});
			alarmNotificationBuilder.setContentIntent(pendingAppOpenIntent);

			// edit intent
			Intent editIntent = new Intent(NotificationService.this, EditBlockActivity.class);
			editIntent.putExtra("id", current.getID());
			PendingIntent pendingEditIntent = PendingIntent.getActivity(NotificationService.this, 0, editIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmNotificationBuilder.addAction(R.drawable.ic_edit_light, getResources().getString(R.string.edit), pendingEditIntent);

			// postpone intent
			Intent postponeIntent = new Intent(NotificationService.this, PostponeActivity.class);
			postponeIntent.putExtra("id", current.getID());
			PendingIntent pendingPostponeIntent = PendingIntent.getActivity(NotificationService.this, 0, postponeIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			alarmNotificationBuilder.addAction(R.drawable.ic_next_light, getResources().getString(R.string.postpone), pendingPostponeIntent);

			notificationManager.notify((int) current.getID(), alarmNotificationBuilder.build());

			current.setReminder(false);
		}
	}

	private class UpdateThread extends Thread {
		private int tick;

		public UpdateThread() {
			super("NotificationService");
		}

		@Override
		public void run() {
			try {
				while (!isInterrupted()) {
					findNextTimeBlocks();
					updateMainNotification();
					updateAlarmNotification();

					if (tick++ > 25) {
						tick = 0;
						invalidated = true;
					}
					Thread.sleep(2500);
				}
			} catch (InterruptedException ignored) {
			}

			// stopping service
			notificationManager.cancel(MAIN_NOTIFICATION_ID);
		}
	}
}
