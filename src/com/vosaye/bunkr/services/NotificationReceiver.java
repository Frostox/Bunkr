package com.vosaye.bunkr.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.sqlcipher.database.SQLiteException;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

public class NotificationReceiver extends IntentService{

	public NotificationReceiver(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public NotificationReceiver() {
		super("com.vosaye.bunkr.services.NotificationReceiver");
		// TODO Auto-generated constructor stub
	}
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
	NotificationManager notificationManager;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	 
	public int onStartCommand(Intent intent, int flags, int startId){
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
		System.out.println("Vosayye is here");
		java.text.DecimalFormat nft = new java.text.DecimalFormat("#00.###");
		
		try {
		//execute response of user on notifications..
			BunKar bunker = (BunKar) this.getApplication();
			ScheduleDatabase sched = bunker.getDatabase(intent.getStringExtra("dbase"));
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(intent.getStringExtra("date")));
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			
			int mins = intent.getIntExtra("mins", -1);
			String structure = sched.meta.selectFromIndex(cal.getTime());
			if(((!structure.equals("labsdecoreblank"))&&cal.getTime().compareTo(sched.start)>=0&&cal.getTime().compareTo(sched.end)<=0)&& !sched.stats.inBlackHole(cal.getTime())){
				while(ValidatorService.HALT){
					try {
						//wait for ui to complete dbase write transactions
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				sched.meta.setAttendance(cal.getTime(), mins, 0);
				Toast.makeText(this, intent.getStringExtra("session")+" at "+nft.format((mins/60>12?(mins/60)-12:mins/60))+" : "+nft.format(mins%60)+" "+((mins/60>12?"PM":"AM"))+" has been marked as bunked!", Toast.LENGTH_SHORT).show();
				
				
				Intent intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.REFRESH");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				sendBroadcast(intentUpdate);
			}
			notificationManager.cancel(intent.getIntExtra("id", -1));
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BunkerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.startService(new Intent(this,MaintenanceManager.class));
		this.startService(new Intent(this,ValidatorService.class));
		return Service.START_NOT_STICKY; 
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		System.out.println("Vosayen : the notif");
		
	}

}
