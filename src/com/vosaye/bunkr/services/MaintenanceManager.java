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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.support.v4.app.NotificationCompat;

public class MaintenanceManager extends IntentService{
	BunKar bunker;
	ScheduleDatabase sdbase;
	AuthDatabase settings;
	AlarmManager alarmManager;
	public int id = 0;
	public Notification notification; 
	float count = 0, countx = 0, maxranges = 0, rangecount = 0;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH); 
	PowerManager mgr;
	WakeLock wakeLock;
	public static boolean STARTED = false, HALT = false, STATUS = true, BUSY = false, FREE = true;
	
	
	public MaintenanceManager(String name) {
		super(name);
		bunker = (BunKar) this.getApplication();
	}
	public MaintenanceManager(){
		super("com.vosaye.bunkr.services.MaintenanceManager");
		bunker = (BunKar) this.getApplication();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("Vosaye: Entered");
		mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();
		
		if(bunker==null)
			bunker = (BunKar) this.getApplication();
		alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		/*
 notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifyBuilder = new NotificationCompat.Builder(this);
		notifyBuilder.setContentTitle("Updating Warehouse")
		    .setContentText("This may take a few minutes !")
		    .setSmallIcon(R.drawable.iconmain);
		notification = notifyBuilder.build();
*/
		settings = bunker.settings;
		STARTED = true;
		count = 0;
		countx = 0;
		maxranges = 0;
		rangecount = 0;
		if(HALT)
			while(HALT){
				STATUS = FREE;
				//System.out.println("Under Halt");
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		STATUS = BUSY;
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0); 
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.add(Calendar.DATE, -1);
		String date = sdf.format(cal.getTime());
		
		Cursor c;
		c = settings.rawQuery("select name, type, lupdated, ROWID from schedules;");
		if(c.moveToFirst())
			do{
				
				
				try {
					Calendar dbasetime = Calendar.getInstance();
					dbasetime.setTime(sdf.parse(c.getString(2)));
					String sname = c.getString(0); 
					sdbase = bunker.getDatabase(sname);

					
					if(sdbase.exists("select name from sqlite_master where name = 'downloaded'"))
					{
						//sdbase.deleteFromTable("pipeline", " where name = 'downloaded'");
						sdbase.dropTable("downloaded");
						try {
							System.out.println("Chaapo creating 1");
							sdbase.stats.createCustomRange(sdbase.standards.getStartOfTerm(), cal.getTime(), false);
							System.out.println("Chaapo creating 2");
							sdbase.stats.createCustomRange(sdbase.standards.getStartOfTerm(), sdbase.standards.getEndOfTerm(), false);
							System.out.println("Chaapo creating 3");
							sdbase.stats.collectAll(sdbase.standards.getStartOfTerm(), sdbase.standards.getEndOfTerm());
						} catch (SQLiteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					
					System.out.println("Vosayye: Selected Database is "+sname);
					
					try {
						if(!sdbase.tableExists(sdbase.stats.getTodayRange())){
							sdbase.stats.createCustomRange(sdbase.start, cal.getTime(), false);
							
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
					
					if(dbasetime.before(cal)||dbasetime.after(cal)){
					//recreate ranges
					try{
					sdbase.stats.deleteCustomRange(sdbase.stats.selectRange(sdbase.start, dbasetime.getTime(), false));
					sdbase.stats.createCustomRange(sdbase.start, cal.getTime(), false);
					System.out.println("Vosaye: Service is creating range");
					}
					catch(Exception e){e.printStackTrace();}
					}
					
					//clean pipeline
					if(sdbase!=null)
					{
						
						
						while(true){
							
							System.out.println("1Vosayye "+sdbase.stats.countWeekFromPipeline()+" "+sdbase.stats.countMonthFromPipeline()+" "+sdbase.stats.countRangesFromPipeline());
							
							while(!sdbase.stats.isPipelineEmpty()){
							if(HALT)
								while(HALT){
									STATUS = FREE;
									//System.out.println("Vosaye: Under Halt");
									//System.out.println("Under Halt");
									try {
										Thread.sleep(1000);
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							STATUS = BUSY;
							if(sdbase.stats.isPipelineEmpty()) {break;}
							
							while(sdbase.stats.peakWeekFromPipeline()!=null){
								String pipelineElement = sdbase.stats.popWeekFromPipeline();
								if(pipelineElement!=null)
								try {
									if(!sdbase.stats.validateWeekly(pipelineElement)) {sdbase.stats.deleteFromPipeline(pipelineElement);sdbase.stats.addToPipeline(pipelineElement);}
									else{
										sdbase.stats.deleteFromPipeline(pipelineElement);
										

										if(sname.equals(bunker.name)&&ValidatorService.FOCUSED){
											
											countx = sdbase.stats.countElementsFromPipeline();
											if(countx>count) count = countx;
											if(count!=0)
												ValidatorService.perc = ((count - countx)/count)*90;
											else ValidatorService.perc = 90;
											/*
 Intent intentUpdate = new Intent();
											intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
											intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
											intentUpdate.putExtra("perc", (int)ValidatorService.perc);
											sendBroadcast(intentUpdate);
											notifyBuilder.setProgress(100,(int) ValidatorService.perc, false);
											notifyBuilder.setContentText(""+(int) ValidatorService.perc+" %");
											notification = notifyBuilder.build();
											notification.flags = Notification.FLAG_ONGOING_EVENT;
											notifyManager.notify(id, notification);
*/
											
										}
										//System.out.println(perc+"% Completed");
									}
									System.out.println("2Vosayye "+sdbase.stats.countWeekFromPipeline()+" "+sdbase.stats.countMonthFromPipeline()+" "+sdbase.stats.countRangesFromPipeline());
									
									if((!ValidatorService.FREEFLOW)&&sname.equals(bunker.name)&&(ValidatorService.FOCUSED))
									Thread.sleep(1000);
								} catch (ParseException e) {
									
									e.printStackTrace();
								} catch (BunkerException e) {
									
									e.printStackTrace();
								} catch (InterruptedException e) {
									
									e.printStackTrace();
								}
								
								if(HALT)
									while(HALT){
										STATUS = FREE;


										//System.out.println("Vosaye: Under Halt");
										//System.out.println("Under Halt");
										try {
											Thread.sleep(1000);
										} catch (Exception e) {
											
											e.printStackTrace();
										}
									}
								STATUS = BUSY;
								
								
							}
							
							String name = sdbase.stats.popMonthFromPipeline();
							if(name!=null)
							try {
								if(name!=null&&(sdbase.stats.peakWeekFromPipeline()==null)){
									if(!sdbase.stats.validateMonthly(name)) {sdbase.stats.deleteFromPipeline(name);sdbase.stats.addToPipeline(name);}
									else{
										sdbase.stats.deleteFromPipeline(name);

										System.out.println("Vosaye: Taking "+name+" from "+sname);
										if(sname.equals(bunker.name)&&ValidatorService.FOCUSED){
											countx = sdbase.stats.countElementsFromPipeline();
											if(countx>count) count = countx;
											if(count!=0)
												ValidatorService.perc = ((count - countx)/count)*90;
											else ValidatorService.perc = 90;
											/*
 Intent intentUpdate = new Intent();
											intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
											intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
											intentUpdate.putExtra("perc", (int)ValidatorService.perc);
											sendBroadcast(intentUpdate);
											notifyBuilder.setProgress(100,(int) ValidatorService.perc, false);
											notifyBuilder.setContentText(""+(int) ValidatorService.perc+" %");
											notification = notifyBuilder.build();
											notification.flags = Notification.FLAG_ONGOING_EVENT;
											notifyManager.notify(id, notification);
*/
										}
									
									}
									
									
									

									if((!ValidatorService.FREEFLOW)&&sname.equals(bunker.name)&&(ValidatorService.FOCUSED))
									Thread.sleep(1000);
								} else{sdbase.stats.addToPipeline(name); }
								
								System.out.println("3Vosayye "+sdbase.stats.countWeekFromPipeline()+" "+sdbase.stats.countMonthFromPipeline()+" "+sdbase.stats.countRangesFromPipeline());
								
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
									STATUS = FREE;
									//System.out.println("Vosaye: Under Halt");
									
									//System.out.println("Under Halt");
									try {
										Thread.sleep(1000);
									} catch (Exception e) {
										
										e.printStackTrace();
									}
								}
								continue;
							}
							STATUS = BUSY;
							
							
							if(sdbase.stats.pipelineHasRanges()){
								try {
									
									
									rangecount = sdbase.stats.countRangesFromPipeline();
									String temp = sdbase.stats.popRangeFromPipeline();
									sdbase.stats.validateRange(temp);
									System.out.println("Vosaye: Taking "+temp+" from "+sname);
									
									
									if(maxranges<rangecount) maxranges = rangecount;
									if(maxranges!=0){ValidatorService.perc = 90+(((maxranges - rangecount)/maxranges)*10);}
									else {ValidatorService.perc = 90;}
									
									if(sname.equals(bunker.name)&&ValidatorService.FOCUSED){
										/*
 Intent intentUpdate = new Intent();
										intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
										intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
										intentUpdate.putExtra("perc", (int)ValidatorService.perc);
										sendBroadcast(intentUpdate);
										notifyBuilder.setProgress(100,(int) ValidatorService.perc, false);
										notifyBuilder.setContentText(""+(int)ValidatorService.perc+" %");
										notification = notifyBuilder.build();
										notification.flags = Notification.FLAG_ONGOING_EVENT;
										notifyManager.notify(id, notification);
*/
									}
									

									if((!ValidatorService.FREEFLOW)&&sname.equals(bunker.name)&&(ValidatorService.FOCUSED))
									Thread.sleep(1000);
									
									System.out.println("4Vosayye "+sdbase.stats.countWeekFromPipeline()+" "+sdbase.stats.countMonthFromPipeline()+" "+sdbase.stats.countRangesFromPipeline());
									
									
								} catch (InterruptedException e) {
									
									e.printStackTrace();
								}}
							else
							break;

							
						}

						
						try{
							Cursor cc = settings.rawQuery("select name, type, lupdated, ROWID from schedules where name = '"+sdbase.getName()+"';");
							if(cc.moveToFirst()){
							Calendar today = (Calendar) cal.clone();
							today.add(Calendar.DATE, 1);
							System.out.println("Vosayye : "+sdf.format(today.getTime())); 
							String structure = sdbase.meta.selectFromIndex(today.getTime());
							if(((!structure.equals("labsdecoreblank"))&&today.getTime().compareTo(sdbase.start)>=0&&today.getTime().compareTo(sdbase.end)<=0)&& !sdbase.stats.inBlackHole(today.getTime())&&(sdbase.tableExists(sdbase.meta.selectRecord(today.getTime())))){
								today = Calendar.getInstance();
								today.set(Calendar.MILLISECOND, 0);
								today.set(Calendar.SECOND, 0);
								int hrs = today.get(Calendar.HOUR_OF_DAY);
								int mins = today.get(Calendar.MINUTE);
								mins = hrs*60 + mins;
								Cursor cx = sdbase.rawQuery("select mins from "+structure+" where mins > "+mins+" order by mins asc;");
								if(cx.moveToFirst()){
									
									System.out.println("Vosayye mins selected : "+ cx.getInt(0));
									int idNotif = cc.getInt(3)+1;
									Intent i = new Intent(this,NotificationService.class);
									i.putExtra("dbase", sdbase.getName());
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
							cc.close();
							}
							catch(Exception e){e.printStackTrace();}
					
					
							
						}
						
						
						
					Calendar cals = Calendar.getInstance(); 
					int mins = cals.get(Calendar.HOUR_OF_DAY) * 60;
					float x = 0;
					mins = mins + cals.get(Calendar.MINUTE);
					cals.set(Calendar.MILLISECOND, 0);
					cals.set(Calendar.SECOND, 0);
					cals.set(Calendar.HOUR_OF_DAY, 0);
					cals.set(Calendar.MINUTE, 0);
					sdbase.createTable("todaytempmngr", sdbase.statDef);
					sdbase.deleteFromTable("todaytempmngr", "");
					String query = "(";
					if((!sdbase.stats.inBlackHole(cals.getTime()))&&(!sdbase.meta.selectFromIndex(cals.getTime()).equals("labsdecoreblank"))&&((cals.getTime().compareTo(sdbase.start)>=0&&cals.getTime().compareTo(sdbase.end)<=0)))
					query = "(select s.IDrel, r.attendance from "+sdbase.meta.selectFromIndex(cals.getTime())+" s, "+sdbase.meta.selectRecord(cals.getTime())+" r where s.mins = r.mins and r.attendance != 3 and (s.mins) <= "+mins+") ";
					
					if(!query.equals("(")){
						sdbase.execQuery("insert into todaytempmngr (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel) group by IDrel) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel) group by IDrel) b on a.IDrel = b.IDrel where b.IDrel is null;");
						if(sdbase.isEmpty("todaytempmngr")) System.out.println("Vosaye :: Empty"); else System.out.println("Vosaye :: Nope");
						sdbase.execQuery("update todaytempmngr set attendance = 0 where attendance is null");
					}
					if(sdbase.tableExists(sdbase.stats.getTodayRange())){
					if(sdbase.isEmpty(sdbase.stats.getTodayRange())&&sdbase.isEmpty("todaytempmngr")){
						//its 0
						//Here.percentNow = 0f;
						x = 0;
					}
					
					else{
						query = "select sum(attendance), sum(total) from (select * from todaytempmngr UNION ALL select * from "+sdbase.stats.getTodayRange()+");";
						Cursor cx = sdbase.rawQuery(query);
						if(cx.moveToFirst())
							x = (cx.getFloat(0)/cx.getFloat(1))*100;
						cx.close();
					}
					}
					else if(!sdbase.isEmpty("todaytempmngr")){
						query = "select sum(attendance), sum(total) from (select * from todaytempmngr);";
						Cursor cx = sdbase.rawQuery(query);
						if(cx.moveToFirst())
							x = (cx.getFloat(0)/cx.getFloat(1))*100;
						cx.close();
					}
					settings.execQuery("update schedules set currentPerc = "+x+" where name = '"+sdbase.getName()+"';");
					
					
					

					
				} catch (ParseException e) { 
					
					e.printStackTrace();
				} catch (SQLiteException e) {
					e.printStackTrace();
				} catch (BunkerException e) {
					e.printStackTrace();
				}
				
				
				
				
			}while(c.moveToNext());
		c.close();
		
		Intent intentUpdate = new Intent();
		intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
		intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
		intentUpdate.putExtra("perc", -1);
		sendBroadcast(intentUpdate); 
		
		
		
		
		
		/*
 Intent intentUpdate = new Intent();
		intentUpdate.setAction("com.vosaye.bunkr.UPDATE");
		intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
		intentUpdate.putExtra("perc", -1);
		sendBroadcast(intentUpdate); 
		notifyBuilder.setProgress(100,(int) ValidatorService.perc, false);
		notifyBuilder.setContentText(""+(int) ValidatorService.perc+" %");
		notification = notifyBuilder.build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notifyManager.notify(id, notification);
		notifyManager.cancel(id);
*/
		STARTED = false;
	}
	
	public void onDestroy(){
		STARTED = false;
		wakeLock.release();
		System.out.println("Vosaye: Exiting Manager");
		
	}
	
	
	
	
}
