package com.zsm.util.file;

import java.util.Arrays;
import java.util.Comparator;

import android.net.Uri;

import com.zsm.log.Log;
import com.zsm.util.file.AsyncFileListMaker.THREAD_STATE;

class MakeListThread<T> extends Thread {
	
	/**
	 * 
	 */
	private THREAD_STATE mState = THREAD_STATE.INIT;
	private Object mContext;
	private Uri mLocation;
	private FileExtensionFilter mFilesFilter;
	private SortableAdapter<T> mFileDataListAdapter;
	private Comparator<T> mComparator;
	private boolean mIncludeSubDir;
	
	private WarppedNotifier mWrappedProgressor;
	private StackTraceElement[] mStackTrace;
	private FileDataListMaker<T> mFileListMaker;

	MakeListThread( final Object context,
					final Uri location,
					final FileExtensionFilter filesFilter,
					final SortableAdapter<T> adapter,
					final Comparator<T> comparator,
					final boolean includeSubDir,
					final FileDataListMakerNotifier n,
					final FileDataListMaker<T> maker ) {
		
		mContext = context;
		mWrappedProgressor = new WarppedNotifier( n, this );
		mLocation = location;
		mFilesFilter = filesFilter;
		mFileDataListAdapter = adapter;
		mComparator = comparator;
		mIncludeSubDir = includeSubDir;
		mFileListMaker = maker;
		
		mStackTrace = new Exception().getStackTrace();
	}
	
	void checkNotifierResued( FileDataListMakerNotifier n ) {
		if( isAlive() && mWrappedProgressor == n ) {
			throw new IllegalStateException( 
				"A thread with the same notifier is running. "
				+ "The former thread should be stopped or a new notifier"
				+ "Should be used. "
				+ "The stack of the former thread is "
				+ Arrays.toString( mStackTrace ) );
		}
	}
	
	public void cancel() {
		if( !afterRunning() ) {
			mState = THREAD_STATE.CANCELLED;
			mWrappedProgressor.cancelled();
			
			Log.d( "Make list thread canncelled." );
		}
	}

	boolean isRunning() {
		return mState == THREAD_STATE.RUNNING;
	}

	/**
	 * Thread finished normally or cancelled
	 * 
	 * @return
	 */
	private boolean afterRunning() {
		return mState == THREAD_STATE.FINISHED || mState == THREAD_STATE.CANCELLED;
	}

	@Override
	public void run() {
		synchronized( mFileDataListAdapter ) {
			if( mState != THREAD_STATE.INIT ) {
				throw new IllegalStateException( "Invalid state: " + mState );
			}
			Log.d( "Start to make file list" );
			
			mWrappedProgressor.show();
			mState = THREAD_STATE.RUNNING;
			mFileDataListAdapter.clear();
			mFileListMaker.makeList(mContext, mLocation, mFilesFilter,
									mFileDataListAdapter, mComparator,
									mIncludeSubDir, mWrappedProgressor);
			
			if( mState == THREAD_STATE.RUNNING ) {
				mState = THREAD_STATE.FINISHED;
				mWrappedProgressor.finished();
			}
		}
	}
	
	final class WarppedNotifier implements FileDataListMakerNotifier {
		private final FileDataListMakerNotifier mWrappedNotifier;
		private MakeListThread<T> mMLThread;

		private WarppedNotifier( FileDataListMakerNotifier progressor,
								 MakeListThread<T> thread ) {
			
			mWrappedNotifier = progressor;
			mMLThread = thread;
		}

		@Override
		public boolean notifyFile(String filename, boolean isFile ) {
			if( !mMLThread.isRunning() ) {
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
}