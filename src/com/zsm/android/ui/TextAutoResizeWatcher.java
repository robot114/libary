package com.zsm.android.ui;

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class TextAutoResizeWatcher implements TextWatcher {

	private Context mContext;
	private TextView mTextView;
	private int mMinSp;
	private int mMaxSp;

	public TextAutoResizeWatcher(Context context, TextView view, int minSp) {
		this( context, view, minSp, 0 );
	}

	/**
	 * Constructor for the text watcher
	 * 
	 * @param context the context
	 * @param view the textview in which the size of the font will be auto adjusted
	 * @param minSp the minimize sp to resize
	 * @param maxSp the maximize sp to resize. If this is 0, the current font size
	 * 			of the view is the max one. And the height of the view will keep
	 * 			unchanged
	 */
	public TextAutoResizeWatcher(Context context, TextView view, int minSp, int maxSp) {
		mContext = context;
		mTextView = view;
		mMinSp = minSp;
		mMaxSp = maxSp;
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}
	
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
		if( mMaxSp == 0 ) {
			mMaxSp = (int) pixelsToSp(mTextView.getTextSize());
		}
	}

	@Override
	public void afterTextChanged(Editable editable) {

		final int widthLimitPixels = mTextView.getWidth() - mTextView.getPaddingRight()
				- mTextView.getPaddingLeft();
		Paint paint = new Paint();
		float fontSizeSP = pixelsToSp(mTextView.getTextSize());
		paint.setTextSize(spToPixels(fontSizeSP));

		String viewText = mTextView.getText().toString();

		float widthPixels = paint.measureText(viewText);

		// Increase font size if necessary.
		if (widthPixels < widthLimitPixels) {
			while (widthPixels < widthLimitPixels && fontSizeSP <= mMaxSp) {
				++fontSizeSP;
				paint.setTextSize(spToPixels(fontSizeSP));
				widthPixels = paint.measureText(viewText);
			}
			--fontSizeSP;
		}
		// Decrease font size if necessary.
		else {
			while (widthPixels > widthLimitPixels || fontSizeSP > mMaxSp) {
				if (fontSizeSP < mMinSp) {
					fontSizeSP = mMinSp;
					break;
				}
				--fontSizeSP;
				paint.setTextSize(spToPixels(fontSizeSP));
				widthPixels = paint.measureText(viewText);
			}
		}

		mTextView.setTextSize(fontSizeSP);
	}

	private float pixelsToSp(float px) {
		float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
		return px / scaledDensity;
	}

	private float spToPixels(float sp) {
		float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
		return sp * scaledDensity;
	}

}
