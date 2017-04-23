package com.zsm.android.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.zsm.R;

public abstract class CheckableExpandableListAdapter<T> extends BaseExpandableListAdapter {

	public interface OnCheckedChangedListener<T> {
		void onCheckedChanged( CheckBox view, T data, boolean checked );
	}
	
	public interface OnClickListener<T> {
		void onClick( View view, T data, Boolean checked );
	}
	
	public interface OnLongClickListener<T> {
		void onLongClick( View view, T data, boolean checked );
	}
	
	final protected class CheckableData {
		private T mData;
		private boolean mChecked;
		
		private CheckableData( T data, boolean checked ) {
			mData = data;
			mChecked = checked;
		}

		@Override
		public boolean equals(Object obj) {
			if( obj == this ) {
				return true;
			}
			
			if( obj == null || !( obj.getClass().equals( CheckableData.class ) ) ) {
				return false;
			}
			
			@SuppressWarnings("unchecked")
			CheckableData cd = (CheckableData)obj;
			return cd.mData.equals(mData);
		}
	}
	
	private final static Object TAG_CHECKBOX = new Object();
	private static final Object TAG_VIEW = new Object();
	
	private static final int TAG_ID_POSITION = R.id.TAG_ID_POSITION;

	protected Context mContext;
	private boolean mItemDuplicable;
	
	protected List<CheckableData> mDataList;
	private boolean mShowCheckbox;
	
	// These are the listeners defined by me
	private OnCheckedChangedListener<T> mOnCheckboxChangedListener;
	private OnLongClickListener<T> mOnCheckBoxLongClickListener;
	
	// There are the listeners defined by android
	private OnCheckedChangeListener mCheckBoxViewChangedListener;
	private View.OnLongClickListener mCheckBoxViewLongClickListener;

	protected CheckableExpandableListAdapter( Context context,
											 boolean itemDuplicable ) {
		
		mContext = context;
		mItemDuplicable = itemDuplicable;
		
		mCheckBoxViewChangedListener = newOnCheckBoxChangListener();
		mCheckBoxViewLongClickListener = newOnCheckBoxLongClickListener();
		
		mDataList = newDataList();
		mShowCheckbox = true;
	}

	protected List<CheckableData> newDataList() {
		return new ArrayList<CheckableData>();
	}
	
	private OnCheckedChangeListener newOnCheckBoxChangListener() {
		return new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				
				int position = (int) buttonView.getTag(TAG_ID_POSITION);
				setChecked(position, isChecked );
				
				if( mOnCheckboxChangedListener != null ) {
					mOnCheckboxChangedListener
						.onCheckedChanged( (CheckBox)buttonView,
										   getGroup(position),
										   isChecked );
				}
			}
		};
	}
	
	private View.OnLongClickListener newOnCheckBoxLongClickListener() {
		return new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if( mOnCheckBoxLongClickListener != null ) {
					int position = (int) v.getTag(TAG_ID_POSITION);
					mOnCheckBoxLongClickListener
						.onLongClick( v, getGroup(position), getChecked(position) );
					
					return true;
				}
				
				return false;
			}
		};
	}

	public void setOnCheckboxChangeListener( OnCheckedChangedListener<T> l) {
		this.mOnCheckboxChangedListener = l;
	}

	public void setOnCheckBoxLongClickListener( OnLongClickListener<T> l) {
		this.mOnCheckBoxLongClickListener = l;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		
		CheckBox checkboxView;
		View view;
		if( convertView == null ) {
			String infService = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li
				= (LayoutInflater)mContext.getSystemService( infService );
			convertView = li.inflate( R.layout.checkable_expandable_group_item, null);
			ViewGroup layoutGroup = (ViewGroup)convertView;
			checkboxView
				= (CheckBox) convertView.findViewById( R.id.libCheckBox );
			checkboxView.setOnCheckedChangeListener( mCheckBoxViewChangedListener );
			checkboxView.setOnLongClickListener(mCheckBoxViewLongClickListener);
			checkboxView.setTag(TAG_CHECKBOX);
			
			view = getGroupContentView( );
			view.setTag(TAG_VIEW);
			
			layoutGroup.addView(view);
		} else {
			checkboxView = (CheckBox) convertView.findViewWithTag(TAG_CHECKBOX);
			view = convertView.findViewWithTag(TAG_VIEW);
		}
		
		view.setTag(TAG_ID_POSITION, groupPosition);
		checkboxView.setTag(TAG_ID_POSITION, groupPosition);
		
		updateGroupView( view, groupPosition );
		boolean checked = getChecked(groupPosition);
		checkboxView.setChecked( checked );
		checkboxView.setVisibility( mShowCheckbox ? View.VISIBLE : View.INVISIBLE );
		
		return convertView;
	}

	public void setCheckAll( boolean checked ) {
		int groupCount = getGroupCount();
		for( int i = 0; i < groupCount; i++ ) {
			setChecked(i, checked);
		}
		notifyDataSetChanged();
	}
	
	public List<T> getAllWithChecked( boolean checked ) {
		int groupCount = getGroupCount();
		ArrayList<T> list = new ArrayList<T>( groupCount );
		for( int i = 0; i < groupCount; i++ ) {
			if( checked == getChecked(i) ) {
				list.add( getGroup( i ) );
			}
		}
		
		return list;
	}
	
	public int getCheckedGroupCount() {
		int count = 0;
		int groupCount = getGroupCount();
		for( int i = 0; i < groupCount; i++ ) {
			if( getChecked( i ) ) {
				count++;
			}
		}
		
		return count;
	}

	public void showCheckBox( boolean show ) {
		mShowCheckbox = show;
		notifyDataSetChanged();
	}
	
	@Override
	public T getGroup(int groupPosition) {
		return mDataList.get(groupPosition).mData;
	}

	@Override
	public int getGroupCount() {
		return mDataList.size();
	}

	public void add( T data, boolean checked ) {
		if( !mItemDuplicable && mDataList.contains(data) ) {
			return;
		}
		mDataList.add( new CheckableData( data, checked ) );
		notifyDataSetChanged();
	}
	
	public void add( T data ) {
		add( data, false );
	}
	
	public T remove( T data ) {
		boolean found = mDataList.remove(new CheckableData( data, false ) );
		if( found ) {
			notifyDataSetChanged();
			return data;
		}
		
		return null;
	}
	
	public void setChecked(int groupPosition, boolean isChecked) {
		CheckableData cd = mDataList.get(groupPosition);
		if( cd.mChecked != isChecked ) {
			cd.mChecked = isChecked;
			notifyDataSetChanged();
		}
	}

	public boolean getChecked(int groupPosition) {
		return mDataList.get(groupPosition).mChecked;
	}

	protected abstract View getGroupContentView();

	protected abstract void updateGroupView(View view, int groupPosition);

}
