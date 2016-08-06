package com.vosaye.bunkr.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;


























import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.Here;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;

import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;

/**
 * @author Roger Cores
 * Serivice to serve notifications on a different thread than the Main thread.
 */
public class NotificationService extends android.app.IntentService {
	BunKar BunKarapp;
	ScheduleDatabase sched;
	AuthDatabase settings;
	NotificationManager notificationManager;
	AlarmManager alarmManager;
	Notification notif;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
	public NotificationService() {
		super("NotificationService");
		BunKarapp = (BunKar) this.getApplication();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		
		
		System.out.println("Vosayye : i am here");
		if(BunKarapp==null) BunKarapp = (BunKar) this.getApplication();
		settings = BunKarapp.settings;
		System.out.println("Vosayye : in notifs");
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); 
		alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		String message = "";
		java.text.DecimalFormat nft = new java.text.DecimalFormat("#00.###");
		int id = intent.getIntExtra("id", -1);
		String name = intent.getStringExtra("dbase");
		sched = BunKarapp.getDatabase(name);
		if(sched==null||id==-1) return START_NOT_STICKY;
		
		Calendar cal = Calendar.getInstance();
		
		try {
			cal.setTime(sdf.parse(intent.getStringExtra("date")));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		int mins = intent.getIntExtra("mins", -1);
		
		
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		
		
		
		
		
		
		
		try {
			
			
			Cursor xc = settings.rawQuery("select type from schedules where name = '"+name+"';");
			if(xc.moveToFirst())
				if(xc.getString(0).equals("true"))
				{
			
			
		
			//verify info if info is correct
			//if yes, send notification
		
			
			String structure = sched.meta.selectFromIndex(cal.getTime());
			if(((!structure.equals("labsdecoreblank"))&&cal.getTime().compareTo(sched.start)>=0&&cal.getTime().compareTo(sched.end)<=0)&& !sched.stats.inBlackHole(cal.getTime())){
				Cursor c = sched.rawQuery("select str.mins, ses.subjname, ses.typname, str.duration, rec.attendance, ses.sessionID from "
						+sched.meta.selectFromIndex(cal.getTime())+
						" str, session ses, "
						+sched.meta.selectRecord(cal.getTime())+
						" rec where str.mins = rec.mins and ses.sessionID = str.IDrel and str.mins = "+mins+";");
				if(c.moveToFirst()){
					
					
					Intent ix = new Intent(this,NotificationReceiver.class);
					ix.putExtra("dbase", intent.getStringExtra("dbase"));
					ix.putExtra("id", intent.getIntExtra("id", -1));
					ix.putExtra("date", intent.getStringExtra("date"));
					ix.putExtra("mins", intent.getIntExtra("mins", -1));
					ix.putExtra("session", c.getString(1)+" - "+c.getString(2)+" ");
					PendingIntent pi = PendingIntent.getService(this, id, ix, PendingIntent.FLAG_CANCEL_CURRENT);
					
					
					RemoteViews remoteViews = new RemoteViews(getPackageName(),R.layout.custom_notif);
					
					Calendar xal = Calendar.getInstance(); 
					int minsx = xal.get(Calendar.HOUR_OF_DAY) * 60;
					int IDrel = c.getInt(5);
					minsx = minsx + xal.get(Calendar.MINUTE);
					xal.set(Calendar.MILLISECOND, 0);
					xal.set(Calendar.SECOND, 0);
					xal.set(Calendar.HOUR_OF_DAY, 0);
					xal.set(Calendar.MINUTE, 0);
					sched.createTable("todaytempnotif", sched.statDef);
					sched.deleteFromTable("todaytempnotif", "");
					String query = "(";
					if((!sched.stats.inBlackHole(xal.getTime()))&&(!sched.meta.selectFromIndex(xal.getTime()).equals("labsdecoreblank"))&&((xal.getTime().compareTo(sched.start)>=0&&xal.getTime().compareTo(sched.end)<=0)))
					query = "(select s.IDrel, r.attendance from "+sched.meta.selectFromIndex(xal.getTime())+" s, "+sched.meta.selectRecord(xal.getTime())+" r where s.mins = r.mins and r.attendance != 3 and (s.mins+1) <= "+minsx+" and IDrel = "+IDrel+") ";
					
					if(!query.equals("(")){
						System.out.println("Vosaye :: inhere");
						sched.execQuery("insert into todaytempnotif (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;");
						if(sched.isEmpty("todaytempnotif")) System.out.println("Vosaye :: Empty"); else System.out.println("Vosaye :: Nope");
						sched.execQuery("update todaytempnotif set attendance = 0 where attendance is null");
					}
					query = "(";
					if(sched.tableExists(sched.stats.getTodayRange())){
					if(sched.isEmpty(sched.stats.getTodayRange())&&sched.isEmpty("todaytempnotif")){
						query = "select 1 as _id, c.IDrel as IDrel, b.subjname, b.typname, 0 as attendance, 0 as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from  session b, "+sched.stats.getOverallRange()+" c where c.IDrel = b.sessionID and c.IDrel = "+IDrel+"";
						
					}
					else
					query = "select 1 as _id, IDrel, b.subjname as subjname, b.typname as typname, sum(attendance) as attendance, sum(total) as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select a.IDrel as IDrel, sum(a.attendance) as attendance, sum(a.total) as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from (select IDrel, sum(attendance) as attendance, sum(total) as total from (select * from todaytempnotif UNION ALL select * from "+sched.stats.getTodayRange()+") group by IDrel ) a, "+sched.stats.getOverallRange()+" c where a.IDrel = c.IDrel group by IDrel UNION ALL select IDrel, 0 as attendance, 0 as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select b.IDrel as IDrel, sum(b.attendance) as oattendance, sum(b.total) as ototal from (select IDrel from "+sched.stats.getOverallRange()+" a except select IDrel from (select IDrel from todaytempnotif UNION ALL select IDrel from "+sched.stats.getTodayRange()+" ) b) a, "+sched.stats.getOverallRange()+" b where a.IDrel = b.IDrel group by IDrel) group by IDrel) a, session b where a.IDrel = b.sessionID and a.IDrel = "+IDrel+" ";
					
					}
					else if(!sched.isEmpty("todaytempnotif")){
						query = "select 1 as _id, IDrel, b.subjname as subjname, b.typname as typname, sum(attendance) as attendance, sum(total) as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select a.IDrel as IDrel, sum(a.attendance) as attendance, sum(a.total) as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from (select IDrel, sum(attendance) as attendance, sum(total) as total from (select * from todaytempnotif) group by IDrel ) a, "+sched.stats.getOverallRange()+" c where a.IDrel = c.IDrel group by IDrel UNION ALL select IDrel, 0 as attendance, 0 as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select b.IDrel as IDrel, sum(b.attendance) as oattendance, sum(b.total) as ototal from (select IDrel from "+sched.stats.getOverallRange()+" a except select IDrel from (select IDrel from todaytempnotif) b) a, "+sched.stats.getOverallRange()+" b where a.IDrel = b.IDrel group by IDrel) group by IDrel) a, session b where a.IDrel = b.sessionID and a.IDrel = "+IDrel+"";
						
					}
					
					Cursor b = sched.rawQuery(query);
					
					if(b.moveToFirst()){
						float remaining = b.getFloat(7) - b.getFloat(5);
			            if(remaining!=0){
			            	float possiblePerc = 0.0f;
			            	float shouldAttend = ((settings.schedules.getCutoff(BunKarapp.name)/100.0f) * b.getFloat(7)) - b.getFloat(4);
			            	int canBunk = 0;
			            	if(shouldAttend<0){
			            		
			            		
			            		if(shouldAttend>-1){
			            			if((b.getFloat(4)+remaining-1)/b.getFloat(7)*100>=settings.schedules.getCutoff(BunKarapp.name)){
			            				canBunk = 1;
			            				possiblePerc = (b.getFloat(4)+remaining-1)/b.getFloat(7)*100;

					            		message = "Can bunk / with %";
			            			}
			            			else {
			            				canBunk = (int) remaining;
			            				possiblePerc = (b.getFloat(4))+remaining/b.getFloat(7)*100;
					            		message = "Should attend / for %";
			            			}
			            			
			            		}
			            		else
			            		{
			            			canBunk = (int) (-shouldAttend);
				            		//remain.setTextColor(Color.parseColor("#006400"));
				            		message = "Can bunk / with %";
				            		if(remaining!=canBunk)
				            		possiblePerc = (b.getFloat(4)+remaining-canBunk)/b.getFloat(7)*100;
				            		else
					            	possiblePerc = (b.getFloat(4)-remaining)/b.getFloat(7)*100;
				            		
			            		}
			            		
			            		
			            		
			            			
			            		}
			            		
			            	else if(shouldAttend==0){
			            		canBunk = (int) remaining;
			            		//remain.setTextColor(Color.parseColor("#006400"));
			            		message = "Can bunk / with %";
			            		possiblePerc = (b.getFloat(4))/b.getFloat(7)*100;
			            		
			            	}
			            	
			            	else if((shouldAttend>0)&&shouldAttend>remaining){
			            		
			            		
			            		if(shouldAttend<1){
			            			if((b.getFloat(4))/b.getFloat(7)*100<settings.schedules.getCutoff(BunKarapp.name)){
			            				canBunk = 1;
			            				message = "Should attend / for %";
			            				possiblePerc = (b.getFloat(4)+1)/b.getFloat(7)*100;
					            		
			            			}
			            			else {
			            				canBunk = (int) remaining;
			            				message = "Can bunk / with %";
			            				possiblePerc = (b.getFloat(4))/b.getFloat(7)*100;
					            		
			            				
			            			}
			            		}
			            		else{
			            		
			            		
			            		canBunk = (int) remaining;
			            		//remain.setTextColor(getResources().getColor(R.color.orrange));
			            		message = "Should attend / for %";
			            		possiblePerc = (b.getFloat(4)+canBunk)/b.getFloat(7)*100;
			            		}
			            	}
			            	else if((shouldAttend>0)&&shouldAttend<=remaining){
			            		
			            		if(shouldAttend<1){
			            			if((b.getFloat(4))/b.getFloat(7)*100<settings.schedules.getCutoff(BunKarapp.name)){
			            				canBunk = 1;
			            				message = "Should attend / for %";
			            				possiblePerc = (b.getFloat(4)+1)/b.getFloat(7)*100;
					            		
			            			}
			            			else {
			            				canBunk = (int) remaining;
			            				message = "Can bunk / with %";
			            				possiblePerc = (b.getFloat(4))/b.getFloat(7)*100;
					            		
			            				
			            			}
			            		}
			            		else{
			            		
			            		
			            		
			            		canBunk = (int) (remaining - shouldAttend);
			            		if(canBunk!=0){
			            		//remain.setTextColor(Color.parseColor("#006400"));
				            	message = "Can bunk / with %";
			            		possiblePerc = (b.getFloat(4)+remaining-canBunk)/b.getFloat(7)*100;
			            		
			            		}
			            		else{
			            		//remain.setTextColor(getResources().getColor(R.color.orrange));
				            	message = "Should Attend / for %";
			            		canBunk = (int) (remaining);
			            		possiblePerc = (b.getFloat(4)+canBunk)/b.getFloat(7)*100;
			            		}
			            		}
			            	}
			            	//remain.setText(""+canBunk+" ");
			            	if(canBunk!=0)
			            	message = message.replaceAll("/", ""+canBunk).replaceAll("%", String.format("%.2f",possiblePerc)+"%");
			            	else message = "None left";
			            }
			            //else remain.setText(""+0+" ");
			        	
				

					}
					b.close();
					
					notif = new NotificationCompat.Builder(this).setTicker("Attending "+c.getString(1)+" "+c.getString(2)+" ?")
							.setSmallIcon(R.drawable.noticonquest)
							.setContentIntent(pi).setContent(remoteViews)
							.build(); 
					remoteViews.setTextViewText(R.id.custom_notif_textView1,name);
					remoteViews.setTextViewText(R.id.custom_notif_textView2, c.getString(1)+" - "+c.getString(2)+" at "+nft.format((mins/60>12?(mins/60)-12:mins/60))+":"+nft.format(mins%60)+" "+((mins/60>12)?"PM":"AM"));
					remoteViews.setTextViewText(R.id.custom_notif_textView3, message);
					
					
					notificationManager.notify(id, notif);
					
					c.close();
					c = sched.rawQuery("select mins from "+structure+" where mins > "+mins+";");
					if(c.moveToFirst()){
						Intent i = new Intent(this,NotificationService.class);
						i.putExtra("dbase", name);
						i.putExtra("id", id);
						cal.set(Calendar.HOUR_OF_DAY, c.getInt(0)/60);
						cal.set(Calendar.MINUTE, c.getInt(0)%60);
						i.putExtra("date", sdf.format(cal.getTime()));
						i.putExtra("mins", c.getInt(0));
						PendingIntent pending = PendingIntent.getService(this, id, i,
					            PendingIntent.FLAG_CANCEL_CURRENT);
						//setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pending);
						alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pending);
					}
					c.close();
					
					
					
					
				}
					
					
					
				}
				else{ return START_NOT_STICKY;}
			}
		
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
		return START_NOT_STICKY;
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		if(BunKarapp==null) BunKarapp = (BunKar) this.getApplication();
		
		
		
		
	}
	
	public void onTaskRemoved(Intent rootIntent){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, 15);
		
		Intent i = new Intent(this,MaintenanceManager.class);
		PendingIntent pending = PendingIntent.getService(this, 300, i,
	            PendingIntent.FLAG_CANCEL_CURRENT);
		//setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pending);
		if(alarmManager==null)
		alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
		alarmManager.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pending);
		
		
		((BunKar) this.getApplication()).deleteAllCache();
		ValidatorService.FOCUSED = false;
		
	}

}
