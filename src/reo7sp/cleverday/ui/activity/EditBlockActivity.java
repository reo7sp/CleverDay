package reo7sp.cleverday.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.DateFormatter;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.HistoryStorage;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.Dialogs;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.ColorUtils;
import reo7sp.cleverday.utils.DateUtils;

public class EditBlockActivity extends Activity {
	private final TextWatcher titleTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			block.setTitle(s.toString());
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	private final Map<Integer, TimeBlock> history = new HashMap<Integer, TimeBlock>();
	private TimeBlock block;
	private TimeBlock backup;
	private boolean create;
	private AutoCompleteTextView titleEdit;
	private TextView notesEdit;
	private Button startTimeButton;
	private Button startDateButton;
	private Button endTimeButton;
	private Button endDateButton;
	private ImageButton historyButton;
	private Button cancelButton;
	private Button saveButton;

	public static void showAdd(TimeBlock block) {
		Intent intent = new Intent(Core.getContext(), EditBlockActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("create", true);
		if (block != null) {
			intent.putExtra("id", block.getID());
		}
		Core.getContext().startActivity(intent);
	}

	public static void showEdit(TimeBlock block) {
		Intent intent = new Intent(Core.getContext(), EditBlockActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("create", false);
		intent.putExtra("id", block.getID());
		Core.getContext().startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// building core
		Core.startBuilding()
				.setContext(getApplicationContext())
				.build();

		// other...
		AndroidUtils.updateTheme(this);
		super.onCreate(savedInstanceState);

		// parsing intent args
		create = getIntent().getBooleanExtra("create", false);
		int id = getIntent().getIntExtra("id", -1);
		if (create && id == -1) {
			block = Core.getDataCenter().newTimeBlock();
		} else {
			block = Core.getDataCenter().getBlock(id);
		}
		if (block == null) {
			finish();
			return;
		}
		backup = block.copy();

		// other...
		getActionBar().setTitle(create ? R.string.add_block : R.string.edit_block);
		setContentView(R.layout.edit_block_activity);

		// finding views
		titleEdit = (AutoCompleteTextView) findViewById(R.id.title_edit);
		notesEdit = (TextView) findViewById(R.id.note_edit);
		startTimeButton = (Button) findViewById(R.id.start_time_button);
		startDateButton = (Button) findViewById(R.id.start_date_button);
		endTimeButton = (Button) findViewById(R.id.end_time_button);
		endDateButton = (Button) findViewById(R.id.end_date_button);
		historyButton = (ImageButton) findViewById(R.id.history_button);
		cancelButton = (Button) findViewById(R.id.cancel_button);
		saveButton = (Button) findViewById(R.id.save_button);

		// fetching completions
		int i = 0;
		for (TimeBlock block : HistoryStorage.getHistory()) {
			history.put(i++, block);
		}

		// filling adapter
		String[] historyStrings = new String[history.size()];
		for (i = 0; i < history.size(); i++) {
			historyStrings[i] = history.get(i).toString();
		}
		ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyStrings);
		titleEdit.setAdapter(adapter);

		// setting listeners
		titleEdit.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> list, View view, int pos, long id) {
				TimeBlock historyBlock = HistoryStorage.getFromHistory(history.get(pos).getTitle());
				if (historyBlock == null) {
					return;
				}

				block.setTitle(historyBlock.getTitle());
				block.setNotes(historyBlock.getNotes());
				block.setColor(historyBlock.getColor());
				block.setReminder(historyBlock.hasReminder());

				long dayStart = DateUtils.trimToDay(System.currentTimeMillis());
				long start = dayStart + historyBlock.getStart() - DateUtils.trimToDay(historyBlock.getStart());
				long end = dayStart + historyBlock.getEnd() - DateUtils.trimToDay(historyBlock.getEnd());
				block.setBounds(start, end, false);

				updateData();
			}
		});
		titleEdit.addTextChangedListener(titleTextWatcher);
		notesEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditBlockActivity.this, EditNotesActivity.class);
				intent.putExtra("id", block.getID());
				startActivity(intent);
			}
		});
		startTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				Dialogs.createBlockTimePickerDialog(EditBlockActivity.this, block, true).show();
			}
		});
		startDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialogs.createBlockDatePickerDialog(EditBlockActivity.this, block).show();
			}
		});
		endTimeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialogs.createBlockTimePickerDialog(EditBlockActivity.this, block, false).show();
			}
		});
		endDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialogs.createBlockDatePickerDialog(EditBlockActivity.this, block).show();
			}
		});
		historyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(EditBlockActivity.this, HistoryViewActivity.class);
				intent.putExtra("id", block.getID());
				startActivity(intent);
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (create) {
					block.remove();
				} else {
					block.setTitle(backup.getTitle());
					block.setBounds(backup.getUtcStart(), backup.getUtcEnd(), true);
					block.setColor(backup.getColor());
					block.setReminder(backup.hasReminder());
					block.setNotes(backup.getNotes());
				}

				finish();
			}
		});
		saveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (block != null) {
			// removing listeners
			titleEdit.removeTextChangedListener(titleTextWatcher);
			notesEdit.setOnClickListener(null);
			startTimeButton.setOnClickListener(null);
			startDateButton.setOnClickListener(null);
			endTimeButton.setOnClickListener(null);
			endDateButton.setOnClickListener(null);
		}
		block = null;
		backup = null;
	}

	@Override
	protected void onStart() {
		super.onStart();
		updateData();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_edit_menu_reminder:
				block.setReminder(!block.hasReminder());
				updateData();
				break;

			case R.id.action_edit_menu_color:
				Dialogs.createBlockColorPickerDialog(this, block).show();
				break;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.edit_activity, menu);
		if (block != null) {
			menu.findItem(R.id.action_edit_menu_reminder).setIcon(block.hasReminder() ? R.drawable.ic_alarm_on_light : R.drawable.ic_alarm_light);
		}
		return true;
	}

	public void updateData() {
		getActionBar().setBackgroundDrawable(new ColorDrawable(ColorUtils.darker(block.getColor(), AndroidUtils.isInDarkTheme() ? 0.25f : 0)));
		invalidateOptionsMenu();
		titleEdit.setText(block.getTitle());
		notesEdit.setText(block.getNotes());
		startTimeButton.setText(Core.getDateFormatter().format(DateFormatter.Format.HOUR_MINUTE, block.getStart()));
		startDateButton.setText(Core.getDateFormatter().format(DateFormatter.Format.WEEKDAY_DAY_MONTH_YEAR, block.getStart()));
		endTimeButton.setText(Core.getDateFormatter().format(DateFormatter.Format.HOUR_MINUTE, block.getEnd()));
		endDateButton.setText(Core.getDateFormatter().format(DateFormatter.Format.WEEKDAY_DAY_MONTH_YEAR, block.getEnd()));
	}
}
