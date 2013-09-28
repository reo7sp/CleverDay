package reo7sp.cleverday.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.ui.BitmapFactory;
import reo7sp.cleverday.ui.GestureListener;
import reo7sp.cleverday.ui.Location2D;
import reo7sp.cleverday.ui.ScrollAssistant;
import reo7sp.cleverday.ui.TimeLinesLeader;
import reo7sp.cleverday.ui.colors.UIColor;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.DateUtils;

public class TimeLineView extends View {
	public static final int STEP = 100;
	private static final Comparator<TimeBlockView> timeBlockViewComparator = new Comparator<TimeBlockView>() {
		@Override
		public int compare(TimeBlockView first, TimeBlockView second) {
			return (int) (first.getBlock().getDuration() - second.getBlock().getDuration());
		}
	};
	private final GestureDetector gestureDetector;
	private final ScaleGestureDetector scaleGestureDetector;
	private final GestureListener gestureListener = new MyGestureListener();
	private final List<TimeBlockView> timeBlockViews = new TimeBlockViewsCollection();
	private final List<TimeBlockView> immutableTimeBlockViews = Collections.unmodifiableList(timeBlockViews);
	private final boolean initialized;
	private long time;
	private ScrollAssistant scrollAssistant;

	public TimeLineView(Context context, long time) {
		super(context);
		gestureDetector = new GestureDetector(context, gestureListener);
		scaleGestureDetector = new ScaleGestureDetector(context, gestureListener);
		setWillNotDraw(false);
		getLeader().addSlave(this);
		setTime(time);
		scrollTo(0, getLeader().getScrollY());
		initialized = true;
		Log.i("TimeLine", "Created new time line on " + DateUtils.FORMAT_DAY_MONTH.format(new Date(time)));

		postDelayed(new Runnable() {
			@Override
			public void run() {
				if (getLeader().getCurrent() == TimeLineView.this) {
					update();
				}
				postDelayed(this, 250);
			}
		}, 250);
	}

	/**
	 * @return the time lines leader
	 */
	public TimeLinesLeader getLeader() {
		return Core.getTimeLinesLeader();
	}

	/**
	 * Adds time block to time line
	 *
	 * @param block block to add
	 */
	public TimeBlockView addTimeBlock(TimeBlock block) {
		if (block == null) {
			return null;
		}
		if (isTimeBlockAcceptable(block) && !hasBlock(block)) {
			final TimeBlockView view = new TimeBlockView(this, block);
			timeBlockViews.add(view);
			Log.i("TimeLine", "Adding new time block \"" + block.getTitle() + "\" with id " + block.getID());
			return view;
		}
		return null;
	}

	/**
	 * Checks if specified time block is acceptable
	 *
	 * @param block block to check
	 * @return true if block is acceptable
	 */
	public boolean isTimeBlockAcceptable(TimeBlock block) {
		return DateUtils.isInOneDay(block.getStart(), time);
	}

	/**
	 * Finds view of specified time block
	 *
	 * @param block block of view
	 * @return view of specified time block
	 */
	public TimeBlockView getView(TimeBlock block) {
		for (TimeBlockView view : timeBlockViews) {
			if (view.getBlock().equals(block)) {
				return view;
			}
		}
		return null;
	}

	/**
	 * Updates all time blocks and time line itself
	 */
	public void update() {
		update(false);
	}

	/**
	 * Updates all time blocks and time line itself
	 *
	 * @param immediate true if animations must be prevented
	 */
	public void update(boolean immediate) {
		if (!initialized || !Core.getDataCenter().isInvalidated()) {
			return;
		}
		for (TimeBlockView view : timeBlockViews) {
			view.update(immediate);
		}
		Collections.sort(timeBlockViews, timeBlockViewComparator);
		postInvalidate();
		if (getLeader().getCurrent() != this) {
			scrollTo(0, getLeader().getScrollY());
		}
	}

	/**
	 * Removes all time blocks
	 */
	public void removeAllTimeBlocks() {
		Log.i("TimeLine", "Removing all time blocks");
		timeBlockViews.clear();
	}

	private void repairScroll() {
		if (getScrollY() < 0) {
			super.scrollTo(0, 0);
		} else if (getScrollY() > getLeader().getScrollMax()) {
			super.scrollTo(0, getLeader().getScrollMax());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		gestureListener.onTouch(e);
		gestureDetector.onTouchEvent(e);
		scaleGestureDetector.onTouchEvent(e);

		return true;
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		repairScroll();
	}

	@Override
	public void scrollBy(int x, int y) {
		super.scrollBy(x, y);
		repairScroll();
	}

	@Override
	public void onDraw(Canvas canvas) {
		boolean darkTheme = AndroidUtils.isInDarkTheme();

		Core.getPaint().setStyle(Paint.Style.FILL);
		Core.getPaint().setAntiAlias(true);
		Core.getPaint().setTextSize(24);
		Core.getPaint().setColor(darkTheme ? UIColor.DARK_BACKGROUND : UIColor.LIGHT_BACKGROUND);

		// drawing graph
		canvas.drawPaint(Core.getPaint());
		int curHour = DateUtils.isInOneDay(Core.getCreationTime(), time) ? DateUtils.getFromTime(System.currentTimeMillis(), Calendar.HOUR_OF_DAY) : -1;
		for (int i = 0; i < 27; i++) {
			int y = STEP * i + 8;

			// time
			Core.getPaint().setColor(i == curHour ? (darkTheme ? Color.WHITE : Color.BLACK) : (i > 23 ? (darkTheme ? Color.DKGRAY : Color.LTGRAY) : Color.GRAY));
			Core.getPaint().setFakeBoldText(i == curHour);
			canvas.drawText(((i % 24) < 10 ? "0" : "") + (i % 24), 10, y + 30, Core.getPaint());
			Core.getPaint().setFakeBoldText(false);

			// line
			Bitmap line = BitmapFactory.getBitmap(R.drawable.dashed_line);
			for (int j = 0, max = getWidth() / line.getWidth() + 1; j < max; j++) {
				canvas.drawBitmap(line, j * line.getWidth(), y, Core.getPaint());
			}
		}

		// drawing blocks
		for (int i = timeBlockViews.size() - 1; i >= 0; i--) {
			TimeBlockView view = timeBlockViews.get(i);
			view.drawBlock(canvas);
		}
		for (TimeBlockView view : timeBlockViews) {
			view.drawSelection(canvas);
		}
	}

	/**
	 * Removes time block from time line, but doesn't remove from database
	 *
	 * @param block block to remove
	 */
	public void removeTimeBlock(TimeBlock block) {
		removeTimeBlock(getView(block));
	}

	/**
	 * Removes time block from time line, but doesn't remove from database
	 *
	 * @param view block view to remove
	 */
	public void removeTimeBlock(final TimeBlockView view) {
		if (view == null) {
			return;
		}
		view.startAlphaAnimation(false, new Runnable() {
			@Override
			public void run() {
				removeTimeBlockView(view);
			}
		});
	}

	private void removeTimeBlockView(TimeBlockView view) {
		Log.i("TimeLine", "Removing time block \"" + view.getBlock().getTitle() + "\" with id " + view.getBlock().getID());
		view.setSelected(false);
		view.stopAllAnimations();
		timeBlockViews.remove(view);
	}

	/**
	 * @return the time line time
	 */
	public long getTime() {
		return time;
	}

	private void setTime(long time) {
		this.time = time;

		// re-adding all blocks
		timeBlockViews.clear();
		for (TimeBlock block : Core.getDataCenter().getTimeBlocks()) {
			addTimeBlock(block);
		}
	}

	/**
	 * @return the immutable collection of time block views
	 */
	public Collection<TimeBlockView> getTimeBlockViews() {
		return immutableTimeBlockViews;
	}

	/**
	 * Checks if time line has specified block
	 *
	 * @param block block to check
	 * @return true if block contains in this time line
	 */
	public boolean hasBlock(TimeBlock block) {
		return getView(block) != null;
	}

	private class MyGestureListener extends GestureListener {
		@Override
		public void onLongPress(MotionEvent e) {
			TimeBlockView view = getLeader().getEditingBlock();
			if (view != null) {
				return;
			}

			onSingleTapUp(e);

			view = getLeader().getEditingBlock();
			if (view != null) {
				int y = (int) e.getY() + getScrollY();
				view.startDragging(new Location2D((int) e.getX(), y - view.getY()), true);
				Core.getVibrator().vibrate(75);
			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			if (getLeader().getEditingBlock() == null || !getLeader().getEditingBlock().isDragging()) {
				scrollBy(0, (int) distanceY);
				getLeader().scrollTo(getScrollY());
				getLeader().updateScroll(true);
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			if (getLeader().getEditingBlock() == null || !getLeader().getEditingBlock().isDragging()) {
				scrollAssistant = new ScrollAssistant(TimeLineView.this, 0, 0, 0, STEP * 25 - getHeight());
				scrollAssistant.fling((int) velocityX, (int) velocityY);
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int y = (int) e.getY() + getScrollY();

			TimeBlockView editingBlock = getLeader().getEditingBlock();

			if (editingBlock != null) {
				if (y < editingBlock.getY() - 32 || y > editingBlock.getY() + editingBlock.getHeight() + 32) {
					editingBlock.setSelected(false);
				}
			}
			for (TimeBlockView view : timeBlockViews) {
				if (y > view.getY() - 32 && y < view.getY() + view.getHeight() + 32) {
					view.setSelected(true);
					break;
				}
			}

			return super.onSingleTapUp(e);
		}

		@Override
		public void onTouch(MotionEvent e) {
			int y = (int) e.getY() + getScrollY();

			// scrolling
			if (e.getAction() == MotionEvent.ACTION_DOWN) {
				if (scrollAssistant != null) {
					scrollAssistant.forceFinished();
					scrollAssistant = null;
					getLeader().scrollTo(getScrollY());
				}
			}

			// block dragging and selecting
			TimeBlockView editingBlock = getLeader().getEditingBlock();
			if (editingBlock != null) {
				if (e.getAction() == MotionEvent.ACTION_UP) {
					editingBlock.stopDragging();
				}
				if (e.getAction() == MotionEvent.ACTION_DOWN && y > editingBlock.getY() - 32 && y < editingBlock.getY() + editingBlock.getHeight() + 32) {
					editingBlock.startDragging(new Location2D((int) e.getX(), y - editingBlock.getY()), false);
				}
				if (editingBlock.isDragging()) {
					boolean resized = false;
					if (editingBlock.getDragStartLocation().getX() - 32 < 50 + (getWidth() - 50) / 2 && editingBlock.getDragStartLocation().getX() + 32 > 50 + (getWidth() - 50) / 2) {
						if (editingBlock.getDragStartLocation().getY() < 32) {
							long nextTime = (long) (DateUtils.trimToDay(time) + y / (float) STEP * 3600000);
							if (DateUtils.isInOneDay(time, nextTime)) {
								editingBlock.getBlock().setStart(nextTime);
							}
							resized = true;
						} else if (editingBlock.getDragStartLocation().getY() > editingBlock.getHeight() - 32) {
							long nextTime = (long) (DateUtils.trimToDay(time) + y / (float) STEP * 3600000);
							if (DateUtils.isInOneDay(time, nextTime)) {
								editingBlock.getBlock().setEnd(nextTime);
							}
							resized = true;

							editingBlock.startDragging(new Location2D(editingBlock.getDragStartLocation().getX(), y - editingBlock.getY()), false); // update drag start y
						}
					}
					if (!resized) {
						long nextTime = (long) (DateUtils.trimToDay(time) + (y - editingBlock.getDragStartLocation().getY()) / (float) STEP * 3600000);
						if (DateUtils.isInOneDay(time, nextTime)) {
							editingBlock.getBlock().setBounds(nextTime, editingBlock.getBlock().getDuration() + nextTime, false);
						}
					}
					editingBlock.update();
				}
			}
		}
	}

	private class TimeBlockViewsCollection extends ArrayList<TimeBlockView> {
		@Override
		public boolean add(TimeBlockView object) {
			super.add(object);
			update();
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends TimeBlockView> collection) {
			super.addAll(collection);
			update();
			return true;
		}

		@Override
		public void clear() {
			super.clear();
			postInvalidate();
		}

		@Override
		public boolean remove(Object object) {
			super.remove(object);
			update();
			return true;
		}

		@Override
		public boolean removeAll(Collection<?> collection) {
			super.removeAll(collection);
			update();
			return true;
		}

		@Override
		public boolean retainAll(Collection<?> collection) {
			super.retainAll(collection);
			update();
			return true;
		}
	}
}
