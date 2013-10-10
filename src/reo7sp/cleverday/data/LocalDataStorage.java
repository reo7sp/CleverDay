package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 8/1/13 at 2:23 PM
 */
public abstract class LocalDataStorage {
	LocalDataStorage() {
	}

	/**
	 * Loads data. Must be called only once.
	 * This method must be invoked in {@link reo7sp.cleverday.data.DataStorageLeader}, which is synchronised
	 */
	abstract void load();

	/**
	 * Saves all data in storage.
	 * This method must be invoked in {@link reo7sp.cleverday.data.DataStorageLeader}, which is synchronised
	 */
	abstract void save();

	/**
	 * Removes time block from storage
	 * This method must be invoked in {@link reo7sp.cleverday.data.DataStorageLeader}, which is synchronised
	 *
	 * @param block block to remove
	 */
	abstract void remove(TimeBlock block);
}
