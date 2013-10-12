package reo7sp.cleverday.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.log.Log;

/**
 * Created by reo7sp on 8/1/13 at 2:12 PM
 */
public class DataCenter {
	private static DataCenter instance;
	private final List<TimeBlock> timeBlocks = new ArrayList<TimeBlock>();
	private final List<TimeBlock> immutableTimeBlocks = Collections.unmodifiableList(timeBlocks);
	private final DBCommunicator dbCommunicator = new DBCommunicator();
	private final DataStorageLeader dataStorageLeader = new DataStorageLeader();
	private final Collection<DataInvalidateListener> invalidateListeners = new HashSet<DataInvalidateListener>();
	private final SyncQueue syncQueue = new SyncQueue();
	private final Comparator<TimeBlock> timeBlockComparator = new Comparator<TimeBlock>() {
		@Override
		public int compare(TimeBlock first, TimeBlock second) {
			return (int) (first.getUtcStart() - second.getUtcStart());
		}
	};

	private DataCenter() {
		startThread();
	}

	/**
	 * Singleton method with lazy initialization.
	 * It'll return null if core isn't built
	 *
	 * @return the instance
	 */
	public static DataCenter getInstance() {
		if (instance == null) {
			synchronized (DataCenter.class) {
				if (instance == null) {
					instance = new DataCenter();
				}
			}
		}
		return instance;
	}

	/**
	 * Creates new time block and adds it
	 *
	 * @return new time block
	 */
	public TimeBlock newTimeBlock() {
		TimeBlock block = new TimeBlock(Core.getRandom().nextLong());
		if (addTimeBlock(block) != null) {
			block = null;
		}
		return block;
	}

	/**
	 * Adds time block in collection. If time block wasn't added, it would return cause of it
	 *
	 * @param block time block to add
	 * @return why time block wasn't added. If everything is right, it will return null
	 */
	synchronized NotAddedCause addTimeBlock(final TimeBlock block) {
		if (System.currentTimeMillis() - block.getStart() > 3 * TimeConstants.DAY) {
			Log.w("Data", "Can't add time block \"" + block.getTitle() + "\" with id " + block.getID() + " to data center. Cause: " + NotAddedCause.TOO_OLD);
			return NotAddedCause.TOO_OLD;
		} else if (System.currentTimeMillis() - block.getStart() < -3 * TimeConstants.DAY) {
			Log.w("Data", "Can't add time block \"" + block.getTitle() + "\" with id " + block.getID() + " to data center. Cause: " + NotAddedCause.TOO_NEW);
			return NotAddedCause.TOO_NEW;
		}

		if (timeBlocks.contains(block)) {
			Log.w("Data", "Can't add time block \"" + block.getTitle() + "\" with id " + block.getID() + " to data center. Cause: " + NotAddedCause.ALREADY_IS);
			return NotAddedCause.ALREADY_IS;
		} else if (hasBlock(block.getID())) {
			Log.w("Data", "Can't add time block \"" + block.getTitle() + "\" with id " + block.getID() + " to data center. Cause: " + NotAddedCause.TWIN);
			return NotAddedCause.TWIN;
		}

		timeBlocks.add(block);
		syncQueue.newCommit(block);
		Core.getHistoryStorage().addToHistory(block);
		notifyDataInvalidateListeners();
		Log.i("Data", "Time block \"" + block.getTitle() + "\" with id " + block.getID() + " added! Collection size: " + timeBlocks.size());

		if (Core.getTimeLinesLeader() != null) {
			Core.getSyncActionQueue().addAction(new Runnable() {
				@Override
				public void run() {
					Core.getTimeLinesLeader().addTimeBlock(block);
				}
			});
		}

		return null;
	}

	/**
	 * Removes time block from collection
	 *
	 * @param block time block to remove
	 */
	synchronized void removeTimeBlock(final TimeBlock block) {
		timeBlocks.remove(block);
		dataStorageLeader.remove(block);
		syncQueue.newCommit(block);
		notifyDataInvalidateListeners();
		Log.i("Data", "Time block \"" + block.getTitle() + "\" with id " + block.getID() + " removed! Collection size: " + timeBlocks.size());

		if (Core.getTimeLinesLeader() != null) {
			Core.getSyncActionQueue().addAction(new Runnable() {
				@Override
				public void run() {
					Core.getTimeLinesLeader().removeTimeBlock(block);
				}
			});
		}
	}

	private void notifyDataInvalidateListeners() {
		for (DataInvalidateListener listener : invalidateListeners) {
			listener.onDataInvalidate();
		}
	}

	private void startThread() {
		new Thread("DataCenter") {
			@Override
			public void run() {
				try {
					synchronized (DataCenter.this) {
						dataStorageLeader.init();
						load();
						syncQueue.load();

						while (!isInterrupted()) {
							if (isDataChanged()) {
								dataStorageLeader.save();
								syncQueue.save();
								Collections.sort(timeBlocks, timeBlockComparator);
								notifyDataInvalidateListeners();
								saveAll();
							}

							DataCenter.this.wait(TimeConstants.SECOND);
						}
					}
				} catch (InterruptedException ignored) {
				}
			}
		}.start();
	}

	/**
	 * Loads all data from local data storages
	 */
	private void load() {
		Log.i("Data", "Loading data");
		dataStorageLeader.load();
		saveAll();
		notifyDataInvalidateListeners();
		Log.i("Data", "Loaded!");
	}

	/**
	 * Notifies every block that they has been saved
	 */
	private void saveAll() {
		for (TimeBlock block : timeBlocks) {
			block.setSaved();
		}
	}

	/**
	 * Checks every block if one of them was changed
	 *
	 * @return true if one of time blocks was changed
	 */
	private boolean isDataChanged() {
		for (TimeBlock block : timeBlocks) {
			if (block.isChanged()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the immutable copy of time blocks collection
	 */
	public List<TimeBlock> getTimeBlocks() {
		return immutableTimeBlocks;
	}

	/**
	 * @param id id of block
	 * @return block with specified id
	 */
	public TimeBlock getBlock(long id) {
		for (TimeBlock block : timeBlocks) {
			if (block.getID() == id) {
				return block;
			}
		}
		return null;
	}

	/**
	 * @param id id of block
	 * @return true if block with specified id exists
	 */
	public boolean hasBlock(long id) {
		return getBlock(id) != null;
	}

	/**
	 * @return the database
	 */
	SQLiteDatabase getDB() {
		return dbCommunicator.getDB();
	}

	/**
	 * @return the sync queue
	 */
	SyncQueue getSyncQueue() {
		return syncQueue;
	}

	/**
	 * Syncs data with every external data storage
	 */
	public void syncData() {
		Core.getAsyncActionQueue().addAction(new Runnable() {
			@Override
			public void run() {
				synchronized (DataCenter.this) {
					Log.i("DataCenter", "Syncing data");
					dataStorageLeader.sync();
					Log.i("DataCenter", "Synced!");
				}
			}
		});
	}

	/**
	 * Registers data invalidate listener
	 *
	 * @param listener listener to register
	 */
	public synchronized void registerDataInvalidateListener(DataInvalidateListener listener) {
		invalidateListeners.add(listener);
	}

	/**
	 * Unregisters data invalidate listener
	 *
	 * @param listener listener to unregister
	 */
	public synchronized void unregisterDataInvalidateListener(DataInvalidateListener listener) {
		invalidateListeners.remove(listener);
	}

	public static enum NotAddedCause {
		/**
		 * Time block has start time which is less on 3 days relative to core creation time
		 */
		TOO_OLD,
		/**
		 * Time block has start time which is more on 3 days relative to core creation time
		 */
		TOO_NEW,
		/**
		 * Time block twin is already in collection
		 */
		TWIN,
		/**
		 * Time block is already in collection
		 */
		ALREADY_IS,
	}
}
