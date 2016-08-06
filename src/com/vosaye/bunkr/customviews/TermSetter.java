package com.vosaye.bunkr.customviews;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.WeekDayStructure.MyBroadcastReceiver;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.events.ScrollListListener;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.ValidatorService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TermSetter implements OnClickListener{
	ScrollListListener listener;
	LinearLayout mainContainer;
	Activity context;
	LayoutInflater inflater;
	InputMethodManager mngr;
	Calendar start,end;
	AlphaAnimation fadeIn;
	DatePicker datePicker;
	Button startedit, endedit;
	TextView pls;
	public Button submit;
	public AlertDialog dialog_forsetter;
	BunKar bunker;
	boolean isTos = false;
	Toast toast;
	TextView startdate,enddate;
	ScheduleDatabase scheduleDB;
	SimpleDateFormat sdf;
	public TermSetter(Activity context, LayoutInflater inflater){
		this.context = context;
		this.inflater = inflater;
		onCreate();
	}
	
	public void setIsTos(boolean isTos){
		this.isTos = isTos;
	}
	
	public void disableAll(){
		startdate.setEnabled(false);
		enddate.setEnabled(false);
		startedit.setEnabled(false);
		endedit.setEnabled(false);
		submit.setEnabled(false);
		
	}
	
	
	@SuppressLint("ShowToast")
	public void onCreate(){
		mainContainer = (LinearLayout) inflater.inflate(R.layout.term_setter, null);
		startdate = (TextView) mainContainer.findViewById(R.id.term_starttext);
		enddate = (TextView) mainContainer.findViewById(R.id.term_endtext);
		startedit = (Button) mainContainer.findViewById(R.id.term_editstart);
		endedit = (Button) mainContainer.findViewById(R.id.term_editend);
		submit = (Button) mainContainer.findViewById(R.id.term_submit);
		pls = (TextView) mainContainer.findViewById(R.id.term_plstextView1);
		toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
		
		
		
		
		submit.setOnClickListener(this);
		
		sdf = new SimpleDateFormat("dd MMM yyyy",Locale.ENGLISH);
		fadeIn = new AlphaAnimation(0.0f , 1.0f );
		bunker = ((BunKar) context.getApplication());
		
		scheduleDB = (bunker.getDatabase(bunker.name));
		start = Calendar.getInstance();
		try {
			start.setTime(scheduleDB.standards.getStartOfTerm());
			startdate.setText(""+sdf.format(start.getTime()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		end = Calendar.getInstance();
		try {
			end.setTime(scheduleDB.standards.getEndOfTerm());
			enddate.setText(""+sdf.format(end.getTime()));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		startedit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Select the Date");
				View view = inflater.inflate(R.layout.date_picker_dialog,null);
				builder.setView(view);
				datePicker = (DatePicker) view.findViewById(R.id.date_picker_dialog_datePicker1);

				
				
				datePicker.updateDate(start.get(Calendar.YEAR), start.get(Calendar.MONDAY), start.get(Calendar.DAY_OF_MONTH));
				builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}});
				builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Calendar temp = Calendar.getInstance();
						temp.set(Calendar.HOUR_OF_DAY, 0);
						temp.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
						temp.set(Calendar.MONTH, datePicker.getMonth());
						temp.set(Calendar.YEAR, datePicker.getYear());
						long diffDays = (end.getTime().getTime() - temp.getTime().getTime()) / (1000 * 60 * 60 * 24);
						if(temp.compareTo(end)>=0){ 
							//end cannot be before temp
							toast.setText("end cannot be before temp");
							toast.show();
						}
						else if(diffDays<=5){ 
							
							//notify.setText("Atleast 1 week needed");
							toast.setText("Atleast 1 week needed");
							toast.show();
						}
						else{
						start.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
						start.set(Calendar.MONTH, datePicker.getMonth());
						start.set(Calendar.YEAR, datePicker.getYear());
						startdate.setText(sdf.format(start.getTime()));
						}
					}});
				builder.show();
				
				
			}});
		
		endedit.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setTitle("Select the Date");
				View view = inflater.inflate(R.layout.date_picker_dialog,null);
				builder.setView(view);
				datePicker = (DatePicker) view.findViewById(R.id.date_picker_dialog_datePicker1);

				
				
				datePicker.updateDate(end.get(Calendar.YEAR), end.get(Calendar.MONDAY), end.get(Calendar.DAY_OF_MONTH));
				builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}});
				builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Calendar temp = Calendar.getInstance();
						temp.set(Calendar.HOUR_OF_DAY, 0);
						temp.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
						temp.set(Calendar.MONTH, datePicker.getMonth());
						temp.set(Calendar.YEAR, datePicker.getYear());
						long diffDays = (temp.getTime().getTime() - start.getTime().getTime()) / (1000 * 60 * 60 * 24);
						if(start.compareTo(temp)>=0){ 
							//end cannot be before temp
							toast.setText("End cannot be before start");
							toast.show();
							//notify.setText("End cannot be before start");
							
						}
						else if(diffDays<=5){ 
							toast.setText("Atleast 1 week needed");
							toast.show();
							//notify.setText("Atleast 1 week needed");
							
						}
						else{
						end.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
						end.set(Calendar.MONTH, datePicker.getMonth());
						end.set(Calendar.YEAR, datePicker.getYear());
						enddate.setText(sdf.format(end.getTime()));
						}
					}});
				builder.show();
				
				
			}});
		
		mBroadcast = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATESTR");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		context.registerReceiver(mBroadcast, intentFilter);
	}
	
	public void setDisabled(){
		if(termsSetting) return;
		submit.setEnabled(false);
		pls.setVisibility(View.VISIBLE);
	}
	public void setOnDoneListener(ScrollListListener listener){
		this.listener = listener;
	}
	public LinearLayout getView(){
		return mainContainer;
	}
	ProgressBar bar;
	@Override
	public void onClick(View arg0) {
		try {
			if(!(scheduleDB.standards.getStartOfTerm().compareTo(start.getTime())==0&&scheduleDB.standards.getEndOfTerm().compareTo(end.getTime())==0)){
				AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(context);
				
				
				builder_forsetter.setTitle("Info");
				View view = inflater.inflate(R.layout.setting_term_wait_dialog,null);
				builder_forsetter.setView(view);
				TextView title, subtitle;
				title = (TextView) view.findViewById(R.id.atos_textView1);
				subtitle = (TextView) view.findViewById(R.id.atos_textView2);
				bar = (ProgressBar) view.findViewById(R.id.atos_progressBar1);
				title.setText("Please wait a moment");
				subtitle.setText("Please do not close the app");
				bar.setMax(16);
				bar.setProgress(0);
				builder_forsetter.setCancelable(false);
				dialog_forsetter = builder_forsetter.create();
				dialog_forsetter.show();
				termsSetting = true;
				new TermBackgroundSetter(start.getTime(),end.getTime(),scheduleDB,isTos).execute();
				
			}
			else{
				/*
 AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(context);
				builder_forsetter.setTitle("Info");
				builder_forsetter.setCancelable(false);
				builder_forsetter.setTitle("Please wait a moment");
				builder_forsetter.setMessage("Setting up your semester!");
				dialog_forsetter = builder_forsetter.create();
				dialog_forsetter.show();
				listener.onDone(TermSetter.this.getView());
*/
				
				
				listener.onDone(TermSetter.this.getView());
				
			}
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
	}
	class TermBackgroundSetter extends AsyncTask<String,Void,String>{
		ScheduleDatabase db;
		Date start,end;
		boolean isTos = false;
		public TermBackgroundSetter(Date start, Date end, ScheduleDatabase db, boolean isTos){
			super();
			this.db = db;
			this.start = start;
			this.end = end;
			this.isTos = isTos;
		}

		@Override
		protected String doInBackground(String... params) {
			try {
				ValidatorService.HALT = true;
				
				while(ValidatorService.status = ValidatorService.BUSY){
					if(!ValidatorService.STARTED)
						context.startService(new Intent(context,ValidatorService.class));
				}
				MaintenanceManager.HALT = true;
				if(bunker.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
					while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
						//wait for manager to be halted by this thread...
					}
				}
				Cursor c = bunker.settings.rawQuery("select max(id) from labsdecore"+bunker.name.replaceAll(" ", "_")+" where id > 0;");
				if(c.moveToFirst()){
					if(c.getString(0)==null){
					//db.close();
					bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
					//db.open();
					}
				}else {
					//db.close();
					bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
					//db.open();
				}
				c.close();
				db.beginTransaction();
				db.standards.setTerm(start, end, true);
				
				db.commit();
				bunker.tempBackupDbase(bunker.name, Calendar.getInstance().getTime(), "Terms changed");
				
				ValidatorService.HALT = false;
			} catch (SQLiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BunkerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				if(db.getDatabase().inTransaction()) db.rollback();
				ValidatorService.HALT = false;
			}
			return "";
		}
		
		protected void onPostExecute(String param){
			if(!this.isTos)
			dialog_forsetter.dismiss();
			listener.onDone(TermSetter.this.getView());
		}
		
	}
	boolean termsSetting = false;
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
