package reo7sp.cleverday.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.DateFormatter;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.data.DataCenter;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.ui.TimePreference;
import reo7sp.cleverday.ui.timeline.TimeBlockView;
import reo7sp.cleverday.ui.timeline.TimeLineView;
import reo7sp.cleverday.utils.AndroidUtils;
import reo7sp.cleverday.utils.DateUtils;
import reo7sp.cleverday.utils.StringUtils;

public class MainActivity extends FragmentActivity {
	public static final int TIMELINES_COUNT = 5;
	private static boolean firstCreation = true;
	private static int viewPagerPos = TIMELINES_COUNT / 2;
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

		// welcome
		if (!AndroidUtils.isWelcomeScreenCompleted()) {
			startActivity(new Intent(this, WelcomeActivity.class));
		}

		// content
		setContentView(R.layout.main_activity);
		viewPager = (ViewPager) findViewById(R.id.time_line_pager);
		viewPager.setAdapter(new TimeLinePagerAdapter());
		viewPager.setOffscreenPageLimit(16);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setTitle(StringUtils.makeFirstCharUpperCased(viewPager.getAdapter().getPageTitle(position).toString()));
				if (viewPagerPos != position) {
					TimeBlockView view = Core.getTimeLinesLeader().getEditingBlock();
					if (view != null) {
						TimeBlock block = view.getBlock();
						block.setBounds(block.getUtcStart() + TimeConstants.DAY * -1 * (viewPagerPos - position), block.getUtcEnd() + TimeConstants.DAY * -1 * (viewPagerPos - position), true);
						view.update(true);
					}
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
		DataCenter.getInstance().syncData();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// cleaning up
		Core.getTimeLinesLeader().clean();
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

			case R.id.action_main_menu_sync:
				Toast.makeText(Core.getContext(), R.string.syncing, Toast.LENGTH_SHORT).show();
				Core.getDataCenter().syncData();
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

	private class TimeLinePagerAdapter extends FragmentStatePagerAdapter {
		public TimeLinePagerAdapter() {
			super(getSupportFragmentManager());
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new TimeLineFragment();
			Bundle args = new Bundle();
			args.putLong("time", Core.getCreationTime() + TimeConstants.DAY * (i - getCount() / 2));
			args.putInt("pos", i);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return TIMELINES_COUNT;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			long time = Core.getCreationTime() + TimeConstants.DAY * (position - getCount() / 2);
			switch (position) {
				case TIMELINES_COUNT / 2 - 1: // yesterday
					return Core.getContext().getResources().getString(R.string.yesterday) + ", " + Core.getDateFormatter().format(DateFormatter.Format.DAY_MONTH, time);

				case TIMELINES_COUNT / 2: // today
					return Core.getContext().getResources().getString(R.string.today) + ", " + Core.getDateFormatter().format(DateFormatter.Format.DAY_MONTH, time);

				case TIMELINES_COUNT / 2 + 1: // tomorrow
					return Core.getContext().getResources().getString(R.string.tomorrow) + ", " + Core.getDateFormatter().format(DateFormatter.Format.DAY_MONTH, time);

				default:
					return Core.getDateFormatter().format(DateFormatter.Format.WEEKDAY_DAY_MONTH, time);
			}
		}
	}

	private class TimeLineFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			TimeLineView timeLine = new TimeLineView(getActivity(), getArguments().getLong("time"));
			timeLine.setTag("tl_" + getArguments().getInt("pos"));
			return timeLine;
		}
	}
}
