package reo7sp.cleverday.ui.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.GoogleCalendar;
import reo7sp.cleverday.data.GoogleCalendarStorage;
import reo7sp.cleverday.log.Log;
import reo7sp.cleverday.utils.AndroidUtils;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AndroidUtils.updateTheme(this);
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		updateAllPrefs();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Core.getPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Core.getPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	private void updateAllPrefs() {
		for (String key : Core.getPreferences().getAll().keySet()) {
			updatePref(key, false);
		}
	}

	@SuppressWarnings("deprecation")
	private void updatePref(String key, boolean isUser) {
		updatePref(findPreference(key), isUser);
	}

	private void updatePref(Preference pref, boolean isUser) {
		if (pref == null || pref.getKey() == null) {
			return;
		}
		Log.i("Settings", "Updating preference " + pref.getKey());

		if (pref.getKey().equals("pref_day_start")) {
			pref.setSummary(getResources().getString(R.string.time_line_scroll_start).replace('9', Core.getPreferences().getString(pref.getKey(), "9:00").charAt(0)));
		} else if (pref.getKey().equals("pref_google_calendar")) {
			// updating google calendar storage settings
			GoogleCalendarStorage.updateSettings();

			// setting summary
			pref.setSummary(GoogleCalendarStorage.getMainCalendar() == null ? getResources().getString(R.string.none) : GoogleCalendarStorage.getMainCalendar().getName());

			// setting value to none if main calendar == null
			if (GoogleCalendarStorage.getMainCalendar() == null) {
				SharedPreferences.Editor editor = Core.getPreferences().edit();
				editor.putString(pref.getKey(), getResources().getString(R.string.none));
				editor.commit();
			}

			// updating preference list entries
			CharSequence[] calendars = new CharSequence[GoogleCalendarStorage.getCalendars().size() + 1];
			CharSequence[] calendarValues = new CharSequence[GoogleCalendarStorage.getCalendars().size() + 1];
			calendars[0] = getResources().getString(R.string.none);
			calendarValues[0] = "none";
			int i = 1;
			for (GoogleCalendar calendar : GoogleCalendarStorage.getCalendars()) {
				calendars[i] = calendar.getName();
				calendarValues[i] = "" + calendar.getID();
				i++;
			}
			ListPreference listPref = (ListPreference) pref;
			listPref.setEntries(calendars);
			listPref.setEntryValues(calendarValues);
		} else if (pref.getKey().equals("pref_google_sync_priority")) {
			pref.setSummary(Core.getPreferences().getString(pref.getKey(), "0").equals("0") ? R.string.app_name : R.string.google_calendar);
		} else if (pref.getKey().equals("pref_dark_theme")) {
			if (isUser) {
				finish();
				Core.getMainActivity().recreate();
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
		updatePref(key, true);
	}
}
