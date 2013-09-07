package reo7sp.cleverday.data;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import reo7sp.cleverday.Core;

/**
 * Created by reo7sp on 8/1/13 at 9:17 PM
 */
public class DataBaseCommunicator extends SQLiteOpenHelper implements DataBaseConstants {
	private static final int VERSION = 1;
	private static final String DATABASE_NAME = "CleverDayData";
	private final SQLiteDatabase db;

	DataBaseCommunicator() {
		super(Core.getContext(), DATABASE_NAME, null, VERSION);
		db = getWritableDatabase();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	/**
	 * @return the writable sqlite db instance
	 */
	public SQLiteDatabase getDB() {
		return db;
	}
}
