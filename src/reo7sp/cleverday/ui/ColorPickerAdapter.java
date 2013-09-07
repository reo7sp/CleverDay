package reo7sp.cleverday.ui;

import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import reo7sp.cleverday.Core;

public class ColorPickerAdapter extends BaseAdapter {
	private static final int ELEMENT_SIZE = 77;

	private final int[] colors;

	public ColorPickerAdapter(int[] colors) {
		this.colors = colors;
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;

		// can we reuse a view?
		if (convertView == null) {
			imageView = new ImageView(Core.getContext());
			imageView.setLayoutParams(new GridView.LayoutParams(ELEMENT_SIZE, ELEMENT_SIZE));
		} else {
			imageView = (ImageView) convertView;
		}

		// creating and setting drawable
		ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
		shapeDrawable.setColorFilter(colors[position], Mode.SRC_ATOP);
		shapeDrawable.getShape().resize(ELEMENT_SIZE, ELEMENT_SIZE);
		if (Build.VERSION.SDK_INT >= 16) {
			imageView.setBackground(shapeDrawable);
		} else {
			imageView.setBackgroundDrawable(shapeDrawable);
		}
		imageView.setId(position);

		return imageView;
	}

	@Override
	public int getCount() {
		return colors.length;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}
}
