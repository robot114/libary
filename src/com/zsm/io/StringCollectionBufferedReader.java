package com.zsm.io;

import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Read from a string collection. Each element in the queue may be a single line
 * string or a multiple lines string.
 * 
 * @author zsm
 *
 */
public class StringCollectionBufferedReader extends BufferedReader {

	private Iterator<String> mIterator;
	private int mPosToBeRead;
	private String mCurrentString;
	
	private char[] mBuffer;
	private boolean mEndOfAll = false;

	public StringCollectionBufferedReader( Collection<String> c ) {
		super(new DummyReader());

		mIterator = c.iterator();
		nextString();
	}

	@Override
	synchronized public int read() throws IOException {
		if( mEndOfAll ) {
			return -1;
		}
		
		if( mPosToBeRead >= mCurrentString.length() ) {
			nextString();
		}
		return mEndOfAll ? -1 : mCurrentString.charAt(mPosToBeRead++);
	}

	private void nextString() {
		mEndOfAll = !mIterator.hasNext();
		if( !mEndOfAll ) {
			mCurrentString = mIterator.next();
			mPosToBeRead = 0;
			if( mBuffer == null || mBuffer.length < mCurrentString.length() ) {
				mBuffer = new char[mCurrentString.length()];
			}
		}
	}

	@Override
	synchronized public String readLine() throws IOException {
		if( mEndOfAll ) {
			return null;
		}
		
		if( mPosToBeRead == 0 && mCurrentString.length() == 0 ) {
			nextString();
			return "";
		}
		
		int index = 0;
		int ch;
		while( mPosToBeRead < mCurrentString.length() && ( ch = read() ) > 0 ) {
			if( ch == '\r' ) {
				skipNextEolChar('\n');
				break;
			} else if( ch == '\n' ) {
				skipNextEolChar('\r');
				break;
			} else {
				mBuffer[index++] = (char) ch;
			}
		}
		if( mPosToBeRead >= mCurrentString.length() ) {
			nextString();
		}
		
		return new String(mBuffer, 0, index);
	}

	private void skipNextEolChar(char nextEolChar) {
		if( mPosToBeRead < mCurrentString.length() ) {
			if( mCurrentString.charAt(mPosToBeRead) == nextEolChar ) {
				mPosToBeRead++;
			}
		}
	}

	@Override
	synchronized public long skip(long n) throws IOException {
		if( mEndOfAll ) {
			return 0;
		}
		
		long left = n;
		while( !mEndOfAll && left > 0 ) {
			if( ( mPosToBeRead + left ) < mCurrentString.length() ) {
				mPosToBeRead += left;
				return n;
			} else {
				left -= ( mCurrentString.length() - mPosToBeRead );
				nextString();
			}
		}
		return n-left;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void mark(int readAheadLimit) throws IOException {
		throw new IOException( "Mark is not suppoted!" );
	}

	@Override
	public void reset() throws IOException {
		throw new IOException( "Mark is not suppoted!" );
	}

	@SuppressLint("NewApi")
	@Override
	public Stream<String> lines() {
        Iterator<String> iter = new Iterator<String>() {
            String nextLine = null;

            @SuppressLint("NewApi")
			@Override
            public boolean hasNext() {
                if (nextLine != null) {
                    return true;
                } else {
                    try {
                        nextLine = readLine();
                        return (nextLine != null);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                }
            }

            @Override
            public String next() {
                if (nextLine != null || hasNext()) {
                    String line = nextLine;
                    nextLine = null;
                    return line;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iter, Spliterator.ORDERED | Spliterator.NONNULL), false);
	}

}
