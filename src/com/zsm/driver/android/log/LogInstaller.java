package com.zsm.driver.android.log;

import java.io.File;

import com.zsm.log.FileLog;
import com.zsm.log.Log;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;

public class LogInstaller {

	private static final int MAX_LOG_FILE_LENGTH = 1024*1024*1024;

    public static final String ANDROID_LOG = "AndroidLog";
	public static final String FILE_LOG = "FileLog";
	public static final String DEFAULT_LOG = ANDROID_LOG;
	
	/**
	 * Needed to be invoked at the time the application constructed. 
	 * <b>This MUST be invoked!</b>
	 * <p>The default level of the android log is debug.
	 * <p>In order to record the log during log being installed,
	 * this method will set the global log level as <b>DEBUG</b>.
	 * 
	 * @param tag tag of the log of the application. Commonly, it should
	 * 					 be the name of the application.
	 * 
	 */
	static public void installAndroidLog( String tag ) {
		Log.setGlobalLevel( Log.LEVEL.DEBUG );
		
		Log.install( ANDROID_LOG, new AndroidLog( tag ) );
		Log.setLevel( ANDROID_LOG, Log.LEVEL.DEBUG );
	}

	/**
	 * Need to be invoked in the method onCreate() of the application.
	 * This is optional. If the file log is not necessary, this should
	 * not be invoked.
	 * 
	 * @param context
	 * @param maxFileLength Max length in bytes of the log file.
	 */
	static public void installFileLog( Context context, long maxFileLength ) {
		if( LogPreferences.getInstance().isLogChannelOn(FILE_LOG) ) {
			installFileLog( FILE_LOG, context.getDir( "log", 0 ).getPath(), maxFileLength );
			installFileLogRetry( context, FILE_LOG, maxFileLength );
			Log.setLevel(FILE_LOG, Log.LEVEL.DEBUG);
		}
	}

	/**
	 * Need to be invoked in the method onCreate() of the application.
	 * This is optional. If the file log is not necessary, this should
	 * not be invoked.
	 * 
	 * @param context
	 */
	static public void installFileLog( Context context ) {
		installFileLog( context, MAX_LOG_FILE_LENGTH );
	}

	static private void installFileLog(String id, String path, long maxFileLength) {
		String logFileName
			= Environment.getExternalStorageDirectory()
				+ id + "/log/.log";
		try {
			Log.install( id, new FileLog( logFileName, maxFileLength ));
		} catch (Exception e) {
			Log.e( "Install log failed!", "id", id, "file name", logFileName );
		}
	}
	
	static private void installFileLogRetry(Context context, String id,
											long maxFileLength ) {
		
		if( !Log.isIinstalled(id) ) {
			ContextWrapper cw = new ContextWrapper(context);
			File directory = cw.getDir("log", Context.MODE_PRIVATE);
			String logFileName = directory.getAbsolutePath() +"/.log";
			try {
				Log.install( id, new FileLog( logFileName, maxFileLength ) );
			} catch (Exception e) {
				Log.e( "Retry to install log failed!", "id", id,
						"file name", logFileName );
			}
		}
	}

}
