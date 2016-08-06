package com.vosaye.bunkr;





import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.services.ValidatorService;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

	private Thread splashThread;
	AuthDatabase auth;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ActionBar action = this.getActionBar();
		
		action.hide();
		
		splashThread = new Thread(){
			

			@Override
			public void run(){
				try{
					sleep(0);
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally{
					finish();
					startActivityForResult(new Intent(MainActivity.this.getBaseContext(),TrialActivity.class),0);
					
				}
			}
		};
		splashThread.start();
		
		
	}

	public void onResume(){
    	super.onResume();
    	ValidatorService.FOCUSED = false;
    }
	

}
