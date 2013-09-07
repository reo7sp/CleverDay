package reo7sp.cleverday;

import android.os.Handler;
import android.os.Looper;

public class SyncActionQueue extends ActionQueue {
	private final Handler handler = new Handler(Looper.getMainLooper());

	SyncActionQueue() {
	}

	@Override
	public void addAction(Runnable action) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			action.run();
		} else {
			handler.post(action);
		}
	}

	@Override
	public void addAction(Runnable action, int delay) {
		handler.postDelayed(action, delay);
	}

	@Override
	public void removeAction(Runnable action) {
		handler.removeCallbacks(action);
	}
}
