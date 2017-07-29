package com.zsm.driver.android.log;

import com.zsm.R;
import com.zsm.log.Log;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceChangeListener;

public class LogPreferenceFragment extends PreferenceFragment {

	private static String KEY_LOG_LEVEL;
	
	public LogPreferenceFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.log_preferences);
		
		KEY_LOG_LEVEL = getString( R.string.prefAdvancedLogKeyLogLevel );
		
		Preference pref = findPreference( KEY_LOG_LEVEL );
		
		if( pref != null ) {
			pref.setOnPreferenceChangeListener(
				new OnPreferenceChangeListener(){
					@Override
					public boolean onPreferenceChange(Preference preference,
													  Object newValue) {
						
						Log.LEVEL level = Log.LEVEL.valueOf(newValue.toString());
						Log.setGlobalLevel( level );
						System.out.println( "Global level is set to " + level );
						return true;
					}
				
			});
		}
	}
}
