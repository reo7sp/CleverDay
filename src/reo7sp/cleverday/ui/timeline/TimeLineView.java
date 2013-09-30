package reo7sp.cleverday.ui.timeline;

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
import java.util.List;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.DateFormatter;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.ui.BitmapFactory;
import reo7sp.cleverday.ui.GestureListener;
import reo7sp.cleverday.ui.Location2D;
import reo7sp.cleverday.ui.ScrollAssistant;
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
	private final boolean initialized;
	private long time;
	private ScrollAssistant scrollAssistant;

	public TimeLineView(Context context, long time) {
		super(context);

		gestureDetector = new GestureDetector(context, gestureListener);
		scaleGestureDetector = new ScaleGestureDetector(context, gestureListener);
		setWillNotDraw(false);

		Core.getTimeLinesLeader().addSlave(this);
		updateTime(time);
		scrollTo(0, Core.getTimeLinesLeader().getScrollY());

		initialized = true;
		Log.i("TimeLine", "Created new time line on " + Core.getDateFormatter().format(DateFormatter.Format.DAY_MONTH, time));

		initUpdater();
	}

	/**
	 * Initializes timer which updates time line
	 */
	private void initUpdater() {
		postDelayed(new Runnable() {
			@Override
			public void run() {
				if (Core.getTimeLinesLeader().getCurrent() == TimeLineView.this) {
					update(false);
				}
				postDelayed(this, 250);
			}
		}, 250);
	}

	/**
	 * Adds time block to time line
	 *
	 * @param block block to add
	 */
	TimeBlockView addTimeBlock(TimeBlock block) {
		if (block == null) {
			return null;
		}
		if (!hasBlock(block) && isTimeBlockAcceptable(block)) {
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
	private boolean isTimeBlockAcceptable(TimeBlock block) {
		return DateUtils.isInOneDay(block.getStart(), time);
	}

	/**
	 * Finds view of specified time block
	 *
	 * @param block block of view
	 * @return view of specified time block
	 */
	private TimeBlockView getView(TimeBlock block) {
		for (TimeBlockView view : timeBlockViews) {
			if (view.getBlock().equals(block)) {
				return view;
			}
		}
		return null;
	}

	/**
	 * Updates all time blocks and time line itself
	 *
	 * @param immediate true if animations must be prevented
	 */
	private void update(boolean immediate) {
		if (!initialized || !Core.getDataCenter().isInvalidated()) {
			return;
		}

		// updating all blocks
		for (TimeBlockView view : timeBlockViews) {
			view.update(immediate);
		}

		// sorting them
		Collections.sort(timeBlockViews, timeBlockViewComparator);

		// updating time line
		postInvalidate();
		if (Core.getTimeLinesLeader().getCurrent() != this) {
			scrollTo(0, Core.getTimeLinesLeader().getScrollY());
		}
	}

	/**
	 * Removes all time blocks
	 */
	void removeAllTimeBlocks() {
		Log.i("TimeLine", "Removing all time blocks");
		timeBlockViews.clear();
	}

	/**
	 * Repairs scroll
	 */
	private void repairScroll() {
		if (getScrollY() < 0) {
			super.scrollTo(0, 0);
		} else if (getScrollY() > Core.getTimeLinesLeader().getScrollMax()) {
			super.scrollTo(0, Core.getTimeLinesLeader().getScrollMax());
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
	 * Removes time block from time line
	 *
	 * @param block block to remove
	 */
	void removeTimeBlock(TimeBlock block) {
		removeTimeBlock(getView(block));
	}

	/**
	 * Removes time block from time line
	 *
	 * @param view block view to remove
	 */
	private void removeTimeBlock(final TimeBlockView view) {
		if (view == null) {
			return;
		}
		view.startAlphaAnimation(false, new Runnable() {
			@Override
			public void run() {
				Log.i("TimeLine", "Removing time block \"" + view.getBlock().getTitle() + "\" with id " + view.getBlock().getID());
				view.setSelected(false);
				view.stopAllAnimations();
				timeBlockViews.remove(view);
			}
		});
	}

	/**
	 * @return the time line time
	 */
	public long getTime() {
		return time;
	}

	private void updateTime(long time) {
		this.time = time;

		// re-adding all blocks
		timeBlockViews.clear();
		for (TimeBlock block : Core.getDataCenter().getTimeBlocks()) {
			addTimeBlock(block);
		}
	}

	/**
	 * @return the collection of time block views
	 */
	Collection<TimeBlockView> getTimeBlockViews() {
		return timeBlockViews;
	}

	/**
	 * Checks if time line has specified block
	 *
	 * @param block block to check
	 * @return true if block contains in this time line
	 */
	private boolean hasBlock(TimeBlock block) {
		return getView(block) != null;
	}

	private class MyGestureListener extends GestureListener {
		@Override
		public void onLongPress(MotionEvent e) {
			onSingleTapUp(e);

			TimeBlockView editingBlock = Core.getTimeLinesLeader().getEditingBlock();
			if (editingBlock != null) {
				int y = (int) e.getY() + getScrollY();
				editingBlock.startDragging(new Location2D((int) e.getX(), y - editingBlock.getY()), true);
				Core.getVibrator().vibrate(75);
			}
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			TimeBlockView editingBlock = Core.getTimeLinesLeader().getEditingBlock();
			if (editingBlock == null || !editingBlock.isDragging()) {
				scrollBy(0, (int) distanceY);
				Core.getTimeLinesLeader().setScrollY(getScrollY());
			}
			return super.onScroll(e1, e2, distanceX, distanceY);
		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			TimeBlockView editingBlock = Core.getTimeLinesLeader().getEditingBlock();
			if (editingBlock == null || !editingBlock.isDragging()) {
				scrollAssistant = new ScrollAssistant(TimeLineView.this, 0, 0, 0, STEP * 25 - getHeight());
				scrollAssistant.fling((int) velocityX, (int) velocityY);
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			int y = (int) e.getY() + getScrollY();

			TimeBlockView editingBlock = Core.getTimeLinesLeader().getEditingBlock();
			boolean findNewBlockToSelect = true;

			if (editingBlock != null) {
				if (y < editingBlock.getY() - 32 || y > editingBlock.getY() + editingBlock.getHeight() + 32) {
					editingBlock.setSelected(false);
				} else {
					findNewBlockToSelect = false;
				}
			}

			if (findNewBlockToSelect) {
				for (TimeBlockView view : timeBlockViews) {
					if (y > view.getY() - 32 && y < view.getY() + view.getHeight() + 32) {
						view.setSelected(true);
						break;
					}
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
					Core.getTimeLinesLeader().setScrollY(getScrollY());
				}
			}

			// block dragging and selecting
			TimeBlockView editingBlock = Core.getTimeLinesLeader().getEditingBlock();
			if (editingBlock != null) {
				if (e.getAction() == MotionEvent.ACTION_UP) {
					editingBlock.stopDragging();
				} else if (e.getAction() == MotionEvent.ACTION_DOWN && y > editingBlock.getY() - 32 && y < editingBlock.getY() + editingBlock.getHeight() + 32) {
					editingBlock.startDragging(new Location2D((int) e.getX(), y - editingBlock.getY()), false);
				}

				if (editingBlock.isDragging()) {
					boolean resized = false;
					final int CENTER_OF_BLOCK = 50 + (getWidth() - 50) / 2;

					// analyzing if user resized block
					if (editingBlock.getDragStartLocation().getX() - 32 < CENTER_OF_BLOCK && editingBlock.getDragStartLocation().getX() + 32 > CENTER_OF_BLOCK) {
						int pos = 0;
						if (editingBlock.getDragStartLocation().getY() < 32) {
							pos = 1;
						} else if (editingBlock.getDragStartLocation().getY() > editingBlock.getHeight() - 32) {
							pos = 2;
						}

						if (pos != 0) {
							long nextTime = (long) (DateUtils.trimToDay(time) + y / (float) STEP * 3600000);
							if (DateUtils.isInOneDay(time, nextTime)) {
								editingBlock.getBlock().setTime(nextTime, pos == 1, false);
							}
							resized = true;

							if (pos == 2) {
								editingBlock.startDragging(new Location2D(editingBlock.getDragStartLocation().getX(), y - editingBlock.getY()), false); // update drag start y
							}
						}
					}

					// if user doesn't resize block, we'll drag it
					if (!resized) {
						long nextTime = (long) (DateUtils.trimToDay(time) + (y - editingBlock.getDragStartLocation().getY()) / (float) STEP * 3600000);
						if (DateUtils.isInOneDay(time, nextTime)) {
							editingBlock.getBlock().setBounds(nextTime, editingBlock.getBlock().getDuration() + nextTime, false);
						}
					}

					editingBlock.update(false);
				}
			}
		}
	}

	private class TimeBlockViewsCollection extends ArrayList<TimeBlockView> {
		@Override
		public boolean add(TimeBlockView object) {
			super.add(object);
			update(false);
			return true;
		}

		@Override
		public boolean addAll(Collection<? extends TimeBlockView> collection) {
			super.addAll(collection);
			update(false);
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
			update(false);
			return true;
		}

		@Override
		public boolean removeAll(Collection<?> collection) {
			super.removeAll(collection);
			update(false);
			return true;
		}

		@Override
		public boolean retainAll(Collection<?> collection) {
			super.retainAll(collection);
			update(false);
			return true;
		}
	}
}
