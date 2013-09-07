package reo7sp.cleverday.ui;

import android.graphics.Bitmap;
import android.util.SparseArray;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.log.Log;

public class BitmapFactory {
	private static final SparseArray<Bitmap> cache = new SparseArray<Bitmap>();

	private BitmapFactory() {
	}

	/**
	 * Returns bitmap and loads it if needed
	 *
	 * @param id id of bitmap
	 * @return the bitmap
	 */
	public static Bitmap getBitmap(int id) {
		if (cache.size() > 32) {
			cache.clear();
		}
		Bitmap bitmap = cache.get(id);
		if (bitmap == null) {
			Log.i("BitmapFactory", "Loading new bitmap with id " + id);
			bitmap = android.graphics.BitmapFactory.decodeResource(Core.getContext().getResources(), id);
			cache.put(id, bitmap);
		}
		return bitmap;
	}
}
