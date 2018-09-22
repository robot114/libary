package com.zsm.util.file.android;

import java.util.Comparator;

import android.net.Uri;

public abstract class DocumentData {

	public enum TYPE { VOLUME, UP_FOLDER, FOLDER, DOCUMENT };
	
	protected static final String UP_FOLDER_NAME = "../";
	protected static final String ROOT_NAME = "Root";
	
	private TYPE mType;
	
	protected DocumentData( TYPE type ) {
		mType = type;
	}
	
	public static final Comparator<DocumentData> DEFAULT_COMPARATOR
		= new Comparator<DocumentData>() {

			@Override
			public int compare(DocumentData lhs, DocumentData rhs) {
				if( lhs.getType() != rhs.getType() ) {
					return lhs.getType().ordinal() - rhs.getType().ordinal();
				}
				
				String lhsName = lhs.getName();
				if( lhsName == null ) {
					lhsName = "";
				}
				String rhsName = rhs.getName();
				rhsName = rhsName == null ? "" : rhsName;
				return lhsName.compareToIgnoreCase( rhsName );
			}
		
	};
	
	public TYPE getType() {
		return mType;
	}
	
	public abstract String getName();
	
	public abstract Uri getUri();
}
