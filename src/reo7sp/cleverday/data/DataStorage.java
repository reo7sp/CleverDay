package reo7sp.cleverday.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by reo7sp on 8/1/13 at 2:23 PM
 */
public abstract class DataStorage {
	protected final DataCenter dataCenter;
	protected final List<TimeBlock> timeBlocks = new ArrayList<TimeBlock>();

	DataStorage(DataCenter dataCenter) {
		this.dataCenter = dataCenter;
	}

	/**
	 * Loads data. Must be called only once.
	 * This method must be invoked in {@link DataStorageLeader}, which is synchronised
	 */
	abstract void load();

	/**
	 * Receives new data from storage.
	 * This method must be invoked in {@link DataStorageLeader}, which is synchronised
	 */
	abstract void receive();

	/**
	 * Saves all data in storage.
	 * This method must be invoked in {@link DataStorageLeader}, which is synchronised
	 */
	abstract void save();

	/**
	 * Adds all new time blocks from data center to this data storage.
	 * This method must be invoked in {@link DataStorageLeader}, which is synchronised
	 */
	void syncMeWithDataCenter() {
		for (TimeBlock block : dataCenter.getTimeBlocks()) {
			if (block.getModifyType() == TimeBlock.ModifyType.ADD) {
				timeBlocks.add(block);
			}
		}
	}

	/**
	 * Adds all new time blocks from this data storage to data center.
	 * This method must be invoked in {@link DataStorageLeader}, which is synchronised
	 */
	void syncDataCenterWithMe() {
		for (Iterator<TimeBlock> iterator = timeBlocks.iterator(); iterator.hasNext(); ) {
			TimeBlock block = iterator.next();

			if (block.getModifyType() != TimeBlock.ModifyType.ADD) {
				continue;
			}

			DataCenter.NotAddedCause cause = dataCenter.addTimeBlock(block);
			if (cause == null) {
				continue;
			}
			switch (cause) {
				case TWIN:
					block.markToRemove();
					break;

				case TOO_OLD:
				case TOO_NEW:
					ActionOnSyncProblem whatToDo = actionOnSyncProblem();
					if (whatToDo == ActionOnSyncProblem.REMOVE_FROM_BUFFER) {
						iterator.remove();
					} else if (whatToDo == ActionOnSyncProblem.REMOVE_COMPLETELY) {
						block.markToRemove();
					}
					break;
			}
		}
	}

	protected abstract ActionOnSyncProblem actionOnSyncProblem();

	protected static enum ActionOnSyncProblem {
		REMOVE_FROM_BUFFER,
		REMOVE_COMPLETELY,
	}
}
