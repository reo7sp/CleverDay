package reo7sp.cleverday;

import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.utils.DateUtils;

public class DayChecker extends Thread {
	static final DayChecker INSTANCE = new DayChecker();

	private DayChecker() {
		super("DayChecker");
		start();
	}

	@Override
	public void run() {
		try {
			while (!isInterrupted()) {
				if (!DateUtils.isInOneDay(System.currentTimeMillis(), Core.getCreationTime())) {
					Log.i("DayChecker", "Day changed!");
					Core.startBuilding().build();
				}
				Thread.sleep(10 * TimeConstants.SECOND);
			}
		} catch (InterruptedException ignored) {
		}
	}
}
