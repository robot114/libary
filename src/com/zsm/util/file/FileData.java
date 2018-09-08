package com.zsm.util.file;

import java.util.Comparator;


/**
 * This class contains information about the file name and type
 */
public class FileData implements Comparable<FileData> {

	public static final Comparator<FileData> DEFAULT_COMPARATOR
			= new Comparator<FileData>() {
				@Override
				public int compare(FileData lhs, FileData rhs) {
					if (lhs.mFileType != rhs.mFileType) {
						return lhs.mFileType - rhs.mFileType;
					}
					return lhs.mFileName.compareTo(rhs.mFileName);
				}
	};
	
	/** Constant that specifies the object is a reference to the parent */
	public static final int UP_FOLDER = 0;
	/** Constant that specifies the object is a folder */
	public static final int DIRECTORY = 1;
	/** Constant that specifies the object is a file */
	public static final int FILE = 2;

	private static final String TYPE_NAME[] = { "UP_FOLDER", "DIRECTORY", "FILE" };
	
	/** Defines the type of file. Can be one of UP_FOLDER, DIRECTORY or FILE */
	final private int mFileType;
	
	/** The file's name */
	final private String mFileName;
	/**
	 * This class holds information about the file.
	 * 
	 * @param fileName
	 * 			  - file name
	 * @param fileType
	 *            - file type - can be UP_FOLDER, DIRECTORY or FILE
	 * @throws IllegalArgumentException
	 *             - when illegal type (different than UP_FOLDER, DIRECTORY or
	 *             FILE)
	 */
	public FileData(final String fileName, final int fileType) {

		if (fileType != UP_FOLDER && fileType != DIRECTORY && fileType != FILE) {
			throw new IllegalArgumentException("Illegel type of file");
		}
		this.mFileName = fileName;
		this.mFileType = fileType;
	}

	@Override
	public int compareTo(final FileData another) {
		return DEFAULT_COMPARATOR.compare(this, another);
	}

	public String getFileName() {
		return mFileName;
	}

	public int getFileType() {
		return mFileType;
	}
	
	@Override
	public String toString() {
		return mFileName + "(" + TYPE_NAME[mFileType] + ")";
	}
}