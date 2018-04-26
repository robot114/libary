package com.zsm.util;

import android.annotation.SuppressLint;

public class TextUtil {

	private TextUtil() {
	}
	
	@SuppressLint("DefaultLocale")
	public static void appendDurationText(StringBuilder text, long duration) {
		duration = duration/1000;
		long h = duration / 3600;
		long m = (duration - h * 3600) / 60;
		long s = duration - (h * 3600 + m * 60);
		String mstr = String.format( "%02d", m);
		String sstr = String.format( "%02d", s);
		if (h != 0) {
			text
				.append( String.format( "%02d", h) )
				.append( ":" ).append( mstr ).append(":").append( sstr );
		} else {
			text.append( mstr ).append(":").append(sstr);
		}
	}

	public static StringBuilder durationToText(long duration) {
		StringBuilder b = new StringBuilder();
		appendDurationText(b, duration);
		return b;
	}
	
	public static boolean isEmptyString( String text ) {
		return text == null || text.length() == 0;
	}

}
