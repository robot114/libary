package com.zsm.util.file;

import java.util.Comparator;


public interface SortableAdapter<T> {

	void add( T data );

	void clear();

	void sort(Comparator<T> c);
}
