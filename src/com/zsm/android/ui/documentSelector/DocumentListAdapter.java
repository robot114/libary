package com.zsm.android.ui.documentSelector;


import com.zsm.android.ui.DataListAdapter;
import com.zsm.util.file.android.DocumentData;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

public class DocumentListAdapter extends DataListAdapter<DocumentData> {

	private Activity mActivity;

	public DocumentListAdapter(Activity activity) {
		super();
		this.mActivity = activity;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleDocumentItemView view;
		if( convertView == null ) {
			view = new SimpleDocumentItemView(mActivity);
		} else {
			view = (SimpleDocumentItemView)convertView;
		}
		
		view.setDocument( getItem(position) );
	
		return view;
	}

}
