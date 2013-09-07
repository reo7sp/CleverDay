package reo7sp.cleverday.ui;

import android.view.View;
import android.widget.Scroller;

public class ScrollAssistant implements Runnable {
	public static final int TYPE_SCROLL = 1;
	public static final int TYPE_FLING = 2;

	private final Scroller scroller;
	private final View view;
	private int lastX = 0, lastY = 0;
	private final int minX, maxX;
	private final int minY, maxY;
	private OnScrollListener scrollListener;
	private OnStopListener stopListener;
	private int scrollType;

	public ScrollAssistant(View view, int minX, int maxX, int minY, int maxY) {
		this.view = view;
		this.minX = minX;
		this.maxX = maxX;
		this.minY = minY;
		this.maxY = maxY;

		scroller = new Scroller(view.getContext());
	}

	/**
	 * Starts scrolling
	 *
	 * @param dx count of pixels, which will be added to x
	 * @param dy count of pixels, which will be added to y
	 */
	public void scroll(int dx, int dy) {
		int startX = view.getScrollX();
		int startY = view.getScrollY();
		lastX = startX;
		lastY = startY;

		scroller.startScroll(startX, startY, dx, dy);
		scrollType = TYPE_SCROLL;

		// starting scroll
		view.post(this);
	}

	/**
	 * Starts flinging
	 *
	 * @param velocityX initial velocity on x coordinate
	 * @param velocityY initial velocity on y coordinate
	 */
	public void fling(int velocityX, int velocityY) {
		int initialX = view.getScrollX();
		int initialY = view.getScrollY();
		lastX = initialX;
		lastY = initialY;

		scroller.fling(initialX, initialY, velocityX, velocityY, minX, maxX, minY, maxY);
		scrollType = TYPE_FLING;

		// starting scroll
		view.post(this);
	}

	@Override
	public void run() {
		if (scroller.isFinished()) {
			return;
		}

		// if it suddenly went out of bounds
		if (view.getScrollX() < minX) {
			view.scrollTo(minX, view.getScrollY());
			finish(false);
		} else if (view.getScrollX() > maxX) {
			view.scrollTo(maxX, view.getScrollY());
			finish(false);
		}
		if (view.getScrollY() < minY) {
			view.scrollTo(view.getScrollX(), minY);
			finish(false);
		} else if (view.getScrollY() > maxY) {
			view.scrollTo(view.getScrollX(), maxY);
			finish(false);
		}

		// scrolling
		if (scroller.computeScrollOffset()) {
			int x = scroller.getCurrX();
			int y = scroller.getCurrY();
			int dx = lastX - x;
			int dy = lastY - y;
			lastX = x;
			lastY = y;

			// scrolling
			view.scrollBy(dx, dy);

			// telling something listeners
			if (scrollListener != null) {
				scrollListener.onScroll(dx, dy);
			}

			// continuing scroll
			view.post(this);
		} else {
			finish(false);
		}
	}

	public static interface OnStopListener {
		public void onStop(int x, int y, boolean isForced);
	}

	public static interface OnScrollListener {
		public void onScroll(int x, int y);
	}

	/**
	 * Forces scroller finished
	 */
	public void forceFinished() {
		finish(true);
	}

	private void finish(boolean isForced) {
		if (!scroller.isFinished()) {
			scroller.forceFinished(true);
			if (stopListener != null) {
				stopListener.onStop(view.getScrollX(), view.getScrollY(), isForced);
			}
		}
	}

	/**
	 * @return true if scroller is scrolling
	 */
	public boolean isScrolling() {
		return !scroller.isFinished();
	}

	/**
	 * @param stopListener stop listener to set
	 */
	public void setStopListener(OnStopListener stopListener) {
		this.stopListener = stopListener;
	}

	/**
	 * @return scrolling view
	 */
	public View getView() {
		return view;
	}

	/**
	 * @param scrollListener scroll listener to set
	 */
	public void setScrollListener(OnScrollListener scrollListener) {
		this.scrollListener = scrollListener;
	}

	/**
	 * @return the scroll type
	 */
	public int getScrollType() {
		return scrollType;
	}
}
