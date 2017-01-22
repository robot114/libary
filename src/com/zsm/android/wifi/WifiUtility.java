package com.zsm.android.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

public class WifiUtility {

	public interface EnableResultListener {
		static final int REASON_FAIL_PERMISSION_DENY = 1;
		static final int REASON_FAIL_FAILED = 2;
		
		public void success( );
		public void failed( int reason );
	}

	private static WifiUtility mInstance;
	private BroadcastReceiver mWifiEnableReceiver;
	
	public static WifiUtility getInstance() {
		if( mInstance == null ) {
			mInstance = new WifiUtility();
		}
		
		return mInstance;
	}
	
	private WifiUtility() {
	}
	
	public void enableWifi( final Context context,
							final EnableResultListener listener ) {
		
	    final WifiManager manager
	    	= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	    
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
	    
	    mWifiEnableReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int state = intent.getIntExtra( WifiManager.EXTRA_WIFI_STATE, -1 );
				if( WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction() ) ) {
					if( state == WifiManager.WIFI_STATE_ENABLED ) {
						context.unregisterReceiver(this);
						mWifiEnableReceiver = null;
						listener.success();
					} else if( state == WifiManager.WIFI_STATE_DISABLED ) {
						listener.failed( EnableResultListener.REASON_FAIL_FAILED );
					}
				}
			}
	    };
	    
	    context.registerReceiver(mWifiEnableReceiver, intentFilter);
	    
	    if( !manager.setWifiEnabled( true ) ) {
	    	listener.failed( EnableResultListener.REASON_FAIL_FAILED );
	    }
	}
	
	public void unregisterWifiEnableReceiver( Context context ) {
		if( mWifiEnableReceiver != null ) {
			context.unregisterReceiver(mWifiEnableReceiver);
			mWifiEnableReceiver = null;
		}
	}

}
