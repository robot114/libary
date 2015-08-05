package com.zsm.android.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SpinnerAdapter;

public class SpinnerClickAdapter extends SimpleCursorAdapter
					implements SpinnerAdapter {
	
	private OnItemClickListener onClickListener;
	private ClickableSpinner spinner;
	private View.OnClickListener viewClickListener;

	public SpinnerClickAdapter(Context context, int layout, Cursor c,
								String[] from, int[] to, int flags) {
		
		super(context, layout, c, from, to, flags);
		viewClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ItemTag tag = (ItemTag) v.getTag();
				clickItem(tag.position, tag.parent, v);
			}

		};
	}

	void setOnItemClickListener( OnItemClickListener l ) {
		onClickListener = l;
	}
	
	void setSpinner( ClickableSpinner s ) {
		spinner = s;
	}
	
	@Override
	public View getDropDownView(final int position, View convertView,
								final ViewGroup parent) {
		
		final View v = super.getDropDownView(position, convertView, parent);

		if (convertView == null) {
			v.setOnClickListener(viewClickListener);
			v.setTag( new ItemTag( position, parent ) );
		} else {
			ItemTag tag = (ItemTag) v.getTag();
			tag.position = position;
			tag.parent = parent;
		}
		return v;
	}
	
	private void clickItem(final int position, final ViewGroup parent, View v) {
		
		if( onClickListener != null ) {
			onClickListener
				.onItemClick((AdapterView<?>) parent, v, position,
							 getItemId(position) );
		}
		spinner.setSelection(position);
		spinner.dismissDropDown();
	}
	
	private static class ItemTag {
		int position;
		ViewGroup parent;
		
		public ItemTag(int position, ViewGroup parent) {
			this.position = position;
			this.parent = parent;
		}
	}
}
