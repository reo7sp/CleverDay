package reo7sp.cleverday.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Created by reo7sp on 8/1/13 at 9:09 PM
 */
public class HistoryStorage extends DataStorage implements DataBaseConstants {
	private static HistoryStorage instance;
	private final SQLiteDatabase db = dataCenter.getDB();
	private final Collection<TimeBlock> immutableTimeBlocks = Collections.unmodifiableCollection(timeBlocks);

	HistoryStorage(DataCenter dataCenter) {
		super(dataCenter);
		instance = this;
	}

	/**
	 * @return the immutable copy of history
	 */
	public static Collection<TimeBlock> getHistory() {
		return instance.immutableTimeBlocks;
	}

	/**
	 * @param title title of the block
	 * @return block with specified title or null
	 */
	public static TimeBlock getFromHistory(CharSequence title) {
		for (TimeBlock block : instance.timeBlocks) {
			if (block.getTitle().equals(title)) {
				return block;
			}
		}
		return null;
	}

	@Override
	void load() {
		createTableInDB();

		Cursor cursor = db.rawQuery("SELECT * FROM " + HISTORY_TABLE_NAME, null);
		if (cursor.moveToFirst()) {
			do {
				TimeBlock block = new TimeBlock(cursor.getInt(0));
				block.setTitle(cursor.getString(1));
				block.setBounds(cursor.getLong(2), cursor.getLong(3), true);
				block.setColor(cursor.getInt(4));
				block.setReminder(cursor.getInt(5) != 0);
				block.setNotes(cursor.getString(6));

				if (!timeBlocks.add(block)) {
					timeBlocks.remove(block);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	@Override
	void receive() {
		// nothing
	}

	@Override
	void save() {
		for (TimeBlock block : timeBlocks) {
			if (block.getModifyType() == null) {
				continue;
			}
			switch (block.getModifyType()) {
				case ADD:
					addToDB(block);
					break;
				case UPDATE:
					updateInDB(block);
					break;
			}
		}
	}

	@Override
	void syncMeWithDataCenter() {
		super.syncMeWithDataCenter();
		filterTimeBlocks();
	}

	@Override
	void syncDataCenterWithMe() {
		// nothing
	}

	private void filterTimeBlocks() {
		for (Iterator<TimeBlock> iterator = timeBlocks.iterator(); iterator.hasNext(); ) {
			TimeBlock block = iterator.next();

			if (block.getTitle() == null) {
				iterator.remove();
				continue;
			}
			for (TimeBlock loopBlock : timeBlocks) {
				if (loopBlock != block && block.getTitle().equals(loopBlock.getTitle())) {
					iterator.remove();
					break;
				}
			}
		}
	}

	@Override
	protected ActionOnSyncProblem actionOnSyncProblem() {
		return null;
	}

	private void addToDB(TimeBlock block) {
		if (isInDB(block)) {
			updateInDB(block);
			return;
		}

		if (block.getTitle() == null) { // if time block hasn't got title
			return;
		}

		if (getDBSize() > 100) { // if history is too big
			db.rawQuery("DROP TABLE " + HISTORY_TABLE_NAME, null);
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
		values.put(KEY_HAS_REMINDER, block.hasReminder());
		values.put(KEY_NOTES, block.getNotes());

		db.insert(HISTORY_TABLE_NAME, null, values);
	}

	private void updateInDB(TimeBlock block) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, block.getTitle());
		values.put(KEY_START, block.getUtcStart());
		values.put(KEY_END, block.getUtcEnd());
		values.put(KEY_COLOR, block.getColor());
		values.put(KEY_HAS_REMINDER, block.hasReminder());
		values.put(KEY_NOTES, block.getNotes());

		db.update(HISTORY_TABLE_NAME, values, KEY_ID + " = " + block.getID(), null);
	}

	private void removeFromDB(int id) {
		db.delete(HISTORY_TABLE_NAME, KEY_ID + " = " + id, null);
	}

	private int getBlockIDFromDB(String title) {
		int id = 0;

		Cursor cursor = db.rawQuery("SELECT " + KEY_ID + " FROM " + HISTORY_TABLE_NAME + " WHERE " + KEY_TITLE + " = \"" + title + "\"", null);
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
		}
		cursor.close();

		return id;
	}

	private boolean isInDB(TimeBlock block) {
		Cursor cursor = db.rawQuery("SELECT * FROM " + HISTORY_TABLE_NAME + " WHERE " + KEY_ID + " = " + block.getID(), null);
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}

	private int getDBSize() {
		Cursor cursor = db.rawQuery("SELECT * FROM " + HISTORY_TABLE_NAME, null);
		int count = cursor.getCount();
		cursor.close();

		return count;
	}

	private void createTableInDB() {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TITLE + " TEXT, " + KEY_START + " LONG, " + KEY_END + " LONG, " + KEY_COLOR + " INTEGER, " + KEY_HAS_REMINDER + " INTEGER, " + KEY_NOTES + " TEXT)");
	}
}
