package com.zsm.util.file;

import java.util.Comparator;
import java.util.List;


public interface SortableAdapter<T> {

	void add( T data );
	
	void addAll( List<T> datas );

	void clear();

	void sort(Comparator<T> c);
}
