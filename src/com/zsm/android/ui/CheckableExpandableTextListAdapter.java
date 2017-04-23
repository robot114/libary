package com.zsm.android.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

public abstract class CheckableExpandableTextListAdapter<T extends ExpandableTextListGroup>
							extends BaseExpandableListAdapter {
	
	public interface OnCheckedChangedListener<T extends ExpandableTextListGroup> {
		void onCheckedChanged( CheckBox view, T data, boolean checked );
	}
	
	public interface OnClickListener<T extends ExpandableTextListGroup> {
		void onClick( View view, T data, Boolean checked );
	}
	
	public interface OnLongClickListener<T extends ExpandableTextListGroup> {
		void onLongClick( View view, T data, boolean checked );
	}
	
	private final static Object TAG_CHECKBOX = new Object();
	private final static Object TAG_TEXTVIEW = new Object();
	
	private final static int TAG_ID_DATA = R.id.TAG_ID_GROUP_DATA;
	private final static int TAG_ID_CHECKED = R.id.TAG_ID_GROUP_CHECKED;
	private static final int TAG_ID_POSITION = R.id.TAG_ID_POSITION;
	
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

	protected CheckableExpandableTextListAdapter( Context context, boolean itemDuplicable ) {
		mContext = context;
		mItemDuplicable = itemDuplicable;
		
		mDataList = new ArrayList<T>();
		mCheckedList = new ArrayList<Boolean>();
		
		mCheckBoxViewChangedListener = newOnCheckBoxChangListener();
		mCheckBoxViewLongClickListener = newOnCheckBoxLongClickListener();
		mTextViewClickListener = newOnTextViewClickListener();
		mTextViewLongClickListener = newOnTextViewLongClickListener();
	}

	protected CheckableExpandableTextListAdapter( Context context ) {
		this( context, true );
	}

	private OnCheckedChangeListener newOnCheckBoxChangListener() {
		return new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
										 boolean isChecked) {
				
				int position = (int) buttonView.getTag(TAG_ID_POSITION);
				mCheckedList.set(position, isChecked );
				
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
			convertView = li.inflate( R.layout.checkable_expandable_group_text_item, null);
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
		textView.setTag(TAG_ID_DATA, data);
		checkboxView.setTag(TAG_ID_DATA, data);
		textView.setTag(TAG_ID_CHECKED, checked);
		checkboxView.setTag(TAG_ID_CHECKED, checked);
		textView.setTag(TAG_ID_POSITION, groupPosition);
		checkboxView.setTag(TAG_ID_POSITION, groupPosition);
		
		textView.setText( data.getShowString( mContext ) );
		checkboxView.setChecked( checked );
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
