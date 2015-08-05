package com.zsm.android.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class ClickableSpinner extends Spinner {

	private OnItemClickListener onClickListener;
	private SpinnerAdapter adapter;

	public ClickableSpinner(Context context, AttributeSet attrs, int defStyle,
			int mode) {
		super(context, attrs, defStyle, mode);
	}

	public ClickableSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public ClickableSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ClickableSpinner(Context context, int mode) {
		super(context, mode);
	}

	public ClickableSpinner(Context context) {
		super(context);
	}
	
	@Override
	public void setOnItemClickListener(OnItemClickListener l) {
		onClickListener = l;
		if( adapter != null && adapter instanceof SpinnerClickAdapter ) {
			((SpinnerClickAdapter)adapter).setOnItemClickListener(l);
		}
	}
	
	@Override
	public void setAdapter( SpinnerAdapter a ) {
		super.setAdapter(a);
		adapter = a;
		if( a instanceof SpinnerClickAdapter ) {
			((SpinnerClickAdapter)adapter).setOnItemClickListener(onClickListener);
			((SpinnerClickAdapter)adapter).setSpinner( this );
		}
	}

	void dismissDropDown() {
		onDetachedFromWindow();
	}
}
