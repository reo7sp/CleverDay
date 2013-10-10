package reo7sp.cleverday.ui.timeline;

import java.util.Calendar;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.data.DataInvalidateListener;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.TimePreference;
import reo7sp.cleverday.ui.activity.MainActivity;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.DateUtils;

public class TimeLinesLeader implements DataInvalidateListener {
	private static TimeLinesLeader instance;
	private final TimeLineView[] slaves = new TimeLineView[MainActivity.TIMELINES_COUNT];
	private int scrollY;

	private TimeLinesLeader() {
		Core.getDataCenter().registerDataInvalidateListener(this);
		initAutoUpdater();
	}

	/**
	 * Singleton method with lazy initialization.
	 * It'll return null if core isn't built
	 *
	 * @return the instance
	 */
	public static TimeLinesLeader getInstance() {
		if (instance == null && Core.isBuilt()) {
			instance = new TimeLinesLeader();
		}
		return instance;
	}

	/**
	 * Sets right step and scroll y
	 */
	public void init() {
		int nextScrollY = TimePreference.getHour(Core.getPreferences().getString("pref_day_start", "9:00")) * TimeLineView.STEP - TimeLineView.STEP / 2;
		int alternativeScrollY = DateUtils.getFromTime(Core.getCreationTime(), Calendar.HOUR_OF_DAY) * TimeLineView.STEP - TimeLineView.STEP / 2;
		if (alternativeScrollY > nextScrollY) {
			nextScrollY = alternativeScrollY;
		}
		setScrollY(nextScrollY);
	}

	/**
	 * Updates scroll of all time lines
	 */
	private void updateScroll() {
		for (TimeLineView slave : slaves) {
			if (slave != null) {
				slave.scrollTo(0, scrollY);
			}
		}
	}

	/**
	 * Adds new slave to our family
	 *
	 * @param slave slave to add
	 */
	void addSlave(TimeLineView slave) {
		filterSlaves();
		for (int i = 0, length = slaves.length; i < length; i++) {
			if (slaves[i] == null) {
				slaves[i] = slave;
				break;
			}
		}
	}

	/**
	 * Removes slave
	 *
	 * @param slave slave to remove
	 */
	private void removeSlave(TimeLineView slave) {
		for (int i = 0, length = slaves.length; i < length; i++) {
			if (slaves[i] == slave) {
				slaves[i] = null;
			}
		}
	}

	/**
	 * Removes all slaves and and its time blocks
	 */
	public void clean() {
		removeAllTimeBlocks();
		for (int i = 0, length = slaves.length; i < length; i++) {
			AndroidUtils.recycleView(slaves[i]);
			slaves[i] = null;
		}
	}

	/**
	 * Removes dead slaves
	 */
	private void filterSlaves() {
		for (TimeLineView slave : slaves) {
			if (slave != null && slave.getParent() == null) {
				removeSlave(slave);
			}
		}
	}

	/**
	 * Adds new time block
	 *
	 * @param block block to add
	 */
	public TimeBlockView addTimeBlock(TimeBlock block) {
		for (TimeLineView slave : slaves) {
			if (slave != null) {
				final TimeBlockView view = slave.addTimeBlock(block);
				if (view != null) {
					return view;
				}
			}
		}
		return null;
	}

	/**
	 * Removes time block
	 *
	 * @param block block to remove
	 */
	public void removeTimeBlock(TimeBlock block) {
		for (TimeLineView slave : slaves) {
			if (slave != null) {
				slave.removeTimeBlock(block);
			}
		}
	}

	/**
	 * Removes all time blocks
	 */
	private void removeAllTimeBlocks() {
		for (TimeLineView slave : slaves) {
			if (slave != null) {
				slave.removeAllTimeBlocks();
			}
		}
	}

	/**
	 * @return current time line
	 */
	TimeLineView getCurrent() {
		return Core.getMainActivity().getCurrentTimeLine();
	}

	/**
	 * @return the scroll y
	 */
	int getScrollY() {
		return scrollY;
	}

	/**
	 * Sets scroll y of all time lines
	 *
	 * @param scrollY scroll y to set
	 */
	void setScrollY(int scrollY) {
		boolean changed = false;
		this.scrollY = scrollY;
		if (this.scrollY < 0) {
			this.scrollY = 0;
		} else if (this.scrollY > getScrollMax()) {
			this.scrollY = getScrollMax();
		}
		if (this.scrollY != scrollY) {
			changed = true;
		}
		if (changed) {
			updateScroll();
		}
	}

	/**
	 * @return the current editing block
	 */
	public TimeBlockView getEditingBlock() {
		for (TimeLineView slave : slaves) {
			if (slave != null) {
				for (TimeBlockView view : slave.getTimeBlockViews()) {
					if (view.isSelected()) {
						return view;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @return the height of time line
	 */
	private int getHeight() {
		return TimeLineView.STEP * 25 + 8;
	}

	/**
	 * @return the scroll max
	 */
	int getScrollMax() {
		return getHeight() - (getCurrent() == null ? 0 : getCurrent().getHeight());
	}

	@Override
	public void onDataInvalidate() {
		updateSlaves();
	}

	/**
	 * Updates all slaves
	 */
	private void updateSlaves() {
		for (TimeLineView slave : slaves) {
			if (slave != null) {
				slave.update(false);
			}
		}
	}

	private void initAutoUpdater() {
		new Thread("TimeLinesAutoUpdater") {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						Thread.sleep(30000);
						updateSlaves();
					}
				} catch (InterruptedException ignored) {
				}
			}
		}.start();
	}
}
