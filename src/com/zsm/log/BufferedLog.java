package com.zsm.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;

import com.zsm.io.StringCollectionBufferedReader;

public class BufferedLog extends Log {
	
	private static final int DEFAULT_MAX_ENTRIES_NUMBER = 10000;
	
	private int mMaxEntriesNumber;

	private ArrayDeque<String> mLogBuffer;

	public BufferedLog( ) {
		this( DEFAULT_MAX_ENTRIES_NUMBER );
	}
	
	/**
	 * Constructor
	 * 
	 * @param maxEntriesNumber max number of the log entries. It must be larger than 0
	 */
	public BufferedLog( int maxEntriesNumber ) {
		if( maxEntriesNumber <= 0 ) {
			throw new IllegalArgumentException( "Invalid maxEntriesNumber: " + maxEntriesNumber );
		}
		
		mMaxEntriesNumber = maxEntriesNumber;
		mLogBuffer = new ArrayDeque<String>(maxEntriesNumber+1);
	}
	
	@Override
	protected void uninstall() throws IOException {
		mLogBuffer = null;
	}

	@Override
	public BufferedReader createReader() throws IOException {
		return new StringCollectionBufferedReader(mLogBuffer.clone());
	}

	@Override
	protected void print(Throwable t, Object message, LEVEL level) throws IOException {
		
		addMessage( message.toString() );
		
		if( t != null ) {
			StackTraceElement[] ste = t.getStackTrace();
			for( StackTraceElement e : ste ) {
				addMessage( e.toString() );
			}
		}
	}

	@Override
	public void clearContent() throws IOException {
		mLogBuffer.clear();
	}
	
	private void addMessage( String message ) {
		if( mLogBuffer.size() >= mMaxEntriesNumber && mLogBuffer.size() > 1 ) {
			mLogBuffer.removeFirst();
		}
		
		mLogBuffer.addLast(message);
	}
}
