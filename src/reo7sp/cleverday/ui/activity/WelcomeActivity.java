package reo7sp.cleverday.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.utils.AndroidUtils;

/**
 * Created by reo7sp on 9/8/13 at 6:20 PM
 */
public class WelcomeActivity extends FragmentActivity {
	private static Slide[] slides = {
			new HelloSlide(),
			new PlanFastSlide(),
			new EditFastSlide(),
	};

	public void onCreate(Bundle savedInstanceState) {
		// building core
		Core.startBuilding().build();

		// other...
		AndroidUtils.updateTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_activity);

		// save visit
		SharedPreferences.Editor editor = Core.getPreferences().edit();
		editor.putBoolean("pref_welcome", true);
		editor.commit();

		// content
		ViewPager viewPager = (ViewPager) findViewById(R.id.slides_pager);
		viewPager.setAdapter(new SlidesPagerAdapter());
		viewPager.setOffscreenPageLimit(16);
	}

	private class SlidesPagerAdapter extends FragmentStatePagerAdapter {
		public SlidesPagerAdapter() {
			super(getSupportFragmentManager());
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new SlidesFragment();
			Bundle args = new Bundle();
			args.putInt("pos", i);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return slides.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getResources().getString(slides[position].getTitle());
		}
	}

	private class SlidesFragment extends Fragment {
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.welcome_slide, null);
			ImageView imageView = (ImageView) view.findViewById(R.id.slide_image);
			TextView titleView = (TextView) view.findViewById(R.id.slide_title_text);
			TextView textView = (TextView) view.findViewById(R.id.slide_text);

			int pos = getArguments().getInt("pos");
			Slide slide = slides[pos];
			imageView.setImageDrawable(getResources().getDrawable(slide.getDrawable()));
			titleView.setText(getResources().getString(slide.getTitle()));
			textView.setText(getResources().getString(slide.getText()));

			return view;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.welcome_activity, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_ok:
				finish();
				break;
		}
		return true;
	}

	private static abstract class Slide {
		public abstract int getDrawable();

		public abstract int getTitle();

		public abstract int getText();
	}

	private static class HelloSlide extends Slide {
		@Override
		public int getDrawable() {
			return R.drawable.ic_launcher_big;
		}

		@Override
		public int getTitle() {
			return R.string.hello;
		}

		@Override
		public int getText() {
			return R.string.hello_text;
		}
	}

	private static class PlanFastSlide extends Slide {
		@Override
		public int getDrawable() {
			return R.drawable.ic_launcher_big;
		}

		@Override
		public int getTitle() {
			return R.string.plan_fast;
		}

		@Override
		public int getText() {
			return R.string.plan_fast_text;
		}
	}

	private static class EditFastSlide extends Slide {
		@Override
		public int getDrawable() {
			return R.drawable.ic_launcher_big;
		}

		@Override
		public int getTitle() {
			return R.string.edit_fast;
		}

		@Override
		public int getText() {
			return R.string.edit_fast_text;
		}
	}
}