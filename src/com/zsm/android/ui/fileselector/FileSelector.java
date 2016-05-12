package com.zsm.android.ui.fileselector;

import java.io.File;

import com.zsm.util.file.FileExtensionFilter;

import android.app.Activity;

/**
 * Create the file selection dialog. This class will create a custom dialog for
 * file selection which can be used to save files.
 */
public class FileSelector {

	private FileDialogFragment dialog;

	/**
	 * Constructor that creates the file selector dialog.
	 * 
	 * @param activity
	 *            The current context.
	 * @param operation
	 *            LOAD - to load file / SAVE - to save file
	 * @param currentPath
	 * 			  The path where we start 
	 * @param onHandleFileListener
	 *            Notified after pressing the save or load button.
	 * @param fileFilters
	 *            Array with filters
	 */
	public FileSelector(final Activity activity, 
						final FileOperation operation,
						final String currentPath,
						final OnHandleFileListener onHandleFileListener,
						final FileExtensionFilter[] fileFilters) {
		
		dialog
			= new FileDialogFragment(activity, "", operation, currentPath, 
									 onHandleFileListener, fileFilters);
	}

	/**
	 * Constructor that creates the file selector dialog.
	 * 
	 * @param activity
	 *            The current context.
	 * @param operation
	 *            LOAD - to load file / SAVE - to save file
	 * @param currentPath
	 * 			  The path where we start 
	 * @param onHandleFileListener
	 *            Notified after pressing the save or load button.
	 * @param fileFilters
	 *            Array with filters
	 * @param withNameField
	 * 			  Show the text field to input file name
	 */
	public FileSelector(final Activity activity,
						final FileOperation operation,
						final String currentPath,
						final OnHandleFileListener onHandleFileListener,
						final FileExtensionFilter[] fileFilters,
						final boolean withNameField ) {
		
		dialog
			= new FileDialogFragment(activity, "", operation, currentPath, 
									 onHandleFileListener, fileFilters,
									 true, withNameField );
	}

	public String getSelectedFileName() {
		return dialog.getSelectedFileName();
	}

	public File getCurrentLocation() {
		return dialog.getCurrentLocation();
	}

	/** Simple wrapper around the Dialog.show() method. */
	public void show() {
		dialog.showDialog();
	}

	/** Simple wrapper around the Dialog.dissmiss() method. */
	public void dismiss() {
		dialog.dismiss();
	}
}
