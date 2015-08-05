package com.zsm.driver.android.log;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.zsm.log.Log;
import com.zsm.log.Log.LEVEL;

public class LogPreferences {

	private static final String LOG_LEVEL = "LOG_LEVEL";
	private static final String LOG_CHANNELS = "LOG_CHANNELS";

	private static final LEVEL DEFAULT_LOG_LEVEL = Log.LEVEL.ERROR;
	private static final Set<String> DEFAULT_LOG_CHANNELS;
	
	static {
		DEFAULT_LOG_CHANNELS = new HashSet<String>();
		DEFAULT_LOG_CHANNELS.add( LogInstaller.DEFAULT_LOG );
	}
	
	private static LogPreferences instance;
	private SharedPreferences preferences;
	private StackTraceElement[] stackTrace;

	private LogPreferences( Context context ) {
		preferences
			= PreferenceManager
				.getDefaultSharedPreferences( context );
		
	}
	
	/**
	 * Initialize the log preferences instance. And set log's global level
	 * according to the preferences.
	 * 
	 * @param c
	 */
	static public void init( Context c ) {
		if( instance != null ) {
			throw new IllegalStateException( "Preference has been initialized! "
											 + "Call getInitStackTrace() to get "
											 + "the initlization place." );
		}
		
		instance = new LogPreferences( c );
		instance.stackTrace = Thread.currentThread().getStackTrace();
		Log.setGlobalLevel( instance.getLogLevel() );
	}
	
	static public LogPreferences getInstance() {
		return instance;
	}
	
	public StackTraceElement[] getInitStackTrace() {
		return stackTrace;
	}
	
	public LEVEL getLogLevel() {
		return Log.LEVEL.valueOf( 
				preferences.getString( LOG_LEVEL, DEFAULT_LOG_LEVEL.name() ) );
	}

	public Set<String> getLogChannels() {
		Set<String> c
			= preferences.getStringSet( LOG_CHANNELS, DEFAULT_LOG_CHANNELS );
		return c;
	}
	
	public boolean isLogChannelOn( String logChannel ) {
		return getLogChannels().contains(logChannel);
		
	}
}
