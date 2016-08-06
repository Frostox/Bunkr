package com.vosaye.bunkr.base;





import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import com.vosaye.bunkr.exception.BunkerException;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.*;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import android.content.Context;
import android.util.Base64;
import android.widget.Toast;


/**This class manages authentications, encryptions of passwords and schedule names.
 * 
 * @author Roger Cores
 * @version 1.01
 */
public class AuthDatabase extends BunkerDatabase{ //tested OK
	Context context;
	public Auth authentication;
	public Schedules schedules;
	public AuthDatabase auth;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
	/**Constructs an AuthDatabase object.
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public AuthDatabase(Context context, String name, CursorFactory factory,int version) { //tested OK
		
		super(context, name, factory, version);
		this.context = context;
		authentication = new Auth();
		auth = this;
		schedules = new Schedules();

		SQLiteDatabase.loadLibs(context);
		
	}
	/* (non-Javadoc)
	 * @see com.labsdecore.bunker.database.BunkerDatabase#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public synchronized void onCreate(SQLiteDatabase dbase) { //tested OK
		this.setDbase(dbase);
		String authdef[] = {"uname varchar not null unique","pass varchar not null"};
		this.createTable("auth", authdef); 
		String schedulesdef[] = {"name varchar not null unique","type varchar not null","lupdated datetime default null","currentPerc number default 0","backups number default 0","rt number default 1","cutoff number default 75"};
		this.createTable("schedules", schedulesdef);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.add(Calendar.DATE, -2);
		String date = sdf.format(cal.getTime());
		this.execQuery("insert into schedules values('TECMPN B','true','"+date+"',0,0,1,75)");
		this.execQuery("insert into schedules values('BEEXTC A','true','"+date+"',0,0,1,75)");
		auth.execQuery("create table labsdecore"+"TECMPN B".replaceAll(" ", "_")+" (id number, name varchar(20), selected number default 0, filename varchar(20), datex varchar(40));");
		auth.execQuery("create table labsdecore"+"BEEXTC A".replaceAll(" ", "_")+" (id number, name varchar(20), selected number default 0, filename varchar(20), datex varchar(40));");
		
		String settingsdef[] = {"name varchar primary key","value varchar"};
		this.createTable("settings", settingsdef);
		this.execQuery("insert into settings values('autologin','false')");
		
	}
	
	/** The class manages authentications and encryptions of passwords.
	 * @author Roger Cores
	 * @version 1.01
	 */
	public class Auth{ //tested OK encryption/decryption/register/login/change pass
		
		
		public synchronized boolean autoLoginActive(){
			Cursor c = AuthDatabase.this.rawQuery("select name, value from settings where name = 'autologin'");
			if(c.moveToFirst()){
				if(c.getString(1).equals("true")){c.close();return true;}
			}
			c.close();
			return false;
		}
		/**
		 * Encrypts a string using base64
		 * @param str
		 * @return
		 */
		public synchronized String encrypt(String str){ //tested OK
			try{
			str = Base64.encodeToString(str.getBytes(), Base64.DEFAULT);
			}catch(Exception e){}
			return str;
		}
		/**
		 * Decrypts a string using base64
		 * @param str
		 * @return
		 */
		public String decrypt(String str){ //tested OK
			try{
			str = new String( Base64.decode( str, Base64.DEFAULT ) );
			}catch(Exception e){}
			return str;
		}
		/**
		 * Registers username and password to the system
		 * @param uname
		 * @param pass
		 */
		public void register(String uname, String pass){ //tested OK
			//Toast.makeText(context, "register", Toast.LENGTH_LONG).show();
			auth.deleteFromTable("auth", "");
			String[] values = {"'"+encrypt(uname)+"'","'"+encrypt(pass)+"'"};
			auth.insertIntoTable("auth", values);
		}
		/**
		 * Logs a username and password in to the system.
		 * If login failed, it returns false.
		 * @param u
		 * @param p
		 * @return
		 */
		public synchronized Boolean login(String u, String p){ //tested OK
			String uname,pass;
			Cursor c;
			c = auth.rawQuery("select * from auth;");
			if(c.moveToFirst()){
				do{
					uname = decrypt(c.getString(0));
					pass = decrypt(c.getString(1));
					if(u.equals(uname)&&p.equals(pass)){
						c.close();
						return true;
					}
					
				}
				while(c.moveToNext());
			}
			c.close();
			return false;
		}
		/**Changes the password of a user from old password to new password.
		 * Before using this method, a login is must to ensure authentication.
		 * @param u
		 * @param p
		 * @param np
		 */
		public boolean changePass(String u, String p, String np){ //tested OK
			if(login(u,p)){
				auth.set("pass", "'"+encrypt(np)+"'", "auth", "where uname='"+encrypt(u)+"'");
				//Toast.makeText(context, "Password is Changed", Toast.LENGTH_LONG).show();
				return true;
			}else{
				//Toast.makeText(context, "either username or the password is incorrect", Toast.LENGTH_LONG).show();
				return false;
			}
		}
		
	}
	/**
	 * The class manages list of available schedules on the system.
	 * @author Roger Cores
	 * @version 1.01
	 */
	public class Schedules{ //pending
		
		public synchronized void setNotification(String name, boolean status) throws BunkerException{
			if(!auth.valueExists("name", "'"+name+"'", "schedules")) throw new BunkerException("Name doesn't Exists",context);
			auth.execQuery("update schedules set type = '"+status+"' where name = '"+name+"'");
		}
		
		public synchronized float getCutoff(String name){
			Cursor c = auth.rawQuery("select cutoff from schedules where name = '"+name+"';");
			if(c.moveToFirst()){
				float perc = c.getInt(0);
				c.close();
				return perc;
			}
			else return 75;
		}
		
		
		/**Creates a new schedule.
		 * @param name
		 * @throws BunkerException 
		 */
		public synchronized void newSchedule(String name) throws BunkerException{ //tested OK
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.add(Calendar.DATE, -2);
			String date = sdf.format(cal.getTime());
			String[] values = {"'"+name+"'","'true'","'"+date+"'","0","0","1","75"};
			if(auth.valueExists("name", "'"+name+"'", "schedules")) throw new BunkerException("Name already Exists",context);
			auth.insertIntoTable("schedules", values);
			auth.execQuery("create table labsdecore"+name.replaceAll(" ", "_")+" (id number primary key, name varchar(20), selected number default 0, filename varchar(20), datex varchar(40));");
			//Toast.makeText(context, "schedule added", Toast.LENGTH_LONG).show();
		}
		
		public boolean isNotificationActive(String name) throws BunkerException{
			if(!auth.valueExists("name", "'"+name+"'", "schedules")) throw new BunkerException("Name doesn't Exists",context);
			Cursor c = auth.rawQuery("select name from schedules where name = '"+name+"' and type = 'true'");
			if(c.moveToFirst()){c.close(); return true;}
			c.close();
			return false;
		}
		/**Deletes an existing schedule.
		 * @param name
		 */
		public synchronized void deleteSchedule(String name){ //tested OK
			auth.deleteFromTable("schedules", "where name='"+name+"'");
			Cursor cx = auth.rawQuery("select value from settings where name = 'default'");
			if(cx.moveToFirst()){
				if(cx.getString(0).equals(name)) auth.deleteFromTable("settings", "where name = 'default'");
			}
			cx.close();
			cx = auth.rawQuery("select filename from labsdecore"+name.replaceAll(" ", "_")+";");
			
			if(cx.moveToFirst())
				do{
					try{
						
						new File(cx.getString(0)).delete();
						
					}
					catch(Exception e){}
					
					
				}
				while(cx.moveToNext());
			
			auth.execQuery("drop table labsdecore"+name.replaceAll(" ", "_")+";");
			
			
			
			
		}
		/**Renames a schedule.
		 * @param name
		 * @param nname
		 * @throws BunkerException 
		 */
		public synchronized void renameSchedule(String name, String nname) throws BunkerException{ //tested OK
			if(auth.valueExists("name", "'"+nname+"'", "schedules")) throw new BunkerException("Name already Exists",context);
			if(!auth.valueExists("name", "'"+name+"'", "schedules")) throw new BunkerException("Name doesn't Exists",context);
			auth.set("name", "'"+nname+"'", "schedules", "where name='"+name+"'");
		}
	}
}
