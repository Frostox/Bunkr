package com.vosaye.bunkr.app;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.R.layout;
import com.vosaye.bunkr.app.StructureActivity.BackgroundStructureEditter;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.customviews.CustomListView;
import com.vosaye.bunkr.customviews.StructureEditter;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.fragments.Stats;
import com.vosaye.bunkr.fragments.StructureEditterFragment;
import com.vosaye.bunkr.fragments.Stats.MyBroadcastReceiver;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.ValidatorService;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

public class WeekDayStructure extends SherlockFragmentActivity {
	Menu menu;
	BunKar bunker;
	ScheduleDatabase sched;
	Date date;
	Calendar datec;
	int count;
	String pseudoStructureName;
	StructureEditter se;
	private RelativeLayout mParentLayout = null;
	SimpleDateFormat sdf;
    SimpleDateFormat sdf2;
	
    public AlertDialog dialog_forsetter;
	
	
	@Override
	public void onBackPressed() {
		if(se!=null)
	    if(!se.getSaved()){
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you wanna exit without saving structure");
			builder.setTitle("Warning");
			builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}});
			builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

				@SuppressWarnings("deprecation")
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
					WeekDayStructure.this.finish();
					
				
				}});
			//authenticator.schedules.deleteSchedule(textView.getText().toString());
			//c.requery();
			//CAdapter.notifyDataSetChanged();
			builder.create();
			builder.show();
	    }
	    else
	    	this.finish();
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_week_day_structure);
		ActionBar action = this.getSupportActionBar();
        action.show();
        bunker = (BunKar) this.getApplication();
		sched = bunker.getDatabase(bunker.name);
		sdf = bunker.sdf4;
		sdf2 = bunker.sdf;
		BunKar.count++;
		count = Integer.parseInt(this.getIntent().getStringExtra("count"));
		mParentLayout = (RelativeLayout) this.findViewById(R.id.activity_week_day_structure_layout);
		try { 
			pseudoStructureName = sched.meta.createPseudoStructure();
			date = sdf.parse(this.getIntent().getStringExtra("date"));
			datec = Calendar.getInstance();
			datec.setTime(date);
			if(!sched.meta.selectFromIndex(date).equals("labsdecoreblank")){
				//sched.meta.copyStructure(sched.meta.selectFromIndex(date), pseudoStructureName);
				sched.meta.copyToPseudoStructure(pseudoStructureName, sched.meta.selectFromIndex(date), date);
			}
			
			
			getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
			action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+ "Edit all "+bunker.days[datec.get(Calendar.DAY_OF_WEEK)-1]+"s" + "</font>"));
			action.setSubtitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+bunker.name + "</font>"));
			action.setDisplayHomeAsUpEnabled(true);
			
			se = new StructureEditter(this, pseudoStructureName);
			se.onCreate();
			mParentLayout.addView(se.getView());
			
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BunkerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATESTR");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		registerReceiver(mBroadcast, intentFilter);
		
	}

	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.week_day_structure, (com.actionbarsherlock.view.Menu) menu);
	    this.menu = menu;
	    return super.onCreateOptionsMenu(menu);
	 }
	 ProgressBar bar;
	 public boolean onOptionsItemSelected(MenuItem item) {
		 
		 int itemId = item.getItemId();
		if (itemId == R.id.week_done) {
			AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(this);
			builder_forsetter.setTitle("Updating Table");
			View view = this.getLayoutInflater().inflate(R.layout.setting_term_wait_dialog, null);
			TextView title, subtitle;
			title = (TextView) view.findViewById(R.id.atos_textView1);
			subtitle = (TextView) view.findViewById(R.id.atos_textView2);
			bar = (ProgressBar) view.findViewById(R.id.atos_progressBar1);
			bar.setMax(100);
			bar.setProgress(0);
			title.setText("Please wait a moment");
			subtitle.setText("Please do not close the app");
			builder_forsetter.setView(view);
			builder_forsetter.setCancelable(false);
			dialog_forsetter = builder_forsetter.create();
			dialog_forsetter.show();
			se.closeList();
			new BackgroundStructureEditter(this,this.pseudoStructureName,this.date).execute();
			return true;
		} else if (itemId == R.id.week_clr) {
			sched.deleteFromTable(this.pseudoStructureName, "");
			se.notifyData();
			se.setSaved(false);
			return true;
		} else if(itemId == android.R.id.home){
			setResult(1);
			finish();
			return true;
			
		} else {
			return super.onCreateOptionsMenu(menu);
		}
	 }
	 class BackgroundStructureEditter extends AsyncTask<String,Void,String>{
	    	
	    	Date date;
	    	String sname;
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    	public BackgroundStructureEditter(WeekDayStructure edit, String sname, Date date){
	    		this.sname = sname;
	    		this.date = date;
	    	}
	    	
			@Override
			protected String doInBackground(String... params) {
	        	Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				MaintenanceManager.HALT = true;
				if(bunker.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
					while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
						//wait for manager to be halted by this thread...
					}
				}
				ValidatorService.HALT = true;
				
				while(ValidatorService.status == ValidatorService.BUSY){
					if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
						startService(new Intent(WeekDayStructure.this,ValidatorService.class));
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
					//Toast.makeText(EditAllStructures.this, ""+sdf.format(cal.getTime()), Toast.LENGTH_LONG).show();
					try {
						if(!sched.isEmpty(pseudoStructureName)){
							String name = sched.meta.insertIntoIndex(cal.getTime(), sched.meta.getUUID(""), count, true);
							if(pseudoStructureName!=null){
								sched.meta.copyStructure(pseudoStructureName, name);
								
								sched.meta.copyRecordBack(pseudoStructureName, cal.getTime(), count);
								Calendar cal2 = cal;
								cal2.add(Calendar.DATE, count*7);
								sched.stats.collectAll(cal.getTime(), cal2.getTime());
							}
						}
						else{
							WeekDayStructure.this.sched.meta.insertBlankIntoIndex(cal.getTime(), count);
						}

						sched.commit();
						//sched.close();
						bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Editted all "+bunker.days[datec.get(Calendar.DAY_OF_WEEK)-1]+"s");
						//sched.open();
						ValidatorService.HALT = false;
						//sched.dropTable(edit.pseudoStructureName);
						
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally{
						//if(!sched.getDatabase().isOpen()) sched.open();
						if(sched.getDatabase().inTransaction()) sched.rollback();
						ValidatorService.HALT = false;
					}
	        	
	        	return null;
			}
			protected void onPostExecute(String param){
				dialog_forsetter.dismiss();
				se.updateList();
				
	    		
	    		se.setSaved(true);
				
	    		finish();
				//startActivity(new Intent(WeekDayStructure.this, Here.class));
				
			}
	    }

	 public void onPause(){
			super.onPause();
			//se.closeList();
			ValidatorService.FREEFLOW = true;
	}
	public void onResume(){
			super.onResume();
			//se.updateList();
			if(sched==null){
				bunker.getDatabase(bunker.name);
				se.sched = bunker.getDatabase(bunker.name);
				if(sched==null){
					sched = bunker.getDatabase(bunker.name);
					se.sched = bunker.getDatabase(bunker.name);
				}
			}
			ValidatorService.FREEFLOW = false;
	    	ValidatorService.FOCUSED = true;

	    	//overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
	}
	public void onDestroy(){
		super.onDestroy();
		if(dialog_forsetter!=null)
		this.dialog_forsetter.dismiss();
		if(sched.getDatabase().inTransaction())
			sched.rollback();
		ValidatorService.HALT = false;
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
		this.unregisterReceiver(mBroadcast);
	}

	MyBroadcastReceiver mBroadcast;
	public class MyBroadcastReceiver extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  int perc = intent.getIntExtra("perc", 0);
			  if(bar!=null)
				  bar.setProgress(perc);
			  
		  }
		 }  
		


}
