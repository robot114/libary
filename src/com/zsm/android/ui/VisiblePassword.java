package com.zsm.android.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.zsm.R;
import com.zsm.log.Log;

public class VisiblePassword extends RelativeLayout {

	private ImageView button;
	private EditText text;
	private TextView label;
	private float mLabelWeight = -1f;

	public VisiblePassword(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		if(!isInEditMode()) {
			init();
			customAttributes(attrs);
		}
	}

	public VisiblePassword(Context context, AttributeSet attrs) {
		super(context, attrs);
		if(!isInEditMode()) {
			init();
			customAttributes(attrs);
		}
	}

	public VisiblePassword(Context context) {
		super(context);
		if(!isInEditMode()) {
			init();
		}
	}

	private void init( ) {
		String infService = Context.LAYOUT_INFLATER_SERVICE;
		LayoutInflater li
			= (LayoutInflater)getContext().getSystemService( infService );
		li.inflate( R.layout.visible_password, this, true );
		
		button = (ImageView)findViewById( R.id.visiblePasswordButton );
		text = (EditText)findViewById( R.id.visiblePasswordText );
		label = (TextView)findViewById( R.id.visiblePasswordLabel );
		
		hookupButton();
	}

	private void hookupButton() {
		button.setOnTouchListener( new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch( event.getAction() ) {
					case MotionEvent.ACTION_UP:
						text.setInputType(
								InputType.TYPE_CLASS_TEXT 
								| InputType.TYPE_TEXT_VARIATION_PASSWORD );
						text.setSelection(text.getText().length());
						return true;
					case MotionEvent.ACTION_DOWN:
						text.setInputType( InputType.TYPE_CLASS_TEXT
										   | InputType.TYPE_TEXT_VARIATION_NORMAL );
						text.setSelection(text.getText().length());
						return true;
					default:
						return false;
				}
			}
		} );
	}
	
	private void customAttributes(AttributeSet attrs) {
	    TypedArray a
	    	= getContext()
	    		.obtainStyledAttributes(attrs, R.styleable.VisiblePassword);

	    int n = a.getIndexCount();
	    for (int i = 0; i < n; i++) {
	        int attr = a.getIndex(i);
	        if( attr == R.styleable.VisiblePassword_labelText ) {
	        	label.setText( a.getString(attr) );
	        } else if( attr == R.styleable.VisiblePassword_labelWeight ) {
	        	mLabelWeight  = a.getFloat(attr, -1);
	        } else if( attr == R.styleable.VisiblePassword_hintText ) {
	        	text.setHint( a.getString(attr) );
	        } else if( attr == R.styleable.VisiblePassword_android_imeOptions ) {
	        	//note that you are accessing standard attributes using your attrs identifier
	        	text.setImeOptions( a.getInt(attr, 0) );
	        } else {
	            Log.e("Unknown attribute for " + getClass().toString() + ": " + attr);
	        }
	    }

	    a.recycle();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	    int width = getMeasuredWidth();
	    if( mLabelWeight >= 0 ) {
	    	int desiredWidth = getDesiredStringWidth();
			setComponentWidth( label, (int)(mLabelWeight*desiredWidth) );
	    }
	    
	    if( !isInEditMode() ) {
	    	int height = Math.max(text.getMeasuredHeight(), button.getMeasuredHeight() );
		    setMeasuredDimension(width, height);
	    } else {
		    setMeasuredDimension(width, 128);
	    }
	    
	}

	private int getDesiredStringWidth( ) {
		return getMeasuredWidth() - button.getMeasuredWidth();
	}
	
	private void setComponentWidth( View v, int width ) {
		LayoutParams layoutParams = (LayoutParams) label.getLayoutParams();
		layoutParams.width = width;
		v.setLayoutParams( layoutParams );
	}
	
	public String getPassword() {
		return text.getText().toString();
	}
	
	public void setOnEditorActionListener( OnEditorActionListener l ) {
		text.setOnEditorActionListener( l  );
	}
	
	public void addTextChangedListener(TextWatcher w) {
		text.addTextChangedListener( w );
	}
	
	public void setLabel( String text ) {
		label.setText(text);
	}

	public void setLabel( int resId ) {
		label.setText(resId);
	}
	
	public int getLabelWidth() {
		return label.getWidth();
	}
	
	public void setLabelWidth(int pixels) {
		label.setWidth( pixels );
	}
	
	public TextView getLabel() {
		return label;
	}

}
