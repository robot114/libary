package com.zsm.util;

import java.util.Collection;
import java.util.Iterator;

public class CollectionUtil {

	private CollectionUtil() {}
	
	public static boolean equalStrictly( Collection<?> c1, Collection<?> c2 ) {
		if( c1 == null && c2 == null ) {
			return true;
		}
		
		if( ( c1 == null && c2 != null ) || ( c1 != null && c2 == null ) ) {
			return false;
		}
		
		if( !c1.getClass().equals( c2.getClass() ) ) {
			return false;
		}
		
		if( c1.size() != c2.size() ) {
			return false;
		}
		
		Object o1, o2;
		Iterator<?> it1 = c1.iterator(), it2 = c2.iterator();
		while( it1.hasNext() ) {
			o1 = it1.next();
			o2 = it2.next();
			
			if( ( o1 == null && o2 != null ) || ( o1 != null && o2 == null ) ) {
				return false;
			}
			
			if( o1 != null && !o1.equals(o2) ) {
				return false;
			}
		}
		
		return true;
	}
}
