/**
 * 
 */
package com.zsm.log;

import java.io.BufferedReader;
import java.io.IOException;

import com.zsm.io.DummyReader;

/**
 * @author zsm
 *
 */
public class SystemOutLog extends Log {

	private String mTag;

	public SystemOutLog( String tag ) {
		mTag = tag;
	}
	
    /**
	 * Return a dummy reader which can read nothing actually.
	 */
	@Override
	public BufferedReader createReader() throws IOException {
		return new DummyReader();
	}

	@Override
	protected void print(Throwable t, Object message, LEVEL level)
			throws IOException {
		
		String taggedMessage = "[" + mTag + "] " + message;
		
		if( level.compareTo( LEVEL.WARNING ) >= 0 ) {
			System.err.println( taggedMessage );
			if( t == null ) {
				t = new Exception();
			}
			t.printStackTrace();
		} else {
			System.out.println( taggedMessage );
		}
	}

    /**
     * Do nothing.
	 */
	@Override
	public void clearContent() throws IOException {
	}

	@Override
	protected void uninstall() throws IOException {
		// Do nothing
	}

	@Override
	public String toString() {
		return "Logs out by System.out";
	}

}
