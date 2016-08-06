package com.vosaye.bunkr.services;
import java.util.Calendar;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;




public class ScheduleManagerService extends IntentService{
	BunKar BunKarapp;
	public ScheduleManagerService() {
		super("Manager");
		BunKarapp = (BunKar) this.getApplication();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if(BunKarapp == null) BunKarapp = (BunKar) this.getApplication();
		Log.i("Checking Service","Inside ManagerService");
		String name = intent.getCharSequenceExtra("scheduleName").toString();
		String op = intent.getCharSequenceExtra("operation").toString();
		if(op.equals("del")){
			for(int i=0; i<BunKarapp.schedulesDbaseList.size(); i++){
				if(BunKarapp.schedulesDbaseList.elementAt(i).getName().equals(name)){
					BunKarapp.schedulesDbaseList.elementAt(i).close();
					BunKarapp.schedulesDbaseList.remove(i);
					if(BunKarapp.settings.exists("select name from schedules where name = '"+name+"'"))
					BunKarapp.settings.schedules.deleteSchedule(name);
					//BunKarapp.alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 1000, BunKarapp.pintent);
					return;
				}
			}
		}
		else{
			ScheduleDatabase newdbase = new ScheduleDatabase(this.getApplicationContext(),name,null,1);
			if(!BunKarapp.schedulesDbaseList.contains(newdbase))
			BunKarapp.schedulesDbaseList.add(newdbase);
			try {
				if(!BunKarapp.settings.exists("select name from schedules where name = '"+name+"'"))
				BunKarapp.settings.schedules.newSchedule(name);
			} catch (BunkerException e) { 
				e.printStackTrace();
			}
			Intent intentx=new Intent();
			intentx.setAction("OpenSchedule");
			this.sendBroadcast(intentx);
		}
		//BunKarapp.alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 1000, BunKarapp.pintent);
		
	}

}
