package com.zsm.util.file.android;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.zsm.util.file.FileData;
import com.zsm.util.file.FileDataListMaker;
import com.zsm.util.file.FileDataListNotifier;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.NotifiableList;

public class DocumentFileListMaker implements FileDataListMaker {

	@Override
	public void makeList(Object context, Uri location,
						 FileExtensionFilter filesFilter,
						 NotifiableList<FileData> fileList,
						 boolean includeSubDir, FileDataListNotifier notifier) {

		final Context c = (Context)context;
		final DocumentFile current = DocumentFile.fromTreeUri(c, location);
		final DocumentFile parentLocation = current.getParentFile();
		if (parentLocation != null) {
			// First item on the list.
			fileList.addAndNotify(new FileData("../", FileData.UP_FOLDER));
		}
		DocumentFile[] listFiles
				= DocumentFileUtilities.listFiles( c, location, filesFilter,
												   includeSubDir, notifier );
		if (listFiles != null) {
			for ( DocumentFile file : listFiles ) {
				int type
					= file.isDirectory() ? FileData.DIRECTORY : FileData.FILE;
				FileData fileData = new FileData(file.getName(), type);
				fileList.addAndNotify(fileData);
			}
			
			notifier.beforeToMakeOrder();
			fileList.sortAndNotify();
		}
		
	}
	
}
