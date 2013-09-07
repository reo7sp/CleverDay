package reo7sp.cleverday.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.NumberPicker;

import java.util.Calendar;
import java.util.Date;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.activity.EditBlockActivity;
import reo7sp.cleverday.ui.colors.SimpleColor;
import reo7sp.cleverday.ui.view.SteppedTimePicker;
import reo7sp.cleverday.utils.DateUtils;
import reo7sp.cleverday.utils.StringUtils;

public class Dialogs {
	private Dialogs() {
	}

	public static Dialog createBlockColorPickerDialog(final EditBlockActivity activity, final TimeBlock block) {
		final int[] colors = SimpleColor.getColorsArray();

		return createColorPickerDialog(
				activity,
				colors,
				new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						block.setColor(colors[position]);
						activity.updateData();
					}
				});
	}

	public static Dialog createBlockTimePickerDialog(final EditBlockActivity activity, final TimeBlock block, final boolean start) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		// title
		builder.setTitle(R.string.set_time);

		// content
		final FrameLayout layout = new FrameLayout(activity);

		final SteppedTimePicker picker = new SteppedTimePicker(activity, 15);
		Calendar calendar = DateUtils.getCalendarInstance(block.getTime(start, false));
		picker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
		picker.setMinute(calendar.get(Calendar.MINUTE));
		layout.addView(picker);

		layout.post(new Runnable() {
			@Override
			public void run() {
				layout.setPadding((layout.getWidth() - 196) / 2, 32, (layout.getWidth() - 196) / 2, 32);
			}
		});
		builder.setView(layout);

		// buttons
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				int hour = picker.getHour();
				int minute = picker.getMinute();

				if (start) {
					Calendar startCalendar = DateUtils.getCalendarInstance(block.getStart());
					startCalendar.set(Calendar.HOUR_OF_DAY, hour);
					startCalendar.set(Calendar.MINUTE, minute);
					block.setBounds(startCalendar.getTimeInMillis(), startCalendar.getTimeInMillis() + block.getDuration(), false);
				} else {
					Calendar endCalendar = DateUtils.getCalendarInstance(block.getEnd());
					endCalendar.set(Calendar.HOUR_OF_DAY, hour);
					endCalendar.set(Calendar.MINUTE, minute);
					block.setEnd(endCalendar.getTimeInMillis());
				}

				activity.updateData();
			}
		});

		// returning dialog instance, but not showing it
		return builder.create();
	}

	public static Dialog createBlockDatePickerDialog(final EditBlockActivity activity, final TimeBlock block) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

		// title
		builder.setTitle(R.string.set_date);

		// content
		final FrameLayout layout = new FrameLayout(activity);

		final NumberPicker picker = new NumberPicker(activity);
		String[] days = new String[5];
		for (int i = 0; i < days.length; i++) {
			days[i] = StringUtils.makeFirstCharUpperCased(DateUtils.FORMAT_WEEKDAY_DAY_MONTH.format(new Date(Core.getCreationTime() + TimeConstants.DAY * (i - 2))));
		}
		picker.setDisplayedValues(days);
		picker.setMinValue(0);
		picker.setMaxValue(days.length - 1);
		picker.setValue((int) ((block.getStart() - Core.getCreationTime()) / TimeConstants.DAY) + 2);
		picker.setWrapSelectorWheel(false);
		picker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		layout.addView(picker);

		layout.post(new Runnable() {
			@Override
			public void run() {
				layout.setPadding((layout.getWidth() - 256) / 2, 32, (layout.getWidth() - 256) / 2, 32);
			}
		});
		builder.setView(layout);

		// buttons
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
			@SuppressWarnings("MagicConstant")
			@Override
			public void onClick(DialogInterface dialog, int which) {
				long time = Core.getCreationTime() + TimeConstants.DAY * (picker.getValue() - 2);
				Calendar calendar = DateUtils.getCalendarInstance(time);
				Calendar start = DateUtils.getCalendarInstance(block.getStart());
				Calendar end = DateUtils.getCalendarInstance(block.getEnd());

				start.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				end.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

				block.setBounds(start.getTimeInMillis(), end.getTimeInMillis(), false);
				activity.updateData();
			}
		});

		// returning dialog instance, but not showing it
		return builder.create();
	}

	public static Dialog createColorPickerDialog(final Context context, final int colors[], final AdapterView.OnItemClickListener listener) {
		// returning dialog instance, but not showing it
		return new Dialog(context) {
			@Override
			protected void onCreate(Bundle savedInstanceState) {
				super.onCreate(savedInstanceState);

				// title
				setTitle(R.string.select_color);

				// content
				GridView gridView = new GridView(context);
				gridView.setPadding(48, 48, 48, 48);
				gridView.setVerticalSpacing(8);
				gridView.setHorizontalSpacing(8);
				gridView.setNumColumns(4);
				gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
				gridView.setAdapter(new ColorPickerAdapter(colors));
				gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						listener.onItemClick(arg0, arg1, arg2, arg3);
						dismiss();
					}
				});
				setContentView(gridView);
			}
		};
	}
}
