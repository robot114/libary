package com.zsm.android.ui.documentSelector;

import android.support.v4.provider.DocumentFile;

public interface DocumentHandler {

	/**
	 * This method will be invoked when the user confirms to select the document.
	 * 
	 * @param operation operation of the user: SAVE, LOAD or FOLDER
	 * @param document The selected document. When the operation is SAVE or LOAD,
	 * 					this points to the document itself. When the operation
	 * 					is FOLDER, the selected directory.
	 * @param name The text in the name field view. When the operation is FOLDER,
	 * 					it may be null
	 */
	void handleDocument(DocumentOperation operation, DocumentFile document, String name );

}
