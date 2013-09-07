package reo7sp.cleverday.ui;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.preference.TimePreference;
import reo7sp.cleverday.ui.view.TimeBlockView;
import reo7sp.cleverday.ui.view.TimeLineView;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.DateUtils;

public class TimeLinesLeader {
	private static TimeLinesLeader instance;
	private final Collection<TimeLineView> slaves = new HashSet<TimeLineView>();
	private int scrollY;

	private TimeLinesLeader() {
	}

	/**
	 * Singleton method with lazy initialization.
	 * It'll return null if core isn't built ever
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
	 * Sets right TimeLineView.STEP and scroll y
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
	 * @param y TimeLineView.STEP
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
		slaves.add(slave);
		if (slaves.size() > TimeLinePagerAdapter.COUNT) {
			filterSlaves();
		}
	}

	/**
	 * Removes slave
	 *
	 * @param slave slave to remove
	 */
	public void removeSlave(TimeLineView slave) {
		removeSlave(slave, false);
	}

	/**
	 * Removes slave and if needed, destroy it
	 *
	 * @param slave   slave to remove
	 * @param destroy destroy slave
	 */
	public void removeSlave(TimeLineView slave, boolean destroy) {
		if (destroy) {
			AndroidUtils.recycleView(slave);
		}
		slaves.remove(slave);
	}

	/**
	 * Removes all slaves and destroys them
	 */
	public void removeAllSlaves() {
		removeAllTimeBlocks();
		Iterator<TimeLineView> iterator = slaves.iterator();
		while (iterator.hasNext()) {
			TimeLineView slave = iterator.next();
			AndroidUtils.recycleView(slave);
			iterator.remove();
		}
	}

	/**
	 * Removes dead slaves
	 */
	public void filterSlaves() {
		Iterator<TimeLineView> iterator = slaves.iterator();
		while (iterator.hasNext()) {
			TimeLineView slave = iterator.next();
			if (slave.getParent() == null) {
				AndroidUtils.recycleView(slave);
				iterator.remove();
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
	 * Invokes {@link reo7sp.cleverday.ui.view.TimeLineView#update()} of all slaves
	 */
	public void updateSlaves() {
		for (TimeLineView slave : slaves) {
			slave.update();
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
	 * @param block block
	 * @return {@link TimeBlockView} instance of block
	 */
	public TimeBlockView getTimeBlockView(TimeBlock block) {
		for (TimeLineView slave : slaves) {
			TimeBlockView view = slave.getView(block);
			if (view != null) {
				return view;
			}
		}
		return null;
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
