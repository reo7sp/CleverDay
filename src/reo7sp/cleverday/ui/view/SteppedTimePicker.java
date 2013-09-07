package reo7sp.cleverday.ui.view;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

/**
 * Created by reo7sp on 8/25/13 at 9:51 PM
 */
public class SteppedTimePicker extends LinearLayout {
	private final int step;
	private final NumberPicker hourPicker;
	private final NumberPicker minutePicker;
	private final TextView separator;
	private OnTimeChangeListener listener = null;

	public SteppedTimePicker(Context context, int step) {
		super(context);
		this.step = step;
		hourPicker = new NumberPicker(context);
		minutePicker = new NumberPicker(context);
		separator = new TextView(context);

		// hour picker
		hourPicker.setMinValue(0);
		hourPicker.setMaxValue(23);
		hourPicker.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
		hourPicker.setWrapSelectorWheel(true);
		hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (listener != null) {
					listener.onTimeChange(SteppedTimePicker.this, newVal, minutePicker.getValue() * SteppedTimePicker.this.step);
				}
			}
		});

		// separator
		separator.setText(":");
		separator.setTextSize(20);
		post(new Runnable() {
			@Override
			public void run() {
				separator.setPadding(8, (getHeight() - 24) / 2, 8, (getHeight() - 24) / 2);
			}
		});

		// minute picker
		minutePicker.setMinValue(0);
		minutePicker.setMaxValue(59 / step);
		minutePicker.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
		minutePicker.setWrapSelectorWheel(true);
		String[] values = new String[59 / step + 1];
		for (int i = 0, count = 59 / step + 1; i < count; i++) {
			if (i == 0) {
				values[0] = "00";
			} else {
				values[i] = "" + (step * i);
			}
		}
		minutePicker.setDisplayedValues(values);
		minutePicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (oldVal == minutePicker.getMaxValue() && newVal == 0) {
					hourPicker.setValue(hourPicker.getValue() + 1);
				} else if (newVal == minutePicker.getMaxValue() && oldVal == 0) {
					hourPicker.setValue(hourPicker.getValue() - 1);
				}
				if (listener != null) {
					listener.onTimeChange(SteppedTimePicker.this, hourPicker.getValue(), newVal * SteppedTimePicker.this.step);
				}
			}
		});

		// view
		setOrientation(HORIZONTAL);
		addView(hourPicker);
		addView(separator);
		addView(minutePicker);
	}

	public void setListener(OnTimeChangeListener listener) {
		this.listener = listener;
	}

	public int getHour() {
		return hourPicker.getValue();
	}

	public void setHour(int hour) {
		hourPicker.setValue(hour);
	}

	public void setMinute(int minute) {
		minutePicker.setValue(minute / step);
	}

	public int getMinute() {
		return minutePicker.getValue() * step;
	}

	public static interface OnTimeChangeListener {
		public void onTimeChange(SteppedTimePicker view, int hour, int minute);
	}
}
