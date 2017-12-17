package com.zsm.util.file.android;

import java.util.ArrayList;

import com.zsm.util.file.FileDataListNotifier;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import static android.provider.DocumentsContract.Document.*;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

public class DocumentFileUtilities {

	private static final String[] LISTFILE_QUERY_COLS
		= new String[] { COLUMN_DOCUMENT_ID, COLUMN_MIME_TYPE };

	public static DocumentFile[] listFiles(Context context, Uri path,
										   StringFilter filter,
										   boolean includeSubDir,
										   FileDataListNotifier notifier) {

		final ContentResolver resolver = context.getContentResolver();
		final Uri childrenUri = DocumentsContract
				.buildChildDocumentsUriUsingTree(path,
						DocumentsContract.getDocumentId(path));
		final ArrayList<DocumentFile> results = new ArrayList<DocumentFile>();

		try (Cursor c = resolver.query(childrenUri, LISTFILE_QUERY_COLS,
									   null, null, null)) {

			int idIndex = c.getColumnIndex( COLUMN_DOCUMENT_ID );
			int typeIndex = c.getColumnIndex( COLUMN_MIME_TYPE );
			while (c.moveToNext()) {
				if(!oneFile(context, path, filter, includeSubDir, notifier,
							results, c, idIndex, typeIndex) ) {
					
					break;
				}
			}
		}

		return results.toArray(new DocumentFile[results.size()]);
	}

	private static boolean oneFile(Context context, Uri path, StringFilter filter,
			boolean includeSubDir, FileDataListNotifier notifier,
			final ArrayList<DocumentFile> results, Cursor c, int idIndex,
			int typeIndex) {
		
		final String type = c.getString(typeIndex);
		final boolean isDir = isDirectory( type );
		final boolean isFile = isFile(type);
		if( isFile || ( isDir && includeSubDir ) ) {
			final String documentId = c.getString(idIndex);
			final Uri documentUri = DocumentsContract
					.buildDocumentUriUsingTree(path, documentId);
			if( notifier != null ) {
				if( !notifier.notifyFile( 
						documentUri.getLastPathSegment(), !isFile )) {
					
					return false;
				}
			}
			if( isDir || filter.accept( documentUri.toString() ) ) {
				final DocumentFile doc
					= DocumentFile.fromTreeUri(context, documentUri);
		
				results.add(doc);
			}
		}
		
		return true;
	}

    public static boolean isDirectory(String type) {
    	return DocumentsContract.Document.MIME_TYPE_DIR.equals(type);
    }
    
    public static boolean isFile(String type) {
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
        	|| TextUtils.isEmpty(type)) {
        	
            return false;
        } else {
            return true;
        }
    }

}
