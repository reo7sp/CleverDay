package reo7sp.cleverday.data;

import reo7sp.cleverday.Core;

/**
 * Created by reo7sp on 10/8/13 at 3:58 PM
 */
public abstract class AbstractValue {
	private final TimeBlock block;
	private boolean changed;

	AbstractValue(TimeBlock block) {
		this.block = block;
	}

	public boolean isChanged() {
		return changed;
	}

	protected void setChanged() {
		changed = true;
		Core.getDataCenter().getSyncQueue().newCommit(block);
	}

	public void setSaved() {
		changed = false;
	}
}
