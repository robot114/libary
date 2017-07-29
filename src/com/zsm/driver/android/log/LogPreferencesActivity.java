package com.zsm.driver.android.log;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.zsm.R;

public class LogPreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced_preferences);
	}

}
