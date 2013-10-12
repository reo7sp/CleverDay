package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 8/1/13 at 2:33 PM
 */
public class DataStorageLeader {
	private LocalDataStorage[] localDataStorages;
	private ExternalDataStorage[] externalDataStorages;

	DataStorageLeader() {
	}

	synchronized void init() {
		localDataStorages = new LocalDataStorage[] {
				new MainDataStorage(),
				new HistoryStorage(),
		};
		externalDataStorages = new ExternalDataStorage[] {
				new GoogleCalendarStorage(),
		};
	}

	public synchronized void load() {
		for (LocalDataStorage slave : localDataStorages) {
			slave.load();
		}
	}

	public synchronized void save() {
		for (LocalDataStorage slave : localDataStorages) {
			slave.save();
		}
	}

	public synchronized void remove(TimeBlock block) {
		for (LocalDataStorage slave : localDataStorages) {
			slave.remove(block);
		}
	}

	public synchronized void sync() {
		receive();
		send();
	}

	private void receive() {
		for (ExternalDataStorage slave : externalDataStorages) {
			slave.receive();
		}
	}

	private void send() {
		for (ExternalDataStorage slave : externalDataStorages) {
			slave.send();
		}
	}
}
