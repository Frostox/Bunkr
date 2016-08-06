package com.vosaye.bunkr.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {   

	public BootReceiver(){
		super();
	}
	
    @Override
    public void onReceive(Context context, Intent intent) {
    Intent myIntent = new Intent(context, MaintenanceManager.class);
    context.startService(myIntent); 
    Log.d("TestActivity", "onCreate!");  
    }
}
