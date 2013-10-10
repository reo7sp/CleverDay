package reo7sp.cleverday.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

import reo7sp.cleverday.Core;

/**
 * Created by reo7sp on 10/7/13 at 8:44 PM
 */
public class SyncQueue implements DBConstants {
	private final Collection<Commit> commits = new CopyOnWriteArrayList<Commit>();
	private final Collection<Commit> immutableCommits = Collections.unmodifiableCollection(commits);

	synchronized void load() {
		commits.clear();
		createTableInDB();

		Cursor cursor = getDB().rawQuery("SELECT * FROM " + SYNC_QUEUE_TABLE_NAME, null);
		if (cursor.moveToFirst()) {
			do {
				Commit commit = new Commit();
				commit.id = cursor.getLong(0);
				commit.changes = new boolean[] {false, cursor.getInt(1) != 0, cursor.getInt(2) != 0, cursor.getInt(3) != 0, cursor.getInt(4) != 0, cursor.getInt(5) != 0, cursor.getInt(6) != 0, false};
				commit.googleSyncID = cursor.getLong(7);
				commit.dead = cursor.getInt(8) != 0;
				commit.googleSynced = cursor.getInt(9) != 0;
				addCommit(commit);
			} while (cursor.moveToNext());
		}
		cursor.close();
	}

	synchronized void save() {
		for (Commit commit : commits) {
			updateToDB(commit);
		}
	}

	private void addToDB(Commit commit) {
		ContentValues values = new ContentValues();
		values.put(KEY_ID, commit.id);
		values.put(KEY_TITLE, commit.changes[TimeBlock.TITLE_VALUE_ID]);
		values.put(KEY_START, commit.changes[TimeBlock.START_VALUE_ID]);
		values.put(KEY_END, commit.changes[TimeBlock.END_VALUE_ID]);
		values.put(KEY_COLOR, commit.changes[TimeBlock.COLOR_VALUE_ID]);
		values.put(KEY_REMINDER, commit.changes[TimeBlock.REMINDER_VALUE_ID]);
		values.put(KEY_NOTES, commit.changes[TimeBlock.NOTES_VALUE_ID]);
		values.put(KEY_GOOGLE_SYNC_ID, commit.googleSyncID);
		values.put(KEY_DEAD, commit.dead);
		values.put(KEY_GOOGLE_SYNCED, commit.googleSynced);

		getDB().insert(SYNC_QUEUE_TABLE_NAME, null, values);
	}

	private void updateToDB(Commit commit) {
		if (!isInDB(commit)) {
			addToDB(commit);
			return;
		}

		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, commit.changes[TimeBlock.TITLE_VALUE_ID]);
		values.put(KEY_START, commit.changes[TimeBlock.START_VALUE_ID]);
		values.put(KEY_END, commit.changes[TimeBlock.END_VALUE_ID]);
		values.put(KEY_COLOR, commit.changes[TimeBlock.COLOR_VALUE_ID]);
		values.put(KEY_REMINDER, commit.changes[TimeBlock.REMINDER_VALUE_ID]);
		values.put(KEY_NOTES, commit.changes[TimeBlock.NOTES_VALUE_ID]);
		values.put(KEY_GOOGLE_SYNC_ID, commit.googleSyncID);
		values.put(KEY_DEAD, commit.dead);
		values.put(KEY_GOOGLE_SYNCED, commit.googleSynced);

		getDB().update(SYNC_QUEUE_TABLE_NAME, values, KEY_ID + " = " + commit.id, null);
	}

	private boolean isInDB(Commit commit) {
		Cursor cursor = getDB().rawQuery("SELECT * FROM " + SYNC_QUEUE_TABLE_NAME + " WHERE " + KEY_ID + " = " + commit.id, null);
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}

	private void removeFromDB(Commit commit) {
		getDB().delete(SYNC_QUEUE_TABLE_NAME, KEY_ID + " = " + commit.id, null);
	}

	private void createTableInDB() {
		getDB().execSQL("CREATE TABLE IF NOT EXISTS " + SYNC_QUEUE_TABLE_NAME + " (" + KEY_ID + " LONG PRIMARY KEY, " + KEY_TITLE + " INTEGER, " + KEY_START + " INTEGER, " + KEY_END + " INTEGER, " + KEY_COLOR + " INTEGER, " + KEY_REMINDER + " INTEGER, " + KEY_NOTES + " INTEGER, " + KEY_GOOGLE_SYNC_ID + " INTEGER, " + KEY_DEAD + " INTEGER, " + KEY_GOOGLE_SYNCED + " INTEGER)");
	}

	void newCommit(TimeBlock block) {
		addCommit(new Commit(block));
	}

	private void addCommit(Commit commit) {
		for (Commit commit1 : commits) {
			if (commit1.id == commit.id) {
				commits.remove(commit1);

				for (int i = 0, length = commit.changes.length; i < length; i++) {
					if (commit1.changes[i]) {
						commit.changes[i] = true;
					}
				}

				break;
			}
		}
		commits.add(commit);

		if (false && Core.getGoogleCalendarStorage().getMainCalendar() == null) {
			commit.setSynced(Core.getGoogleCalendarStorage());
		}
	}

	Commit getCommit(long id) {
		for (Commit commit : commits) {
			if (commit.id == id) {
				return commit;
			}
		}
		return null;
	}

	/**
	 * @return the immutable copy of commits
	 */
	Collection<Commit> getCommits() {
		return immutableCommits;
	}

	private SQLiteDatabase getDB() {
		return Core.getDataCenter().getDB();
	}

	public class Commit {
		private long id;
		private boolean[] changes;
		private boolean dead;
		private long googleSyncID;
		private boolean googleSynced;

		private Commit() {
		}

		private Commit(TimeBlock block) {
			id = block.getID();
			changes = block.whatWasChanged();
			googleSyncID = block.getGoogleSyncID();
			dead = block.isDead();
		}

		synchronized void setSynced(ExternalDataStorage storage) {
			if (storage instanceof GoogleCalendarStorage) {
				googleSynced = true;
			}

			if (googleSynced) {
				commits.remove(this);
				removeFromDB(this);
			} else {
				updateToDB(this);
			}
		}

		long getID() {
			return id;
		}

		boolean[] getChanges() {
			return changes;
		}

		boolean isDead() {
			return dead;
		}

		long getGoogleSyncID() {
			return googleSyncID;
		}

		synchronized boolean isSynced(ExternalDataStorage storage) {
			if (storage instanceof GoogleCalendarStorage) {
				return googleSynced;
			} else {
				throw new UnsupportedOperationException("Can't handle " + storage.getClass());
			}
		}
	}
}
