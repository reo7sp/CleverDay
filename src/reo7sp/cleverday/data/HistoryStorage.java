package reo7sp.cleverday.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import reo7sp.cleverday.Core;

/**
 * Created by reo7sp on 8/1/13 at 9:09 PM
 */
public class HistoryStorage extends LocalDataStorage implements DBConstants {
	private static HistoryStorage instance;
	private final Collection<TimeBlock> timeBlocks = new ArrayList<TimeBlock>();
	private final Collection<TimeBlock> immutableTimeBlocks = Collections.unmodifiableCollection(timeBlocks);

	HistoryStorage() {
		instance = this;
	}

	/**
	 * Singleton method
	 *
	 * @return the instance
	 */
	public static HistoryStorage getInstance() {
		return instance;
	}

	/**
	 * @return the immutable copy of history
	 */
	public Collection<TimeBlock> getHistory() {
		return immutableTimeBlocks;
	}

	/**
	 * @param title title of the block
	 * @return block with specified title or null
	 */
	public TimeBlock getFromHistory(CharSequence title) {
		for (TimeBlock block : timeBlocks) {
			if (block.getTitle().equals(title)) {
				return block;
			}
		}
		return null;
	}

	/**
	 * @param block block to add
	 */
	public void addToHistory(TimeBlock block) {
		if (block.getTitle() == null) {
			return;
		}
		for (Iterator<TimeBlock> iterator = timeBlocks.iterator(); iterator.hasNext(); ) {
			TimeBlock block1 = iterator.next();
			if (block1.getTitle().equals(block.getTitle())) {
				iterator.remove();
				break;
			}
		}
		timeBlocks.add(block);
	}

	/**
	 * @param block block to remove
	 */
	public void removeFromHistory(TimeBlock block) {
		timeBlocks.remove(block);
	}

	/**
	 * @param title title of the block
	 */
	public void removeFromHistory(CharSequence title) {
		for (TimeBlock block : timeBlocks) {
			if (block.getTitle().equals(title)) {
				timeBlocks.remove(block);
			}
		}
	}

	@Override
	void load() {
		createTableInDB();

		Cursor cursor = getDB().rawQuery("SELECT * FROM " + HISTORY_TABLE_NAME, null);
		if (cursor.moveToFirst()) {
			do {
				TimeBlock block = new TimeBlock(cursor.getLong(0));
				block.setTitle(cursor.getString(1));
				block.setBounds(cursor.getLong(2), cursor.getLong(3), true);
				block.setColor(cursor.getInt(4));
				block.setReminder(cursor.getInt(5) != 0);
				block.setNotes(cursor.getString(6));
				addToHistory(block);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	@Override
	void save() {
		for (Iterator<TimeBlock> iterator = timeBlocks.iterator(); iterator.hasNext(); ) {
			TimeBlock block = iterator.next();
			if (block.getTitle() == null) {
				iterator.remove();
				removeFromDB(block.getID());
				continue;
			}
			updateInDB(block);
		}
	}

	@Override
	void remove(TimeBlock block) {
		// nothing
	}

	private void addToDB(TimeBlock block) {
		if (block.getTitle() == null) { // if time block hasn't got title
			return;
		}

		if (getDBSize() > 100) { // if history is too big
			getDB().rawQuery("DROP TABLE " + HISTORY_TABLE_NAME, null);
			createTableInDB();
		}

		// finding similar block in history and removing it
		removeFromDB(getBlockIDFromDB(block.getTitle()));

		// inserting block
		ContentValues values = new ContentValues();
		values.put(KEY_ID, block.getID());
		values.put(KEY_TITLE, block.getTitle());
		values.put(KEY_START, block.getUtcStart());
		values.put(KEY_END, block.getUtcEnd());
		values.put(KEY_COLOR, block.getColor());
		values.put(KEY_REMINDER, block.hasReminder());
		values.put(KEY_NOTES, block.getNotes());

		getDB().insert(HISTORY_TABLE_NAME, null, values);
	}

	private void updateInDB(TimeBlock block) {
		if (!isInDB(block)) {
			addToDB(block);
			return;
		}

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, block.getTitle());
		values.put(KEY_START, block.getUtcStart());
		values.put(KEY_END, block.getUtcEnd());
		values.put(KEY_COLOR, block.getColor());
		values.put(KEY_REMINDER, block.hasReminder());
		values.put(KEY_NOTES, block.getNotes());

		getDB().update(HISTORY_TABLE_NAME, values, KEY_ID + " = " + block.getID(), null);
	}

	private void removeFromDB(long id) {
		getDB().delete(HISTORY_TABLE_NAME, KEY_ID + " = " + id, null);
	}

	private int getBlockIDFromDB(String title) {
		int id = 0;

		Cursor cursor = getDB().rawQuery("SELECT " + KEY_ID + " FROM " + HISTORY_TABLE_NAME + " WHERE " + KEY_TITLE + " = \"" + title + "\"", null);
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
		}
		cursor.close();

		return id;
	}

	private boolean isInDB(TimeBlock block) {
		Cursor cursor = getDB().rawQuery("SELECT * FROM " + HISTORY_TABLE_NAME + " WHERE " + KEY_ID + " = " + block.getID(), null);
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}

	private int getDBSize() {
		Cursor cursor = getDB().rawQuery("SELECT * FROM " + HISTORY_TABLE_NAME, null);
		int count = cursor.getCount();
		cursor.close();

		return count;
	}

	private void createTableInDB() {
		getDB().execSQL("CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE_NAME + " (" + KEY_ID + " LONG PRIMARY KEY, " + KEY_TITLE + " TEXT, " + KEY_START + " LONG, " + KEY_END + " LONG, " + KEY_COLOR + " INTEGER, " + KEY_REMINDER + " INTEGER, " + KEY_NOTES + " TEXT)");
	}

	private SQLiteDatabase getDB() {
		return Core.getDataCenter().getDB();
	}
}
