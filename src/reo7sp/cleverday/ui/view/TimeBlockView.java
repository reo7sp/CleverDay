package reo7sp.cleverday.ui.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.DateFormatter;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.BitmapFactory;
import reo7sp.cleverday.ui.Location2D;
import reo7sp.cleverday.ui.activity.EditBlockActivity;
import reo7sp.cleverday.ui.colors.SimpleColor;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.ColorUtils;
import reo7sp.cleverday.utils.DateUtils;

public class TimeBlockView {
	private TimeLineView timeLine;
	private TimeBlock block;
	private boolean isSelected;
	private boolean isDragging;
	private ActionMode actionMode;
	private Location2D dragStartLocation;
	private boolean deselectAfterDragging;
	private int y;
	private int height;
	private int alpha;
	private PositionAnimation positionAnimation;
	private SizeAnimation sizeAnimation;
	private AlphaAnimation alphaAnimation;
	private String[] notes;

	TimeBlockView(TimeLineView timeLine, TimeBlock block) {
		if (block == null) {
			throw new NullPointerException("Time block can't be null");
		}
		this.timeLine = timeLine;
		this.block = block;

		startAlphaAnimation(true, null);
		repairSize(true);
		repairPosition(true);
	}

	/**
	 * Draws block with no selection on specified canvas
	 *
	 * @param canvas time line canvas
	 */
	void drawBlock(Canvas canvas) {
		Core.getPaint().setStyle(Paint.Style.FILL);
		Core.getPaint().setAntiAlias(true);
		Core.getPaint().setTextSize(24);

		// block
		Core.getPaint().setColor(ColorUtils.darker(ColorUtils.changeAlpha(block.getColor(), alpha), AndroidUtils.isInDarkTheme() ? 0.25f : 0));
		canvas.drawRect(50, y, timeLine.getWidth() - 8, y + height, Core.getPaint());

		// text
		Core.getPaint().setColor(ColorUtils.changeAlpha(Color.WHITE, alpha));
		canvas.drawText(block.getHumanTitle(), 130, y + 30, Core.getPaint());
		canvas.drawText(Core.getDateFormatter().format(DateFormatter.Format.HOUR_MINUTE, block.getStart()), 60, y + 30, Core.getPaint());
		if (height > 70) {
			canvas.drawText(Core.getDateFormatter().format(DateFormatter.Format.HOUR_MINUTE, block.getEnd()), 60, y + height - 10, Core.getPaint());
		}

		// notes
		if (notes != null) {
			for (int i = 0, length = notes.length; i < length; i++) {
				int lineY = 10 + 30 * (i + 2);
				if (height > lineY + 10) {
					canvas.drawText(notes[i], 130, y + lineY, Core.getPaint());
				}
			}
		}

		// alarm icon
		if (block.hasReminder()) {
			Bitmap bitmap = BitmapFactory.getBitmap(R.drawable.ic_alarm_light);
			if (height > bitmap.getHeight() + 8) {
				canvas.drawBitmap(bitmap, timeLine.getWidth() - bitmap.getWidth() - 10, y + 4, Core.getPaint());
			}
		}
	}

	/**
	 * Draw selection of block view on specified canvas
	 */
	void drawSelection(Canvas canvas) {
		if (!isSelected) {
			return;
		}

		Core.getPaint().setColor(ColorUtils.changeAlpha(ColorUtils.brighter(SimpleColor.BLUE, !AndroidUtils.isInDarkTheme() ? 0.15f : 0), alpha));

		Core.getPaint().setStrokeWidth(4);
		Core.getPaint().setStyle(Paint.Style.STROKE);
		canvas.drawRect(52, y + 2, timeLine.getWidth() - 10, y + height - 2, Core.getPaint());
		Core.getPaint().setStrokeWidth(1);
		Core.getPaint().setStyle(Paint.Style.FILL);

		Bitmap bitmap = BitmapFactory.getBitmap(R.drawable.blue_circle);
		canvas.drawBitmap(bitmap, (timeLine.getWidth() + 50) / 2 - bitmap.getWidth() / 2, y + 2 - bitmap.getHeight() / 2, Core.getPaint());
		canvas.drawBitmap(bitmap, (timeLine.getWidth() + 50) / 2 - bitmap.getWidth() / 2, y + height - 2 - bitmap.getHeight() / 2, Core.getPaint());
	}

	/**
	 * Starts alpha animation of block
	 *
	 * @param toFullyVisible true if alpha must be incremented to 255
	 * @param onStop         stop listener
	 */
	public void startAlphaAnimation(boolean toFullyVisible, Runnable onStop) {
		Core.getSyncActionQueue().removeAction(alphaAnimation);
		alphaAnimation = new AlphaAnimation(toFullyVisible, onStop);
		Core.getSyncActionQueue().addAction(alphaAnimation);
	}

	/**
	 * Updates block
	 */
	public void update() {
		update(false);
	}

	/**
	 * Updates block
	 *
	 * @param immediate if true animations will be prevented
	 */
	public void update(boolean immediate) {
		checkDay();
		repairPosition(immediate);
		repairSize(immediate);
		updateNotes();
	}

	/**
	 * Checks if time block is in wrong time line and repairs it if needed
	 */
	private void checkDay() {
		if (!DateUtils.isInOneDay(block.getStart(), timeLine.getTime())) {
			boolean selected = isSelected;
			setSelected(false);
			Core.getTimeLinesLeader().removeTimeBlock(block);
			TimeBlockView view = Core.getTimeLinesLeader().addTimeBlock(block);
			if (view != null) {
				view.setSelected(selected);
			}
		}
	}

	/**
	 * Repairs y
	 *
	 * @param immediate true if animations must be prevented
	 */
	private void repairPosition(boolean immediate) {
		Core.getSyncActionQueue().removeAction(positionAnimation);
		if (immediate) {
			y = generateY();
		} else {
			positionAnimation = new PositionAnimation();
			Core.getSyncActionQueue().addAction(positionAnimation);
		}
	}

	/**
	 * Repairs size
	 *
	 * @param immediate true if animations must be prevented
	 */
	private void repairSize(boolean immediate) {
		Core.getSyncActionQueue().removeAction(sizeAnimation);
		if (immediate) {
			height = generateHeight();
		} else {
			sizeAnimation = new SizeAnimation();
			Core.getSyncActionQueue().addAction(sizeAnimation);
		}
	}

	/**
	 * @return generated y
	 */
	private int generateY() {
		Calendar calendar = DateUtils.getCalendarInstance(block.getStart());
		return (int) ((calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) / 60F * TimeLineView.STEP + 8);
	}

	/**
	 * Stops all animations of view
	 */
	void stopAllAnimations() {
		Core.getSyncActionQueue().removeAction(alphaAnimation);
		Core.getSyncActionQueue().removeAction(positionAnimation);
		Core.getSyncActionQueue().removeAction(sizeAnimation);

		alphaAnimation = null;
		positionAnimation = null;
		sizeAnimation = null;
	}

	private void updateNotes() {
		String[] notes = block.getNotes() == null ? null : block.getNotes().replaceAll("\n\n+", "\n").split("\n");
		if (notes != null) {
			Collection<String> lines = new ArrayList<String>();
			for (String line : notes) {
				int width = 0;
				StringBuilder builder = new StringBuilder();
				String[] words = line.split(" ");

				for (String word : words) {
					word += " ";
					width += Core.getPaint().measureText(word);

					if (width > timeLine.getWidth() - 130) {
						lines.add(builder.toString());
						builder = new StringBuilder();
						width = (int) Core.getPaint().measureText(word);
					}
					builder.append(word);
				}

				lines.add(builder.toString());
			}

			this.notes = new String[lines.size()];
			lines.toArray(this.notes);
		} else {
			this.notes = null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TimeBlockView)) {
			return false;
		}
		TimeBlockView other = (TimeBlockView) obj;
		if (block == null) {
			if (other.block != null) {
				return false;
			}
		} else if (!block.equals(other.block)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((block == null) ? 0 : block.hashCode());
		return result;
	}

	/**
	 * @return generated height
	 */
	private int generateHeight() {
		return (int) (block.getDuration() / TimeConstants.HOUR * TimeLineView.STEP);
	}

	/**
	 * @return true if is dragging
	 */
	public boolean isDragging() {
		return isDragging;
	}

	/**
	 * Starts drag mode of view
	 *
	 * @param dragStartLocation drag start location
	 */
	public void startDragging(Location2D dragStartLocation, boolean deselectAfterDragging) {
		if (isSelected) {
			isDragging = true;
			this.dragStartLocation = dragStartLocation;
			this.deselectAfterDragging = deselectAfterDragging;
		}
	}

	/**
	 * Stops drag mode of view
	 */
	public void stopDragging() {
		isDragging = false;
		if (deselectAfterDragging) {
			setSelected(false);
		}
	}

	/**
	 * @return the time block
	 */
	public TimeBlock getBlock() {
		return block;
	}

	/**
	 * @return true if is selected
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @param isSelected true if block must be selected
	 */
	public void setSelected(final boolean isSelected) {
		// stopping dragging if unselected
		if (!isSelected) {
			isDragging = false;
		}

		// deselecting other if selected
		if (isSelected) {
			while (Core.getTimeLinesLeader().getEditingBlock() != null) {
				Core.getTimeLinesLeader().getEditingBlock().setSelected(false);
			}
		}

		// removing CAB if it exists and showing CAB if view is selected
		Core.getSyncActionQueue().addAction(new Runnable() {
			public void run() {
				if (actionMode != null) {
					actionMode.finish();
				}
				if (isSelected) {
					Core.getMainActivity().startActionMode(new MyActionMode());
				}
			}
		});

		// done!
		this.isSelected = isSelected;
		timeLine.postInvalidate();
	}

	/**
	 * @return the drag start location
	 */
	public Location2D getDragStartLocation() {
		return dragStartLocation;
	}

	/**
	 * @return the y
	 */
	public int getY() {
		return positionAnimation == null ? y : positionAnimation.nextY;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return sizeAnimation == null ? height : sizeAnimation.nextHeight;
	}

	private class PositionAnimation implements Runnable {
		public final int nextY = generateY();

		@Override
		public void run() {
			if (nextY != y) {
				// if view is away from screen bounds, block way will be shorted
				if (y < timeLine.getScrollY() - getHeight() && nextY > timeLine.getScrollY() - getHeight()) {
					y = timeLine.getScrollY() - getHeight();
				} else if (y > timeLine.getScrollY() + timeLine.getHeight() && nextY < timeLine.getScrollY() + timeLine.getHeight()) {
					y = timeLine.getScrollY() + timeLine.getHeight();
				}

				// calculating y
				int multiplier = (nextY - y) / 8;
				if (multiplier > 32) {
					multiplier = 32;
				} else if (multiplier < -32) {
					multiplier = -32;
				} else if (multiplier == 0) {
					multiplier = nextY > y ? 1 : -1;
				}
				if (positionAnimation == this) {
					y += multiplier;
					timeLine.postInvalidate();
					Core.getSyncActionQueue().addAction(this, 16);
				}
			}
		}
	}

	private class SizeAnimation implements Runnable {
		public final int nextHeight = generateHeight();

		@Override
		public void run() {
			if (nextHeight != height) {
				// calculating height
				int multiplier = (nextHeight - height) / 8;
				if (multiplier > 32) {
					multiplier = 32;
				} else if (multiplier < -32) {
					multiplier = -32;
				} else if (multiplier == 0) {
					multiplier = nextHeight > height ? 1 : -1;
				}
				if (sizeAnimation == this) {
					height += multiplier;
					timeLine.postInvalidate();
					Core.getSyncActionQueue().addAction(this, 16);
				}
			}
		}
	}

	private class MyActionMode implements ActionMode.Callback {
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			mode.getMenuInflater().inflate(R.menu.block_edit, menu);
			mode.setTitle(Core.getMainActivity().getActionBar().getTitle());
			actionMode = mode;
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.action_block_edit:
					EditBlockActivity.showEdit(block);
					break;
				case R.id.action_block_delete:
					block.remove();
					new PopupBar(null, new PopupBar.PopupBarElement() { // message
						@Override
						public CharSequence getTitle() {
							return Core.getContext().getResources().getString(R.string.deleted);
						}
					}, new PopupBar.PopupBarElement() { // button
						@Override
						public boolean onClick() {
							long diff = DateUtils.trimToDay(block.getStart()) - DateUtils.trimToDay(Core.getMainActivity().getCurrentTimeLine().getTime());
							Core.getMainActivity().getViewPager().setCurrentItem(Core.getMainActivity().getViewPager().getCurrentItem() + (int) (diff / TimeConstants.DAY));
							block.add();
							return true;
						}

						@Override
						public CharSequence getTitle() {
							return Core.getContext().getResources().getString(R.string.undo);
						}

						@Override
						public int getDrawable() {
							return R.drawable.ic_undo_light;
						}

						@Override
						public boolean isDrawableNearCenter() {
							return true;
						}
					}
					).show();
					break;
			}
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			actionMode = null;
			setSelected(false);
		}
	}

	private class AlphaAnimation implements Runnable {
		public final boolean toFullyVisible;
		public final Runnable onStop;

		public AlphaAnimation(boolean toFullyVisible, Runnable onStop) {
			this.toFullyVisible = toFullyVisible;
			this.onStop = onStop;
		}

		@Override
		public void run() {
			alpha += toFullyVisible ? 32 : -32;
			timeLine.postInvalidate();
			if (alphaAnimation == this && alpha > 0 && alpha < 255) {
				Core.getSyncActionQueue().addAction(this, 1);
			} else {
				Core.getSyncActionQueue().removeAction(this);
				if (onStop != null) {
					onStop.run();
				}
				if (alpha < 0) {
					alpha = 0;
				} else if (alpha > 255) {
					alpha = 255;
				}
			}
		}
	}
}
