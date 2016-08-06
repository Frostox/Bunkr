package com.vosaye.bunkr;

import java.text.ParseException;

import net.sqlcipher.Cursor;

import com.vosaye.bunkr.app.Here;
import com.vosaye.bunkr.app.ScheduleListActivity;
import com.vosaye.bunkr.app.TOS;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.services.ValidatorService;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;


public class TrialActivity extends Activity {
	AuthDatabase auth;
	BunKar bunker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trial);
        bunker = ((BunKar) this.getApplication());
		auth = bunker.settings;
		BunKar.count++;

        System.out.println("Vosayen : "+BunKar.count);
		new Handler().postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				TrialActivity.this.finish();
				if(auth.recordExists("uname", "auth")){
					if(auth.authentication.autoLoginActive()){
						Cursor cx = auth.rawQuery("select value from settings where name = 'default' and value not like 'NONE' and value not like 'none'");
						if(cx.moveToFirst()){
							((BunKar) TrialActivity.this.getApplication()).name = cx.getString(0);
							ScheduleDatabase db = (bunker.getDatabase(bunker.name));
							try {
								{
									//this.finish();
									Intent inte = new Intent(TrialActivity.this.getBaseContext(),Here.class);
									inte.putExtra("base", "yes");
									startActivityForResult(inte,0);
									TrialActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						else{
							cx.close();
							//TrialActivity.this.finish();
							Intent inte = new Intent(TrialActivity.this.getBaseContext(),ScheduleListActivity.class);
							inte.putExtra("base", "yes");
							startActivityForResult(inte,0);
							TrialActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
							//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
						}
						
					}
					else{
					//TrialActivity.this.finish();
					startActivityForResult(new Intent(TrialActivity.this.getBaseContext(),LoginActivity.class),0);
					TrialActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
					//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
					}
					
				}
				else{
					//TrialActivity.this.finish();
					startActivityForResult(new Intent(TrialActivity.this.getBaseContext(),RegisterActivity.class),0);
					TrialActivity.this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
					//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
					
				}
			}}, 2000);
		
    }

    public void onBackPressed(){
    	
    }
    
    /*
     public void onStart(){
    	super.onStart();
    	try {
			//Thread.sleep(1500);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	finally{
    	if(auth.recordExists("uname", "auth")){
			if(auth.authentication.autoLoginActive()){
				Cursor cx = auth.rawQuery("select value from settings where name = 'default'");
				if(cx.moveToFirst()){
					((BunKar) this.getApplication()).name = cx.getString(0);
					ScheduleDatabase db = (bunker.getDatabase(bunker.name));
					try {
						if(!db.exists("select * from session")||db.standards.getEndOfTerm()==null){
							//TrialActivity.this.finish(); 
							startActivityForResult(new Intent(TrialActivity.this,TOS.class));
							Toast.makeText(this, "to tos for filling", Toast.LENGTH_LONG).show();
						}
						else{
							//this.finish();
							startActivityForResult(new Intent(TrialActivity.this, Here.class));
							Toast.makeText(this, "Entry Point", Toast.LENGTH_LONG).show();
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
				else{
					cx.close();
					//TrialActivity.this.finish();
					startActivityForResult(new Intent(TrialActivity.this,ScheduleListActivity.class));

					Toast.makeText(this, "to schedules list", Toast.LENGTH_LONG).show();
				}
				
			}
			else{
			//TrialActivity.this.finish();
			startActivityForResult(new Intent(TrialActivity.this,LoginActivity.class));
			}
			
		}
		else{
			//TrialActivity.this.finish();
			startActivityForResult(new Intent(TrialActivity.this,RegisterActivity.class)); 
		}
    	
    	}
    }
*/
    public void onResume(){
    	super.onResume();
    	ValidatorService.FOCUSED = false;
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	BunKar.count--;
    	if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
    }
	
    
}
