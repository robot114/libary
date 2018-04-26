package com.zsm.android.ui.documentSelector;

import android.app.Activity;
import android.net.Uri;

import com.zsm.util.file.FileExtensionFilter;

/**
 * Create the file selection dialog. This class will create a custom dialog for
 * file selection which can be used to save files.
 */
public class DocumentSelector {

	private DocumentSelectorFragment dialog;

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
	public DocumentSelector(final Activity activity, 
							final DocumentOperation operation,
							final Uri currentPath,
							final DocumentHandler handler,
							final FileExtensionFilter[] fileFilters) {
		
		dialog
			= new DocumentSelectorFragment(activity, "", operation, currentPath,
									 	   handler, fileFilters);
	}

	/**
	 * Constructor that creates the file selector dialog.
	 * 
	 * @param activity
	 *            The current context.
	 * @param operation
	 *            LOAD - to load file / SAVE - to save file / FOLDER - to load folder
	 * @param currentPath
	 * 			  The path where we start 
	 * @param handler
	 *            Notified after pressing the save or load button.
	 * @param fileFilters
	 *            Array with filters
	 * @param withNameField
	 * 			  Show the text field to input file name
	 */
	public DocumentSelector(final Activity activity,
							final DocumentOperation operation,
							final Uri currentPath,
							final DocumentHandler handler,
							final FileExtensionFilter[] fileFilters,
							final boolean withNameField ) {
		
		dialog
			= new DocumentSelectorFragment(activity, "", operation, currentPath,
									 	   handler, fileFilters, true,
									 	   withNameField );
	}

	/**
	 * Constructor that creates the file selector dialog.
	 * 
	 * @param activity
	 *            The current context.
	 * @param operation
	 *            LOAD - to load file / SAVE - to save file / FOLDER - to load folder
	 * @param currentPath
	 * 			  The path where we start 
	 * @param handler
	 *            Notified after pressing the save or load button.
	 * @param fileFilters
	 *            Array with filters
	 * @param withNameField
	 * 			  Show the text field to input file name
	 * @param withCustomerFilter
	 * 			  File filter can be input by user
	 */
	public DocumentSelector(final Activity activity,
							final DocumentOperation operation,
							final Uri currentPath,
							final DocumentHandler handler,
							final FileExtensionFilter[] fileFilters,
							final boolean withNameField,
							final boolean withCustomerFilter ) {
		
		dialog
			= new DocumentSelectorFragment(activity, "", operation, currentPath, 
									 	   handler, fileFilters, true,
									 	   withNameField, withCustomerFilter );
	}

	public String getSelectedFileName() {
		return dialog.getInputedName();
	}

	public Uri getCurrentLocation() {
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
