package com.zsm.util.file;

import android.net.Uri;

public interface FileDataListMaker {

	/**
	 * The method that fills the list with a directories contents.
	 * @param context
	 * 				Context to handle the list operation. For some concrete class,
	 * 				e.g. {@link FileListMaker}, it may be useless and can be null.
	 * 				And for some concrete class, e.g.
	 * 				{@link com.zsm.util.file.android.DocumentFileListMaker DocumentFileList},
	 * 				it should be instance of {@link android.content.Context Context}
	 * @param location
	 *            	Indicates the directory whose contents should be displayed in
	 *            	the dialog.
	 * @param filesFilter
	 *            	The filter specifies the type of file to be displayed
	 * @param fileList
	 * 				The notifiable list to store the files. Each time a new file
	 * 				added to the list, the VIEW of the list will be notofied
	 * @param includeSubDir
	 * 				Whether the sub directories are included in the list. If it
	 * 				is false, only the files in {@linkplain location} included.
	 * 				The directories are excluded  
	 * @param notifier
	 * 				Notifier to notify the caller the progress
	 * 				
	 */
	void makeList(Object context, final Uri location, 
				  final FileExtensionFilter filesFilter,
				  final NotifiableList<FileData> fileList,
				  final boolean includeSubDir,
				  final FileDataListNotifier notifier);
}
