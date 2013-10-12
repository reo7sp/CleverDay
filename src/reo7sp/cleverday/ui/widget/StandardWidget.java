package reo7sp.cleverday.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import reo7sp.cleverday.Core;
import reo7sp.cleverday.R;
import reo7sp.cleverday.data.DataInvalidateListener;
import reo7sp.cleverday.data.TimeBlock;
import reo7sp.cleverday.ui.activity.MainActivity;

/**
 * Created by reo7sp on 9/6/13 at 6:11 PM
 */
public class StandardWidget extends AppWidgetProvider implements DataInvalidateListener {
	private int[] appWidgetIds;
	private TimeBlock current, next, later;

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
		Core.getDataCenter().registerDataInvalidateListener(this);
		appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, getClass()));
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
		Core.getDataCenter().unregisterDataInvalidateListener(this);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.standard_widget);
		this.appWidgetIds = appWidgetIds;

		// setting listeners
		Intent appOpenIntent = new Intent(context, MainActivity.class);
		appOpenIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		remoteViews.setOnClickPendingIntent(R.id.root, PendingIntent.getActivity(context, 0, appOpenIntent, PendingIntent.FLAG_UPDATE_CURRENT));

		// building core
		Core.startBuilding().setContext(context).build();

		// updating data
		findNextTimeBlocks();
		if (current == null) {
			if (next == null) {
				remoteViews.setTextViewText(R.id.first_text, context.getResources().getString(R.string.no_plan));
				remoteViews.setTextViewText(R.id.second_text, "");
			} else {
				remoteViews.setTextViewText(R.id.first_text, context.getResources().getString(R.string.next) + ": " + next);
				if (later == null) {
					remoteViews.setTextViewText(R.id.second_text, "");
				} else {
					remoteViews.setTextViewText(R.id.first_text, context.getResources().getString(R.string.later) + ": " + later);
				}
			}
		} else {
			remoteViews.setTextViewText(R.id.first_text, context.getResources().getString(R.string.now) + ": " + current);
			if (next == null) {
				remoteViews.setTextViewText(R.id.second_text, "");
			} else {
				remoteViews.setTextViewText(R.id.second_text, context.getResources().getString(R.string.next) + ": " + next);
			}
		}
		appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
	}

	private void findNextTimeBlocks() {
		current = null;
		next = null;
		later = null;

		long now = System.currentTimeMillis();
		int i = 0;
		for (TimeBlock block : Core.getDataCenter().getTimeBlocks()) {
			if (i >= 3) {
				break;
			} else if (block.getEnd() < now) {
				continue;
			} else if (i == 0 && !(block.getStart() < now && block.getEnd() > now)) {
				i++;
			}

			switch (i) {
				case 0:
					current = block;
					break;
				case 1:
					next = block;
					break;
				case 2:
					later = block;
					break;
			}
		}
	}

	@Override
	public void onDataInvalidate() {
		Intent intent = new Intent(Core.getContext(), StandardWidget.class);
		intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		Core.getContext().sendBroadcast(intent);
	}
}
