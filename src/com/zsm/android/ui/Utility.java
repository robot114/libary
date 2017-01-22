package com.zsm.android.ui;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class Utility {

	private Utility() {
		
	}
	
	/**
	 * Get the TextView height before the TextView will render
	 * @param textView the TextView to measure
	 * @return the height of the textView
	 */
	@SuppressWarnings("deprecation")
	public static int getTextViewHeight(TextView textView) {
	    WindowManager wm =
	            (WindowManager) textView.getContext().getSystemService(Context.WINDOW_SERVICE);
	    Display display = wm.getDefaultDisplay();

	    int deviceWidth;

	    if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
	        Point size = new Point();
	        display.getSize(size);
	        deviceWidth = size.x;
	    } else {
	        deviceWidth = display.getWidth();
	    }

	    int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
	    int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
	    textView.measure(widthMeasureSpec, heightMeasureSpec);
	    return textView.getMeasuredHeight();
	}
	
	public static int convertDpToPx(float dp, Context context ) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
	    float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
	    return Math.round(pixels);
	}
	

}
