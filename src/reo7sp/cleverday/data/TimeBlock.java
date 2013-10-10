package reo7sp.cleverday.data;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.DateFormatter;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.ui.colors.SimpleColor;
import reo7sp.cleverday.utils.DateUtils;

/**
 * Created by reo7sp on 8/1/13 at 1:56 PM
 */
public class TimeBlock implements TimeConstants {
	public static final int ID_VALUE_ID = 0;
	public static final int TITLE_VALUE_ID = 1;
	public static final int START_VALUE_ID = 2;
	public static final int END_VALUE_ID = 3;
	public static final int COLOR_VALUE_ID = 4;
	public static final int REMINDER_VALUE_ID = 5;
	public static final int NOTES_VALUE_ID = 6;
	public static final int GOOGLE_SYNC_ID_VALUE_ID = 7;
	private final LongValue id;
	private final StringValue title = new StringValue(this);
	private final LongValue start = new LongValue(this);
	private final LongValue end = new LongValue(this);
	private final IntValue color = new IntValue(this, SimpleColor.getRandomColor());
	private final BooleanValue reminder = new BooleanValue(this);
	private final StringValue notes = new StringValue(this);
	private final LongValue googleSyncID = new LongValue(this, -1);

	TimeBlock(long id) {
		this.id = new LongValue(this, id);
		setBounds(System.currentTimeMillis(), System.currentTimeMillis() + HOUR, false);
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
		return id.hashCode();
	}

	@Override
	public String toString() {
		return getHumanTitle() + " (" + Core.getDateFormatter().format(DateFormatter.Format.HOUR_MINUTE, getStart()) + " - " + Core.getDateFormatter().format(DateFormatter.Format.HOUR_MINUTE, getEnd()) + ")";
	}

	/**
	 * @return the id of this time block
	 */
	public long getID() {
		return id.getValue();
	}

	/**
	 * @return the title. Can return null
	 */
	public String getTitle() {
		return title.getValue();
	}

	/**
	 * @param title title to set
	 */
	public void setTitle(String title) {
		if (title == null || title.isEmpty() || title.equals(Core.getContext().getResources().getString(R.string.untitled_block))) {
			this.title.setValue(null);
		} else {
			this.title.setValue(title.trim());
		}
	}

	/**
	 * @return the title which can't be null
	 */
	public String getHumanTitle() {
		return title.getValue() == null ? Core.getContext().getResources().getString(R.string.untitled_block) : title.getValue();
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
		if (start.getValue() > end.getValue()) {
			start.setValue(end.getValue() - -getDuration());
		}

		// size
		if (getDuration() < HALF_OF_HOUR) {
			end.setValue(start.getValue() + HALF_OF_HOUR);
		} else if (getDuration() > DAY - QUARTER_OF_HOUR) {
			end.setValue(start.getValue() + (DAY - QUARTER_OF_HOUR));
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
		if (nextStart == this.start.getValue() && nextEnd == this.end.getValue()) {
			return;
		}

		this.start.setValue(nextStart);
		this.end.setValue(nextEnd);

		repairBounds();
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
		if (next == (startTime ? start.getValue() : end.getValue())) {
			return;
		}

		if (startTime) {
			start.setValue(next);
		} else {
			end.setValue(next);
		}

		repairBounds();
	}

	/**
	 * @return start time in UTC
	 */
	public long getUtcStart() {
		return start.getValue();
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
		return DateUtils.getLocalTime(start.getValue());
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
		return end.getValue();
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
		return DateUtils.getLocalTime(end.getValue());
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
		return end.getValue() - start.getValue();
	}

	/**
	 * @return the color
	 */
	public int getColor() {
		return color.getValue();
	}

	/**
	 * @param color color to set. If color isn't one of {@link reo7sp.cleverday.ui.colors.SimpleColor}, time block color will be one of {@link reo7sp.cleverday.ui.colors.SimpleColor} colors
	 */
	public void setColor(int color) {
		if (this.color.getValue() == color) {
			return;
		}
		if (SimpleColor.contains(color)) {
			this.color.setValue(color);
		} else {
			this.color.setValue(SimpleColor.getRandomColor());
		}
	}

	/**
	 * @return true if time block has reminder
	 */
	public boolean hasReminder() {
		return reminder.getValue();
	}

	/**
	 * @param reminder if true, time block will have reminder
	 */
	public void setReminder(boolean reminder) {
		if (this.reminder.getValue() == reminder) {
			return;
		}
		this.reminder.setValue(reminder);
	}

	/**
	 * @return the notes. Can return null
	 */
	public String getNotes() {
		return notes.getValue();
	}

	/**
	 * @param notes notes to set
	 */
	public void setNotes(String notes) {
		if (notes == null || notes.isEmpty()) {
			this.notes.setValue(null);
		} else {
			this.notes.setValue(notes.trim());
		}
	}

	/**
	 * @return the id of event in google calendar
	 */
	public long getGoogleSyncID() {
		return googleSyncID.getValue();
	}

	/**
	 * @param googleSyncID google calendar event id to set
	 */
	public void setGoogleSyncID(long googleSyncID) {
		if (this.googleSyncID.getValue() == googleSyncID) {
			return;
		}
		this.googleSyncID.setValue(googleSyncID);
	}

	/**
	 * Notifies all variables that they has been saved
	 */
	void setSaved() {
		title.setSaved();
		start.setSaved();
		end.setSaved();
		color.setSaved();
		reminder.setSaved();
		notes.setSaved();
		googleSyncID.setSaved();
	}

	/**
	 * Returns array which shows what was changed
	 *
	 * @return array which shows what was changed
	 */
	boolean[] whatWasChanged() {
		return new boolean[] {false, title.isChanged(), start.isChanged(), end.isChanged(), color.isChanged(), reminder.isChanged(), notes.isChanged(), googleSyncID.isChanged()};
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
		setBounds(start.getValue() + diff, end.getValue() + diff, true);
	}

	/**
	 * Checks if something was changed and not saved
	 *
	 * @return true if something was changed and not saved
	 */
	boolean isChanged() {
		return title.isChanged() || start.isChanged() || end.isChanged() || color.isChanged() || reminder.isChanged() || notes.isChanged() || googleSyncID.isChanged();
	}

	/**
	 * Checks if time block is dead
	 *
	 * @return true if time block is dead
	 */
	boolean isDead() {
		return !Core.getDataCenter().getTimeBlocks().contains(this);
	}

	/**
	 * Creates copy of this time block
	 *
	 * @return copy of this time block
	 */
	public TimeBlock copy() {
		TimeBlock block = new TimeBlock(id.getValue());
		block.title.setValue(title.getValue());
		block.start.setValue(start.getValue());
		block.end.setValue(end.getValue());
		block.color.setValue(color.getValue());
		block.reminder.setValue(reminder.getValue());
		block.notes.setValue(notes.getValue());
		block.googleSyncID.setValue(googleSyncID.getValue());
		return block;
	}
}
