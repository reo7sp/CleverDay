package reo7sp.cleverday.ui.colors;

import reo7sp.cleverday.Core;

public class SimpleColor {
	public static final int BLUE = 0xFF60B4CC;
	public static final int GREEN = 0xFFA5CC2F;
	public static final int ORANGE = 0xFFE6931F;
	public static final int RED = 0xFFF66546;
	public static final int YELLOW = 0xFFCCBD1D;
	public static final int CYAN = 0xFF68BFA2;
	public static final int PURPLE = 0xFFB568DB;
	public static final int GRAY = 0xFF808C8C;

	private static final int[] COLORS = {
			BLUE,
			CYAN,
			GRAY,
			GREEN,
			ORANGE,
			PURPLE,
			RED,
			YELLOW,
	};

	private SimpleColor() {
	}

	public static int getRandomColor() {
		return COLORS[Core.getRandom().nextInt(COLORS.length)];
	}

	public static int[] getColorsArray() {
		return COLORS.clone();
	}

	public static boolean contains(int color) {
		for (int c : COLORS) {
			if (color == c) {
				return true;
			}
		}
		return false;
	}
}
