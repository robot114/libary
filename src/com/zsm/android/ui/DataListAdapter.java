package com.zsm.android.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.widget.BaseAdapter;

import com.zsm.util.file.SortableAdapter;

abstract public class DataListAdapter<T> extends BaseAdapter implements SortableAdapter<T> {

	private List<T> mDataList;
	private Handler mHandler;
	
	public DataListAdapter() {
		this.mDataList = new ArrayList<T>();
		mHandler = new Handler( Looper.getMainLooper() );
	}
	
	public DataListAdapter(List<T> list) {
		this.mDataList = list;
	}

	@Override
	public void add( final T data ) {
		if( Looper.getMainLooper().getThread() == Thread.currentThread() ) {
			mDataList.add(data);
			super.notifyDataSetChanged();
		} else {
			if( mHandler == null ) {
				mHandler = new Handler( Looper.getMainLooper() );
			}
			
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					mDataList.add(data);
					notifyDataSetChanged();
				}
			} );
		}
	}
	
	@Override
	public void addAll( final List<T> datas ) {
		if( Looper.getMainLooper().getThread() == Thread.currentThread() ) {
			mDataList.addAll(datas);
			super.notifyDataSetChanged();
		} else {
			if( mHandler == null ) {
				mHandler = new Handler( Looper.getMainLooper() );
			}
			
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					mDataList.addAll(datas);
					notifyDataSetChanged();
				}
			} );
		}
	}
	
	@Override
	public int getCount() {
		return mDataList.size();
	}

	@Override
	public T getItem(int position) {
		return mDataList.get(position);
	}
	
	@Override
	public void sort( final Comparator<T> c ) {
		if( Looper.getMainLooper().getThread() == Thread.currentThread() ) {
			super.notifyDataSetChanged();
		} else {
			if( mHandler == null ) {
				mHandler = new Handler( Looper.getMainLooper() );
			}
			
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					Collections.sort( mDataList, c );
					notifyDataSetChanged();
				}
			} );
		}
	}

	public void clear() {
		if( Looper.getMainLooper().getThread() == Thread.currentThread() ) {
			super.notifyDataSetChanged();
		} else {
			if( mHandler == null ) {
				mHandler = new Handler( Looper.getMainLooper() );
			}
			
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					mDataList.clear();
					notifyDataSetChanged();
				}
			} );
		}
	}
	
}
