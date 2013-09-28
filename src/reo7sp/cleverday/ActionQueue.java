package reo7sp.cleverday;

public abstract class ActionQueue {
	/**
	 * Adds action to queue
	 *
	 * @param action action to add
	 */
	public abstract void addAction(Runnable action);

	/**
	 * Adds action to queue
	 *
	 * @param action action to add
	 * @param delay  delay of action
	 */
	public abstract void addAction(Runnable action, int delay);

	/**
	 * Removes action from queue
	 *
	 * @param action action to remove
	 */
	public abstract void removeAction(Runnable action);
}
