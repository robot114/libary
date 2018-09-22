package com.zsm.android.ui.quickAction;

import java.util.ArrayList;
import java.util.List;

import com.zsm.R;
import com.zsm.android.ui.PopupWindows;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * QuickAction dialog, shows action list as icon and text like the one in Gallery3D app. Currently supports vertical 
 * and horizontal layout.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 * 
 * Contributors:
 * - Kevin Peck <kevinwpeck@gmail.com>
 */
public class QuickAction extends PopupWindows implements OnDismissListener {
    public enum ORIENTATION { HORIZONTAL, VERTICAL };
    
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private LayoutInflater mInflater;
	private ViewGroup mTrack;
	private ScrollView mScroller;
	private OnActionItemClickListener mItemClickListener;
	private OnDismissListener mDismissListener;
	
	private List<ActionItem> actionItems = new ArrayList<ActionItem>();
	
	private boolean mDidAction;
	
	private int mChildPos;
    private int mInsertPos;
    private ORIENTATION mOrientation;
    
    /**
     * Constructor for default vertical layout
     * 
     * @param context  Context
     */
    public QuickAction(Context context) {
        this(context, ORIENTATION.VERTICAL);
    }

    /**
     * Constructor allowing orientation override
     * 
     * @param context    Context
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public QuickAction(Context context, ORIENTATION orientation) {
        this(context,
        	 orientation == ORIENTATION.HORIZONTAL 
        	 	? R.layout.quick_action_popup_horizontal
        	 		: R.layout.quick_action_popup_vertical,
        	 orientation);
    }

    /**
     * Constructor allowing orientation override
     * 
     * @param context    Context
     * @param layoutId id of the layout resource
     * @param orientation Layout orientation, can be vartical or horizontal
     */
    public QuickAction(Context context, int layoutId, ORIENTATION orientation ) {
        super(context);
        
        mOrientation = orientation;
        
        mInflater 	 = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setRootViewId(layoutId);

        mChildPos 	= 0;
    }

    /**
     * Get action item at an index
     * 
     * @param index  Index of item (position from callback)
     * 
     * @return  Action Item at the position
     */
    public ActionItem getActionItem(int index) {
        return actionItems.get(index);
    }
    
	/**
	 * Set root view.
	 * 
	 * @param id Layout resource id
	 */
	private void setRootViewId(int id) {
		mRootView	= (ViewGroup) mInflater.inflate(id, null);
		mTrack 		= (ViewGroup) mRootView.findViewById(R.id.tracks);

		mArrowDown 	= (ImageView) mRootView.findViewById(R.id.arrow_down);
		mArrowUp 	= (ImageView) mRootView.findViewById(R.id.arrow_up);

		mScroller	= (ScrollView) mRootView.findViewById(R.id.scroller);
		
		//This was previously defined on show() method, moved here to prevent force close that occured
		//when tapping fastly on a view to show quickaction dialog.
		//Thanx to zammbi (github.com/zammbi)
		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		setContentView(mRootView);
	}
	
	/**
	 * Set listener for action item clicked.
	 * 
	 * @param listener Listener
	 */
	public void setOnActionItemClickListener(OnActionItemClickListener listener) {
		mItemClickListener = listener;
	}
	
	/**
	 * Add action item
	 * 
	 * @param action  {@link ActionItem}
	 */
	public void addActionItem(ActionItem action) {
		actionItems.add(action);
		
		String title 	= action.getTitle();
		Drawable icon 	= action.getIcon();
		
		View container;
		
		if (mOrientation == ORIENTATION.HORIZONTAL) {
            container = mInflater.inflate(R.layout.quick_action_item_horizontal, (ViewGroup)null);
        } else {
            container = mInflater.inflate(R.layout.quick_action_item_vertical, (ViewGroup)null);
        }
		
		ImageView img 	= (ImageView) container.findViewById(R.id.iv_icon);
		TextView text 	= (TextView) container.findViewById(R.id.tv_title);
		
		if (icon != null) {
			img.setImageDrawable(icon);
		} else {
			img.setVisibility(View.GONE);
		}
		
		if (title != null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}
		
		final int pos 		=  mChildPos;
		final int actionId 	= action.getActionId();
		
		container.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(QuickAction.this, pos, actionId);
                }
				
                if (!getActionItem(pos).isSticky()) {  
                	mDidAction = true;
                	
                    dismiss();
                }
			}
		});
		
		container.setFocusable(true);
		container.setClickable(true);
			 
		if (mOrientation == ORIENTATION.HORIZONTAL && mChildPos != 0) {
            View separator = mInflater.inflate(R.layout.quick_action_horiz_separator, null);
            
            RelativeLayout.LayoutParams params
            	= new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
            									  LayoutParams.MATCH_PARENT);
            
            separator.setLayoutParams(params);
            separator.setPadding(5, 0, 5, 0);
            
            mTrack.addView(separator, mInsertPos);
            
            mInsertPos++;
        }
		
		mTrack.addView(container, mInsertPos);
		
		mChildPos++;
		mInsertPos++;
	}
	
	public void addActionItems( Context context, boolean sticky, int actionIds[],
								int titleIds[], int iconIds[] ) {
		
		if( actionIds.length != titleIds.length || actionIds.length != iconIds.length ) {
			throw new IllegalArgumentException( "The number of the items in the ids"
												+ " are not equal: "
												+ "len of actionIds = " + actionIds.length
												+ "len of titleIds = " + titleIds.length
												+ "len of iconIds = " + iconIds.length );
		}
		
		for( int i = 0; i < actionIds.length; i++ ) {
			ActionItem item
				= new ActionItem( context, actionIds[i], titleIds[i], iconIds[i] );
			item.setSticky(sticky);
			addActionItem(item);
		}
	}
	
	/**
	 * Pre show
	 * 
	 * @param rootWidth 
	 * @param xPos
	 * @param yPos 
	 * 
	 */
	@Override
	protected void preShow (View anchor, int rootWidth, int xPos, int yPos) {
		mDidAction 			= false;
		
		int[] location 		= new int[2];
	
		anchor.getLocationOnScreen(location);

		Rect anchorRect 	= new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] 
		                	+ anchor.getHeight());

		int rootHeight 		= mRootView.getMeasuredHeight();
		
		int screenHeight = mWindowManager.getDefaultDisplay().getHeight();
		int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
		
		int dyTop			= anchorRect.top;
		int dyBottom		= screenHeight - anchorRect.bottom;

		boolean onTop		= (dyTop > dyBottom) ? true : false;

		int arrowPos;
		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			arrowPos 	= anchorRect.centerX()-xPos;
			
		} else {
			arrowPos = anchorRect.centerX()-xPos;
		}
		if (onTop) {
			if (rootHeight > dyTop) {
				LayoutParams l 	= mScroller.getLayoutParams();
				l.height		= dyTop - anchor.getHeight();
			}
		} else {
			if (rootHeight > dyBottom) { 
				LayoutParams l 	= mScroller.getLayoutParams();
				l.height		= dyBottom;
			}
		}
		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up), arrowPos);
	}
	
	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility(View.VISIBLE);
        
        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
       
        param.leftMargin = requestedX - arrowWidth / 2;
        
        hideArrow.setVisibility(View.INVISIBLE);
    }
	
	/**
	 * Set listener for window dismissed. This listener will only be fired if the quicakction dialog is dismissed
	 * by clicking outside the dialog or clicking on sticky item.
	 */
	public void setOnDismissListener(QuickAction.OnDismissListener listener) {
		setOnDismissListener(this);
		
		mDismissListener = listener;
	}
	
	@Override
	public void onDismiss() {
		if (!mDidAction && mDismissListener != null) {
			mDismissListener.onDismiss();
		}
	}
	
	/**
	 * Listener for item click
	 *
	 */
	public interface OnActionItemClickListener {
		public abstract void onItemClick(QuickAction source, int pos, int actionId);
	}
	
	/**
	 * Listener for window dismiss
	 * 
	 */
	public interface OnDismissListener {
		public abstract void onDismiss();
	}
}