package com.zsm.util.file;

import com.zsm.log.Log;

import android.net.Uri;

public class AsyncFileListMaker {
	
	private enum THREAD_STATE { INIT, RUNNING, FINISHED, CANCELLED };
	
	private FileDataListMaker mFileListMaker;
	private MakeListThread mThread;
	private NotifiableList<FileData> mFileList;
	
	public AsyncFileListMaker( FileDataListMaker listMaker ) {
		mFileListMaker = listMaker;
	}
	
	public void makeList(final Uri location,
						 final FileExtensionFilter filesFilter,
						 final NotifiableList<FileData> fileList,
						 final boolean includeSubDir,
						 final FileDataListNotifier n ) {
		
		mFileList = fileList;
		synchronized( mFileList ) {
			if( mThread != null && mThread.mState == THREAD_STATE.RUNNING ) {
				if( mThread.mWrappedProgressor.mWrappedNotifier == n ) {
					throw new IllegalStateException( 
						"A thread with the same notifier is running. "
						+ "The former thread should be stopped or a new notifier"
						+ "Should be used. "
						+ "The stack of the former thread is " + mThread.mStackTrace );
				}
				mThread.cancel();
			}
		
			mThread
				= new MakeListThread( location, filesFilter, fileList,
									  includeSubDir, n );
			
			mThread.start();
		}
	}
	
	private final class WarppedNotifier implements FileDataListNotifier {
		private final FileDataListNotifier mWrappedNotifier;
		private MakeListThread mMLThread;

		private WarppedNotifier( FileDataListNotifier progressor,
								 MakeListThread thread ) {
			
			mWrappedNotifier = progressor;
			mMLThread = thread;
		}

		@Override
		public boolean notifyFile(String filename, boolean isFile ) {
			if( mMLThread.mState != THREAD_STATE.RUNNING ) {
				Log.d( "Thread is not running." );
				return false;
			}
			
			return mWrappedNotifier.notifyFile(filename, isFile);
		}

		@Override
		public void beforeToMakeOrder() {
			mWrappedNotifier.beforeToMakeOrder();
		}

		@Override
		public void cancelled() {
			mWrappedNotifier.cancelled();
		}

		@Override
		public void finished() {
			mWrappedNotifier.finished();
		}

		@Override
		public void forAcception(String filename, boolean accepted) {
			mWrappedNotifier.forAcception(filename, accepted);
		}

		@Override
		public void show() {
			mWrappedNotifier.show();
		}

		@Override
		public void dismiss() {
			mWrappedNotifier.dismiss();
		}
	}

	private class MakeListThread extends Thread {
		
		private THREAD_STATE mState = THREAD_STATE.INIT;
		private Uri mLocation;
		private FileExtensionFilter mFilesFilter;
		private NotifiableList<FileData> mFileDataList;
		private boolean mIncludeSubDir;
		
		private WarppedNotifier mWrappedProgressor;
		private StackTraceElement[] mStackTrace;

		private MakeListThread( final Uri location,
								final FileExtensionFilter filesFilter,
								final NotifiableList<FileData> fileList,
								final boolean includeSubDir,
								final FileDataListNotifier n) {
			
			mWrappedProgressor = new WarppedNotifier( n, this );
			mLocation = location;
			mFilesFilter = filesFilter;
			mFileDataList = fileList;
			mIncludeSubDir = includeSubDir;
			mStackTrace = new Exception().getStackTrace();
		}
		
		public void cancel() {
			if( mState == THREAD_STATE.RUNNING ) {
				mState = THREAD_STATE.CANCELLED;
				mWrappedProgressor.cancelled();
				
				Log.d( "Make list thread canncelled." );
			}
		}

		@Override
		public void run() {
			synchronized( mFileList ) {
				if( mState != THREAD_STATE.INIT ) {
					throw new IllegalStateException( "Invalid state: " + mState );
				}
				
				mState = THREAD_STATE.RUNNING;
				mFileDataList.clear();
				mFileListMaker.makeList(null, mLocation, mFilesFilter, mFileDataList,
								   		mIncludeSubDir, mWrappedProgressor);
				mState = THREAD_STATE.FINISHED;
				mWrappedProgressor.finished();
			}
		}
		
	}
	
}
