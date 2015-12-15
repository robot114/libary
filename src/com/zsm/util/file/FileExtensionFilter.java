package com.zsm.util.file;

import java.io.File;
import java.util.Arrays;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressLint("DefaultLocale")
public class FileExtensionFilter implements Parcelable {

	/** Filter which accepts every file */
	public static final String FILTER_ALLOW_ALL = "*.*";
	
	private String extensions[];
	private String toString;

	private FileExtensionFilter() {
		
	}
	
	public FileExtensionFilter( String exts[], String filterDescription ) {
		
		extensions = Arrays.copyOf( exts, exts.length );
		lowerAndSortExts();
		String extDescription = buildExtDescription();
		toString = filterDescription + "(" + extDescription + ")";
	}
	
	public FileExtensionFilter( String allowAllfilterDescription ) {
		this( new String[]{ FILTER_ALLOW_ALL }, allowAllfilterDescription );
	}
	
	private void lowerAndSortExts() {
		for( int i = 0; i < extensions.length; i++ ) {
			extensions[0] = extensions[0].toLowerCase();
		}
		Arrays.sort( extensions );
	}
	
	private String buildExtDescription() {
		if( acceptAll() ) {
			return FILTER_ALLOW_ALL;
		}
		
		StringBuilder builder = new StringBuilder( extensions.length*5 );
		extensions[0] = extensions[0];
		builder.append( "*" ).append( extensions[0] );
		for( int i = 1; i < extensions.length; i++ ) {
			extensions[i] = extensions[i];
			builder.append( "|" ).append( "*" ).append( extensions[i] );
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
			return false;
		}
		String fileType = filename.substring(lastIndexOfPoint).toLowerCase();
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
