package reo7sp.cleverday.ui;

import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;

public class GestureListener extends SimpleOnGestureListener implements OnScaleGestureListener {
	public static final int SWIPE_LEFT = 0;
	public static final int SWIPE_RIGHT = 1;
	public static final int SWIPE_TOP = 2;
	public static final int SWIPE_BOTTOM = 3;

	private static final int SWIPE_THRESHOLD = 100;
	private static final int SWIPE_VELOCITY_THRESHOLD = 100;

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		float diffY = e2.getY() - e1.getY();
		float diffX = e2.getX() - e1.getX();
		if (Math.abs(diffX) > Math.abs(diffY)) {
			if (diffX > 0) {
				onSwiping(e1, e2, SWIPE_RIGHT, (int) distanceX);
			} else {
				onSwiping(e1, e2, SWIPE_LEFT, (int) distanceX);
			}
		} else {
			if (diffY > 0) {
				onSwiping(e1, e2, SWIPE_BOTTOM, (int) distanceY);
			} else {
				onSwiping(e1, e2, SWIPE_TOP, (int) distanceY);
			}
		}
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float diffY = e2.getY() - e1.getY();
		float diffX = e2.getX() - e1.getX();
		if (Math.abs(diffX) > Math.abs(diffY)) {
			if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
				if (diffX > 0) {
					onSwipe(e1, e2, SWIPE_RIGHT);
				} else {
					onSwipe(e1, e2, SWIPE_LEFT);
				}
			}
		} else {
			if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
				if (diffY > 0) {
					onSwipe(e1, e2, SWIPE_BOTTOM);
				} else {
					onSwipe(e1, e2, SWIPE_TOP);
				}
			}
		}
		return true;
	}

	public void onSwiping(MotionEvent e1, MotionEvent e2, int direction, int difference) {
	}

	public void onSwipe(MotionEvent e1, MotionEvent e2, int direction) {
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return true;
	}

	public void onTouch(MotionEvent e) {
	}
}
