package com.zsm.android;

import android.os.Build;

public class AndroidUtility {

	public static boolean isAndroidEmulator() {
	    String product = Build.PRODUCT;
	    boolean isEmulator = false;
	    if (product != null) {
	        isEmulator
	        	= product.equals("sdk") || product.contains("_sdk")
	        	  || product.contains("sdk_");
	    }
	    return isEmulator;
	}
}
