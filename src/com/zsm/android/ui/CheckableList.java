package com.zsm.android.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;

public class CheckableList<E> {

	private List<E> mDataList;
	private List<Boolean> mCheckList;
	
	public CheckableList() {
		mDataList = new ArrayList<E>();
		mCheckList = new ArrayList<Boolean>();
	}
	
	public CheckableList( List<E> list ) {
		mDataList = list;
		mCheckList = new ArrayList<Boolean>( list.size() + 1 );
	}
	
	public int size() {
		return mDataList.size();
	}

	public boolean isEmpty() {
		return mDataList.isEmpty();
	}

	public boolean contains(Object o) {
		return mDataList.contains(o);
	}

	public boolean add(E e) {
		return add( e, false );
	}

	public synchronized boolean add(E e, Boolean checked) {
		mDataList.add(e);
		mCheckList.add( checked );
		return true;
	}

	public synchronized boolean remove(Object o) {
		int index = mDataList.indexOf(o);
		if( index >= 0 ) {
			mCheckList.remove(index);
			mDataList.remove(index);
			return true;
		}
			
		return false;
	}

	public boolean containsAll(Collection<?> c) {
		return mDataList.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		for( E e : c ) {
			add( e );
		}
		return true;
	}

	public boolean removeAll(Collection<?> c) {
		boolean res = false;
		for( Object o : c ) {
			res = res | remove( o );
		}
		return res;
	}

	public synchronized void clear() {
		mDataList.clear();
		mCheckList.clear();
	}

	public E get(int index) {
		return mDataList.get(index);
	}

	/**
	 * Set the element at index. The check status of this element will be set as false
	 * 
	 * @param index
	 * @param element
	 * @return
	 */
	public synchronized E set(int index, E element) {
		return set( index, element, false );
	}

	/**
	 * Set the element at index. The check status of this element will be set 
	 * as {@link checked}
	 * 
	 * @param index
	 * @param element
	 * @param checked
	 * @return
	 */
	public E set(int index, E element, Boolean checked) {
		mCheckList.set(index, checked);
		return mDataList.set(index, element);
	}

	synchronized public void add(int index, E element, Boolean checked) {
		mDataList.add(index, element);
		mCheckList.add(index, checked);
	}

	public void add(int index, E element) {
		add( index, element, false );
	}

	public synchronized E remove(int index) {
		mCheckList.remove(index);
		return mDataList.remove(index);
	}

	public int indexOf(Object o) {
		return mDataList.indexOf(o);
	}

	public int lastIndexOf(Object o) {
		return mDataList.lastIndexOf(o);
	}

	public synchronized boolean isChecked( E e ) {
		int index = mDataList.indexOf(e);
		if( index < 0 ) {
			throw new ConcurrentModificationException( 
						"Data list and check list is not synchronized!" );
		}
		
		return mCheckList.get(index);
	}
	
	public synchronized void setChecked( E e, Boolean checked ) {
		int index = mDataList.indexOf(e);
		if( index < 0 ) {
			throw new ConcurrentModificationException( 
						"Data list and check list is not synchronized!" );
		}
		
		mCheckList.set(index, checked);
	}

	public boolean setChecked(int index, boolean isChecked) {
		return mCheckList.set(index, isChecked);
	}

	public boolean isChecked(int index) {
		return mCheckList.get(index);
	}

}
