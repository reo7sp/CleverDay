package reo7sp.cleverday.data;

import java.util.Date;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.ui.colors.SimpleColor;
import reo7sp.cleverday.utils.DateUtils;

/**
 * Created by reo7sp on 8/1/13 at 1:56 PM
 */
public class TimeBlock implements TimeConstants {
	private final int id;
	private String title;
	private long start;
	private long end;
	private int color = SimpleColor.getRandomColor();
	private boolean reminder;
	private String notes;
	private long googleSyncID = -1;
	private ModifyType modifyType = ModifyType.ADD;

	public TimeBlock() {
		this(Core.getRandom().nextInt());
	}

	TimeBlock(int id) {
		this.id = id;
		setBounds(System.currentTimeMillis(), System.currentTimeMillis() + HOUR, false);
		DataCenter.getInstance().invalidate();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		TimeBlock timeBlock = (TimeBlock) o;

		return id == timeBlock.id;
	}

	@Override
	public int hashCode() {
		int result = id;
		result = 31 * result + (title != null ? title.hashCode() : 0);
		result = 31 * result + (int) (start ^ (start >>> 32));
		result = 31 * result + (int) (end ^ (end >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return getHumanTitle() + " (" + DateUtils.FORMAT_HOUR_MINUTE.format(new Date(getStart())) + " - " + DateUtils.FORMAT_HOUR_MINUTE.format(new Date(getEnd())) + ")";
	}

	/**
	 * @return the id of this time block
	 */
	public int getID() {
		return id;
	}

	/**
	 * @return the title. Can return null
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title title to set
	 */
	public void setTitle(String title) {
		if (title == null) {
			this.title = null;
		} else if (title.isEmpty() || title.equals(Core.getContext().getResources().getString(R.string.untitled_block))) {
			this.title = null;
		} else {
			this.title = title.trim();
		}
		markToUpdate();
	}

	/**
	 * @return the title which can't be null
	 */
	public String getHumanTitle() {
		return title == null ? Core.getContext().getResources().getString(R.string.untitled_block) : title;
	}

	/**
	 * @param start is start time
	 * @param utc   is utc time
	 * @return time
	 */
	public long getTime(boolean start, boolean utc) {
		if (start) {
			if (utc) {
				return getUtcStart();
			} else {
				return getStart();
			}
		} else if (utc) {
			return getUtcEnd();
		} else {
			return getEnd();
		}
	}

	private void repairBounds() {
		// position
		if (start > end) {
			start = end - -getDuration();
		}

		// size
		if (getDuration() < HALF_OF_HOUR) {
			end = start + HALF_OF_HOUR;
		} else if (getDuration() > DAY - QUARTER_OF_HOUR) {
			end = start + (DAY - QUARTER_OF_HOUR);
		}

		// different days
		if (!DateUtils.isInOneDay(getStart(), getEnd())) { // utc time isn't used here
			setBounds(DateUtils.trimToDay(getStart()) + 23 * HOUR + 50 * MINUTE - getDuration(),
					DateUtils.trimToDay(getStart()) + 23 * HOUR + 50 * MINUTE,
					false);
		}
	}

	/**
	 * @param start time to set
	 * @param end   time to set
	 * @param utc   is time in UTC
	 */
	public void setBounds(long start, long end, boolean utc) {
		long nextStart = start / QUARTER_OF_HOUR * QUARTER_OF_HOUR;
		long nextEnd = end / QUARTER_OF_HOUR * QUARTER_OF_HOUR;

		if (!utc) {
			nextStart = DateUtils.getUtcTime(nextStart);
			nextEnd = DateUtils.getUtcTime(nextEnd);
		}
		if (nextStart == this.start && nextEnd == this.end) {
			return;
		}

		this.start = nextStart;
		this.end = nextEnd;

		repairBounds();
		markToUpdate();
	}

	/**
	 * @param time      time to set
	 * @param startTime is it start time
	 * @param utc       is time in UTC
	 */
	public void setTime(long time, boolean startTime, boolean utc) {
		long next = time / QUARTER_OF_HOUR * QUARTER_OF_HOUR;

		if (!utc) {
			next = DateUtils.getUtcTime(next);
		}
		if (next == (startTime ? start : end)) {
			return;
		}

		if (startTime) {
			start = next;
		} else {
			end = next;
		}

		repairBounds();
		markToUpdate();
	}

	/**
	 * @return start time in UTC
	 */
	public long getUtcStart() {
		return start;
	}

	/**
	 * @param time utc start time to set
	 */
	public void setUtcStart(long time) {
		setTime(time, true, true);
	}

	/**
	 * @return start time in local time
	 */
	public long getStart() {
		return DateUtils.getLocalTime(start);
	}

	/**
	 * @param time local start time to set
	 */
	public void setStart(long time) {
		setTime(time, true, false);
	}

	/**
	 * @return end time in utc
	 */
	public long getUtcEnd() {
		return end;
	}

	/**
	 * @param time utc end time to set
	 */
	public void setUtcEnd(long time) {
		setTime(time, false, true);
	}

	/**
	 * @return end time in local time
	 */
	public long getEnd() {
		return DateUtils.getLocalTime(end);
	}

	/**
	 * @param time local end time to set
	 */
	public void setEnd(long time) {
		setTime(time, false, false);
	}

	/**
	 * @return the duration in milliseconds
	 */
	public long getDuration() {
		return end - start;
	}

	/**
	 * @return the color
	 */
	public int getColor() {
		return color;
	}

	/**
	 * @param color color to set. If color isn't one of {@link reo7sp.cleverday.ui.colors.SimpleColor}, time block color will be one of {@link reo7sp.cleverday.ui.colors.SimpleColor} colors
	 */
	public void setColor(int color) {
		if (this.color == color) {
			return;
		}
		if (SimpleColor.contains(color)) {
			this.color = color;
		} else {
			this.color = SimpleColor.getRandomColor();
		}
		markToUpdate();
	}

	/**
	 * @return true if time block has reminder
	 */
	public boolean hasReminder() {
		return reminder;
	}

	/**
	 * @param reminder if true, time block will have reminder
	 */
	public void setReminder(boolean reminder) {
		if (this.reminder == reminder) {
			return;
		}
		this.reminder = reminder;
		markToUpdate();
	}

	/**
	 * @return the notes. Can return null
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * @param notes notes to set
	 */
	public void setNotes(String notes) {
		if (notes == null) {
			this.notes = null;
		} else if (notes.isEmpty()) {
			this.notes = null;
		} else {
			this.notes = notes.trim();
		}
		markToUpdate();
	}

	/**
	 * @return the id of event in google calendar
	 */
	public long getGoogleSyncID() {
		return googleSyncID;
	}

	/**
	 * @param googleSyncID google calendar event id to set
	 */
	public void setGoogleSyncID(long googleSyncID) {
		if (this.googleSyncID == googleSyncID) {
			return;
		}
		this.googleSyncID = googleSyncID;
		markToUpdate();
	}

	/**
	 * @return why this block is modified. If block is not modified, it will return null
	 */
	public ModifyType getModifyType() {
		return modifyType;
	}

	/**
	 * Clears modify type variable
	 */
	void setSaved() {
		modifyType = null;
	}

	/**
	 * Sets add modify type variable
	 */
	void markToAdd() {
		modifyType = ModifyType.ADD;
		DataCenter.getInstance().invalidate();
	}

	/**
	 * Sets update modify type variable
	 */
	void markToUpdate() {
		if (modifyType == ModifyType.ADD || modifyType == ModifyType.REMOVE) {
			return;
		}
		modifyType = ModifyType.UPDATE;
		DataCenter.getInstance().invalidate();
	}

	/**
	 * Sets remove modify type variable
	 */
	void markToRemove() {
		modifyType = ModifyType.REMOVE;
		DataCenter.getInstance().invalidate();
	}

	/**
	 * Removes time block
	 */
	public void remove() {
		Core.getDataCenter().removeTimeBlock(this);
	}

	/**
	 * Adds time block
	 */
	public DataCenter.NotAddedCause add() {
		return Core.getDataCenter().addTimeBlock(this);
	}

	/**
	 * Moves time block bounds on specified milliseconds
	 *
	 * @param diff milliseconds
	 */
	public void move(long diff) {
		setBounds(start + diff, end + diff, true);
	}

	/**
	 * Creates copy of this time block
	 *
	 * @return copy of this time block
	 */
	public TimeBlock copy() {
		TimeBlock block = new TimeBlock(id);
		block.title = title;
		block.start = start;
		block.end = end;
		block.color = color;
		block.reminder = reminder;
		block.notes = notes;
		block.googleSyncID = googleSyncID;
		return block;
	}

	/**
	 * Created by reo7sp on 8/1/13 at 2:26 PM
	 */
	public static enum ModifyType {
		ADD,
		UPDATE,
		REMOVE,
	}
}
