package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 8/1/13 at 2:23 PM
 */
public abstract class ExternalDataStorage {
	ExternalDataStorage() {
	}

	/**
	 * Receives data from storage.
	 * This method must be invoked in {@link reo7sp.cleverday.data.DataStorageLeader}, which is synchronised
	 */
	abstract void receive();

	/**
	 * Sends data to storage.
	 * This method must be invoked in {@link reo7sp.cleverday.data.DataStorageLeader}, which is synchronised
	 */
	abstract void send();
}
