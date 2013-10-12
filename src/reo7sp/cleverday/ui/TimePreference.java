package reo7sp.cleverday.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;

public class TimePreference extends DialogPreference {
	private int hour = 9;
	private int minute = 0;
	private TimePicker picker;

	public TimePreference(Context context, AttributeSet attrs) {
		super(context, attrs);

		setPositiveButtonText(R.string.set);
		setNegativeButtonText(R.string.cancel);
	}

	public static int getHour(String time) {
		String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[0]));
	}

	public static int getMinute(String time) {
		String[] pieces = time.split(":");

		return (Integer.parseInt(pieces[1]));
	}

	@Override
	protected View onCreateDialogView() {
		picker = new TimePicker(getContext());
		picker.setIs24HourView(Core.getDateFormatter().is24HourFormat());

		return picker;
	}

	@Override
	protected void onBindDialogView(View v) {
		super.onBindDialogView(v);

		picker.setCurrentHour(hour);
		picker.setCurrentMinute(minute);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			hour = picker.getCurrentHour();
			minute = picker.getCurrentMinute();

			String time = String.valueOf(hour) + ":" + String.valueOf(minute);

			if (callChangeListener(time)) {
				persistString(time);
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return (a.getString(index));
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		String time;

		if (restoreValue) {
			if (defaultValue == null) {
				time = getPersistedString("00:00");
			} else {
				time = getPersistedString(defaultValue.toString());
			}
		} else {
			time = defaultValue.toString();
		}

		hour = getHour(time);
		minute = getMinute(time);
	}
}
