package com.zsm.android.image;

import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class ImageUtilities {

	// TODO: NOT TESTED. Copied from 
	// http://stackoverflow.com/questions/20358803/how-to-use-scriptintrinsicyuvtorgb-converting-byte-yuv-to-byte-rgba 
	// and rewritten 
	public static int[] yuv420ToRgb8888( Context context, byte[] yuvByteArray,
								 		 int width, int height ) {
		
	    RenderScript rs = RenderScript.create(context);
	    ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic
	    	= ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
	
	    Type.Builder yuvType
	    	= new Type.Builder(rs, Element.U8(rs)).setX(yuvByteArray.length);
	    Allocation in
	    	= Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
	
	    Type.Builder rgbaType
	    	= new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
	    Allocation out
	    	= Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
	
	    in.copyFrom(yuvByteArray);
	
	    yuvToRgbIntrinsic.setInput(in);
	    yuvToRgbIntrinsic.forEach(out);
	    
	    int[] pixels = new int[width*height];
	    
	    out.copyTo(pixels);
	    
	    return pixels;
	}
}
