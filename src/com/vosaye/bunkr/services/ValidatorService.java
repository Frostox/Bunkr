package com.vosaye.bunkr.services;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.BaseActivity;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

public class ValidatorService extends IntentService{ 
	BunKar BunKarapp;
	public static boolean BUSY = false, FREE = true, FOCUSED = false;
	public static boolean HALT = false;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
	public static boolean STARTED = false; 
	public static boolean FREEFLOW = false;
	public static boolean status = BUSY;
	public static int id = 0;
	float count = 0, countx=0, maxranges=0, rangecount = 0;
	public static float perc = 0;
	public NotificationManager notifyManager;
	public NotificationCompat.Builder notifyBuilder;
	public Notification notification;
	AlarmManager alarmManager;
	AuthDatabase settings; 
	PowerManager mgr;
	WakeLock wakeLock;
	public ValidatorService(String name) {
		super(name);
		BunKarapp = (BunKar) this.getApplication();
	}
	public ValidatorService(){
		super("com.vosaye.bunkr.services.ValidatorService");
		BunKarapp = (BunKar) this.getApplication();
	}
 	@Override
	protected void onHandleIntent(Intent intent) {
			ValidatorService.STARTED = true;
			System.out.println("Vosaye: Validator started");
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
			wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
			wakeLock.acquire();
			 
			
			notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			notifyBuilder = new NotificationCompat.Builder(this);
			notifyBuilder.setContentTitle("Updating Warehouse")
			    .setContentText("This may take a few minutes !")
			    .setSmallIcon(R.drawable.noticoninfo);
			notification = notifyBuilder.build();
			

			count = 0;
			countx = 0;
			maxranges = 0;
			rangecount = 0;
			BunKarapp = (BunKar) this.getApplication();
			MaintenanceManager.HALT = true;
			if(BunKarapp.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
				while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
					//wait for manager to be halted by this ValidatorService...
					
				}
			}
			if(HALT)
				while(HALT){
					ValidatorService.status = ValidatorService.FREE;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			
			
			System.out.println("Vosaye: Validator continued");
			ScheduleDatabase dbase = BunKarapp.getDatabase(BunKarapp.name);
			
			
			settings = BunKarapp.settings;
			if(dbase!=null)
			{
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.MILLISECOND, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.add(Calendar.DATE, -1);
				String date = sdf.format(cal.getTime());
				
				
				if(dbase.exists("select name from sqlite_master where name = 'downloaded'"))
				{
					//dbase.deleteFromTable("pipeline", " where name = 'downloaded'");
					dbase.dropTable("downloaded");
					try {
						System.out.println("Chaapo creating 1");
						dbase.stats.createCustomRange(dbase.standards.getStartOfTerm(), cal.getTime(), false);
						System.out.println("Chaapo creating 2");
						dbase.stats.createCustomRange(dbase.standards.getStartOfTerm(), dbase.standards.getEndOfTerm(), false);
						System.out.println("Chaapo creating 3");
						
						dbase.stats.collectAll(dbase.start, dbase.end);
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
				}
				
				try {
					if(!dbase.tableExists(dbase.stats.getTodayRange())){
						dbase.stats.createCustomRange(dbase.start, cal.getTime(), false);
					}
				} catch (SQLiteException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ParseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (BunkerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				Cursor c = settings.rawQuery("select name, type, lupdated, ROWID from schedules where name = '"+BunKarapp.name+"';");
				if(c.moveToFirst())
						try {
							Calendar dbasetime = Calendar.getInstance();
							dbasetime.setTime(sdf.parse(c.getString(2)));
							

							
							if(dbasetime.before(cal)||dbasetime.after(cal)){
							//recreate ranges
							try{
							dbase.stats.deleteCustomRange(dbase.stats.selectRange(dbase.start, dbasetime.getTime(), false));
							dbase.stats.createCustomRange(dbase.start, cal.getTime(), false);
							System.out.println("Vosaye: Service is creating range");
							}
							catch(Exception e){e.printStackTrace();}
							}
							
							
							}
						catch(Exception e){e.printStackTrace();}					
				c.close();
				
				
				
				
				
				
				
					toploop:	while(true){
					
					
					while(!dbase.stats.isPipelineEmpty()){
					if(HALT)
						while(HALT){
							ValidatorService.status = ValidatorService.FREE;
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							 
						}
					ValidatorService.status = ValidatorService.BUSY; 
					if(dbase.stats.isPipelineEmpty()) {break;}
					
					while(dbase.stats.peakWeekFromPipeline()!=null){
						String pipelineElement = dbase.stats.popWeekFromPipeline();
						if(pipelineElement!=null)
						try { 
							if(!dbase.stats.validateWeekly(pipelineElement)) {dbase.stats.deleteFromPipeline(pipelineElement);dbase.stats.addToPipeline(pipelineElement);}
							else{
								dbase.stats.deleteFromPipeline(pipelineElement);
								System.out.println("jVoss "+pipelineElement);
								countx = dbase.stats.countElementsFromPipeline(); 
								if(countx>count) count = countx;
								if(count!=0)
								perc = ((count - countx)/count)*90; 
								else perc = 90;
								Intent intentUpdate = new Intent();
								intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
								intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
								intentUpdate.putExtra("perc", (int)perc);
								sendBroadcast(intentUpdate);
								
								Intent resultIntent = new Intent();
								PendingIntent resultPendingIntent =
								    PendingIntent.getActivity(
								    this,
								    0,
								    resultIntent,
								    PendingIntent.FLAG_UPDATE_CURRENT
								);
								
								notifyBuilder.setProgress(100,(int) perc, false);
								notifyBuilder.setContentText(""+(int)perc+" %");
								notifyBuilder.setContentIntent(resultPendingIntent);
								notification = notifyBuilder.build();
								notification.flags = Notification.FLAG_ONGOING_EVENT;
								notifyManager.notify(id, notification);
								
								
								//System.out.println(perc+"% Completed");
							}
							
							ScheduleDatabase newbase = BunKarapp.getDatabase(BunKarapp.name);
							if(newbase==null) return;
							if(!FOCUSED){ 
								this.startService(new Intent(this,MaintenanceManager.class));
								//notifyManager.cancel(id);
								break toploop;
							}
							if(!newbase.getName().equals(dbase.getName())){this.startService(new Intent(this,MaintenanceManager.class));}
							dbase = newbase;
							if(!FREEFLOW)
							Thread.sleep(2000);
						} catch (ParseException e) {
							
							e.printStackTrace();
						} catch (BunkerException e) {
							
							e.printStackTrace();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
						
						if(HALT)
							while(HALT){
								ValidatorService.status = ValidatorService.FREE;

								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
						ValidatorService.status = ValidatorService.BUSY;
						
						
					}
					
					String name = dbase.stats.popMonthFromPipeline();
					if(name!=null)
					try {
						if(name!=null&&(dbase.stats.peakWeekFromPipeline()==null)){
							if(!dbase.stats.validateMonthly(name)) {dbase.stats.deleteFromPipeline(name);dbase.stats.addToPipeline(name);}
							else{dbase.stats.deleteFromPipeline(name);
								
							System.out.println("jVoss "+name);
							countx = dbase.stats.countElementsFromPipeline();
							if(countx>count) count = countx;
							if(count!=0)
							perc = ((count - countx)/count)*90;
							else perc = 90;
							Intent intentUpdate = new Intent();
							intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
							intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
							intentUpdate.putExtra("perc", (int)perc);
							sendBroadcast(intentUpdate);
							
							Intent resultIntent = new Intent();
							PendingIntent resultPendingIntent =
							    PendingIntent.getActivity(
							    this,
							    0,
							    resultIntent,
							    PendingIntent.FLAG_UPDATE_CURRENT
							);
							
							notifyBuilder.setProgress(100,(int) perc, false);
							notifyBuilder.setContentText(""+(int)perc+" %");
							notifyBuilder.setContentIntent(resultPendingIntent);
							notification = notifyBuilder.build();
							notification.flags = Notification.FLAG_ONGOING_EVENT;
							notifyManager.notify(id, notification);
							}
							
							
							ScheduleDatabase newbase = BunKarapp.getDatabase(BunKarapp.name);
							if(newbase==null) return;
							if(!FOCUSED){ 
								this.startService(new Intent(this,MaintenanceManager.class));
								//notifyManager.cancel(id);
								break toploop;
							}
							if(!newbase.getName().equals(dbase.getName())){this.startService(new Intent(this,MaintenanceManager.class));}
							dbase = newbase;
							if(!FREEFLOW)
							Thread.sleep(2000);
						} else{dbase.stats.addToPipeline(name); }
					} catch (SQLiteException e) {
						
						e.printStackTrace();
					} catch (BunkerException e) {
						
						e.printStackTrace();
					} catch (ParseException e) {
						
						e.printStackTrace();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					}
					
 					if(HALT){
						while(HALT){
							ValidatorService.status = ValidatorService.FREE;

							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						continue;
					}
					ValidatorService.status = ValidatorService.BUSY;
					ScheduleDatabase newbase = BunKarapp.getDatabase(BunKarapp.name);
					if(newbase==null) return;
					if(!FOCUSED){ 
						this.startService(new Intent(this,MaintenanceManager.class));
						//notifyManager.cancel(id);
						break toploop;
					}
					if(!newbase.getName().equals(dbase.getName())){this.startService(new Intent(this,MaintenanceManager.class));}
					dbase = newbase;
					if(dbase.stats.pipelineHasRanges()){
						try {
							rangecount = dbase.stats.countRangesFromPipeline();
							System.out.println("jVoss ranges left "+rangecount);
							dbase.stats.validateRange(dbase.stats.popRangeFromPipeline());
							if(maxranges<rangecount) maxranges = rangecount;
							if(maxranges!=0){perc = 90+(((maxranges - rangecount)/maxranges)*10);}
							else {perc = 90;}

							Intent intentUpdate = new Intent();
							intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
							intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
							intentUpdate.putExtra("perc", (int)perc);
							sendBroadcast(intentUpdate);
							
							
							Intent resultIntent = new Intent();
							PendingIntent resultPendingIntent =
							    PendingIntent.getActivity(
							    this,
							    0,
							    resultIntent,
							    PendingIntent.FLAG_UPDATE_CURRENT
							);
							
							
							notifyBuilder.setProgress(100,(int) perc, false);
							notifyBuilder.setContentText(""+(int)perc+" %");
							notifyBuilder.setContentIntent(resultPendingIntent);
							notification = notifyBuilder.build();
							notification.flags = Notification.FLAG_ONGOING_EVENT;
							notifyManager.notify(id, notification);
							if(!FREEFLOW)
							Thread.sleep(2000);
						} catch (ParseException e) {
							
							e.printStackTrace();
						} catch (BunkerException e) {
							
							e.printStackTrace();
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}}
					else
					break;

					
				}

				
				
				
				
				//notif update 
				cal = Calendar.getInstance();
				cal.set(Calendar.MILLISECOND, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.add(Calendar.DATE, -1);
				
				
				try{
				c = settings.rawQuery("select name, type, lupdated, ROWID from schedules where name = '"+BunKarapp.name+"';");
				if(c.moveToFirst()){
				Calendar today = (Calendar) cal.clone();
				today.add(Calendar.DATE, 1);
				System.out.println("Vosayye : "+sdf.format(today.getTime())); 
				String structure = dbase.meta.selectFromIndex(today.getTime());
				if(((!structure.equals("labsdecoreblank"))&&today.getTime().compareTo(dbase.start)>=0&&today.getTime().compareTo(dbase.end)<=0)&& !dbase.stats.inBlackHole(today.getTime())&&(dbase.tableExists(dbase.meta.selectRecord(today.getTime())))){
					today = Calendar.getInstance();
					today.set(Calendar.MILLISECOND, 0);
					today.set(Calendar.SECOND, 0);
					int hrs = today.get(Calendar.HOUR_OF_DAY);
					int mins = today.get(Calendar.MINUTE);
					mins = hrs*60 + mins;
					Cursor cx = dbase.rawQuery("select mins from "+structure+" where mins > "+mins+" order by mins asc;");
					if(cx.moveToFirst()){

						System.out.println("Vosayye mins selected : "+ cx.getInt(0));
						int idNotif = c.getInt(3)+1;
						Intent i = new Intent(this,NotificationService.class);
						i.putExtra("dbase", BunKarapp.name);
						i.putExtra("id", idNotif);
						today.set(Calendar.HOUR_OF_DAY, cx.getInt(0)/60);
						today.set(Calendar.MINUTE, cx.getInt(0)%60);
						i.putExtra("date", sdf.format(today.getTime()));
						i.putExtra("mins", cx.getInt(0));
						PendingIntent pending = PendingIntent.getService(this, idNotif, i,
					            PendingIntent.FLAG_CANCEL_CURRENT);
						alarmManager.set(AlarmManager.RTC_WAKEUP, today.getTimeInMillis(), pending);
						System.out.println("Alarm is set!");
					}
					cx.close();
				}
				
				}
				c.close();
				}
				catch(Exception e){e.printStackTrace();}
				//notif updated
				
				try{
				settings.execQuery("update schedules set lupdated = '"+sdf.format(cal.getTime())+"' where name = '"+BunKarapp.name+"';");
				}
				catch(Exception e){e.printStackTrace();}
				
				
				
			}
			
			
			try{
				float percentage = 0f;
			  	Calendar cal = Calendar.getInstance(); 
				int mins = cal.get(Calendar.HOUR_OF_DAY) * 60;
				mins = mins + cal.get(Calendar.MINUTE);
				cal.set(Calendar.MILLISECOND, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				dbase.createTable("todaytempvali", dbase.statDef);
				dbase.deleteFromTable("todaytempvali", "");
				String query = "(";
				if((!dbase.stats.inBlackHole(cal.getTime()))&&(!dbase.meta.selectFromIndex(cal.getTime()).equals("labsdecoreblank"))&&((cal.getTime().compareTo(dbase.start)>=0&&cal.getTime().compareTo(dbase.end)<=0)))
				query = "(select s.IDrel, r.attendance from "+dbase.meta.selectFromIndex(cal.getTime())+" s, "+dbase.meta.selectRecord(cal.getTime())+" r where s.mins = r.mins and r.attendance != 3 and (s.mins) <= "+mins+") ";
				
				if(!query.equals("(")){
					dbase.execQuery("insert into todaytempvali (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel) group by IDrel) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel) group by IDrel) b on a.IDrel = b.IDrel where b.IDrel is null;");
					if(dbase.isEmpty("todaytempvali")) System.out.println("Vosaye :: Empty"); else System.out.println("Vosaye :: Nope");
					dbase.execQuery("update todaytempvali set attendance = 0 where attendance is null");
				}
				if(dbase.tableExists(dbase.stats.getTodayRange())){
				if(dbase.isEmpty(dbase.stats.getTodayRange())&&dbase.isEmpty("todaytempvali")){
					//its 0
					//Here.percentNow = 0f;
					percentage = 0;
				}
				
				else{
					query = "select sum(attendance), sum(total) from (select * from todaytempvali UNION ALL select * from "+dbase.stats.getTodayRange()+");";
					Cursor c = dbase.rawQuery(query);
					if(c.moveToFirst())
						percentage = (c.getFloat(0)/c.getFloat(1))*100;
					c.close();
				}
				}
				else if(!dbase.isEmpty("todaytempvali")){
					query = "select sum(attendance), sum(total) from (select * from todaytempvali);";
					Cursor c = dbase.rawQuery(query);
					if(c.moveToFirst())
						percentage = (c.getFloat(0)/c.getFloat(1))*100;
					c.close();
				}
				
				
				settings.execQuery("update schedules set currentPerc = "+percentage+" where name = '"+dbase.getName()+"';");
				
			  	}
			  	
			  	
			  	catch(Exception e){e.printStackTrace();}
			
			
			
			
			Intent intentUpdate = new Intent();
			intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
			intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
			intentUpdate.putExtra("perc", -1);
			sendBroadcast(intentUpdate);
			
			Intent resultIntent = new Intent(this, BaseActivity.class);
			PendingIntent resultPendingIntent =
			    PendingIntent.getActivity(
			    this,
			    0,
			    resultIntent,
			    PendingIntent.FLAG_UPDATE_CURRENT
			);
			
			
			
			  	
			
			
			notifyBuilder.setProgress(100,(int) perc, false);
			notifyBuilder.setContentText(""+(int)perc+" %");
			notifyBuilder.setContentIntent(resultPendingIntent);
			notification = notifyBuilder.build();
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			Intent notificationIntent = new Intent();
		    notification.contentIntent = PendingIntent.getActivity(this, id, notificationIntent, 0);
			notifyManager.notify(id, notification);
			notifyManager.cancel(id);
			
			
			
			
			
			
	}
 	public void onDestroy(){
 		super.onDestroy();
 		wakeLock.release();
		System.out.println("Vosaye: Validator exiting");
 		ValidatorService.STARTED = false;
 		MaintenanceManager.HALT = false; //let manager go ahead!
 		
 	}

}
