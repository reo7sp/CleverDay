package reo7sp.cleverday.ui;

import java.util.Calendar;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.activity.MainActivity;
import reo7sp.cleverday.ui.preference.TimePreference;
import reo7sp.cleverday.ui.view.TimeBlockView;
import reo7sp.cleverday.ui.view.TimeLineView;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.DateUtils;

public class TimeLinesLeader {
	private static TimeLinesLeader instance;
	private final TimeLineView[] slaves = new TimeLineView[MainActivity.TIMELINES_COUNT];
	private int scrollY;

	private TimeLinesLeader() {
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
		scrollY = TimePreference.getHour(Core.getPreferences().getString("pref_day_start", "9:00")) * TimeLineView.STEP - TimeLineView.STEP / 2;
		int alternativeScrollY = DateUtils.getFromTime(Core.getCreationTime(), Calendar.HOUR_OF_DAY) * TimeLineView.STEP - TimeLineView.STEP / 2;
		if (alternativeScrollY > scrollY) {
			scrollY = alternativeScrollY;
		}

		// updating
		setScrollY(scrollY);
		updateScroll(true);
	}

	/**
	 * Scrolls time lines by specified y
	 *
	 * @param y step
	 */
	public void scrollBy(int y) {
		setScrollY(getScrollY() + y);
	}

	/**
	 * Scrolls time lines to specified y
	 *
	 * @param y y to set
	 */
	public void scrollTo(int y) {
		setScrollY(y);
	}

	/**
	 * Updates scroll of all time lines or only current
	 *
	 * @param all all time lines
	 */
	public void updateScroll(boolean all) {
		if (all) {
			for (TimeLineView slave : slaves) {
				slave.scrollTo(0, scrollY);
			}
		} else if (getCurrent() != null) {
			getCurrent().scrollTo(0, scrollY);
		}
	}

	/**
	 * Adds new slave to our family
	 *
	 * @param slave slave to add
	 */
	public void addSlave(TimeLineView slave) {
		filterSlaves();
		for (int i = 0, length = slaves.length; i < length; i++) {
			if (slaves[i] == null) {
				slaves[i] = slave;
				break;
			}
		}
	}

	/**
	 * Removes slave and if needed, destroy it
	 *
	 * @param slave   slave to remove
	 * @param destroy destroy slave completely
	 */
	public void removeSlave(TimeLineView slave, boolean destroy) {
		if (destroy) {
			AndroidUtils.recycleView(slave);
		}
		for (int i = 0, length = slaves.length; i < length; i++) {
			if (slaves[i] == slave) {
				slaves[i] = null;
			}
		}
	}

	/**
	 * Removes all slaves and destroys them
	 */
	public void removeAllSlaves() {
		removeAllTimeBlocks();
		for (int i = 0, length = slaves.length; i < length; i++) {
			AndroidUtils.recycleView(slaves[i]);
			slaves[i] = null;
		}
	}

	/**
	 * Removes dead slaves
	 */
	public void filterSlaves() {
		for (TimeLineView slave : slaves) {
			if (slave != null && slave.getParent() == null) {
				removeSlave(slave, true);
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
			final TimeBlockView view = slave.addTimeBlock(block);
			if (view != null) {
				return view;
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
			slave.removeTimeBlock(block);
		}
	}

	/**
	 * Removes all time blocks
	 */
	public void removeAllTimeBlocks() {
		for (TimeLineView slave : slaves) {
			slave.removeAllTimeBlocks();
		}
	}

	/**
	 * @return current time line
	 */
	public TimeLineView getCurrent() {
		return Core.getMainActivity().getCurrentTimeLine();
	}

	/**
	 * @return the scroll y
	 */
	public int getScrollY() {
		return scrollY;
	}

	/**
	 * @see TimeLinesLeader#scrollTo(int)
	 */
	public void setScrollY(int scrollY) {
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
			updateScroll(false);
		}
	}

	/**
	 * @return the current editing block
	 */
	public TimeBlockView getEditingBlock() {
		for (TimeLineView slave : slaves) {
			for (TimeBlockView view : slave.getTimeBlockViews()) {
				if (view.isSelected()) {
					return view;
				}
			}
		}
		return null;
	}

	/**
	 * @return true if any block is selected
	 */
	public boolean isAnyBlockSelected() {
		return getEditingBlock() != null;
	}

	/**
	 * @return the height of time line
	 */
	public int getHeight() {
		return TimeLineView.STEP * 25 + 8;
	}

	/**
	 * @return the scroll max
	 */
	public int getScrollMax() {
		return getHeight() - (getCurrent() == null ? 0 : getCurrent().getHeight());
	}
}
