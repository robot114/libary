package com.zsm.util.file;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.zsm.util.file.android.StringFilter;

@SuppressLint("DefaultLocale")
public class FileExtensionFilter implements Parcelable, StringFilter {

	private static final char[] INVALID_CHAR_SET
		= new char[]{ '\\', '\"', '/', '|', '*', ':', '?', '<', '>', '.' };
	
	/** Filter which accepts every file */
	public static final String FILTER_ALLOW_ALL = "*.*";
	
	private String extensions[];
	private String mExtDescription;
	private String mDefaultExtension = "";
	
	private String toString;

	private FileDataListNotifier mNotifier;

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

	/**
	 * Constructor of filter by extension.
	 * @param exts Extensions with the format ".mp3"
	 * @param filterDescription File filter's description, such as "Audio file"
	 * @param notifier Notifier be notified when a file is accepted or rejected
	 */
	public FileExtensionFilter( String exts[], String filterDescription,
								FileDataListNotifier notifier ) {
		
		extensions = Arrays.copyOf( exts, exts.length );
		mNotifier = notifier;
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
		this( exts, filterDescription, (FileDataListNotifier)null );
	}
	
	/**
	 * Construct filter from a string with extensions, which are separated by "|".
	 * Each extension can be as the format "*.ext", ".ext", or "ext".
	 * An example of the string is "*.mp3|.wav|wmv".
	 * 
	 * @param exts extension string
	 * @param filterDescription File filter's description, such as "Audio file"
	 * @param notifier Notifier be notified when a file is accepted or rejected
	 */
	public FileExtensionFilter(String exts, String filterDescription,
							   FileDataListNotifier notifier ) {
		
		TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter('|');
		splitter.setString(exts);
		
		Vector<String> v = new Vector<String>( 10 );
		for( String ext : splitter ) {
			v.add(ext);
		}
		
		extensions = new String[ v.size() ];
		v.toArray( extensions );
		init( filterDescription );
		mNotifier = notifier;
	}

	public FileExtensionFilter( String allowAllfilterDescription ) {
		this( allowAllfilterDescription, (FileDataListNotifier)null );
	}
	
	public FileExtensionFilter(String allowAllfilterDescription,
							   FileDataListNotifier notifier ) {
		
		this( new String[]{ FILTER_ALLOW_ALL }, allowAllfilterDescription );
		mNotifier = notifier;
	}

	private void normalizeAndSortExts() {
		if( extensions.length == 1 && extensions[0].equals( FILTER_ALLOW_ALL ) ) {
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
			extensions[i] = ext.trim();
			if( extensions[i].length() == 0 ) {
				throw new IllegalArgumentException( "Empty extension found!" );
			}
		}
		
		mDefaultExtension = extensions[0];
		
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
		return accept( file.getName() );
	}

	public boolean accept(String filename) {
		boolean a = acceptByName(filename);
		if( mNotifier != null ) {
			mNotifier.forAcception( filename, a );
		}
		
		return a;
	}
	
	public boolean acceptByName(String filename) {
		if( acceptAll() ) {
			return true;
		}
		
		String fileType = FilenameUtils.getExtension(filename).toLowerCase();
		return Arrays.binarySearch( extensions, fileType ) >= 0;
	}
	
	/**
	 * The default extension is the first extension in the extension's array.
	 * For example, the extension is "*.mp3|.wav|wmv", when the filter is
	 * constructed, the default one will be ".mp3".
	 * If the method {@link acceptAll} is true, "" will be returned.
	 * 
	 * @return the default extension.
	 */
	public String getDefaultExtension() {
		return mDefaultExtension;
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
