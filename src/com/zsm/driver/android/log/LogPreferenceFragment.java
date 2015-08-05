package com.zsm.driver.android.log;

import com.zsm.R;

import android.os.Bundle;
import android.preference.PreferenceFragment;

public class LogPreferenceFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.log_preferences);
	}
}
