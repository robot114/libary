package com.zsm.android.ui.fileselector;

import java.io.File;

import com.zsm.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * This Listener handles Save or Load button clicks.
 */
public class SaveLoadClickListener implements OnClickListener {

	/** Performed operation. */
	private final FileOperation mOperation;

	/** FileFragment in which you used SaveLoadClickListener */
	private final FileDialogFragment mFileFragment;

	private final Context mContext;

	/**
	 * @param operation
	 *            Performed operation.
	 * @param fileSelector
	 *            The FileSeletor which used this Listener.
	 * @param context
	 *            context.
	 */
	public SaveLoadClickListener(final FileOperation operation,
								 final FileDialogFragment fileFragment,
								 final Context context) {
		mOperation = operation;
		mFileFragment = fileFragment;
		mContext = context;
	}

	@Override
	public void onClick(final View view) {
		final String text = mFileFragment.getSelectedFileName();
		String filePath
			= mFileFragment.getCurrentLocation().getAbsolutePath()
				+ File.separator + text;
		
		final File file = new File(filePath);
		int messageText = 0;
		// Check file access rights.
		switch (mOperation) {
			case SAVE:
				if (!checkFileName(text)) {
					return;
				}
				if ((file.exists()) && (!file.canWrite())) {
					messageText = R.string.cannotSaveFileMessage;
				}
				break;
			case LOAD:
				if (!checkFileName(text)) {
					return;
				}
				messageText = checkFileAndFolder(file);
				if( messageText == 0 && !file.isFile() ) {
					messageText = R.string.fileNeeded;
				}
				break;
			case FOLDER:
				messageText = checkFileAndFolder(file);
				if( messageText == 0 && !file.isDirectory() ) {
					filePath = file.getParent();
				}
				break;
			default:
				throw new IllegalArgumentException( "Invalid operation: " + mOperation );
		}
		if (messageText != 0) {
			final Toast t = Toast.makeText(mContext, messageText, Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		} else {
			mFileFragment.handleFile(mOperation, filePath);
			mFileFragment.dismiss();
		}
	}

	private int checkFileAndFolder(final File file) {
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
