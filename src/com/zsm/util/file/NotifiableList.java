package com.zsm.util.file;


public interface NotifiableList<T> {

	void addAndNotify( T data );

	void sortAndNotify();

	void clear();
}
