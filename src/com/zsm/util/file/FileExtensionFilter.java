package com.zsm.util.file;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

@SuppressLint("DefaultLocale")
public class FileExtensionFilter implements Parcelable {

	private static final char[] INVALID_CHAR_SET
		= new char[]{ '\\', '\"', '/', '|', '*', ':', '?', '<', '>', '.' };
	
	/** Filter which accepts every file */
	public static final String FILTER_ALLOW_ALL = "*.*";
	
	private String extensions[];

	private String mExtDescription;
	private String toString;

	private boolean mHasEmptyExt = false;

	private FileExtensionFilter() {
		
	}
	
	/**
	 * Constructor of filter by extension.
	 * @param exts Extensions with the format ".mp3"
	 * @param filterDescription File filter's description, such as "Audio file"
	 */
	public FileExtensionFilter( String exts[], String filterDescription ) {
		
		extensions = Arrays.copyOf( exts, exts.length );
		init(filterDescription);
	}

	private void init(String filterDescription) {
		normalizeAndSortExts();
		mExtDescription = buildExtDescription();
		toString = filterDescription + "(" + mExtDescription + ")";
	}
	
	/**
	 * Construct filter from a string with extensions, which are separated by "|".
	 * Each extension can be as the format "*.ext", ".ext", or "ext".
	 * An example of the string is "*.mp3|.wav|wmv".
	 * 
	 * @param exts extension string
	 * @param filterDescription File filter's description, such as "Audio file"
	 */
	public FileExtensionFilter( String exts, String filterDescription ) {
		TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter('|');
		splitter.setString(exts);
		
		Vector<String> v = new Vector<String>( 10 );
		for( String ext : splitter ) {
			v.add(ext);
		}
		
		extensions = new String[ v.size() ];
		v.toArray( extensions );
		init( filterDescription );
	}
	
	public FileExtensionFilter( String allowAllfilterDescription ) {
		this( new String[]{ FILTER_ALLOW_ALL }, allowAllfilterDescription );
	}
	
	private void normalizeAndSortExts() {
		if( extensions.length == 1 && extensions[0].equals( FILTER_ALLOW_ALL ) ) {
			mHasEmptyExt = true;
			return;
		}
		
		for( int i = 0; i < extensions.length; i++ ) {
			String ext = extensions[i];
			if( ext.startsWith( "*." ) ) {
				extensions[i] = ext.substring( 2 );
			} else if( ext.startsWith( "." ) ) {
				extensions[i] = ext.substring( 1 );
			}
			ext = extensions[i];
			
			if( !validExtension( ext ) ) {
				throw new IllegalArgumentException( "Invalid extension :" + ext );
			}
			extensions[i] = ext.trim().toLowerCase();
			if( extensions[i].length() == 0 ) {
				mHasEmptyExt = true;
			}
		}
		Arrays.sort( extensions );
	}
	
	public static boolean validExtension( String ext ) {
		for( char c : INVALID_CHAR_SET ) {
			if( ext.indexOf(c) >= 0 ) {
				return false;
			}
		}
		
		return true;
	}
	
	public String getExtDescription() {
		return mExtDescription;
	}
	
	/**
	 * @return The string which can be rebuild back to a filter instance
	 */
	private String buildExtDescription() {
		if( acceptAll() ) {
			return FILTER_ALLOW_ALL;
		}
		
		StringBuilder builder = new StringBuilder( extensions.length*8 );
		builder.append( "*." ).append( extensions[0] );
		for( int i = 1; i < extensions.length; i++ ) {
			extensions[i] = extensions[i];
			builder.append( "|" ).append( "*." ).append( extensions[i] );
		}
		return builder.toString();
	}

	private boolean acceptAll() {
		return extensions == null
				|| extensions.length == 0
				|| FILTER_ALLOW_ALL.equals( extensions[0] );
	}

	public boolean accept(File file) {
		if( acceptAll() ) {
			return true;
		}
		
		String filename = file.getName();
		int lastIndexOfPoint = filename.lastIndexOf('.');
		if (lastIndexOfPoint == -1) {
			return mHasEmptyExt;
		}
		String fileType = filename.substring(lastIndexOfPoint+1).toLowerCase();
		return Arrays.binarySearch( extensions, fileType ) >= 0;
	}

	@Override
	public String toString() {
		return toString;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeStringArray(extensions);
		dest.writeString( toString );
	}

	public static final Parcelable.Creator<FileExtensionFilter> CREATOR
							= new Parcelable.Creator<FileExtensionFilter>() {

		@Override
		public FileExtensionFilter createFromParcel(Parcel in) {
			FileExtensionFilter ff = new FileExtensionFilter();
			
			ff.extensions = in.createStringArray();
			ff.toString = in.readString();
		
			return ff;
		}

		// We just need to copy this and change the type to match our class.
		@Override
		public FileExtensionFilter[] newArray(int size) {
			return new FileExtensionFilter[size];
		}
	};
}
