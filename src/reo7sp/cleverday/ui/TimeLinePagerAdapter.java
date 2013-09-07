package reo7sp.cleverday.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Date;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.TimeConstants;
import reo7sp.cleverday.utils.DateUtils;

public class TimeLinePagerAdapter extends FragmentStatePagerAdapter {
	public static final int COUNT = 5;

	public TimeLinePagerAdapter() {
		super(Core.getMainActivity().getSupportFragmentManager());
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
		return COUNT;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Date date = new Date(Core.getCreationTime() + TimeConstants.DAY * (position - getCount() / 2));
		switch (position) {
			case COUNT / 2 - 1: // yesterday
				return Core.getContext().getResources().getString(R.string.yesterday) + ", " + DateUtils.FORMAT_DAY_MONTH.format(date);

			case COUNT / 2: // today
				return Core.getContext().getResources().getString(R.string.today) + ", " + DateUtils.FORMAT_DAY_MONTH.format(date);

			case COUNT / 2 + 1: // tomorrow
				return Core.getContext().getResources().getString(R.string.tomorrow) + ", " + DateUtils.FORMAT_DAY_MONTH.format(date);

			default:
				return DateUtils.FORMAT_WEEKDAY_DAY_MONTH.format(date);
		}
	}
}
