package com.zsm.android.ui.documentSelector;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.zsm.android.ui.DataListAdapter;
import com.zsm.util.file.android.DocumentData;

public class DocumentListAdapter extends DataListAdapter<DocumentData> {

	private Context mContext;

	public DocumentListAdapter(Context context) {
		super();
		this.mContext = context;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleDocumentItemView view;
		if( convertView == null ) {
			view = new SimpleDocumentItemView(mContext);
		} else {
			view = (SimpleDocumentItemView)convertView;
		}
		
		view.setDocument( getItem(position) );
	
		return view;
	}

}
