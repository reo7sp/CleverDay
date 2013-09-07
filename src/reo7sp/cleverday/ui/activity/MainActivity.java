package reo7sp.cleverday.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.data.DataCenter;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.ui.TimeLinePagerAdapter;
import reo7sp.cleverday.ui.preference.TimePreference;
import reo7sp.cleverday.ui.view.TimeBlockView;
import reo7sp.cleverday.ui.view.TimeLineView;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.DateUtils;
import reo7sp.cleverday.utils.StringUtils;

public class MainActivity extends FragmentActivity {
	private static boolean firstCreation = true;
	private static int viewPagerPos = TimeLinePagerAdapter.COUNT / 2;
	private ViewPager viewPager;
	private TimeLineView currentTimeLine;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// building core
		Core.startBuilding()
				.setMainActivity(this)
				.build();

		// other...
		AndroidUtils.updateTheme(this);
		super.onCreate(savedInstanceState);
		getActionBar().setLogo(android.R.color.transparent);

		// content
		setContentView(R.layout.main_activity);
		viewPager = (ViewPager) findViewById(R.id.time_line_pager);
		viewPager.setAdapter(new TimeLinePagerAdapter());
		viewPager.setOffscreenPageLimit(16);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setTitle(StringUtils.makeFirstCharUpperCased(viewPager.getAdapter().getPageTitle(position).toString()));
				if (Core.getTimeLinesLeader().isAnyBlockSelected() && viewPagerPos != position) {
					TimeBlockView view = Core.getTimeLinesLeader().getEditingBlock();
					TimeBlock block = view.getBlock();
					block.setBounds(block.getUtcStart() + TimeConstants.DAY * -1 * (viewPagerPos - position), block.getUtcEnd() + TimeConstants.DAY * -1 * (viewPagerPos - position), true);
					view.update(true);
				}
				currentTimeLine = (TimeLineView) viewPager.findViewWithTag("tl_" + position);
				viewPagerPos = position;
			}
		});

		// other...
		viewPager.post(new Runnable() {
			@Override
			public void run() {
				viewPager.setCurrentItem(viewPagerPos, false);
				if (firstCreation) {
					Core.getTimeLinesLeader().init();
					firstCreation = false;
				}
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		DataCenter.getInstance().receiveData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// cleaning up
		Core.getTimeLinesLeader().removeAllSlaves();
		AndroidUtils.recycleView(findViewById(R.layout.main_activity));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_activity, menu);
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.block_edit, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_main_menu_add:
				TimeBlock block = Core.getDataCenter().newTimeBlock();

				// moving time block to right time
				long startOfToday = DateUtils.trimToDay(System.currentTimeMillis()) + TimePreference.getHour(Core.getPreferences().getString("pref_day_start", "9:00")) * TimeConstants.HOUR;
				if (block.getStart() < startOfToday) {
					block.move(startOfToday - block.getStart());
				}

				// moving time block to right day
				long diff = DateUtils.trimToDay(currentTimeLine.getTime()) - DateUtils.trimToDay(block.getStart());
				if (diff != 0) {
					block.move(diff);
					Core.getTimeLinesLeader().removeTimeBlock(block);
					Core.getTimeLinesLeader().addTimeBlock(block);
				}

				EditBlockActivity.showAdd(block);
				break;
			case R.id.action_main_menu_settings:
				startActivity(new Intent(this, SettingsActivity.class));
				break;
			case R.id.action_main_menu_send_log:
				Intent sendIntent = new Intent();
				sendIntent.setAction(Intent.ACTION_SEND);
				sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(Log.getLogFile()));
				sendIntent.setType("text/plain");
				startActivity(Intent.createChooser(sendIntent, "Send log to"));
				break;
		}
		return true;
	}

	public ViewPager getViewPager() {
		return viewPager;
	}

	public TimeLineView getCurrentTimeLine() {
		return currentTimeLine;
	}
}