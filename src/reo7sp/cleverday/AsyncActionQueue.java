package reo7sp.cleverday;

import android.os.Process;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class AsyncActionQueue extends ActionQueue implements Runnable {
	private final Collection<Task> tasks = new HashSet<Task>();
	private final Thread thread;

	AsyncActionQueue() {
		thread = new Thread(this, "AsyncActionQueue");
		thread.start();
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		try {
			while (!thread.isInterrupted()) {
				// running next task
				Task task = null;
				for (Task loopTask : tasks) {
					if (System.currentTimeMillis() > loopTask.execTime) {
						task = loopTask;
						break;
					}
				}
				if (task != null) {
					task.handle.run();
					tasks.remove(task);
				}

				// sleeping for a while
				Thread.sleep(tasks.isEmpty() ? 500 : 10);
			}
		} catch (InterruptedException ignored) {
		}
	}

	@Override
	public void addAction(Runnable action) {
		addAction(action, 0);
	}

	@Override
	public void addAction(Runnable action, int delay) {
		if (action == null) {
			return;
		}
		tasks.add(new Task(action, System.currentTimeMillis() + delay));
	}

	@Override
	public void removeAction(Runnable action) {
		for (Iterator<Task> iterator = tasks.iterator(); iterator.hasNext(); ) {
			Task task = iterator.next();
			if (task.handle.equals(action)) {
				iterator.remove();
			}
		}
	}

	private static class Task {
		public final Runnable handle;
		public final long execTime;

		public Task(Runnable handle, long execTime) {
			this.handle = handle;
			this.execTime = execTime;
		}
	}
}
