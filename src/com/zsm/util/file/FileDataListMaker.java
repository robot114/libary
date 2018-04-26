package com.zsm.util.file;

import java.util.Comparator;

import android.net.Uri;

public interface FileDataListMaker<T> {

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
	 * @param adapter
	 * 				The notifiable adapter to store the files. Each time a new file
	 * 				added to the list, the VIEW of the list will be notified
	 * @param mComparator
	 * 				The comparator to sort the files 
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
				  final SortableAdapter<T> adapter,
				  final Comparator<T> mComparator,
				  final boolean includeSubDir,
				  final FileDataListMakerNotifier notifier);
}
