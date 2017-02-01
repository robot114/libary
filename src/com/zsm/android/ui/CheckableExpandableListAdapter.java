package com.zsm.android.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.zsm.R;

public abstract class CheckableExpandableListAdapter<T extends ExpandableListGroup>
							extends BaseExpandableListAdapter {
	
	public interface OnCheckedChangedListener<T extends ExpandableListGroup> {
		void onCheckedChanged( CheckBox view, T data, boolean checked );
	}
	
	public interface OnClickListener<T extends ExpandableListGroup> {
		void onClick( View view, T data, Boolean checked );
	}
	
	public interface OnLongClickListener<T extends ExpandableListGroup> {
		void onLongClick( View view, T data, boolean checked );
	}
	
	private final static Object TAG_CHECKBOX = new Object();
	private final static Object TAG_TEXTVIEW = new Object();
	
	private final static int TAG_ID_DATA = R.id.TAG_ID_GROUP_DATA;
	private final static int TAG_ID_CHECKED = R.id.TAG_ID_GROUP_CHECKED;
	
	protected Context mContext;
	private boolean mItemDuplicable;
	
	private ArrayList<T> mDataList;
	private ArrayList<Boolean> mCheckedList;
	
	// These are the listeners defined by me
	private OnCheckedChangedListener<T> mOnCheckboxChangedListener;
	private OnLongClickListener<T> mOnCheckBoxLongClickListener;
	private OnClickListener<T> mOnTextClickListener;
	private OnLongClickListener<T> mOnTextLongClickListener;
	
	// There are the listeners defined by android
	private OnCheckedChangeListener mCheckBoxViewChangedListener;
	private View.OnClickListener mTextViewClickListener;
	private View.OnLongClickListener mCheckBoxViewLongClickListener;
	private View.OnLongClickListener mTextViewLongClickListener;

	protected CheckableExpandableListAdapter( Context context, boolean itemDuplicable ) {
		mContext = context;
		mItemDuplicable = itemDuplicable;
		
		mDataList = new ArrayList<T>();
		mCheckedList = new ArrayList<Boolean>();
		
		mCheckBoxViewChangedListener = newOnCheckBoxChangListener();
		mCheckBoxViewLongClickListener = newOnCheckBoxLongClickListener();
		mTextViewClickListener = newOnTextViewClickListener();
		mTextViewLongClickListener = newOnTextViewLongClickListener();
	}

	protected CheckableExpandableListAdapter( Context context ) {
		this( context, true );
	}

	private OnCheckedChangeListener newOnCheckBoxChangListener() {
		return new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				
				if( mOnCheckboxChangedListener != null ) {
					mOnCheckboxChangedListener
						.onCheckedChanged( (CheckBox)buttonView,
										   (T) buttonView.getTag(TAG_ID_DATA),
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
					mOnCheckBoxLongClickListener
						.onLongClick( v, (T)v.getTag(TAG_ID_DATA),
								  (Boolean)v.getTag(TAG_ID_CHECKED) );
					
					return true;
				}
				
				return false;
			}
		};
	}

	private View.OnClickListener newOnTextViewClickListener() {
		return new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if( mOnTextClickListener != null ) {
					mOnTextClickListener
						.onClick( v, (T)v.getTag(TAG_ID_DATA),
								  (Boolean)v.getTag(TAG_ID_CHECKED) );
				}
			}
		};
	}

	private View.OnLongClickListener newOnTextViewLongClickListener() {
		return new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if( mOnTextLongClickListener != null ) {
					mOnTextLongClickListener
						.onLongClick( v, (T)v.getTag(TAG_ID_DATA),
								  (Boolean)v.getTag(TAG_ID_CHECKED) );
					
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

	public void setOnTextClickListener(OnClickListener<T> l) {
		this.mOnTextClickListener = l;
	}

	public void setOnTextLongClickListener(OnLongClickListener<T> l) {
		this.mOnTextLongClickListener = l;
	}

	@Override
	public int getGroupCount() {
		return mDataList.size();
	}

	@Override
	public T getGroup(int groupPosition) {
		return mDataList.get(groupPosition);
	}

	@SuppressWarnings("deprecation")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
							 View convertView, ViewGroup parent) {
		
		CheckBox checkboxView;
		TextView textView;
		if( convertView == null ) {
			String infService = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater li
				= (LayoutInflater)mContext.getSystemService( infService );
			convertView = li.inflate( R.layout.checkabel_expandable_group_item, null);
			checkboxView
				= (CheckBox) convertView.findViewById( R.id.libCheckBox );
			checkboxView.setOnCheckedChangeListener( mCheckBoxViewChangedListener );
			checkboxView.setOnLongClickListener(mCheckBoxViewLongClickListener);
			checkboxView.setTag(TAG_CHECKBOX);
			
			textView = (TextView) convertView.findViewById( R.id.libTextView );
			textView.setOnClickListener( mTextViewClickListener );
			textView.setOnLongClickListener(mTextViewLongClickListener);
			textView.setTag(TAG_TEXTVIEW);
		} else {
			checkboxView = (CheckBox) convertView.findViewWithTag(TAG_CHECKBOX);
			textView = (TextView)convertView.findViewWithTag(TAG_TEXTVIEW);
		}
		
		T data = getGroup(groupPosition);
		Boolean checked = mCheckedList.get(groupPosition);
		textView.setText( data.getShowString( mContext ) );
		checkboxView.setChecked( checked );
		textView.setTag(TAG_ID_DATA, data);
		checkboxView.setTag(TAG_ID_DATA, data);
		textView.setTag(TAG_ID_CHECKED, checked);
		checkboxView.setTag(TAG_ID_CHECKED, checked);
		
		int styleResId = data.getGroupStyleResId();
		if( styleResId > 0 ) {
			textView.setTextAppearance( mContext, styleResId );
		}
		
		return convertView;
	}

	public void showPopupMenuForView( View v, int menuResId,
									  OnMenuItemClickListener l ) {
		
		PopupMenu menu = new PopupMenu(mContext, v);
		MenuInflater inflater = menu.getMenuInflater();
		inflater.inflate(menuResId, menu.getMenu());
		menu.setOnMenuItemClickListener( l );
		menu.show();
	}

	public Boolean isChecked( int groupPosition ) {
		return mCheckedList.get(groupPosition);
	}
	
	public void setChecked( int groupPosition, Boolean checked ) {
		mCheckedList.set(groupPosition, checked);
		notifyDataSetChanged();
	}
	
	public void add( T data ) {
		add( data, Boolean.FALSE );
	}

	public void add( T data, Boolean checked ) {
		if( mItemDuplicable || !mDataList.contains(data) ) {
			mDataList.add(data);
			mCheckedList.add( checked );
			notifyDataSetChanged();
		}
	}
	
	public void add( Collection<T> c ) {
		for( T data : c ) {
			add(data);
		}
	}
	
	public T remove( int groupPosition ) {
		mCheckedList.remove( groupPosition );
		T data = mDataList.remove(groupPosition);
		notifyDataSetChanged();
		
		return data;
	}

	public boolean remove( T data ) {
		int index = mDataList.indexOf(data);
		if( index >=0 && index < mDataList.size() ) {
			mCheckedList.remove( index );
			mDataList.remove(index);
			
			notifyDataSetChanged();
			return true;
		}
		
		return false;
	}
	
	public void setCheckAll( Boolean checked ) {
		for( int i = 0; i < mCheckedList.size(); i++ ) {
			mCheckedList.set(i, checked);
		}
		notifyDataSetChanged();
	}
	
	public void clear() {
		mDataList.clear();
		mCheckedList.clear();
		notifyDataSetChanged();
	}
	
	public List<T> getAllWithChecked( Boolean checked ) {
		ArrayList<T> list = new ArrayList<T>( mDataList.size() );
		for( int i = 0; i < mDataList.size(); i++ ) {
			if( checked == mCheckedList.get(i) ) {
				list.add( mDataList.get(i) );
			}
		}
		
		return list;
	}
	
	public int getCheckedGroupCount() {
		int count = 0;
		for( Boolean checked : mCheckedList ) {
			if( checked ) {
				count++;
			}
		}
		
		return count;
	}
}
