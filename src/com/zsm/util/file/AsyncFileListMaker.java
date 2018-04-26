package com.zsm.util.file;

import java.util.Comparator;

import android.net.Uri;

public class AsyncFileListMaker<T> {
	
	enum THREAD_STATE { INIT, RUNNING, FINISHED, CANCELLED };
	
	FileDataListMaker<T> mFileListMaker;
	private MakeListThread<T> mThread;
	SortableAdapter<T> mAdapter;
	
	public AsyncFileListMaker( FileDataListMaker<T> listMaker ) {
		mFileListMaker = listMaker;
	}
	
	public void makeList(final Object context,
						 final Uri location,
						 final FileExtensionFilter filesFilter,
						 final SortableAdapter<T> adapter,
						 final Comparator<T> comparator,
						 final boolean includeSubDir,
						 final FileDataListMakerNotifier n ) {
		
		mAdapter = adapter;
		synchronized( mAdapter ) {
			if( mThread != null && mThread.isAlive() ) {
				mThread.checkNotifierResued(n);
				mThread.cancel();
			}
		
			mThread
				= new MakeListThread<T>( context, location, filesFilter, adapter,
									     comparator, includeSubDir, n,
									     mFileListMaker );
			
			mThread.start();
		}
	}

	public boolean isMaking() {
		return mThread != null && mThread.isRunning();
	}

	public void cancel() {
		mThread.cancel();
	}
	
	
}
