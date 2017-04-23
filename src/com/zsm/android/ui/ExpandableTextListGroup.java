package com.zsm.android.ui;

import android.content.Context;

public interface ExpandableTextListGroup {

	String getShowString( Context context );
	
	/**
	 * Get style resource id of this group
	 * 
	 * @return style resource id, or 0 when no style defined
	 */
	int getGroupStyleResId();
	
}
