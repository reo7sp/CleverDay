package reo7sp.cleverday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import reo7sp.cleverday.data.DataCenter;

/**
 * Created by reo7sp on 8/27/13 at 6:50 PM
 */
public class NetWatcher extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = manager.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			onNetOn(netInfo.getType());
		}
	}

	private void onNetOn(int type) {
		if (!canUseMobileNetwork() && type != ConnectivityManager.TYPE_WIFI) {
			return;
		}
		DataCenter.getInstance().receiveData();
	}

	private boolean canUseMobileNetwork() {
		return Core.getPreferences().getBoolean("pref_use_mobile_network", false);
	}
}
