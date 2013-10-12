package reo7sp.cleverday.data;

/**
 * Created by reo7sp on 10/1/13 at 7:40 PM
 */
public class BooleanValue extends AbstractValue {
	private boolean value;

	BooleanValue(TimeBlock block) {
		super(block);
	}

	BooleanValue(TimeBlock block, boolean value) {
		super(block);
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
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

		BooleanValue value1 = (BooleanValue) o;

		return value == value1.value;
	}

	@Override
	public int hashCode() {
		return (value ? 1 : 0);
	}
}
