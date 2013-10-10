package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 10/1/13 at 7:40 PM
 */
public class LongValue extends AbstractValue {
	private long value;

	LongValue(TimeBlock block) {
		super(block);
	}

	LongValue(TimeBlock block, long value) {
		super(block);
		this.value = value;
	}

	public synchronized long getValue() {
		return value;
	}

	public synchronized void setValue(long value) {
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

		LongValue value1 = (LongValue) o;

		return value == value1.value;
	}

	@Override
	public int hashCode() {
		return (int) (value ^ (value >>> 32));
	}
}
