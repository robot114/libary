package com.zsm.android.ui;

import com.zsm.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CheckableExpandableChildListAdapter<T extends ExpandableListChildGroup>
				extends CheckableExpandableListAdapter<T> {

	private static final Object TAG_LABEL = new Object();
	private static final Object TAG_CONTENT = new Object();

	public CheckableExpandableChildListAdapter(Context context, boolean itemDuplicable) {
		super(context, itemDuplicable);
	}

	public CheckableExpandableChildListAdapter(Context context) {
		super(context);
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		return getGroup(groupPosition).getChildrenCount();
	}

	@Override
	public String getChild(int groupPosition, int childPosition) {
		return getGroup(groupPosition).getChildShowString(mContext, childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
							 boolean isLastChild, View convertView,
							 ViewGroup parent) {

		TextView labelView;
		TextView contentView;
		if( convertView == null ) {
			String infService = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li
				= (LayoutInflater)mContext.getSystemService( infService );
			convertView = li.inflate( R.layout.checkable_expandable_child_item, null);
			
			labelView = (TextView) convertView.findViewById( R.id.textViewLabel );
			contentView = (TextView) convertView.findViewById( R.id.textViewContent );
			
			labelView.setTag( TAG_LABEL );
			contentView.setTag( TAG_CONTENT );
		} else {
			labelView = (TextView) convertView.findViewWithTag(TAG_LABEL);
			contentView = (TextView) convertView.findViewWithTag(TAG_CONTENT);
		}
		
		T group = getGroup(groupPosition);
		int labelStyleResId = group.getChildLabelStyleResId();
		if( labelStyleResId > 0 ) {
			labelView.setTextAppearance(mContext, labelStyleResId);
		}
		int contentStyleResId = group.getChildTextStyleResId();
		if( contentStyleResId > 0 ) {
			contentView.setTextAppearance(mContext, contentStyleResId);
		}
		
		setLabelViewWidth( labelView, group );
		String label = group.getChildShowLabel(mContext, childPosition);
		if( label != null && label.trim().length() > 0 ) {
			labelView.setText( label );
		}
		contentView.setText( group.getChildShowString(mContext, childPosition) );
		
		return convertView;
	}

	private void setLabelViewWidth( TextView labelView, T group ) {
		int maxWidth = 0;
		for( int i = 0; i < group.getChildrenCount(); i++ ) {
			String text = group.getChildShowLabel(mContext, i);
			int width = (int) labelView.getPaint().measureText( text );
			maxWidth = Math.max(maxWidth, width);
		}
		
		labelView.setMaxWidth(maxWidth);
		labelView.setWidth(maxWidth);
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

}
