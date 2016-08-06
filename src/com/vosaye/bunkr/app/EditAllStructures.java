package com.vosaye.bunkr.app;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.TrialActivity;
import com.vosaye.bunkr.app.WeekDayStructure.MyBroadcastReceiver;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.externalpackages.Title_Adapter;
import com.vosaye.bunkr.externalpackages.master.TitleFlowIndicator;
import com.vosaye.bunkr.externalpackages.master.ViewFlow_Master;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.Uploader;
import com.vosaye.bunkr.services.ValidatorService;

public class EditAllStructures extends SherlockFragmentActivity{
	private ViewFlow_Master viewFlow;
	String[] pseudoForDays;
	Title_Adapter adapter;
	BunKar bunker;
	ScheduleDatabase sched;
	AlertDialog dialog_forsetter;
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.setTitle("Title ViewFlow");

		bunker = (BunKar) this.getApplication();
		setContentView(R.layout.viewflow_title_layout);
		ActionBar action = this.getSupportActionBar();
        action.show();
        action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Create Time Table" + "</font>"));
        BunKar.count++;
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        action.setSubtitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+bunker.name + "</font>"));
		
        viewFlow = (ViewFlow_Master) findViewById(R.id.title_viewflow);
		adapter = new Title_Adapter(this);
		
		//viewFlow.setAdapter(adapter);
		TitleFlowIndicator indicator = (TitleFlowIndicator) findViewById(R.id.title_viewflowindicator);
		indicator.setTitleProvider(adapter);
		viewFlow.setFlowIndicator(indicator);
		
		sched = bunker.getDatabase(bunker.name);
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATESTR");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(mBroadcast, intentFilter);
		

		viewFlow.setAdapter(adapter);
		
	}
	
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
	}
	
	public void onPostResume(){
		super.onPostResume();
		
	}
	
	public void onStart(){
		super.onStart();
		
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);
		viewFlow.onConfigurationChanged(newConfig);
	}
	
	
	
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
       com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
       inflater.inflate(R.menu.donemenu, (com.actionbarsherlock.view.Menu) menu);
       return super.onCreateOptionsMenu(menu);
    }
    
    
    ProgressBar bar;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.done_button) {
			//check if atleast one index has anything
			//go to base
			AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(this);
			builder_forsetter.setTitle("Setting up your Semester");
			View view = this.getLayoutInflater().inflate(R.layout.setting_term_wait_dialog, null);
			TextView title, subtitle;
			title = (TextView) view.findViewById(R.id.atos_textView1);
			subtitle = (TextView) view.findViewById(R.id.atos_textView2);
			bar = (ProgressBar) view.findViewById(R.id.atos_progressBar1);
			bar.setMax(7);
			bar.setProgress(0);
			title.setText("Please wait a moment");
			subtitle.setText("It might take a while depending on the size of your term\nPlease do not close the app");
			builder_forsetter.setView(view);
			builder_forsetter.setCancelable(false);
			dialog_forsetter = builder_forsetter.create();
			dialog_forsetter.show();
			adapter.closeLists();
			bg = new BackgroundStructureEditter(this);
			bg.execute();
			return true;
		} else if(itemId == android.R.id.home){
			this.setResult(1);
			finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
        
        
    }
    BackgroundStructureEditter bg;
    
    
    
    
    
    class BackgroundStructureEditter extends AsyncTask<String,Void,String>{

    	EditAllStructures edit;
    	
    	public BackgroundStructureEditter(EditAllStructures edit){
    		this.edit = edit;
    		
    		
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			MaintenanceManager.HALT = true;
			if(bunker.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
				while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
					//wait for manager to be halted by this thread...
				}
			}
			
			ValidatorService.HALT = true;
			
			while(ValidatorService.status==ValidatorService.BUSY){
				if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
					startService(new Intent(EditAllStructures.this,ValidatorService.class));
			}
			
			Cursor c = bunker.settings.rawQuery("select max(id) from labsdecore"+bunker.name.replaceAll(" ", "_")+" where id > 0;");
			if(c.moveToFirst()){
				if(c.getString(0)==null){
				//sched.close();
				bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
				//sched.open();
				}
			}else {
				//sched.close();
				bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
				//sched.open();
			}
			c.close();
			sched.beginTransaction();
			edit.pseudoForDays = adapter.pseudoForDays;
        	Date date;
        	try {
        		date = sched.standards.getStartOfTerm();
        		Calendar cal = Calendar.getInstance();
        		cal.setTime(sched.start);
        		int day;
        		
        		for(int i=0; i<7; i++){
        			//Toast.makeText(EditAllStructures.this, ""+sdf.format(cal.getTime()), Toast.LENGTH_LONG).show();
            		try {
            			day = cal.get(Calendar.DAY_OF_WEEK);
            			day=day-1;
            			if(!sched.isEmpty(pseudoForDays[day])){
            				String name = sched.meta.insertIntoIndex(cal.getTime(), sched.meta.getUUID(""), sched.meta.countWeeks(cal.getTime(), sched.standards.getEndOfTerm()), true);
    						System.out.println(pseudoForDays[day]+"    "+name);
    						System.out.println("checking in here mate....,  "+day);
    						if(edit.pseudoForDays[day]!=null){
    							sched.meta.copyStructure(edit.pseudoForDays[day], name);
    							sched.meta.copyRecordBack(edit.pseudoForDays[day], cal.getTime(), sched.meta.countWeeks(cal.getTime(), sched.end));
    							sched.stats.addToPipeline(sched.stats.selectMonthly(cal.getTime()));
    							sched.stats.addToPipeline(sched.stats.selectWeekly(cal.getTime()));
    						}
            			}
            			else{
            				try{
							sched.meta.insertBlankIntoIndex(cal.getTime(), sched.meta.countWeeks(cal.getTime(), sched.standards.getEndOfTerm()));
            				}
            				catch(BunkerException e){e.printStackTrace();}
						}
            			sched.dropTable(edit.pseudoForDays[day]);
            			
            			if(bg!=null)
            				if(bg.isCancelled()){
            					sched.rollback();
            					return null;
            				}
						
            		} catch (SQLiteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BunkerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		cal.add(Calendar.DATE, 1);
            	}
        		sched.commit();
        		//sched.close();
        		bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Editted whole semester");
        		//sched.open();
        		ValidatorService.HALT = false;

            	
        		
        	} catch (ParseException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();
        	} catch (SQLiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				if(sched.getDatabase().inTransaction()) sched.rollback();
				ValidatorService.HALT = false;
			}
        	return null;
		}
		protected void onPostExecute(String param){
			dialog_forsetter.dismiss();
			
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(EditAllStructures.this);
			builder.setTitle("Do you wanna upload your time table?");
			builder.setMessage("Help your class-mates to bunkr!");
    		View v = EditAllStructures.this.getLayoutInflater().inflate(R.layout.upload, null);
    		builder.setView(v);
    		final EditText tags = (EditText) v.findViewById(R.id.editText1);
    		tags.setHorizontallyScrolling(false);
    		tags.setLines(5);
    		builder.setPositiveButton("Upload", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(tags.getText().toString().equals("")||tags.getText().toString().length()>140){
						Toast.makeText(EditAllStructures.this, "Tags cant be blank and they should be maximum 140 characters. Upload canceled", Toast.LENGTH_LONG).show();
						return;
					}else if(!StringUtils.isAlphanumericSpace(tags.getText().toString())){
						Toast.makeText(EditAllStructures.this, "Tags should be alphanumeric. Upload canceled", Toast.LENGTH_LONG).show();
						return; 
					}
					bunker.tagForUpload = tags.getText().toString();
					
					if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.Uploader"))
						startService(new Intent(EditAllStructures.this,Uploader.class));
					
					dialog.dismiss();
					EditAllStructures.this.finish();
		    		Intent intent = new Intent(EditAllStructures.this.getApplicationContext(),Here.class);
		    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		    		
					startActivityForResult(intent,0);
				}});
    		builder.setNegativeButton("Skip", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					EditAllStructures.this.finish();
		    		Intent intent = new Intent(EditAllStructures.this.getApplicationContext(),Here.class);
		    		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
		    		
					startActivityForResult(intent,0);
				}});
    		builder.setCancelable(false);
			builder.show();
			
			//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
			
		}
    }  
    
    public void onPause(){
    	super.onPause();
    	
    	ValidatorService.FREEFLOW = true;
    }
    public void onResume(){
    	super.onResume();
    	
    	ValidatorService.FREEFLOW = false;
    	ValidatorService.FOCUSED = true;
    }
	public void onDestroy(){
		super.onDestroy();
		if(sched.getDatabase().inTransaction())
			sched.rollback();
		ValidatorService.HALT = false;
		this.unregisterReceiver(mBroadcast);
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
		adapter.closeLists();
		if(bg!=null)
		bg.cancel(true);
		
		
		
		
	}
	int totaldays = 1;
	MyBroadcastReceiver mBroadcast;
	public class MyBroadcastReceiver extends BroadcastReceiver { 

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  int perc = intent.getIntExtra("perc", 0);
			  if(bar!=null){
				  if(perc>=100){
					  totaldays++;
					  
				  }
				  bar.setProgress(totaldays);
			  }
			  
			  if(totaldays>7) totaldays=0;
		  }
		 }  
	
	public void onBackPressed(){
		this.setResult(1);
		this.finish();
	}
	
	
}
