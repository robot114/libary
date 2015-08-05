package com.zsm.driver.android.log;

import android.app.Activity;
import android.content.Intent;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.TextWatcher;

public class ShowLogTrigger implements TextWatcher {
	
	private Activity context;
	private Class<? extends Activity> logActivityClass;
	private Class<? extends PreferenceActivity> preferenceActivityClass;

	public ShowLogTrigger( Activity context ) {
		this( context, LogActivity.class, LogPreferencesActivity.class );
	}

	public ShowLogTrigger( Activity context, 
						   Class<? extends Activity> logActivityClass,
						   Class<? extends PreferenceActivity> preferenceActivityClass ) {
		this.context = context;
		this.logActivityClass = logActivityClass;
		this.preferenceActivityClass = preferenceActivityClass;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if( s.toString().equals( "show log!" ) ) {
			Intent intent = new Intent( context, logActivityClass );
			intent.putExtra( LogActivity.KEY_PREFERENCE_ACTIVITY,
							 preferenceActivityClass);
			context.startActivity( intent );
		}
	}

}
