package com.zsm.util.file;

import java.io.File;
import java.util.Comparator;

import android.net.Uri;

import com.zsm.log.Log;


public class FileListMaker implements FileDataListMaker<FileData> {

	@Override
	public void makeList(Object context, Uri location,
						 FileExtensionFilter filesFilter,
						 SortableAdapter<FileData> adapter,
						 Comparator<FileData> comparator,
						 boolean includeSubDir,
						 FileDataListMakerNotifier notifier ) {

		final File current = new File( location.getPath() );
		final File parentLocation = current.getParentFile();
		if (parentLocation != null) {
			// First item on the list.
			adapter.add(new FileData("../", FileData.UP_FOLDER));
		}
		File listFiles[]
				= FileUtilities.listFile( current, filesFilter, includeSubDir );
		if (listFiles != null) {
			for ( File file : listFiles ) {
				boolean isDir = file.isDirectory();
				int type = isDir ? FileData.DIRECTORY : FileData.FILE;
				String filename = file.getName();
				
				if( !notifier.notifyFile(filename, !isDir) ) {
					Log.d( "Making list is cancelled by user." );
					break;
				} else {
					FileData fileData = new FileData(filename, type);
					adapter.add(fileData);
				}
			}
			
			notifier.beforeToMakeOrder();
			adapter.sort(comparator);
		}
		
	}

}
