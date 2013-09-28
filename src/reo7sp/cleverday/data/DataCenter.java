package reo7sp.cleverday.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.service.NotificationService;
import reo7sp.cleverday.ui.widget.StandardWidget;

/**
 * Created by reo7sp on 8/1/13 at 2:12 PM
 */
public class DataCenter {
	private static DataCenter instance;
	private final List<TimeBlock> timeBlocks = new ArrayList<TimeBlock>();
	private final List<TimeBlock> immutableTimeBlocks = Collections.unmodifiableList(timeBlocks);
	private final DataBaseCommunicator dbCommunicator = new DataBaseCommunicator();
	private final DataStorageLeader dataStorageLeader = new DataStorageLeader(this);
	private final Comparator<TimeBlock> timeBlockComparator = new Comparator<TimeBlock>() {
		@Override
		public int compare(TimeBlock first, TimeBlock second) {
			return (int) (first.getUtcStart() - second.getUtcStart());
		}
	};
	private boolean invalidated;

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
		if (instance == null && Core.isBuilt()) {
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
		TimeBlock block = new TimeBlock();
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
			if (block.getModifyType() == TimeBlock.ModifyType.REMOVE) { // undo remove
				block.markToAdd();
			} else {
				Log.w("Data", "Can't add time block \"" + block.getTitle() + "\" with id " + block.getID() + " to data center. Cause: " + NotAddedCause.ALREADY_IS);
				return NotAddedCause.ALREADY_IS;
			}
		} else if (hasBlock(block.getID())) {
			Log.w("Data", "Can't add time block \"" + block.getTitle() + "\" with id " + block.getID() + " to data center. Cause: " + NotAddedCause.TWIN);
			return NotAddedCause.TWIN;
		}

		timeBlocks.add(block);
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
		block.markToRemove();
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

	private void startThread() {
		new Thread("DataCenter") {
			@Override
			public void run() {
				try {
					synchronized (DataCenter.this) {
						load();
						while (!isInterrupted()) {
							if (invalidated) {
								dataStorageLeader.sync();
								dataStorageLeader.save();

								Collections.sort(timeBlocks, timeBlockComparator);

								NotificationService.invalidate();
								StandardWidget.invalidate();

								for (TimeBlock block : timeBlocks) {
									block.setSaved();
								}
								invalidated = false;
							}

							DataCenter.this.wait(TimeConstants.SECOND);
						}
					}
				} catch (InterruptedException ignored) {
				}
			}
		}.start();
	}

	private void load() {
		Log.i("Data", "Loading data");
		dataStorageLeader.load();
		for (TimeBlock block : timeBlocks) {
			block.setSaved();
		}
		Log.i("Data", "Loaded!");
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
	public TimeBlock getBlock(int id) {
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
	public boolean hasBlock(int id) {
		return getBlock(id) != null;
	}

	/**
	 * Sets special mark which saves data later
	 */
	void invalidate() {
		invalidated = true;
	}

	/**
	 * @return true if data was invalidated
	 */
	public boolean isInvalidated() {
		return invalidated;
	}

	/**
	 * @return the writable sqlite db instance
	 */
	SQLiteDatabase getDB() {
		return dbCommunicator.getDB();
	}

	/**
	 * Receive new data from every data storage. This method doesn't perform receiving but this method schedules it
	 */
	public void receiveData() {
		Core.getAsyncActionQueue().addAction(new Runnable() {
			@Override
			public void run() {
				synchronized (DataCenter.this) {
					Log.i("DataCenter", "Receiving data");
					dataStorageLeader.receive();
					Log.i("DataCenter", "Received!");
				}
			}
		});
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
