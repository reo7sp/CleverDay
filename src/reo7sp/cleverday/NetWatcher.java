package reo7sp.cleverday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by reo7sp on 8/27/13 at 6:50 PM
 */
public class NetWatcher extends BroadcastReceiver {
	static void invalidate(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = manager.getActiveNetworkInfo();
		if (netInfo == null || !netInfo.isConnected()) {
			onNetOff();
		} else {
			onNetOn(netInfo.getType());
		}
	}

	private static void onNetOn(int type) {
		if (!canUseMobileNetwork() && type != ConnectivityManager.TYPE_WIFI) {
			Core.setNetOn(false);
			return;
		}
		Core.setNetOn(true);

		Core.getDataCenter().syncData();
	}

	private static void onNetOff() {
		Core.setNetOn(false);
	}

	private static boolean canUseMobileNetwork() {
		return Core.getPreferences().getBoolean("pref_use_mobile_network", false);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		invalidate(context);
	}
}
