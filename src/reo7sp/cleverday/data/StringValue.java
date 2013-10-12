package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 10/1/13 at 7:40 PM
 */
public class StringValue extends AbstractValue {
	private String value;

	StringValue(TimeBlock block) {
		super(block);
	}

	StringValue(TimeBlock block, String value) {
		super(block);
		this.value = value;
	}

	public synchronized String getValue() {
		return value;
	}

	public synchronized void setValue(String value) {
		this.value = value;
		setChanged();
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof StringValue)) {
			return false;
		}

		StringValue value1 = (StringValue) o;

		if (!value.equals(value1.value)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}
