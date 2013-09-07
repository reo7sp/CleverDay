package reo7sp.cleverday.utils;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;

public class AndroidUtils {
	private AndroidUtils() {
	}

	/**
	 * Recycles {@link View}
	 *
	 * @param view {@link View} instance
	 */
	public static void recycleView(View view) {
		if (view == null) {
			return;
		}
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}
		if (view instanceof ViewGroup) {
			ViewGroup viewGroup = ((ViewGroup) view);
			for (int i = 0; i < viewGroup.getChildCount(); i++) {
				recycleView(viewGroup.getChildAt(i));
			}
			viewGroup.removeAllViews();
		}
	}

	/**
	 * Checks if app runs on tablet
	 *
	 * @return true if app runs on tablet
	 */
	public static boolean isTablet() {
		boolean xlarge = ((Core.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((Core.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}

	/**
	 * Updates theme of {@link Activity}. This method must be invoked in {@link Activity#onCreate(android.os.Bundle)} before super method
	 *
	 * @param activity {@link Activity} instance
	 */
	public static void updateTheme(Activity activity) {
		activity.setTheme(isInDarkTheme() ? R.style.MyStyle_Dark : R.style.MyStyle);
	}

	/**
	 * Checks if app is in dark theme
	 *
	 * @return true if app is in dark theme
	 */
	public static boolean isInDarkTheme() {
		return Core.getPreferences().getBoolean("pref_dark_theme", false);
	}

	/**
	 * Checks if notification must be shown
	 *
	 * @return true if notification must be shown
	 */
	public static boolean isNotificationShown() {
		return Core.getPreferences().getBoolean("pref_show_notification", true);
	}
}
