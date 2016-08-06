package com.vosaye.bunkr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;










import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.vosaye.bunkr.app.BaseActivity;
import com.vosaye.bunkr.app.ScheduleListActivity;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.NotificationService;
import com.vosaye.bunkr.services.ScheduleManagerService;
import com.vosaye.bunkr.services.ValidatorService;

public class BunKar extends android.app.Application { 
	public String name = "";
	public String tagForUpload = "";
	public AuthDatabase settings;
	public static int count = 0;
	public ScheduleDatabase notificationDbase, mainDbase;
	public Vector<ScheduleDatabase> schedulesDbaseList;
	public static final int RESULT_CLOSE_ALL = 0;
	public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
	public static SimpleDateFormat sdf2 = new SimpleDateFormat("dd MMMM ''yy",Locale.ENGLISH);
	public static SimpleDateFormat sdf3 = new SimpleDateFormat("dd MMMM yy",Locale.ENGLISH);
	public static SimpleDateFormat sdf4 = new SimpleDateFormat("dd MMMM yyyy",Locale.ENGLISH);
	public static SimpleDateFormat forUpload = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
	
	public static SimpleDateFormat sdftime = new SimpleDateFormat("hh:mm:ss a",Locale.ENGLISH);
	public static SimpleDateFormat sdftime2 = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
	Notification notifier;
	public AlarmManager alarm;
	public NotificationManager notify;
	public PendingIntent pintent;
	public String pseudoStructureNameClipBoard = " ";
	public String days[] = {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
	
	public void onCreate(){
		super.onCreate();
		schedulesDbaseList = new Vector<ScheduleDatabase>();
		settings = new AuthDatabase(this.getApplicationContext(),"settings.db",null,1);
		//init all databases and add to pool
		Cursor c = settings.rawQuery("select name from schedules");
		if(c.moveToFirst())
			do{
				schedulesDbaseList.add(new ScheduleDatabase(this.getApplicationContext(),c.getString(0),null,1));
			}
			while(c.moveToNext());
		c.close();
		notify = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		
		Intent notifService = new Intent(this,NotificationService.class);
		notifService.putExtra("command", "none");
		this.startService(notifService);
		this.startService(new Intent(this,MaintenanceManager.class));
		
		
		//Calendar cal = Calendar.getInstance();
		//Intent intentx = new Intent(this, NotificationService.class);
		//intentx.putExtra("command", "no");
		//pintent = PendingIntent.getService(this, 0, intentx, 0);
		//alarm =  (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		//alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000, pintent);
	}
	public void onTerminate(){
		super.onTerminate();
	}
	public ScheduleDatabase getDatabase(String name){//returns a database if available. Usually database will be available, if its not, that might be some kind of error.
		for(int i=0; i<schedulesDbaseList.size(); i++){
			if(schedulesDbaseList.elementAt(i).getName().equals(name)){
				return schedulesDbaseList.elementAt(i);
			}
		}
		return null;
	}
	
	
	@SuppressWarnings("deprecation")
	public String uploadDbase(){
		String urlServer = "http://vosaye.hol.es/upload.php";
		String responseString = "";
		HttpResponse response;
		File dbase = this.getDatabasePath(name);
		String path = this.getApplicationInfo().dataDir;
		path = path+"/databases/cache/forUpload"+".backup";
		File destination = new File(path);
		if(destination.exists()) destination.delete();
		
		try {
			copyFile(dbase,destination);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ScheduleDatabase sd = new ScheduleDatabase(this, path, null, 1);
		Cursor c;
		
		c = sd.rawQuery("select name from sqlite_master where type = 'table' and (name like '%range%' or name like '%month%' or name like '%week%') and name != 'ranges';");
		
		if(c.moveToFirst()){
			do{
				sd.dropTable(c.getString(0));
			}
			while(c.moveToNext());
		}
		c.close();
		
		c = sd.rawQuery("select name from sqlite_master where type = 'table' and name like '%record%';");
		if(c.moveToFirst()){
			do{
				sd.set("attendance", "2", c.getString(0), "");
			}while(c.moveToNext());
		}
		
			sd.deleteFromTable("ranges","");
		
		sd.execQuery("create table downloaded(name varchar(10))");
		//sd.close();
		try
		{
			HttpClient httpClient = new DefaultHttpClient();
			HttpContext localContext = new BasicHttpContext();
			HttpPost httpPost = new HttpPost(urlServer);
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.addPart("name",new StringBody(name,  Charset.forName( "UTF-8" )));
			builder.addPart("tag",new StringBody(tagForUpload,  Charset.forName( "UTF-8" )));
			builder.addPart("strt",new StringBody(forUpload.format(sd.standards.getStartOfTerm()),  Charset.forName( "UTF-8" )));
			builder.addPart("end",new StringBody(forUpload.format(sd.standards.getEndOfTerm()),  Charset.forName( "UTF-8" )));
			builder.addPart("yek",new StringBody("Holiness is in protecting those who cannot defend themselves!",  Charset.forName( "UTF-8" )));
			builder.addPart("db", new FileBody(destination));
			
			httpPost.setEntity(builder.build());
			sd.close();
			response = httpClient.execute(httpPost, localContext);
		    
			
			
			
			
					BufferedReader in = new BufferedReader
	                (new InputStreamReader(response.getEntity().getContent()));
	                StringBuffer sb = new StringBuffer("");
	                String line = "";
	                String NL = System.getProperty("line.separator");
	                while ((line = in.readLine()) != null) {
	                    sb.append(line + NL);
	                }
	                in.close();
	                responseString = sb.toString();
			httpClient.getConnectionManager().shutdown();
			
		}
		catch (Exception ex)
		{
		    //Exception handling
			
			ex.printStackTrace();
		}finally{
			destination.delete();
		}
		return responseString;
		
	}
	public void deleteAllCache(){
		
		try{
		Cursor cx = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id > 0;");
		if(cx!=null)
		if(cx.moveToFirst())
			do{
				try{
					
					new File(cx.getString(0)).delete();
					
				}
				catch(Exception e){}
				
				
			}
			while(cx.moveToNext());
		if(cx!=null)
			cx.close();
		
		settings.execQuery("delete from labsdecore"+name.replaceAll(" ", "_")+" where id > 0");
		
		}catch(Exception e){}
		ValidatorService.FOCUSED = false;
		
		
		
		
		
		
		
		
		
		notify.cancel(0);
		
	}
	
	private void backupDbase(String name, int id, Date datex, String tag){
		//close dbase
		//copy dbase
		//open dbase
		//insert into table, replace if exists
		
		Cursor temp = settings.rawQuery("select backups from schedules where name = '"+name+"';");
		if(temp.moveToFirst())
			if(temp.getInt(0)==0)
				return;
		
		temp.close();
		String date = sdf.format(datex);
		if(this.settings.exists("select name from schedules where name = '"+name+"'")){
			for(int i=0; i<schedulesDbaseList.size(); i++){
				if(schedulesDbaseList.elementAt(i).getName().equals(name)){
					ScheduleDatabase sd = schedulesDbaseList.elementAt(i);
					try{
					//settings.beginTransaction();
					//sd.close();
					String path = this.getApplicationInfo().dataDir;
					path = path+"/databases/per/"+(name+date).replaceAll(" ", "")+".backup";
					File src = this.getDatabasePath(name);
					File srcj = new File(this.getDatabasePath(name).getPath()+"-journal");
					File dest = new File(path);
					File destj = new File(path+"-journal");
					
					//File onSD = new File(Environment.getExternalStorageDirectory()+"/bnkr/"+(name+date).replaceAll(" ", "")+".db");
					//sd.open();
					try{
					temp = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id = "+id+";");
					if(temp.moveToFirst())
						new File(temp.getString(0)).delete();
					temp.close();
					}catch(Exception e){}
					settings.deleteFromTable("labsdecore"+name.replaceAll(" ", "_"), "where id = "+id+"");
					
					settings.execQuery("insert into labsdecore"+name.replaceAll(" ", "_")+" values("+id+",'"+tag+"',"+0+",'"+path+"','"+date+"')");

					copyFile(src,dest);
					copyFile(srcj,destj);
					//copyFile(src,onSD);
					//copyFile(srcj,onSD);
					
					
					}
					catch(IOException e){
						e.printStackTrace();
						settings.rollback();
					}
					finally{
						if(!sd.getDatabase().isOpen())
							sd.open();
						//if(settings.getDatabase().inTransaction()) settings.rollback();
					}
					break;
				}
			}
		}
		
	}
	public void tempBackupDbase(String name, Date datex, String tag){
		//check greatest number
		//if less than 5
			//close dbase
			//copy dbase
			//open dbase
			//delete all >number, insert into table with inc number and selected = 1, make all other selected = 0
		//else
			//delete the one with number 1
			//update each number with number-1
			//close dbase
			//copy dbase
			//open dbase
			//delete all >number, insert into table with inc number and selected = 1, make all other selected = 0
		
		String date = sdf.format(datex);
		for(int i=0; i<schedulesDbaseList.size(); i++){
			if(schedulesDbaseList.elementAt(i).getName().equals(name)){
				//ScheduleDatabase sd = schedulesDbaseList.elementAt(i);
				try{
					
				
				Cursor c = settings.rawQuery("select max(id) from labsdecore"+name.replaceAll(" ", "_")+" where id > 0;");
				int max = 1;
				if(c.moveToFirst())
					max = c.getInt(0)+1;
				c.close();
				//File onSD = new File(Environment.getExternalStorageDirectory()+"/bnkr/"+(name+date).replaceAll(" ", "")+".db");
				//System.out.println("Vossaye bnkr : "+onSD.getPath());
				
				if(max<=5){
					
					String path = this.getApplicationInfo().dataDir;
					path = path+"/databases/sessioncache/"+(name+date).replaceAll(" ", "")+".backup";
					System.out.println("Vossaye bnkr : "+path);
					File src = this.getDatabasePath(name);
					File dest = new File(path);
					//File srcj = new File(this.getDatabasePath(name).getPath()+"-journal");
					//File destj = new File(path+"-journal");
					
					settings.set("selected", "0", "labsdecore"+name.replaceAll(" ", "_"), "");
					settings.execQuery("insert into labsdecore"+name.replaceAll(" ", "_")+" values("+(max)+",'"+tag+"',"+1+",'"+path+"','"+date+"')");
					copyFile(src,dest);
					//copyFile(srcj,destj);
					//copyFile(src,onSD);
					//copyFile(srcj,onSD);
				}
				else{
					c = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id = 1");
					if(c.moveToFirst()){
						String path = c.getString(0);
						File old = new File(path);
						File oldj = new File(path+"-journal");
						try{
						old.delete();
						}catch(Exception e){}
					}
					c.close();
					settings.deleteFromTable("labsdecore"+name.replaceAll(" ", "_"), "where id = 1");
					settings.set("id", "1", "labsdecore"+name.replaceAll(" ", "_"), "where id = 2");
					settings.set("id", "2", "labsdecore"+name.replaceAll(" ", "_"), "where id = 3");
					settings.set("id", "3", "labsdecore"+name.replaceAll(" ", "_"), "where id = 4");
					settings.set("id", "4", "labsdecore"+name.replaceAll(" ", "_"), "where id = 5");
					
					String path = this.getApplicationInfo().dataDir;
					path = (path+"/databases/sessioncache/"+(name+date).replaceAll(" ", "")+".backup").replaceAll(":", "").replaceAll("-", "");
					
					System.out.println("Vossaye bnkr : "+path);
					File src = this.getDatabasePath(name);
					File dest = new File(path);
					//File srcj = new File(this.getDatabasePath(name).getPath()+"-journal");
					//File destj = new File(path+"-journal");
					
					settings.set("selected", "0", "labsdecore"+name.replaceAll(" ", "_"), "");
					settings.execQuery("insert into labsdecore"+name.replaceAll(" ", "_")+" values("+5+",'"+tag+"',"+1+",'"+path+"','"+date+"')");

					copyFile(src,dest);
					//copyFile(srcj,destj);
					//copyFile(src,onSD);
					//copyFile(srcj,onSD);
					
				}
				
				boolean backups = false;
				Cursor checker = settings.rawQuery("select backups from schedules where name = '"+name+"';");
				if(checker.moveToFirst()){
					if(checker.getInt(0)==1) backups = true;
				}
				checker.close();
				
				if(backups){
				Calendar tmp = Calendar.getInstance();
				Calendar time = Calendar.getInstance();
				c = settings.rawQuery("select id, datex, filename from labsdecore"+name.replaceAll(" ", "_")+" where id = -1;");
				if(c.moveToFirst()){
					time.setTime(sdf.parse(c.getString(1)));
					if(time.get(Calendar.MONTH)!=tmp.get(Calendar.MONTH)||time.get(Calendar.DATE)!=tmp.get(Calendar.DATE)||time.get(Calendar.YEAR)!=tmp.get(Calendar.YEAR)){
						//replace
						Cursor cget = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id = (select max(id) from labsdecore"+name.replaceAll(" ", "_")+" where id > 0)");
						if(cget.moveToFirst()){
							File src, dest;
							src = new File(cget.getString(0));
							String path = this.getApplicationInfo().dataDir;
							path = (path+"/databases/per/"+(name+date).replaceAll(" ", "")+"daily.backup").replaceAll(":", "").replaceAll("-", "");
							dest = new File(path);
							
							copyFile(src,dest);
							settings.deleteFromTable("labsdecore"+name.replaceAll(" ", "_"), " where id = -1");
							new File(c.getString(2)).delete();
							settings.execQuery("insert into labsdecore"+name.replaceAll(" ", "_")+" values("+-1+",'Daily',"+0+",'"+path+"','"+sdf.format(tmp.getTime())+"')");

						}
					}
				}else{
					//add
					Cursor cget = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id = (select max(id) from labsdecore"+name.replaceAll(" ", "_")+" where id > 0)");
					if(cget.moveToFirst()){
						File src, dest;
						src = new File(cget.getString(0));
						String path = this.getApplicationInfo().dataDir;
						path = (path+"/databases/per/"+(name+date).replaceAll(" ", "")+"daily.backup").replaceAll(":", "").replaceAll("-", "");
						dest = new File(path);
						
						copyFile(src,dest);
						settings.deleteFromTable("labsdecore"+name.replaceAll(" ", "_"), " where id = -1");
						settings.execQuery("insert into labsdecore"+name.replaceAll(" ", "_")+" values("+-1+",'Daily',"+0+",'"+path+"','"+sdf.format(tmp.getTime())+"')");

					}
				}
				c.close();
 
 				
				
				c = settings.rawQuery("select id, datex, filename from labsdecore"+name.replaceAll(" ", "_")+" where id = -2;");
				if(c.moveToFirst()){
					time.setTime(sdf.parse(c.getString(1)));
					
					if(TimeUnit.MILLISECONDS.toDays(Math.abs(tmp.getTimeInMillis() - time.getTimeInMillis()))>6||time.after(tmp)){
						//replace
						Cursor cget = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id = (select max(id) from labsdecore"+name.replaceAll(" ", "_")+" where id > 0)");
						if(cget.moveToFirst()){
							File src, dest;
							src = new File(cget.getString(0));
							String path = this.getApplicationInfo().dataDir;
							path = (path+"/databases/per/"+(name+date).replaceAll(" ", "")+"daily.backup").replaceAll(":", "").replaceAll("-", "");
							dest = new File(path);
							
							copyFile(src,dest);
							settings.deleteFromTable("labsdecore"+name.replaceAll(" ", "_"), " where id = -2");
							new File(c.getString(2)).delete();
							settings.execQuery("insert into labsdecore"+name.replaceAll(" ", "_")+" values("+-2+",'Weekly',"+0+",'"+path+"','"+sdf.format(tmp.getTime())+"')");

						}
					}
				}else{
					//add
					Cursor cget = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id = (select max(id) from labsdecore"+name.replaceAll(" ", "_")+" where id > 0)");
					if(cget.moveToFirst()){
						File src, dest;
						src = new File(cget.getString(0));
						String path = this.getApplicationInfo().dataDir;
						path = (path+"/databases/per/"+(name+date).replaceAll(" ", "")+"daily.backup").replaceAll(":", "").replaceAll("-", "");
						dest = new File(path);
						
						copyFile(src,dest);
						settings.deleteFromTable("labsdecore"+name.replaceAll(" ", "_"), " where id = -2");
						settings.execQuery("insert into labsdecore"+name.replaceAll(" ", "_")+" values("+-2+",'Weekly',"+0+",'"+path+"','"+sdf.format(tmp.getTime())+"')");

					}
				}
				c.close();
				}
				
				
				}catch(IOException e){
					e.printStackTrace();
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally{
					
				}
				break;
			}
		}
		
		
		
			
	}
	
	public void reStore(String name, int id){
		
		//get backupfilename
		//close db
		//copy db from path
		//open db
		//set selected = 1
		
		for(int i=0; i<schedulesDbaseList.size(); i++){
			if(schedulesDbaseList.elementAt(i).getName().equals(name)){
				ScheduleDatabase sd = schedulesDbaseList.elementAt(i);
				
				try{
					File bkr = getDatabasePath(name);
					//File bkrj = new File(this.getDatabasePath(name).getPath()+"-journal");
					
					String pathx = this.getApplicationInfo().dataDir;
					pathx = pathx+"/databases/sessioncache/bkrtmp";
					File tmpbkr =  new File(pathx);
					//File tmpbkrj = new File(pathx+"-journal");
					
					copyFile(bkr, tmpbkr);
					//copyFile(bkrj,tmpbkrj); 
					
					
					
					
					String path = null;
					Cursor c = settings.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+" where id = "+id+";");
					if(c.moveToFirst())
						path = c.getString(0);
					if(path==null) return;
					File src = new File(path);
					File dest = this.getDatabasePath(name);
					//File srcj = new File(this.getDatabasePath(name).getPath()+"-journal");
					//File destj = new File(path+"-journal");

					sd.close();
					
					dest.delete();
					//destj.delete();
					copyFile(src,dest);
					//copyFile(srcj,destj);
					sd.open();
					
					settings.set("selected", "0", "labsdecore"+name.replaceAll(" ", "_"), "");
					settings.set("selected", "1", "labsdecore"+name.replaceAll(" ", "_"), "where id = "+id+"");
					
					
				}
				catch(Exception e){
					e.printStackTrace();
					File bkr = getDatabasePath(name);
					//File bkrj = new File(this.getDatabasePath(name).getPath()+"-journal");
					
					if(!bkr.exists()){
						String pathx = this.getApplicationInfo().dataDir;
						pathx = pathx+"/databases/name/sessioncache/bkrtmp";
						File tmpbkr =  new File(pathx);
						//File tmpbkrj = new File(pathx+"-journal");
						if(tmpbkr.exists())
						try {
							bkr.delete();
							//bkrj.delete();
							copyFile(tmpbkr,bkr);
							//copyFile(tmpbkrj,bkrj);
							
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					
				}
				finally{
					sd.open();
				}
				break;
			}
		}
		
	}
	public void setScheduleName(String name){
		this.name = name;
	}
	public void deleteSchedule(String name){//manages database pool in new thread, which is single, queing thread
		
		Intent i = new Intent(this,ScheduleManagerService.class);
		i.putExtra("scheduleName", name);
		i.putExtra("operation", "del");
		this.startService(i);
	}
	public void createSchedule(String name){//manages database pool in new thread, which is single, queing thread
		
		//call manager service
		Intent i = new Intent(this,ScheduleManagerService.class);
		i.putExtra("scheduleName", name);
		i.putExtra("operation", "create");
		this.startService(i);
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		
		String destPath = destFile.getPath().substring(0,destFile.getPath().lastIndexOf("/"));
		File destDirs = new File(destPath);
		destDirs.mkdirs();
		if(!destFile.exists()) {
	        destFile.createNewFile();
	    }
	    
	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	public boolean isMyServiceRunning(String name) {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (name.equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
	
	
}
