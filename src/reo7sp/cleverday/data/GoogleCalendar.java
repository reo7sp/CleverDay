package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 8/1/13 at 10:52 PM
 */
public class GoogleCalendar {
	private final long id;
	private final String owner;
	private final String name;

	GoogleCalendar(long id, String owner, String name) {
		this.id = id;
		this.owner = owner;
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof GoogleCalendar)) {
			return false;
		}
		GoogleCalendar other = (GoogleCalendar) obj;
		return id == other.id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @return the id
	 */
	public long getID() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
}
