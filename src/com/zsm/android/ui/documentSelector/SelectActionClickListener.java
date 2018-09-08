package com.zsm.android.ui.documentSelector;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.zsm.R;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.android.DocumentFileUtilities;

/**
 * This Listener handles Save or Load button clicks.
 */
public class SelectActionClickListener implements OnClickListener {

	/** Performed operation. */
	private final DocumentOperation mOperation;

	private final DocumentUserInterface mDocuemntUI;

	private final Context mContext;

	private FileExtensionFilter mFileExtensionFilter;

	private DocumentHandler mDocuemntHandler;

	/**
	 * @param operation
	 *            Performed operation.
	 * @param selector
	 *            The DocumentSelector which used this Listener.
	 * @param context
	 *            context.
	 */
	public SelectActionClickListener(final DocumentOperation operation,
								 	 final DocumentUserInterface selector,
								 	 final DocumentHandler handler,
								 	 final Context context) {
		mOperation = operation;
		mDocuemntUI = selector;
		mDocuemntHandler = handler;
		mContext = context;
	}

	public void setFileExtensionFilter( FileExtensionFilter ff ) {
		mFileExtensionFilter = ff;
	}
	
	@Override
	public void onClick(final View view) {
		String text = mDocuemntUI.getInputedName();
		if( mOperation == DocumentOperation.SAVE
			|| mOperation == DocumentOperation.LOAD ) {
			
			if (!checkFileName(text)) {
				return;
			}
			if( mFileExtensionFilter != null && !mFileExtensionFilter.accept(text) 
				&& !mFileExtensionFilter.getDefaultExtension().isEmpty() ) {
				text = text + "." + mFileExtensionFilter.getDefaultExtension();
			}
		}
		
		int messageText = 0;
		DocumentFile document;
		Uri childUri;
		Uri pathUri = mDocuemntUI.getCurrentLocation();
		// Check file access rights.
		switch (mOperation) {
			case SAVE:
				childUri = DocumentFileUtilities.getChildUri(pathUri, text);
				document = DocumentFile.fromSingleUri(mContext, childUri);
				
				if ((document.exists()) && (!document.canWrite())) {
					messageText = R.string.cannotSaveFileMessage;
				}
				break;
			case LOAD:
				childUri = DocumentFileUtilities.getChildUri(pathUri, text);
				document = DocumentFile.fromSingleUri(mContext, childUri);
				messageText = checkFileAndFolder(document);
				if( messageText == 0 && !document.isFile() ) {
					messageText = R.string.fileNeeded;
				}
				break;
			case FOLDER:
				if( !DocumentFileUtilities.isTreeDocuemntUri( pathUri ) ) {
					String documentId
						= DocumentFileUtilities.getDocumentId(pathUri);
					pathUri
						= DocumentsContract.buildTreeDocumentUri(
								pathUri.getAuthority(), documentId); 
				}
				document = DocumentFile.fromSingleUri(mContext, pathUri);
				messageText = checkFileAndFolder(document);
				break;
			default:
				throw new IllegalArgumentException( "Invalid operation: " + mOperation );
		}
		if (messageText != 0) {
			final Toast t = Toast.makeText(mContext, messageText, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		} else {
			mDocuemntUI.dismiss();
			mDocuemntHandler.handleDocument(mOperation, document, text );
		}
	}

	private int checkFileAndFolder(final DocumentFile file) {
		int messageText = 0;
		if (!file.exists()) {
			messageText = R.string.missingFile;
		} else if (!file.canRead()) {
			messageText = R.string.accessDenied;
		}
		return messageText;
	}

	/**
	 * Check if file name is correct, e.g. if it isn't empty.
	 * 
	 * @return False, if file name is empty true otherwise.
	 */
	boolean checkFileName(String text) {
		if (text.length() == 0) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.information);
			builder.setMessage(R.string.fileNameFirstMessage);
			builder.setNeutralButton(R.string.okButtonText, null);
			builder.show();
			return false;
		}
		return true;
	}
}
