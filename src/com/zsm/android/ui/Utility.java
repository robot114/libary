package com.zsm.android.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
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
	 * @param view the TextView to measure
	 * @return the height of the textView
	 */
	@SuppressWarnings("deprecation")
	public static int getViewHeight(View view) {
	    WindowManager wm =
	            (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
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
	    view.measure(widthMeasureSpec, heightMeasureSpec);
	    return view.getMeasuredHeight();
	}
	
	public static int convertDpToPx(float dp, Context context ) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
	    float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, dm);
	    return Math.round(pixels);
	}
	

	public static Drawable getDrawableFromAttr( Context context, int attrId ) {
		// Create an array of the attributes we want to resolve
		// using values from a theme
		int[] attrs = new int[] { attrId /* index 0 */};

		// Obtain the styled attributes. 'context' is a context with a
		// theme, typically the current Activity (i.e. 'this')
		TypedArray ta = context.obtainStyledAttributes(attrs);

		// To get the value of the 'attrId' attribute that was
		// set in the theme used in 'context'. The parameter is the index
		// of the attribute in the 'attrs' array. The returned Drawable
		// is what you are after
		Drawable drawableFromTheme = ta.getDrawable(0 /* index */);

		// Finally, free the resources used by TypedArray
		ta.recycle();
		
		return drawableFromTheme;
	}
}
