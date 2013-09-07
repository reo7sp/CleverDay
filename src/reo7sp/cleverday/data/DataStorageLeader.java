package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 8/1/13 at 2:33 PM
 */
public class DataStorageLeader {
	private final DataStorage[] slaves;

	DataStorageLeader(DataCenter dataCenter) {
		slaves = new DataStorage[] {
				new LocalDataStorage(dataCenter),
				new HistoryStorage(dataCenter),
				new GoogleCalendarStorage(dataCenter),
		};
	}

	public synchronized void load() {
		for (DataStorage slave : slaves) {
			slave.load();
		}
	}

	public synchronized void receive() {
		for (DataStorage slave : slaves) {
			slave.receive();
		}
	}

	public synchronized void save() {
		for (DataStorage slave : slaves) {
			slave.save();
		}
	}

	public synchronized void sync() {
		for (DataStorage slave : slaves) {
			slave.syncDataCenterWithMe();
		}
		for (DataStorage slave : slaves) {
			slave.syncMeWithDataCenter();
		}
	}
}
