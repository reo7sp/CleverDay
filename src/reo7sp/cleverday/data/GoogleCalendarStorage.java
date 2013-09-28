package reo7sp.cleverday.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TimeZone;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.utils.DateUtils;

/**
 * Created by reo7sp on 8/1/13 at 9:08 PM
 */
public class GoogleCalendarStorage extends DataStorage {
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

	GoogleCalendarStorage(DataCenter dataCenter) {
		super(dataCenter);
		instance = this;
		receiveCalendars();
		update();
	}

	/**
	 * @return all google calendars
	 */
	public static Collection<GoogleCalendar> getCalendars() {
		return instance.calendars;
	}

	/**
	 * @return the current main calendar
	 */
	public static GoogleCalendar getMainCalendar() {
		return instance.mainCalendar;
	}

	/**
	 * Updates main calendar
	 */
	public static void updateSettings() {
		instance.update();
	}

	private synchronized void update() {
		String calendarPref = Core.getPreferences().getString("pref_google_calendar", "none");
		if (mainCalendar == null || !calendarPref.equals("" + mainCalendar.getID())) {
			int id = -1;
			try {
				id = Integer.parseInt(calendarPref);
			} catch (Exception ignored) {
			}
			mainCalendar = null;
			for (GoogleCalendar calendar : calendars) {
				if (calendar.getID() == id) {
					mainCalendar = calendar;
					break;
				}
			}
		}
	}

	@Override
	void load() {
		// nothing
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

			TimeBlock block = null;
			for (TimeBlock loopBlock : timeBlocks) {
				if (loopBlock.getGoogleSyncID() == id) {
					block = loopBlock;
					break;
				}
			}
			if (block == null) {
				block = new TimeBlock(Core.getRandom().nextInt());
				block.setGoogleSyncID(id);
			}

			if (block.getModifyType() != TimeBlock.ModifyType.UPDATE || block.getModifyType() != TimeBlock.ModifyType.REMOVE) {
				block.setTitle(title);
				block.setNotes(description);
				block.setBounds(DateUtils.getUtcTime(start, timeZone), DateUtils.getUtcTime(end, timeZone), true);
				if (canUseCleverDayColors()) {
					block.setColor(color);
				}
			}

			if (!timeBlocks.add(block)) {
				removeFromGoogle(id);
			}
		}
		cursor.close();
	}

	@Override
	void save() {
		if (!canModifyGoogle()) {
			return;
		}

		for (Iterator<TimeBlock> iterator = timeBlocks.iterator(); iterator.hasNext(); ) {
			TimeBlock block = iterator.next();

			if (block.getModifyType() == null) {
				continue;
			}
			switch (block.getModifyType()) {
				case ADD:
					addToGoogle(block);
					break;
				case UPDATE:
					updateInGoogle(block);
					break;
				case REMOVE:
					removeFromGoogle(block.getGoogleSyncID());
					iterator.remove();
					break;
			}
		}
	}

	@Override
	protected ActionOnSyncProblem actionOnSyncProblem() {
		return ActionOnSyncProblem.REMOVE_FROM_BUFFER;
	}

	@Override
	void syncDataCenterWithMe() {
		super.syncDataCenterWithMe();
		for (Iterator<TimeBlock> iterator = timeBlocks.iterator(); iterator.hasNext(); ) {
			TimeBlock block = iterator.next();

			if (block.getGoogleSyncID() != -1 && !isInGoogle(block.getGoogleSyncID())) {
				block.remove();
				iterator.remove();
			}
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
		if (isInGoogle(block.getGoogleSyncID())) {
			updateInGoogle(block);
			return;
		}

		if (!canModifyGoogle() || mainCalendar == null) {
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

	private void updateInGoogle(TimeBlock block) {
		if (block.getGoogleSyncID() == -1 || !isInGoogle(block.getGoogleSyncID())) {
			addToGoogle(block);
			return;
		}

		if (!canModifyGoogle() || block.getGoogleSyncID() == -1) {
			return;
		}

		ContentValues values = new ContentValues();
		values.put(CalendarContract.Events.TITLE, block.getTitle());
		values.put(CalendarContract.Events.DESCRIPTION, block.getNotes());
		values.put(CalendarContract.Events.DTSTART, block.getStart());
		values.put(CalendarContract.Events.DTEND, block.getEnd());
		values.put(CalendarContract.Events.CALENDAR_ID, mainCalendar.getID());
		values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
		if (canUseCleverDayColors()) {
			values.put(CalendarContract.Events.EVENT_COLOR, block.getColor());
		}

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
