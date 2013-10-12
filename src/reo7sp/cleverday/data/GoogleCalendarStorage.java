package reo7sp.cleverday.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.Collection;
import java.util.HashSet;
import java.util.TimeZone;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.ui.colors.SimpleColor;
import reo7sp.cleverday.utils.DateUtils;

/**
 * Created by reo7sp on 8/1/13 at 9:08 PM
 */
public class GoogleCalendarStorage extends ExternalDataStorage {
	private static final String[] CALENDAR_REQUEST_PROJECTION = new String[] {
			CalendarContract.Calendars._ID,
			CalendarContract.Calendars.ACCOUNT_NAME,
			CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
	};
	private static final String[] EVENT_REQUEST_PROJECTION = new String[] {
			CalendarContract.Events._ID,
			CalendarContract.Events.TITLE,
			CalendarContract.Events.DESCRIPTION,
			CalendarContract.Events.DTSTART,
			CalendarContract.Events.DTEND,
			CalendarContract.Events.EVENT_TIMEZONE,
			CalendarContract.Events.EVENT_COLOR,
	};
	private static GoogleCalendarStorage instance;
	private final ContentResolver contentResolver = Core.getContext().getContentResolver();
	private final Collection<GoogleCalendar> calendars = new HashSet<GoogleCalendar>();
	private GoogleCalendar mainCalendar;

	GoogleCalendarStorage() {
		instance = this;
		receiveCalendars();
		updateSettings();
	}

	/**
	 * Singleton method
	 *
	 * @return the instance
	 */
	public static GoogleCalendarStorage getInstance() {
		return instance;
	}

	/**
	 * @return all google calendars
	 */
	public Collection<GoogleCalendar> getCalendars() {
		return instance.calendars;
	}

	/**
	 * @return the current main calendar
	 */
	public GoogleCalendar getMainCalendar() {
		return instance.mainCalendar;
	}

	/**
	 * Updates main calendar
	 */
	public void updateSettings() {
		String calendarName = Core.getPreferences().getString("pref_google_calendar", "none");

		if (mainCalendar == null || !calendarName.equals("" + mainCalendar.getID())) {
			int id = -1;
			try {
				id = Integer.parseInt(calendarName);
			} catch (Exception ignored) {
			}

			if (mainCalendar != null) {
				for (SyncQueue.Commit commit : Core.getDataCenter().getSyncQueue().getCommits()) {
					commit.setSynced(this);
				}
			}

			mainCalendar = null;
			if (id != -1) {
				for (GoogleCalendar calendar : calendars) {
					if (calendar.getID() == id) {
						mainCalendar = calendar;
						break;
					}
				}
			}

			if (mainCalendar != null) {
				Core.getDataCenter().syncData();
			}
		}
	}

	@Override
	void receive() {
		if (!canReceiveFromGoogle() || mainCalendar == null) {
			return;
		}

		Cursor cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI, EVENT_REQUEST_PROJECTION, CalendarContract.Events.CALENDAR_ID + " = " + mainCalendar.getID(), null, null);
		while (cursor.moveToNext()) {
			long id = cursor.getLong(0);
			String title = cursor.getString(1);
			String description = cursor.getString(2);
			long start = cursor.getLong(3);
			long end = cursor.getLong(4);
			TimeZone timeZone = TimeZone.getTimeZone(cursor.getString(5));
			int color = cursor.getInt(6);

			SyncQueue.Commit commit = getCommitWithGoogleSyncID(id);
			if (commit != null && commit.isDead() && !commit.isSynced(this)) {
				continue;
			}

			TimeBlock block = getBlockWithGoogleSyncID(id);
			if (block == null) {
				block = Core.getDataCenter().newTimeBlock();
				block.setTitle(title);
				block.setNotes(description);
				block.setBounds(DateUtils.getUtcTime(start, timeZone), DateUtils.getUtcTime(end, timeZone), true);
				block.setGoogleSyncID(id);
				if (canUseCleverDayColors()) {
					block.setColor(color);
				}
			} else {
				if (commit == null || commit.isSynced(this)) {
					block.setTitle(title);
					block.setNotes(description);
					block.setBounds(DateUtils.getUtcTime(start, timeZone), DateUtils.getUtcTime(end, timeZone), true);
					if (canUseCleverDayColors() && SimpleColor.contains(color)) {
						block.setColor(color);
					}
				} else if (!commit.isDead()) {
					if (!commit.getChanges()[TimeBlock.TITLE_VALUE_ID]) {
						block.setTitle(title);
					}
					if (!commit.getChanges()[TimeBlock.NOTES_VALUE_ID]) {
						block.setNotes(description);
					}
					if (!commit.getChanges()[TimeBlock.START_VALUE_ID] && !commit.getChanges()[TimeBlock.END_VALUE_ID]) {
						block.setBounds(DateUtils.getUtcTime(start, timeZone), DateUtils.getUtcTime(end, timeZone), true);
					}
					if (!commit.getChanges()[TimeBlock.COLOR_VALUE_ID] && canUseCleverDayColors()) {
						block.setColor(color);
					}
				}
			}
		}
		cursor.close();
	}

	private SyncQueue.Commit getCommitWithGoogleSyncID(long id) {
		for (SyncQueue.Commit commit : Core.getDataCenter().getSyncQueue().getCommits()) {
			if (commit.getGoogleSyncID() == id) {
				return commit;
			}
		}
		return null;
	}

	private TimeBlock getBlockWithGoogleSyncID(long id) {
		for (TimeBlock block : Core.getDataCenter().getTimeBlocks()) {
			if (block.getGoogleSyncID() == id) {
				return block;
			}
		}
		return null;
	}

	@Override
	void send() {
		if (!canModifyGoogle()) {
			return;
		}

		for (SyncQueue.Commit commit : Core.getDataCenter().getSyncQueue().getCommits()) {
			if (commit.isSynced(this)) {
				continue;
			}

			if (commit.isDead()) {
				removeFromGoogle(commit.getGoogleSyncID());
			} else {
				updateInGoogle(commit);
			}
			commit.setSynced(this);
		}
	}

	private void receiveCalendars() {
		Cursor cursor = contentResolver.query(CalendarContract.Calendars.CONTENT_URI, CALENDAR_REQUEST_PROJECTION, null, null, null);
		while (cursor.moveToNext()) {
			long id = cursor.getLong(0);
			String owner = cursor.getString(1);
			String name = cursor.getString(2);

			GoogleCalendar calendar = new GoogleCalendar(id, owner, name);
			calendars.add(calendar);
		}
		cursor.close();
	}

	private void addToGoogle(TimeBlock block) {
		if (!canModifyGoogle() || mainCalendar == null || block == null) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(CalendarContract.Events.TITLE, block.getHumanTitle());
		values.put(CalendarContract.Events.DESCRIPTION, block.getNotes() == null ? "" : block.getNotes());
		values.put(CalendarContract.Events.DTSTART, block.getStart());
		values.put(CalendarContract.Events.DTEND, block.getEnd());
		values.put(CalendarContract.Events.CALENDAR_ID, mainCalendar.getID());
		values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		if (canUseCleverDayColors()) {
			values.put(CalendarContract.Events.EVENT_COLOR, block.getColor());
		}

		Uri uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values);
		long id = Long.parseLong(uri.getLastPathSegment());
		block.setGoogleSyncID(id);
	}

	private void updateInGoogle(SyncQueue.Commit commit) {
		if (!canModifyGoogle() || mainCalendar == null) {
			return;
		}

		TimeBlock block = Core.getDataCenter().getBlock(commit.getID());
		if (block == null) {
			return;
		}

		if (commit.getGoogleSyncID() == -1 || !isInGoogle(commit.getGoogleSyncID())) {
			addToGoogle(block);
			return;
		}

		ContentValues values = new ContentValues();
		if (commit.getChanges()[TimeBlock.TITLE_VALUE_ID]) {
			values.put(CalendarContract.Events.TITLE, block.getTitle());
		}
		if (commit.getChanges()[TimeBlock.NOTES_VALUE_ID]) {
			values.put(CalendarContract.Events.DESCRIPTION, block.getNotes());
		}
		if (commit.getChanges()[TimeBlock.START_VALUE_ID] || commit.getChanges()[TimeBlock.END_VALUE_ID]) {
			values.put(CalendarContract.Events.DTSTART, block.getStart());
			values.put(CalendarContract.Events.DTEND, block.getEnd());
			values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		}
		if (commit.getChanges()[TimeBlock.COLOR_VALUE_ID] && canUseCleverDayColors()) {
			values.put(CalendarContract.Events.EVENT_COLOR, block.getColor());
		}
		values.put(CalendarContract.Events.CALENDAR_ID, mainCalendar.getID());

		contentResolver.update(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, block.getGoogleSyncID()), values, null, null);
	}

	private void removeFromGoogle(long id) {
		if (!canModifyGoogle() || id == -1) {
			return;
		}

		contentResolver.delete(ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, id), null, null);
	}

	private boolean isInGoogle(long id) {
		Cursor cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI, EVENT_REQUEST_PROJECTION, CalendarContract.Events._ID + " = " + id, null, null);
		int count = cursor.getCount();
		cursor.close();

		return count > 0;
	}

	private boolean canModifyGoogle() {
		return Core.getPreferences().getBoolean("pref_modify_google", false);
	}

	private boolean canReceiveFromGoogle() {
		return Core.getPreferences().getBoolean("pref_receive_from_google", false);
	}

	private boolean canUseCleverDayColors() {
		return !Core.getPreferences().getBoolean("pref_google_event_color", false);
	}
}
