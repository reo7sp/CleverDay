package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 10/1/13 at 7:40 PM
 */
public class IntValue extends AbstractValue {
	private int value;

	IntValue(TimeBlock block) {
		super(block);
	}

	IntValue(TimeBlock block, int value) {
		super(block);
		this.value = value;
	}

	public synchronized int getValue() {
		return value;
	}

	public synchronized void setValue(int value) {
		this.value = value;
		setChanged();
	}

	@Override
	public String toString() {
		return "" + value;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof BooleanValue)) {
			return false;
		}

		IntValue value1 = (IntValue) o;

		return value == value1.value;
	}

	@Override
	public int hashCode() {
		return value;
	}
}
