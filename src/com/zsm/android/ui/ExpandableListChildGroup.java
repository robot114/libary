package com.zsm.android.ui;

import android.content.Context;

public interface ExpandableListChildGroup extends ExpandableListGroup {

	int getChildrenCount();
	
	/**
	 * Get label of a child
	 * @param childPosition
	 * @return label of the child, null or empty string for no label
	 */
	String getChildShowLabel( Context context, int childPosition );
	String getChildShowString( Context context, int childPosition );
	
	/**
	 * Get style resource id of the label of the child
	 * 
	 * @return style resource id, or 0 when no style defined
	 */
	int getChildLabelStyleResId( );
	
	/**
	 * Get style resource id of the context of the child
	 * 
	 * @return style resource id, or 0 when no style defined
	 */
	int getChildTextStyleResId( );
}
