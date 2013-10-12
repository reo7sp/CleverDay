package reo7sp.cleverday.ui;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.NumberPicker;

import reo7sp.cleverday.R;

/**
 * Created by reo7sp on 8/25/13 at 9:51 PM
 */
public class SteppedTimePicker extends FrameLayout {
	private final int step;
	private final NumberPicker hourPicker;
	private final NumberPicker minutePicker;
	private final NumberPicker h12Picker;
	private final boolean h12;

	public SteppedTimePicker(Context context, int step) {
		super(context);
		this.step = step;
		h12 = !DateFormat.is24HourFormat(context);
		View layout = inflate(context, h12 ? R.layout.stepped_time_picker_12 : R.layout.stepped_time_picker, null);
		addView(layout);
		hourPicker = (NumberPicker) layout.findViewById(R.id.hour_picker);
		minutePicker = (NumberPicker) layout.findViewById(R.id.minute_picker);
		if (h12) {
			h12Picker = (NumberPicker) layout.findViewById(R.id.h12_picker);
		} else {
			h12Picker = null;
		}

		// hour picker
		hourPicker.setMinValue(h12 ? 1 : 0);
		hourPicker.setMaxValue(h12 ? 12 : 23);
		hourPicker.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
		hourPicker.setWrapSelectorWheel(true);
		hourPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
			@Override
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				if (h12) {
					if ((oldVal == 11 && newVal == 12) || (newVal == 11 && oldVal == 12)) {
						setPM(!isPM());
					}
				}
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
				if (oldVal == minutePicker.getMaxValue() && newVal == minutePicker.getMinValue()) {
					hourPicker.setValue(hourPicker.getValue() + 1);
				} else if (newVal == minutePicker.getMaxValue() && oldVal == minutePicker.getMinValue()) {
					hourPicker.setValue(hourPicker.getValue() - 1);
				}
			}
		});

		// h12
		if (h12) {
			h12Picker.setMinValue(0);
			h12Picker.setMaxValue(1);
			h12Picker.setDisplayedValues(new String[] {"AM", "PM"});
			h12Picker.setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
			h12Picker.setWrapSelectorWheel(true);
		}
	}

	public int getHour() {
		if (h12) {
			int h = hourPicker.getValue();
			if (h == 12) {
				if (!isPM()) {
					h = 0;
				}
			} else if (isPM()) {
				h += 12;
			}
			return h;
		} else {
			return hourPicker.getValue();
		}
	}

	public void setHour(int hour) {
		if (h12) {
			boolean pm = hour > 11;
			int h = hour - (pm ? 12 : 0);
			if (hour == 0) {
				h = 12;
			}

			hourPicker.setValue(h);
			setPM(pm);
		} else {
			hourPicker.setValue(hour);
		}
	}

	private boolean isPM() {
		return h12Picker.getValue() == 1;
	}

	private void setPM(boolean pm) {
		h12Picker.setValue(pm ? 1 : 0);
	}

	public int getMinute() {
		return minutePicker.getValue() * step;
	}

	public void setMinute(int minute) {
		minutePicker.setValue(minute / step);
	}
}
