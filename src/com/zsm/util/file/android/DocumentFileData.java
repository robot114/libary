package com.zsm.util.file.android;

import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;


public class DocumentFileData extends DocumentData {

	private DocumentFile mDocumentFile;
	private String mName;

	public DocumentFileData( Context context, Uri uri, TYPE type ) {
		super(type);
		
		switch( type ) {
			case DOCUMENT:
				mDocumentFile = buildSingleDocumentFile(context, uri);
				mName = mDocumentFile.getName();
				break;
			case FOLDER:
			case VOLUME:
				mDocumentFile = DocumentFile.fromSingleUri(context, uri);
				mName = mDocumentFile.getName();
				break;
			case UP_FOLDER:
				mDocumentFile = DocumentFile.fromSingleUri(context, uri);
				mName = UP_FOLDER_NAME;
				break;
			default:
				throw new IllegalArgumentException( "Unsupported type: " + type );
		}
		
		if( mName == null ) {
			mName = uri.getLastPathSegment();
		}
	}
	
	public DocumentFileData( DocumentFile df, TYPE type ) {
		super( type );
		
		mDocumentFile = df;
		if( type == TYPE.UP_FOLDER ) {
			mName = UP_FOLDER_NAME;
		} else {
			mName = df.getName();
		}
	}

	private DocumentFile buildSingleDocumentFile(Context context, Uri uri) {
		String documentId = DocumentsContract.getDocumentId(uri);
		String authority = uri.getAuthority();
		Uri duri = DocumentsContract.buildDocumentUri( authority, documentId );
		
		return DocumentFile.fromSingleUri(context, duri);
	}

	@Override
	public String getName() {
		return mName;
	}

	@Override
	public Uri getUri() {
		return mDocumentFile.getUri();
	}

}
