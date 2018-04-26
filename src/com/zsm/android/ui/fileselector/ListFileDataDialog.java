package com.zsm.android.ui.fileselector;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.zsm.R;
import com.zsm.log.Log;
import com.zsm.util.file.FileDataListMakerNotifier;

public class ListFileDataDialog implements FileDataListMakerNotifier {

	private static final int MESSAGE_INTERVAL = 5;
	private Context mContext;
	private ProgressDialog mProgressDialog;
	private int mNotifiedCounter;
	private boolean mRunning;
	private Handler mHandler;

	public ListFileDataDialog( Context context ) {
		mContext = context;
		mProgressDialog = buildProgressDlg();
		mHandler = new Handler( Looper.getMainLooper() );
	}
	
	private ProgressDialog buildProgressDlg( ) {

		ProgressDialog dlg = new ProgressDialog(mContext);
		dlg.setTitle(R.string.titleFileListProgressDlg );
		dlg.setMessage( "" );
		dlg.setCancelable(false);
		dlg.setIndeterminate(false);
		dlg.setButton(DialogInterface.BUTTON_NEGATIVE,
			mContext.getText(android.R.string.cancel),
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d( "Make list is cancelled by user." );
					mRunning = false;
					dismiss();
				}

			});

		return dlg;
	}

	@Override
	public void show() {
		mNotifiedCounter = 0;
		mRunning = true;
		if( Looper.myLooper() == Looper.getMainLooper() ) {
			mProgressDialog.show();
		} else {
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					mProgressDialog.show();
				}
			} );
		}
	}

	@Override
	public void dismiss() {
		mRunning = false;
		if( Looper.myLooper() == Looper.getMainLooper() ) {
			mProgressDialog.dismiss();
		} else {
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					mProgressDialog.dismiss();
				}
			} );
		}
		Log.d( "Progress dialg dismissed." );
	}
	
	@Override
	public boolean notifyFile(String filename, boolean isFile) {
		messageFile(filename);
		return mRunning;
	}

	private void messageFile(String filename) {
		mNotifiedCounter++;
		if( mNotifiedCounter % MESSAGE_INTERVAL == 1 ) {
			String message
				= mContext.getString( R.string.messageFileListProgressDlg, filename );
			setMessage(message);
		}
	}

	@Override
	public void beforeToMakeOrder() {
		String message
			= mContext.getString( R.string.messageFileListProgressDlgSorting );
		setMessage(message);
	}

	private void setMessage(final String message) {
		if( Looper.myLooper() == Looper.getMainLooper() ) {
			mProgressDialog.setMessage( message );
		} else {
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					mProgressDialog.setMessage( message );
				}
			} );
		}
	}

	@Override
	public void cancelled() {
		dismiss();
	}

	@Override
	public void finished() {
		dismiss();
	}

	@Override
	public void forAcception(String filename, boolean accepted) {
		messageFile(filename);
	}

}
