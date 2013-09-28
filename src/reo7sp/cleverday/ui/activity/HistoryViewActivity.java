package reo7sp.cleverday.ui.activity;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.HistoryStorage;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.ColorUtils;
import reo7sp.cleverday.utils.DateUtils;

public class HistoryViewActivity extends Activity {
	private final Map<Integer, TimeBlock> history = new HashMap<Integer, TimeBlock>();
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
		getActionBar().setBackgroundDrawable(new ColorDrawable(ColorUtils.darker(block.getColor(), AndroidUtils.isInDarkTheme() ? 0.25f : 0)));
		invalidateOptionsMenu();
		setContentView(R.layout.history_view_activity);

		// finding views
		ListView historyList = (ListView) findViewById(R.id.history_list);

		// fetching completions
		int i = 0;
		for (TimeBlock block : HistoryStorage.getHistory()) {
			history.put(i++, block);
		}

		if (history.isEmpty()) {
			setContentView(R.layout.stub);
			TextView stubText = (TextView) findViewById(R.id.stub_text);
			stubText.setText(R.string.no_history);
			return;
		}

		// filling adapter
		String[] historyStrings = new String[history.size()];
		for (i = 0; i < history.size(); i++) {
			historyStrings[i] = history.get(i).toString();
		}
		ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, historyStrings);
		historyList.setAdapter(adapter);

		// setting listeners
		historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

				finish();
			}
		});
	}
}
