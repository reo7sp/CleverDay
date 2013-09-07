package reo7sp.cleverday.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;

/**
 * Created by reo7sp on 8/1/13 at 9:08 PM
 */
public class LocalDataStorage extends DataStorage implements DataBaseConstants {
	private final SQLiteDatabase db = dataCenter.getDB();

	LocalDataStorage(DataCenter dataCenter) {
		super(dataCenter);
	}

	@Override
	void load() {
		createTableInDB();

		Cursor cursor = db.rawQuery("SELECT * FROM " + TIME_BLOCKS_TABLE_NAME, null);
		if (cursor.moveToFirst()) {
			do {
				TimeBlock block = new TimeBlock(cursor.getInt(0));
				block.setTitle(cursor.getString(1));
				block.setBounds(cursor.getLong(2), cursor.getLong(3), true);
				block.setColor(cursor.getInt(4));
				block.setReminder(cursor.getInt(5) != 0);
				block.setNotes(cursor.getString(6));
				block.setGoogleSyncID(cursor.getLong(7));

				if (!timeBlocks.add(block)) {
					removeFromDB(block);
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
		for (Iterator<TimeBlock> iterator = timeBlocks.iterator(); iterator.hasNext(); ) {
			TimeBlock block = iterator.next();

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
				case REMOVE:
					removeFromDB(block);
					iterator.remove();
					break;
			}
		}
	}

	@Override
	protected ActionOnSyncProblem actionOnSyncProblem() {
		return ActionOnSyncProblem.REMOVE_COMPLETELY;
	}

	private void addToDB(TimeBlock block) {
		if (isInDB(block)) {
			updateInDB(block);
			return;
		}

		ContentValues values = new ContentValues();
		values.put(KEY_ID, block.getID());
		values.put(KEY_TITLE, block.getTitle());
		values.put(KEY_START, block.getUtcStart());
		values.put(KEY_END, block.getUtcEnd());
		values.put(KEY_COLOR, block.getColor());
		values.put(KEY_HAS_REMINDER, block.hasReminder());
		values.put(KEY_NOTES, block.getNotes());
		values.put(KEY_GOOGLE_SYNC_ID, block.getGoogleSyncID());

		db.insert(TIME_BLOCKS_TABLE_NAME, null, values);
	}

	private void updateInDB(TimeBlock block) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, block.getTitle());
		values.put(KEY_START, block.getUtcStart());
		values.put(KEY_END, block.getUtcEnd());
		values.put(KEY_COLOR, block.getColor());
		values.put(KEY_HAS_REMINDER, block.hasReminder());
		values.put(KEY_NOTES, block.getNotes());
		values.put(KEY_GOOGLE_SYNC_ID, block.getGoogleSyncID());

		db.update(TIME_BLOCKS_TABLE_NAME, values, KEY_ID + " = " + block.getID(), null);
	}

	private void removeFromDB(TimeBlock block) {
		db.delete(TIME_BLOCKS_TABLE_NAME, KEY_ID + " = " + block.getID(), null);
	}

	private boolean isInDB(TimeBlock block) {
		Cursor cursor = db.rawQuery("SELECT * FROM " + TIME_BLOCKS_TABLE_NAME + " WHERE " + KEY_ID + " = " + block.getID(), null);
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}

	private void createTableInDB() {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + TIME_BLOCKS_TABLE_NAME + " (" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TITLE + " TEXT, " + KEY_START + " LONG, " + KEY_END + " LONG, " + KEY_COLOR + " INTEGER, " + KEY_HAS_REMINDER + " INTEGER, " + KEY_NOTES + " TEXT, " + KEY_GOOGLE_SYNC_ID + " LONG)");
	}
}
