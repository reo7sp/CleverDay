package reo7sp.cleverday.ui.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.AutoCompleteTextView;

public class CleanableAutoCompleteTextView extends AutoCompleteTextView implements OnTouchListener, OnFocusChangeListener, TextWatcher {
	private Drawable clearButtonDrawable;
	private OnClearListener onClearListener;
	private OnTouchListener onTouchListener;
	private OnFocusChangeListener onFocusChangeListener;

	public CleanableAutoCompleteTextView(Context context) {
		super(context);
		init();
	}

	public CleanableAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CleanableAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		clearButtonDrawable = getCompoundDrawables()[2];
		if (clearButtonDrawable == null) {
			clearButtonDrawable = getResources().getDrawable(android.R.drawable.presence_offline);
		}
		clearButtonDrawable.setBounds(0, 0, clearButtonDrawable.getIntrinsicWidth(), clearButtonDrawable.getIntrinsicHeight());
		setClearIconVisible(false);
		super.setOnTouchListener(this);
		super.setOnFocusChangeListener(this);
		addTextChangedListener(this);
	}

	public interface OnClearListener {
		void onClearText();
	}

	public void setListener(OnClearListener listener) {
		this.onClearListener = listener;
	}

	@Override
	public void setOnTouchListener(OnTouchListener listener) {
		this.onTouchListener = listener;
	}

	@Override
	public void setOnFocusChangeListener(OnFocusChangeListener listener) {
		this.onFocusChangeListener = listener;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (getCompoundDrawables()[2] != null) {
			boolean tappedClearButton = event.getX() > (getWidth() - getPaddingRight() - clearButtonDrawable.getIntrinsicWidth());
			if (tappedClearButton) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					setText("");
					if (onClearListener != null) {
						onClearListener.onClearText();
					}
				}
				return true;
			}
		}
		return onTouchListener != null && onTouchListener.onTouch(v, event);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			setClearIconVisible(!getText().toString().isEmpty());
		} else {
			setClearIconVisible(false);
		}
		if (onFocusChangeListener != null) {
			onFocusChangeListener.onFocusChange(v, hasFocus);
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (isFocused()) {
			setClearIconVisible(!s.toString().isEmpty());
		}
	}

	private void setClearIconVisible(boolean visible) {
		Drawable clearButton = visible ? clearButtonDrawable : null;
		setCompoundDrawables(getCompoundDrawables()[0], getCompoundDrawables()[1], clearButton, getCompoundDrawables()[3]);
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}
}
