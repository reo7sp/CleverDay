package reo7sp.cleverday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import reo7sp.cleverday.service.NotificationService;

public class AutoStartReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			context.startService(new Intent(context, NotificationService.class));
		}
	}
}
