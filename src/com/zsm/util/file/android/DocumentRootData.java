package com.zsm.util.file.android;

import android.net.Uri;

public class DocumentRootData extends DocumentData {

	private String mName;

	protected DocumentRootData(TYPE type) {
		super(type);
		if( type == TYPE.UP_FOLDER ) {
			mName = DocumentData.UP_FOLDER_NAME;
		} else {
			mName = DocumentData.ROOT_NAME;
		}
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public Uri getUri() {
		return null;
	}

}
