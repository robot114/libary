package com.zsm.util;

public class NumberUtil {

	private NumberUtil() {
		
	}
	
	/**
	 * Convert bytes array to Little Endian Integer
	 * 
	 * @param bytes bytes array
	 * @return the Little Endian integer
	 */
	public static int byteArrayToInt(byte[] bytes) {
	    int value = (bytes[3] << (Byte.SIZE * 3));
	    value |= (bytes[2] & 0xFF) << (Byte.SIZE * 2);
	    value |= (bytes[1] & 0xFF) << (Byte.SIZE * 1);
	    value |= (bytes[0] & 0xFF);
	    return value;
	}

	/**
	 * Convert a Little Endian integer to a bytes array
	 * 
	 * @param value the Little Endian integer
	 * @return the bytes array
	 */
	public static byte[] intToByteArray(int value) {
	    byte[] bytes = new byte[Integer.SIZE / Byte.SIZE];
	    bytes[3] = (byte) (value >> Byte.SIZE * 3);
	    bytes[2] = (byte) (value >> Byte.SIZE * 2);   
	    bytes[1] = (byte) (value >> Byte.SIZE);   
	    bytes[0] = (byte) value;
	    return bytes;
	}
}
