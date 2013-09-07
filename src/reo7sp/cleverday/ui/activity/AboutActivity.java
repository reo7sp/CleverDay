package reo7sp.cleverday.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.utils.AndroidUtils;

public class AboutActivity extends Activity {
	private String[] authors;
	private int currentAuthorText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AndroidUtils.updateTheme(this);
		super.onCreate(savedInstanceState);

		setContentView(R.layout.about_activity);

		authors = new String[] {
				getResources().getString(R.string.created_by) + " Oleg Morozenkov",
				getResources().getString(R.string.idea_by) + " Konstantin Morozenkov",
		};

		final TextView authorTextView = (TextView) findViewById(R.id.about_author);
		final TextView versionTextView = (TextView) findViewById(R.id.about_version);

		versionTextView.setText("v" + Core.VERSION);

		if (authors.length == 1) {
			authorTextView.setText(authors[0]);
		} else if (authors.length != 0) {
			authorTextView.post(new Runnable() {
				@Override
				public void run() {
					authorTextView.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							if (currentAuthorText == authors.length) {
								currentAuthorText = 0;
							}
							authorTextView.setText(authors[currentAuthorText++]);
							authorTextView.animate().alpha(1).setListener(null);
						}
					});
					authorTextView.postDelayed(this, 3000);
				}
			});
		}
	}
}
