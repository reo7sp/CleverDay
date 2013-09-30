package reo7sp.cleverday.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.Button;
import android.widget.TextView;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;

public class PopupBar {
	private static PopupBar currentBar;
	private static Button button1View;
	private static TextView messageView;
	private static Button button2View;
	private static View view;
	private static ViewPropertyAnimator animator;
	private final HideTimerRunnable hideTimerRunnable = new HideTimerRunnable();
	private final PopupBarElement firstButtonElement;
	private final PopupBarElement messageElement;
	private final PopupBarElement secondButtonElement;
	private final boolean fixed;

	public PopupBar(PopupBarElement firstButtonElement, PopupBarElement messageElement, PopupBarElement secondButtonElement) {
		this(firstButtonElement, messageElement, secondButtonElement, false);
	}

	public PopupBar(PopupBarElement firstButtonElement, PopupBarElement messageElement, PopupBarElement secondButtonElement, boolean fixed) {
		this.firstButtonElement = firstButtonElement;
		this.messageElement = messageElement;
		this.secondButtonElement = secondButtonElement;
		this.fixed = fixed;
	}

	private void init() {
		if (view == null) {
			view = Core.getMainActivity().findViewById(R.id.popupbar);
			animator = view.animate();
			button1View = (Button) view.findViewById(R.id.popupbar_button_1);
			messageView = (TextView) view.findViewById(R.id.popupbar_message);
			button2View = (Button) view.findViewById(R.id.popupbar_button_2);
		}

		for (int i = 1; i <= 3; i++) {
			final int pos = i;
			final PopupBarElement element;
			final TextView view;

			switch (pos) {
				case 1:
					element = firstButtonElement;
					view = button1View;
					break;

				case 2:
					element = messageElement;
					view = messageView;
					break;

				case 3:
					element = secondButtonElement;
					view = button2View;
					break;

				default:
					continue;
			}

			if (element == null) {
				view.setVisibility(View.GONE);
			} else {
				view.setVisibility(View.VISIBLE);

				// icon
				Drawable img;
				if (element.getDrawable() == 0) {
					img = null;
				} else {
					img = view.getContext().getResources().getDrawable(element.getDrawable());
				}
				if ((i == 1 && !element.isDrawableNearCenter()) || (i == 3 && element.isDrawableNearCenter())) {
					view.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
				} else if ((i == 3 && !element.isDrawableNearCenter()) || (i == 1 && element.isDrawableNearCenter())) {
					view.setCompoundDrawablesWithIntrinsicBounds(null, null, img, null);
				}

				// text
				view.setText(element.getTitle() == null ? null : element.getTitle());

				// creating click listener
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (element.onClick()) {
							hide();
						}
					}
				});
			}
		}

		if (firstButtonElement != null && secondButtonElement == null) {
			messageView.setGravity(Gravity.RIGHT);
		} else if (firstButtonElement == null && secondButtonElement != null) {
			messageView.setGravity(Gravity.LEFT);
		} else {
			messageView.setGravity(Gravity.CENTER);
		}

		// hiding shy bar
		view.setVisibility(View.GONE);
	}

	/**
	 * Shows popup bar
	 */
	public void show() {
		show(true);
	}

	/**
	 * Show popup bar
	 *
	 * @param animations if false, animations will be prevented
	 */
	public void show(final boolean animations) {
		Core.getSyncActionQueue().addAction(new Runnable() {
			public void run() {
				// removing concurrent
				if (currentBar != null && currentBar != PopupBar.this) {
					currentBar.hide(animations);
					currentBar = null;
				}

				// if nobody is here, bar will be initialized
				if (currentBar == null) {
					init();
					currentBar = PopupBar.this;
				}

				// starting new hide timer
				Core.getSyncActionQueue().removeAction(hideTimerRunnable);
				if (!fixed) {
					Core.getSyncActionQueue().addAction(hideTimerRunnable, 5000);
				}

				// making it visible
				view.setVisibility(View.VISIBLE);

				// animations! =^-^=
				if (animations) {
					view.setAlpha(0);
					animator.alpha(1).setListener(null);
				} else {
					view.setAlpha(1);
				}
			}
		});
	}

	/**
	 * Hides popup bar
	 */
	public void hide() {
		hide(true);
	}

	/**
	 * Hides popup bar
	 *
	 * @param animations prevent animations
	 */
	public void hide(final boolean animations) {
		Core.getSyncActionQueue().addAction(new Runnable() {
			public void run() {
				// removing concurrent
				if (currentBar != null && currentBar != PopupBar.this) {
					currentBar.hide(animations);
					currentBar = null;
				}

				// if nobody is here, bar will be initialized
				if (currentBar == null) {
					init();
					currentBar = PopupBar.this;
				}

				// removing hide timer
				Core.getSyncActionQueue().removeAction(hideTimerRunnable);

				// animations! =^-^=
				if (animations) {
					view.setVisibility(View.VISIBLE);
					animator.alpha(0).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							view.setVisibility(View.GONE);
						}
					});
				} else {
					view.setVisibility(View.GONE);
					view.setAlpha(0);
				}
			}
		});
	}

	/**
	 * @return true if bar can't hide automatically
	 */
	public boolean isFixed() {
		return fixed;
	}

	/**
	 * @return true if bar is current
	 */
	public boolean isCurrent() {
		return currentBar == this;
	}

	public static abstract class PopupBarElement {
		public boolean onClick() {
			return false;
		}

		public CharSequence getTitle() {
			return "";
		}

		public int getDrawable() {
			return 0;
		}

		public boolean isDrawableNearCenter() {
			return false;
		}
	}

	private class HideTimerRunnable implements Runnable {
		@Override
		public void run() {
			hide();
		}
	}
}
