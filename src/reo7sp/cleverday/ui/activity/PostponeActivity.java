package reo7sp.cleverday.ui.activity;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.ColorUtils;

public class PostponeActivity extends Activity {
	private TimeBlock block;

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
		int id = getIntent().getIntExtra("id", -1);
		block = Core.getDataCenter().getBlock(id);
		if (block == null) {
			finish();
			return;
		}

		// other...
		getActionBar().setTitle(getResources().getString(R.string.postpone) + " \"" + block.getHumanTitle() + "\"");
		getActionBar().setBackgroundDrawable(new ColorDrawable(ColorUtils.darker(block.getColor(), AndroidUtils.isInDarkTheme() ? 0.25f : 0)));
		invalidateOptionsMenu();
		setContentView(R.layout.postpone_activity);

		// finding views
		ListView postponeList = (ListView) findViewById(R.id.postpone_list);

		// setting data
		final long[] durations = {
				TimeConstants.QUARTER_OF_HOUR,
				TimeConstants.HALF_OF_HOUR,
				TimeConstants.HOUR,
				TimeConstants.HOUR + TimeConstants.HALF_OF_HOUR,
				2 * TimeConstants.HOUR,
				4 * TimeConstants.HOUR,
				TimeConstants.DAY,
		};
		String[] durationsStrings = {
				getResources().getString(R.string.minutes15),
				getResources().getString(R.string.minutes30),
				getResources().getString(R.string.hour),
				getResources().getString(R.string.hour1minutes30),
				getResources().getString(R.string.hours2),
				getResources().getString(R.string.hours4),
				getResources().getString(R.string.tomorrow),
		};
		ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, durationsStrings);
		postponeList.setAdapter(adapter);

		// setting listeners
		postponeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				block.move(durations[position]);
				block.setReminder(true);
				finish();
			}
		});
	}
}
