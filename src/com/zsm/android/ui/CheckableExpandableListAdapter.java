package com.zsm.android.ui;

import java.security.InvalidParameterException;
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

	private static final int DEFAULT_ITEM_LAYOUT_RES_ID = R.layout.checkable_expandable_group_item;
	private static final int INVALID_RES_ID = -1;

	public interface OnCheckedChangedListener<T> {
		void onCheckedChanged( CheckBox view, T data, boolean checked );
	}
	
	public interface OnClickListener<T> {
		void onClick( View view, T data, Boolean checked );
	}
	
	public interface OnLongClickListener<T> {
		void onLongClick( View view, T data, boolean checked );
	}
	
	private final static Object TAG_CHECKBOX = new Object();
	private static final Object TAG_VIEW = new Object();
	
	protected static final int TAG_ID_POSITION = R.id.TAG_ID_POSITION;

	protected Context mContext;
	private boolean mItemDuplicable;
	
	private CheckableList<T> mDataList;
	
	private boolean mShowCheckbox;
	
	// Item layout resource id. If it is not specified by the subclass, 
	// DEFAULT_ITEM_LAYOUT_RES_ID will be used
	final private int mItemLayoutResId;
	
	// These are the listeners defined by me
	private OnCheckedChangedListener<T> mOnCheckboxChangedListener;
	private OnLongClickListener<T> mOnCheckBoxLongClickListener;
	
	// There are the listeners defined by android
	private OnCheckedChangeListener mCheckBoxViewChangedListener;
	private View.OnLongClickListener mCheckBoxViewLongClickListener;

	protected CheckableExpandableListAdapter( Context context,
											  boolean itemDuplicable ) {
		
		mItemLayoutResId = INVALID_RES_ID;
		mDataList = newDataList();
		init( context, itemDuplicable );
	}

	/**
	 * Constructor to specify the item layout res id
	 * 
	 * @param context
	 * @param itemDuplicable
	 * @param itemLayoutResId Item layout resource id
	 */
	protected CheckableExpandableListAdapter(Context context,
											 boolean itemDuplicable,
											 int itemLayoutResId) {

		mDataList = newDataList();
		init(context, itemDuplicable);
		mItemLayoutResId = itemLayoutResId;
	}

	protected CheckableExpandableListAdapter( Context context,
											  List<T> data,
				 							  boolean itemDuplicable ) {
	
		mItemLayoutResId = INVALID_RES_ID;
		mDataList = newDataList( data );
		init(context, itemDuplicable);
	}

	private void init(Context context, boolean itemDuplicable) {
		mContext = context;
		mItemDuplicable = itemDuplicable;
		
		mCheckBoxViewChangedListener = newOnCheckBoxChangListener();
		mCheckBoxViewLongClickListener = newOnCheckBoxLongClickListener();
		
		mShowCheckbox = true;
	}

	private CheckableList<T> newDataList() {
		return new CheckableList<T>();
	}
	
	private CheckableList<T> newDataList( List<T> list ) {
		return new CheckableList<T>( list );
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
						.onLongClick( v, getGroup(position), isChecked(position) );
					
					return true;
				}
				
				return false;
			}
		};
	}

	final public void setDataList( List<T> data ) {
		mDataList = newDataList( data );
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
			if( mItemLayoutResId != INVALID_RES_ID ) {
				convertView = li.inflate( mItemLayoutResId, null );
			} else {
				convertView = li.inflate( DEFAULT_ITEM_LAYOUT_RES_ID, null);
			}
			
			ViewGroup layoutGroup = (ViewGroup)convertView;
			checkboxView = getCheckboxView( convertView );
			checkboxView.setOnCheckedChangeListener( mCheckBoxViewChangedListener );
			checkboxView.setOnLongClickListener(mCheckBoxViewLongClickListener);
			checkboxView.setTag(TAG_CHECKBOX);
			
			view = getGroupContentView( convertView );
			view.setTag(TAG_VIEW);
			
			// Default item layout no content, should be added
			if( mItemLayoutResId == INVALID_RES_ID ) {
				layoutGroup.addView(view);
			}
		} else {
			checkboxView = (CheckBox) convertView.findViewWithTag(TAG_CHECKBOX);
			view = convertView.findViewWithTag(TAG_VIEW);
		}
		
		view.setTag(TAG_ID_POSITION, groupPosition);
		checkboxView.setTag(TAG_ID_POSITION, groupPosition);
		
		updateGroupView( view, groupPosition );
		boolean checked = isChecked(groupPosition);
		checkboxView.setChecked( checked );
		checkboxView.setVisibility( mShowCheckbox ? View.VISIBLE : View.INVISIBLE );
		
		return convertView;
	}
	
	public synchronized void setCheckAll( boolean checked ) {
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
			if( checked == isChecked(i) ) {
				list.add( getGroup( i ) );
			}
		}
		
		return list;
	}
	
	public int getCheckedGroupCount() {
		int count = 0;
		int groupCount = getGroupCount();
		for( int i = 0; i < groupCount; i++ ) {
			if( isChecked( i ) ) {
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
		return mDataList.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mDataList.size();
	}

	public synchronized void add( T data, boolean checked ) {
		if( !mItemDuplicable && mDataList.contains(data) ) {
			return;
		}
		mDataList.add( data, checked );
		notifyDataSetChanged();
	}
	
	public synchronized void add( T data ) {
		add( data, false );
	}
	
	public synchronized boolean remove( T data ) {
		boolean has = mDataList.remove( data );
		if( has ) {
			notifyDataSetChanged();
		}
		
		return has;
	}
	
	public synchronized boolean removeChcekced( boolean isChecked ) {
		int size = mDataList.size();
		
		boolean changed = false;
		for( int i = 0; i < size; i++ ) {
			if( mDataList.isChecked(i) == isChecked ) {
				changed = true;
				mDataList.remove(i);
			}
		}
		
		if( changed ) {
			notifyDataSetChanged();
		}
		
		return changed;
	}
	
	public synchronized void setChecked(int groupPosition, boolean isChecked) {
		boolean prevChecked = mDataList.setChecked(groupPosition, isChecked);
		if( prevChecked != isChecked ) {
			notifyDataSetChanged();
		}
	}

	public synchronized boolean isChecked(int groupPosition) {
		return mDataList.isChecked(groupPosition);
	}

	protected CheckBox getCheckboxView( View convertView ) {
		if( mItemLayoutResId != INVALID_RES_ID ) {
			throw new InvalidParameterException(
				"getCheckboxView MUST be overrided when item layout resource id is specified!" );
		}
		
		return (CheckBox) convertView.findViewById( R.id.libCheckBox );
	}
	
	public synchronized void checkAll( boolean isChecked ) {
		boolean changed = false;
		int size = mDataList.size();
		for( int i = 0; i < size; i++ ) {
			if( mDataList.isChecked(i) != isChecked ) {
				changed = true;
				mDataList.setChecked(i, isChecked);
			}
		}
		
		if( changed ) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Get the whole view, except the checkbox, of one item. This method will only be invoked
	 * when the {@link convertView} is inflated or created
	 *  
	 * @param convertView the view that includes the checkbox and the content view
	 * @return the content view
	 */
	protected abstract View getGroupContentView(View convertView);

	/**
	 * Update the content view, which includes the whole view, except the checkbox, of one item
	 * 
	 * @param view the content view to be updated
	 * @param groupPosition the index of the data in the list 
	 */
	protected abstract void updateGroupView(View view, int groupPosition);

}
