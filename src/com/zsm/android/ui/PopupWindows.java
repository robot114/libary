package com.zsm.android.ui;

import com.zsm.R;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

/**
 * Custom popup window.
 * 
 * @author Lorensius W. L. T <lorenz@londatiga.net>
 *
 */
public class PopupWindows {
    public static final int ANIM_GROW_FROM_LEFT = 1;
	public static final int ANIM_GROW_FROM_RIGHT = 2;
	public static final int ANIM_GROW_FROM_CENTER = 3;
	public static final int ANIM_REFLECT = 4;
	public static final int ANIM_AUTO = 5;
	
	protected Context mContext;
	protected PopupWindow mWindow;
	protected View mRootView;
	protected Drawable mBackground = null;
	protected WindowManager mWindowManager;
	
    private int mRootWidth = 0;
	private int mAnimStyle = ANIM_AUTO;
	
	/**
	 * Constructor.
	 * 
	 * @param context Context
	 */
	public PopupWindows(Context context) {
		mContext	= context;
		mWindow 	= new PopupWindow(context);

		mWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mWindow.dismiss();
					
					return true;
				}
				
				return false;
			}
		});

		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
	}
	
	/**
	 * Constructor
	 * 
	 * @param context
	 * @param resId layout resource id of the window
	 */
	public PopupWindows( Context context, int resId ) {
		this(context);
		
        LayoutInflater inflater
        	= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView( inflater.inflate(resId, null) );
	}
	
	/**
	 * On dismiss
	 */
	protected void onDismiss() {		
	}
	
	/**
	 * On show
	 */
	protected void onShow() {		
	}

	/**
	 * Initialize the window before it is shown
	 */
	protected void initWindow() {
		if (mRootView == null) 
			throw new IllegalStateException("setContentView was not called with a view to display.");
	
		onShow();

		if (mBackground == null) 
			mWindow.setBackgroundDrawable(new BitmapDrawable());
		else 
			mWindow.setBackgroundDrawable(mBackground);

		mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		mWindow.setTouchable(true);
		mWindow.setFocusable(true);
		mWindow.setOutsideTouchable(true);

		mWindow.setContentView(mRootView);
	}

	/**
	 * Set background drawable.
	 * 
	 * @param background Background drawable
	 */
	public void setBackgroundDrawable(Drawable background) {
		mBackground = background;
	}

	/**
	 * Set content view.
	 * 
	 * @param root Root view
	 */
	public void setContentView(View root) {
		mRootView = root;
		
		mWindow.setContentView(root);
	}

	/**
	 * Set content view.
	 * 
	 * @param layoutResID Resource id
	 * 
	 * @return the content view set
	 */
	public View setContentView(int layoutResID) {
		LayoutInflater inflator
			= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View view = inflator.inflate(layoutResID, null);
		setContentView(view);
		
		return view;
	}

	/**
	 * Set listener on window dismissed.
	 * 
	 * @param listener
	 */
	public void setOnDismissListener(PopupWindow.OnDismissListener listener) {
		mWindow.setOnDismissListener(listener);  
	}

	/**
	 * Dismiss the popup window.
	 */
	public void dismiss() {
		mWindow.dismiss();
	}
	
	/**
	 * Show quickaction popup. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show (View anchor) {
		initWindow();
		
		int xPos, yPos;
		
		int[] location = new int[2];
	
		anchor.getLocationOnScreen(location);

		Rect anchorRect
			= new Rect(location[0], location[1], location[0] + anchor.getWidth(),
						location[1] + anchor.getHeight());
		//mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		mRootView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	
		int rootHeight 		= mRootView.getMeasuredHeight();
		
		if (mRootWidth == 0) {
			mRootWidth		= mRootView.getMeasuredWidth();
		}
		
		int screenWidth 	= mWindowManager.getDefaultDisplay().getWidth();
		int screenHeight	= mWindowManager.getDefaultDisplay().getHeight();
		
		//automatically get X coord of popup (top left)
		if ((anchorRect.left + mRootWidth) > screenWidth) {
			xPos 		= anchorRect.left - (mRootWidth-anchor.getWidth());			
			xPos 		= (xPos < 0) ? 0 : xPos;
		} else {
			if (anchor.getWidth() > mRootWidth) {
				xPos = anchorRect.centerX() - (mRootWidth/2);
			} else {
				xPos = anchorRect.left;
			}
		}
		
		int dyTop			= anchorRect.top;
		int dyBottom		= screenHeight - anchorRect.bottom;

		boolean onTop		= (dyTop > dyBottom) ? true : false;

		if (onTop) {
			if (rootHeight > dyTop) {
				yPos = 15;
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;
			
			if (rootHeight > dyBottom) { 
			}
		}
		
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		
		preShow(anchor, mRootWidth, xPos, yPos);
		
		mWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}

	protected void preShow(View anchor, int rootWidth, int xPos, int yPos) {
	}

	/**
	 * Set animation style
	 * 
	 * @param screenWidth screen width
	 * @param requestedX distance from left edge
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
	 * 		  and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {

		switch (mAnimStyle) {
		case ANIM_GROW_FROM_LEFT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			break;
					
		case ANIM_GROW_FROM_RIGHT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			break;
					
		case ANIM_GROW_FROM_CENTER:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		break;
			
		case ANIM_REFLECT:
			mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Reflect : R.style.Animations_PopDownMenu_Reflect);
		break;
		
		case ANIM_AUTO:
			if (requestedX <= screenWidth/4) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
			} else if (requestedX > screenWidth/4 && requestedX < 3 * (screenWidth/4)) {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
			} else {
				mWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
			}
					
			break;
		}
	}
	
	/**
	 * Set animation style
	 * 
	 * @param animStyle animation style, default is set to ANIM_AUTO
	 */
	public void setAnimStyle(int animStyle) {
		mAnimStyle  = animStyle;
	}
	
}