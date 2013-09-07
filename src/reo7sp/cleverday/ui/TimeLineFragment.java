package reo7sp.cleverday.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import reo7sp.cleverday.ui.view.TimeLineView;

public class TimeLineFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		TimeLineView timeLine = new TimeLineView(getActivity(), getArguments().getLong("time"));
		timeLine.setTag("tl_" + getArguments().getInt("pos"));
		return timeLine;
	}
}
