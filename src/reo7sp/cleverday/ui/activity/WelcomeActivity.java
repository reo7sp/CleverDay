package reo7sp.cleverday.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.Dialogs;
import reo7sp.cleverday.ui.colors.SimpleColor;
import reo7sp.cleverday.utils.AndroidUtils;

/**
 * Created by reo7sp on 9/8/13 at 6:20 PM
 */
public class WelcomeActivity extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		// building core
		Core.startBuilding().build();

		// other...
		AndroidUtils.updateTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.welcome_activity);
		getActionBar().hide();

		// finding views
		Button loginButton = (Button) findViewById(R.id.login_button);
		Button skipButton = (Button) findViewById(R.id.skip_button);

		// setting listeners
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Dialog dialog = Dialogs.createCalendarChooseDialog(WelcomeActivity.this);
				dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						onWelcomeScreenPass();
					}
				});
				dialog.show();
			}
		});
		skipButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onWelcomeScreenPass();
			}
		});
	}

	public void onWelcomeScreenPass() {
		TimeBlock block = Core.getDataCenter().newTimeBlock();
		block.setTitle(getResources().getString(R.string.press_me));
		block.setNotes(getResources().getString(R.string.and_drag));
		block.setColor(SimpleColor.RED);

		SharedPreferences.Editor editor = Core.getPreferences().edit();
		editor.putBoolean("pref_welcome", true);
		editor.commit();

		finish();
	}
}