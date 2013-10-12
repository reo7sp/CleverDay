package reo7sp.cleverday.ui.activity;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.ColorUtils;

public class EditNotesActivity extends Activity {
	private final TextWatcher notesTextWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			block.setNotes(s.toString());
		}


		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	private TimeBlock block;
	private TextView notesEdit;

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
		long id = getIntent().getLongExtra("id", -1);
		block = Core.getDataCenter().getBlock(id);
		if (block == null) {
			finish();
			return;
		}

		// other...
		getActionBar().setBackgroundDrawable(new ColorDrawable(ColorUtils.darker(block.getColor(), AndroidUtils.isInDarkTheme() ? 0.25f : 0)));
		getActionBar().setTitle(block.getTitle() == null ? getResources().getString(R.string.untitled_block) : block.getTitle());
		setContentView(R.layout.edit_notes_activity);

		// initializing views
		notesEdit = (TextView) findViewById(R.id.notes_edit);
		notesEdit.setText(block.getNotes());
		notesEdit.addTextChangedListener(notesTextWatcher);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (block != null) {
			block.setNotes(notesEdit.getText().toString());
			notesEdit.removeTextChangedListener(notesTextWatcher);
		}
		block = null;
	}
}
