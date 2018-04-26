package com.zsm.android.ui.documentSelector;

import java.security.InvalidParameterException;

import android.content.Context;

import com.zsm.R;
import com.zsm.android.ui.TextViewWithImage;
import com.zsm.util.file.android.DocumentData;

public class SimpleDocumentItemView extends TextViewWithImage {

	private DocumentData mDocument;

	public SimpleDocumentItemView(Context context) {
		super(context);
	}

	void setDocument( DocumentData doc ) {
		mDocument = doc;
		setText( doc.getName() );
		setImageResource( getImageResourceId() );
	}

	private int getImageResourceId() {
		int imgRes;
		switch (mDocument.getType()) {
			case VOLUME:
				imgRes = R.drawable.folder_root;
				break;
			case UP_FOLDER:
				imgRes = R.drawable.folder_up;
				break;
			case FOLDER:
				imgRes = R.drawable.folder;
				break;
			case DOCUMENT:
				imgRes = R.drawable.file;
				break;
			default:
				throw new InvalidParameterException(
						"Invalid type: " + mDocument.getType() );
		}
		return imgRes;
	}
}
