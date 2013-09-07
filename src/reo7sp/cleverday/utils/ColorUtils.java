package reo7sp.cleverday.utils;

import android.graphics.Color;

public class ColorUtils {
	private ColorUtils() {
	}

	/**
	 * Changes alpha value of color
	 *
	 * @param color color
	 * @param alpha next alpha value of color
	 * @return new color
	 */
	public static int changeAlpha(int color, int alpha) {
		return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
	}

	/**
	 * Changes red value of color
	 *
	 * @param color color
	 * @param red   next red value of color
	 * @return new color
	 */
	public static int changeRed(int color, int red) {
		return Color.argb(Color.alpha(color), red, Color.green(color), Color.blue(color));
	}

	/**
	 * Changes green value of color
	 *
	 * @param color color
	 * @param green next green value of color
	 * @return new color
	 */
	public static int changeGreen(int color, int green) {
		return Color.argb(Color.alpha(color), Color.red(color), green, Color.blue(color));
	}

	/**
	 * Changes blue value of color
	 *
	 * @param color color
	 * @param blue  next blue value of color
	 * @return new color
	 */
	public static int changeBlue(int color, int blue) {
		return Color.argb(Color.alpha(color), Color.red(color), Color.green(color), blue);
	}

	/**
	 * Makes color brighter
	 *
	 * @param color color
	 * @param scale brightness scale
	 * @return new color
	 */
	public static int brighter(int color, float scale) {
		scale += 1;
		int a = Color.alpha(color);
		int r = (int) (Color.red(color) * scale);
		int g = (int) (Color.green(color) * scale);
		int b = (int) (Color.blue(color) * scale);
		r = Math.min(Math.max(r, 0), 255);
		g = Math.min(Math.max(g, 0), 255);
		b = Math.min(Math.max(b, 0), 255);
		return Color.argb(a, r, g, b);
	}

	/**
	 * Makes color darker
	 *
	 * @param color color
	 * @param scale darkness scale
	 * @return new color
	 */
	public static int darker(int color, float scale) {
		scale += 1;
		int a = Color.alpha(color);
		int r = (int) (Color.red(color) / scale);
		int g = (int) (Color.green(color) / scale);
		int b = (int) (Color.blue(color) / scale);
		r = Math.min(Math.max(r, 0), 255);
		g = Math.min(Math.max(g, 0), 255);
		b = Math.min(Math.max(b, 0), 255);
		return Color.argb(a, r, g, b);
	}

	/**
	 * Makes color less transparent
	 *
	 * @param color color
	 * @param scale opaqueness scale
	 * @return new color
	 */
	public static int lessTransparent(int color, float scale) {
		scale += 1;
		int a = (int) (Color.alpha(color) * scale);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		a = Math.min(Math.max(a, 0), 255);
		return Color.argb(a, r, g, b);
	}

	/**
	 * Makes color more transparent
	 *
	 * @param color color
	 * @param scale transparent scale
	 * @return new color
	 */
	public static int moreTransparent(int color, float scale) {
		scale += 1;
		int a = (int) (Color.alpha(color) / scale);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		a = Math.min(Math.max(a, 0), 255);
		return Color.argb(a, r, g, b);
	}
}
