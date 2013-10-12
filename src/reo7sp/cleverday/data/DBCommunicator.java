package reo7sp.cleverday.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import reo7sp.cleverday.Core;

/**
 * Created by reo7sp on 8/1/13 at 9:17 PM
 */
public class DBCommunicator extends SQLiteOpenHelper implements DBConstants {
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "CleverDayData";
	private final SQLiteDatabase db;

	DBCommunicator() {
		super(Core.getContext(), DATABASE_NAME, null, VERSION);
		db = getReadableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public synchronized SQLiteDatabase getDB() {
		return db;
	}
}
