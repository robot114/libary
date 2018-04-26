package com.zsm.android.ui.documentSelector;

import android.net.Uri;

public interface DocumentUserInterface {

	Uri getCurrentLocation();
	
	String getInputedName();

	void dismiss();
}
