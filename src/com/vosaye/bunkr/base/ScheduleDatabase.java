package com.vosaye.bunkr.base;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.IOException;
import java.lang.*;

import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.ValidatorService;

import android.content.Context;
import android.content.Intent;
import net.sqlcipher.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
//import net.sqlcipher.database.*;
//import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
//import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.*;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import android.widget.Toast;
/**
* The class defines a set of methods to query the database.
* It denotes a database for a specific Schedule.
* @author Roger Cores
* @version 1.0 | untested | prerealease
*/
public class ScheduleDatabase extends BunkerDatabase{
	
	//public variables
	public String days[] = {"sun","mon","tue","wed","thu","fri","sat"};
	public boolean downloaded = false;
	public Date start, end;
	public Context context; //App Context
	public ScheduleDatabase schedule; //Base Database
	public TableOfStandards standards; //Standards
	public Meta meta; //Metas
	public Stats stats; //Stats
	protected SimpleDateFormat sdf;
	/** The datastructure is deprecated. Use the database table 'pipeline' instead
	 * @deprecated 
	 */
	private Vector<String> validatePipeline;
	//protected Cursor c; //SQLite Cursor
	protected String trial; //The Exception Collector Object
	protected Calendar x; //Calendar
	protected Vector<String> stringVector;
	
	public int count = 0;
	
	
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------
	
	
	
	//Standard Strings
		//Tabels of Structures
			//Index
			public final String[] indexDef = {"datenm datetime primary key","refDate varchar not null"}; 
			//Structure
			public final String[] structureDef = {"mins number primary key","IDrel number not null","duration number","foreign key(IDrel) references session(sessionID)"};
			public final String[] pseudoStructureDef = {"mins number primary key","IDrel number not null","duration number","attendance  number not null default 2","foreign key(IDrel) references session(sessionID)"};
			//StructureTriggers
			public final String triggerDefInsert = "create trigger if not exists tester_trig_7 before insert on / for each row when(exists(select null from / where (mins between (new.mins+1) and (new.mins+new.duration-1)) or ((mins+duration) between (new.mins+1) and (new.mins+new.duration-1)))) begin select raise(abort, 'Overwrite Not Allowed'); end;";
			public final String triggerDefUpdate = "create trigger if not exists tester_trig_7 before update on / for each row when(exists(select null from / where (mins between (new.mins+1) and (new.mins+new.duration-1)) or ((mins+duration) between (new.mins+1) and (new.mins+new.duration-1)))) begin select raise(abort, 'Overwrite Not Allowed'); end;";
			public final String triggerDefUpdate1 = "create trigger if not exists tester_trig_7u2 before update on / for each row when((new.mins not between 0 and 1439) or (new.mins+new.duration not between 0 and 1440)) begin select raise(abort, 'Insert within 12 AM to 11:59 PM'); end;";
			public final String triggerDefInsert1 = "create trigger if not exists tester_trig_7i2 before insert on / for each row when((new.mins not between 0 and 1439) or (new.mins+new.duration not between 0 and 1440)) begin select raise(abort, 'Insert within 12 AM to 11:59 PM'); end;";
			//Tabels of Records
			//Record & virtual
			public final String[] recordDef = {"mins number primary key","attendance number not null default 2","foreign key(mins) references /(mins)"};
	
		//Tabels of Stats
			//weekly & monthly
			public final String[] statDef = {"IDrel number primary key","attendance number","total number not null default 0","foreign key(IDrel) references session(sessionID)"};
			//public String[] statIndexDef = {"datenm datetime primary key"};
			
		//Ranges
			public final String[] rangesDef = {"rname varchar not null primary key","start datetime not null","end datetime not null","unique(start,end)"};
			
			
		//Tags
			public final String[] tagContainerDef = {"mins number not null","tagname varchar not null unique"};
			public final String[] tagDef = {"name primary key"};
	//------------------------------------------------------------------------------------------------------------------------------------------------		
			
			
	
			
	/**
	* ScheduleDatabase Cunstructor
	* Cunstructs a new ScheduleDatabase object for a specific database file.
	* This file is the only sqlite file a perticular schedule uses.
	* @param context Activity from which this database is called. This object is used for testing purposes only and hence a null can be passed instead. 
	* @param name The name of the database.
	* @param factory Refer SQLiteOpenHelper.
	* @param version Refer SQLiteOpenHelper.
	* @see SQLiteOpenHelper
	* @see SQLiteDatabase
	* @see Cursor
	*/	
	public ScheduleDatabase(Context context, String name,CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
		this.schedule = this;
		standards = new TableOfStandards();
		stats = new Stats();
		meta = new Meta();
		x = Calendar.getInstance();
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
		validatePipeline = new Vector<String>();
		stringVector = new Vector<String>();
		
		try {
			start = this.standards.getStartOfTerm();
			end = this.standards.getEndOfTerm();
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		

		SQLiteDatabase.loadLibs(context);
	}//constructor-end
	 
	
	
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------	
	
	
	
 	public void open(){
 		
 		this.dbase = this.getWritableDatabase(key);
 	}
 	@Override
	public   void onOpen(SQLiteDatabase db){
		super.onOpen(db);
	    if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	        this.setDbase(db); 
	    }
	    
	    
	  //ensure schema is constructed, if something is missing, build it right away
	  //Tabel of Standards
		//Subject
	    if(!schedule.tableExists("subject")){
		stringVector.clear();
		stringVector.add("name varchar primary key");
		//String[] subdef = {"name varchar primary key"};
		schedule.createTable("subject", stringVector);
	    }
		//Type
	    if(!schedule.tableExists("type")){
		stringVector.clear();
		stringVector.add("name varchar primary key");
		stringVector.add("mins number not null");
		//String[] typDef = {"name varchar primary key","mins number not null"};
		schedule.createTable("type", stringVector);
	    }
		//Session
	    if(!schedule.tableExists("session")){
		stringVector.clear();
		stringVector.add("subjname varchar");
		stringVector.add("typname varchar");
		stringVector.add("sessionID INTEGER primary key");
		stringVector.add("unique(subjname,typname)");
		stringVector.add("foreign key(subjname) references subject(name) ON UPDATE CASCADE");
		stringVector.add("foreign key(typname) references type(name) ON UPDATE CASCADE");
		//String[] sessionDef = {"subjname varchar","typname varchar","sessionID INTEGER primary key","unique(subjname,typname)","foreign key(subjname) references subject(name)","foreign key(typname) references type(name)"};
		schedule.createTable("session", stringVector);
	    }
		//Holi
	    if(!schedule.tableExists("holi")){
		stringVector.clear();
		stringVector.add("datex datetime primary key");
		//String[] holiDef = {"datex datetime primary key"};
		schedule.createTable("holi", stringVector);
	    }
		//Term
	    if(!schedule.tableExists("term")){
		stringVector.clear();
		stringVector.add("start datetime unique not null");
		stringVector.add("end datetime unique");
		stringVector.add("check(start<end)");
		//String[] termDef = {"start datetime unique not null","end datetime unique","check(start<end)"};
		schedule.createTable("term", stringVector);
	    }
		//structureNamesTrackRecord
	    if(!schedule.tableExists("structureNames")){
		stringVector.clear();
		stringVector.add("name varchar primary key");
		stringVector.add("countz number default 1");
		//String[] structureNamesDef = {"name varchar primary key","countz number default 1"};
		schedule.createTable("structureNames", stringVector);
	    }
		//Ranges
	    if(!schedule.tableExists("ranges"))
		schedule.createTable("ranges", rangesDef);
		
		//create indexes
		try{
		if(!schedule.tableExists("mon"))
		meta.createIndex("mon");
		if(!schedule.tableExists("tue"))
		meta.createIndex("tue");
		if(!schedule.tableExists("wed"))
		meta.createIndex("wed");
		if(!schedule.tableExists("thu"))
		meta.createIndex("thu");
		if(!schedule.tableExists("fri"))
		meta.createIndex("fri");
		if(!schedule.tableExists("sat"))
		meta.createIndex("sat");
		if(!schedule.tableExists("sun"))
		meta.createIndex("sun");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		try {
			if(schedule.standards.getStartOfTerm()==null||schedule.standards.getEndOfTerm()==null){
			x = Calendar.getInstance();
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//set the term
			try{
			standards.setTerm(sdf.parse("2013-01-01 00:00:0"),sdf.parse("2013-12-31 00:00:0"),false);
			this.createTable("xtemp", statDef);
			}catch(Exception e){e.printStackTrace();e.printStackTrace();}
			}
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
 	
 	}//onOpen-end

	
	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------	
	
	
	/**
	* The method is called when the specified database doesn't exist and is created for the first time. Please refer SQLiteOpenHelper for more details.
	* @param dbase The database object for this ScheduleDatabse.
	* @see SQLiteOpenHelper
	* @see SQLiteDatabase
	* @see Cursor
	*/	
	@Override
	public   void onCreate(SQLiteDatabase dbase) { //tested: pending
		this.setDbase(dbase);
		//this.dbase.execSQL("PRAGMA key = '';");
		//this.dbase.execSQL("PRAGMA rekey = '"+key+"';");
		//Tabel of Standards
			//Subject

			this.execQuery("create table pipeline (name varchar(20) primary key);");
			stringVector.clear();
			stringVector.add("name varchar primary key");
			//String[] subdef = {"name varchar primary key"};
			schedule.createTable("subject", stringVector);
			//Type
			stringVector.clear();
			stringVector.add("name varchar primary key");
			stringVector.add("mins number not null");
			//String[] typDef = {"name varchar primary key","mins number not null"};
			schedule.createTable("type", stringVector);
			//Session
			stringVector.clear();
			stringVector.add("subjname varchar");
			stringVector.add("typname varchar");
			stringVector.add("sessionID INTEGER primary key");
			stringVector.add("unique(subjname,typname)");
			stringVector.add("foreign key(subjname) references subject(name) ON UPDATE CASCADE");
			stringVector.add("foreign key(typname) references type(name) ON UPDATE CASCADE");
			//String[] sessionDef = {"subjname varchar","typname varchar","sessionID INTEGER primary key","unique(subjname,typname)","foreign key(subjname) references subject(name)","foreign key(typname) references type(name)"};
			schedule.createTable("session", stringVector);
			//Holi
			stringVector.clear();
			stringVector.add("datex datetime primary key");
			//String[] holiDef = {"datex datetime primary key"};
			schedule.createTable("holi", stringVector);
			//Term
			stringVector.clear();
			stringVector.add("start datetime unique not null");
			stringVector.add("end datetime unique");
			stringVector.add("check(start<end)");
			//String[] termDef = {"start datetime unique not null","end datetime unique","check(start<end)"};
			schedule.createTable("term", stringVector);
			
			//structureNamesTrackRecord
			stringVector.clear();
			stringVector.add("name varchar primary key");
			stringVector.add("countz number default 1");
			//String[] structureNamesDef = {"name varchar primary key","countz number default 1"};
			schedule.createTable("structureNames", stringVector);
			
			//Ranges
			schedule.createTable("ranges", rangesDef);
			
			//create indexes
			try{
			meta.createIndex("mon");
			meta.createIndex("tue");
			meta.createIndex("wed");
			meta.createIndex("thu");
			meta.createIndex("fri");
			meta.createIndex("sat");
			meta.createIndex("sun");
			}
			catch(Exception e){
				e.printStackTrace();
			}
			
			x = Calendar.getInstance();
			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//set the term
			try{
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.DAY_OF_MONTH, 1);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			
			Calendar cal2 = Calendar.getInstance();
			cal2.set(Calendar.MONTH, Calendar.DECEMBER);
			cal2.set(Calendar.DAY_OF_MONTH, 31);
			cal2.set(Calendar.HOUR_OF_DAY, 0);
			cal2.set(Calendar.MINUTE, 0);
			cal2.set(Calendar.SECOND, 0);
			standards.setTerm(cal.getTime(),cal2.getTime(),false);
			this.createTable("xtemp", statDef);
			
			
			}catch(Exception e){e.printStackTrace();}
	}//onCreate-end
	
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------	
	
	
	
	
	
	/**
	* Table Of Standards
	* The table includes standard methods to read and write standard data to the database
	* For eg. entities like Subject, Type etc. are standards that can be created, deleted and read from the database.
	* The class covers the following entities<br><br>
	* > Subject<br>
	* > Type<br>
	* > Term<br>
	* > Session<br>
	* > Holi<br>
	* @author Roger Cores
	* @version 1.0 | untested | prerealease
	*/
	public class TableOfStandards{ //tested OK
		//Subjects
		/**
		* Adds a subject to the database
		* @param name Subject name
		* @throws BunkerException
		* @throws SQLiteException
		* @see SQLiteOpenHelper
		* @see SQLiteDatabase
		* @see Cursor
		*/	
			public synchronized  void addSub(String name) throws BunkerException, SQLiteException{ //tested OK
				if(schedule.valueExists("name", "'"+name+"'", "subject")) throw new BunkerException("Subject already exists",context);
				
				stringVector.clear();
				stringVector.add("'"+name+"'");
				//String[] values = {"'"+name+"'"};
				schedule.insertIntoTable("subject", stringVector);
				cartesian();
					
			}//addSub-end
			
			//------------------------------------------------
			
			
			public synchronized void cartesian(){
				schedule.execQuery("insert into session (subjname, typname) select s.name as subjname, t.name as typname from subject s,type t except select a.subjname, a.typname from session a;");
				
			}
			/**
			* Deletes a subject from the database
			* @param name Subject name
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void deleteSub(String name) throws BunkerException, SQLiteException{ //tested OK
				if(!schedule.valueExists("name", "'"+name+"'", "subject")) throw new BunkerException("Subject doesnot exists",context);
				
				boolean deleted = false;
				try{
					schedule.deleteFromTable("session", "where subjname = '"+name+"'");
					deleted = true;
				}
				catch(Exception e){
					e.printStackTrace();
					cartesian();
					throw new BunkerException("Cannot delete this subject because it is used in schedules",context);
				}
				if(deleted)
					schedule.deleteFromTable("subject", "where name = '"+name+"'");
				
				
			}//deleteSub-end
			
			//------------------------------------------------
			/**
			* Renames a subject
			* @param oldname Old Subject name
			* @param newname New Subject name
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void renameSub(String oldname, String newname) throws BunkerException, SQLiteException{ //tested OK
				if(!schedule.valueExists("name", "'"+oldname+"'", "subject")) throw new BunkerException("Subject doesnot exists",context);
				//if(schedule.valueExists("subjname", oldname, "session")) throw new BunkerException("Subject is used in a session. Delete the session first.",context);
				if(schedule.valueExists("name", "'"+newname+"'", "subject")) throw new BunkerException("Subject with this name already exists",context);
				
				schedule.set("name", "'"+newname+"'", "subject", "where name = '"+oldname+"'");
				
				
				
			}//renameSub-end
			
			//------------------------------------------------
		
			
			
			
			
			
		//Types
			/**
			* Add a type to the database
			* @param name The type name.
			* @param mins Number of minutes at which it starts.
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void addTyp(String name, int mins) throws BunkerException, SQLiteException{ //tested OK
				if(schedule.valueExists("name", "'"+name+"'", "type")) throw new BunkerException("Type already exists",context);
				
				
				
				
				stringVector.clear();
				stringVector.add("'"+name+"'");
				stringVector.add(""+mins);
				//String[] values = {"'"+name+"'",""+mins};
				schedule.insertIntoTable("type", stringVector);
				cartesian();
				
			}//addTyp-end
			
			//------------------------------------------------
			
			
			/**
			* Delete a Typ from the database
			* @param name The name of the Type to be deleted
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void deleteTyp(String name) throws BunkerException, SQLiteException{ //tested OK
				if(!schedule.valueExists("name", "'"+name+"'", "type")) throw new BunkerException("Type doesnot exists",context);
				
				boolean deleted = false;
				try{
					schedule.deleteFromTable("session", "where typname = '"+name+"'");
					deleted = true;
				}
				catch(Exception e){
					e.printStackTrace();
					cartesian();
					throw new BunkerException("Cannot delete this Type because it is used in schedules",context);
				}
				if(deleted)
					schedule.deleteFromTable("type", "where name = '"+name+"'");
				
			}//deleteTyp-end
			
			//------------------------------------------------
			
			
			/**
			* Rename the Type to a new name
			* @param oldname The old name of the Type
			* @param newname The new name to which Type will be renamed
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void renameTyp(String oldname, String newname) throws BunkerException, SQLiteException{ //tested OK
				if(!schedule.valueExists("name", "'"+oldname+"'", "type")) throw new BunkerException("Subject doesnot exists",context);
				//if(schedule.valueExists("typname", oldname, "session")) throw new BunkerException("Type is used in a session. Delete the session first.",context);
				if(schedule.valueExists("name", "'"+newname+"'", "type")) throw new BunkerException("Type with this name already exists",context);
				
				schedule.set("name", "'"+newname+"'", "type", "where name = '"+oldname+"'");
				
			}//renameTyp-end
			
			//------------------------------------------------
			
			
			
		
		//Sessions
			/**
			* Create a new Session based on a Subject and a Type
			* @param SubName The Subject name with which this Session will be created
			* @param TypName The Type name with which this Session will be created.
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void createSession(String SubName, String TypName) throws BunkerException, SQLiteException{ //tested OK
				if(!schedule.valueExists("name", "'"+SubName+"'", "subject")) throw new BunkerException("Subject doesn't exist",context);
				if(!schedule.valueExists("name", "'"+TypName+"'", "type")) throw new BunkerException("Type doesn't exist",context);
				Cursor c = schedule.rawQuery("select subjname, typname from session where subjname = '"+SubName+"' and typname = '"+TypName+"'");
				if(c.moveToFirst()) throw new BunkerException("Session already exists",context);
				c.close();
				schedule.execQuery("insert into session(subjname,typname) values('"+SubName+"','"+TypName+"');");
				
				
			}//createSession-end
			
			//------------------------------------------------
			
			
			/**
			* Delete a Session
			* @param IDrel The ID of the Session which is to be deleted
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void deleteSession(int IDrel) throws BunkerException, SQLiteException{ //tested OK
				Cursor c = schedule.rawQuery("select IDrel from session where IDrel = "+IDrel);
				if(!c.moveToFirst()) {c.close(); throw new BunkerException("Session doesn't exists",context);}
				c.close();
				schedule.deleteFromTable("session", "where sessionID = "+IDrel);
				
			}//deleteSession-end
			
			
			//------------------------------------------------
			
			
			/**
			* Delete a Session
			* @param SubName The Subject name which participates in this Session
			* @param TypName The Type name which participates in this Session
			* @throws BunkerException
			* @throws SQLiteException
			* @see SQLiteOpenHelper
			* @see SQLiteDatabase
			* @see Cursor
			*/	
			public  synchronized  void deleteSession(String SubName, String TypName) throws BunkerException, SQLiteException{ //tested OK
				Cursor c = schedule.rawQuery("select subjname, typname from session where subjname = '"+SubName+"' and typname = '"+TypName+"'");
				if(!c.moveToFirst()) {c.close(); throw new BunkerException("Session doesn't exists",context);}
				c.close();
				schedule.deleteFromTable("session", "where subjname = '"+SubName+"' and typname = '"+TypName+"';");
				
			}//deleteSession-end
			
			
			//------------------------------------------------
			
			
			
		//Holidays
			/**
			* Marks a Date as Holiday<br>
			* The String parameters need to be in format yyyy-MM-dd HH:mm:ss
			* @param date The date which is to be marked as a holiday
			* @throws BunkerException
			* @throws SQLiteException
			* @see SimpleDateFormat
			*/	
			public  synchronized  void markHoliday(String date) throws BunkerException, SQLiteException{ //tested OK
				if(schedule.valueExists("datex", "'"+date+"'", "holi")) throw new BunkerException("This date is already marked as a holiday !",context);
				
				stringVector.clear();
	 			stringVector.add("'"+date+"'");
				//String[] values = {"'"+date+"'"};
				insertIntoTable("holi", stringVector);
				
			}//markHoliday-end
			
			
			//------------------------------------------------
			
			
			/**
			* Marks a Date as Holiday<br>
			* The String parameters need to be in Date format
			* @param date The date which is to be marked as a holiday
			* @throws BunkerException
			* @throws SQLiteException
			* @see Date
			* @see Calendar
			*/	
			public  synchronized  void markHoliday(Date date) throws BunkerException, SQLiteException{ //tested OK
				if(schedule.valueExists("datex", "'"+sdf.format(date)+"'", "holi")) throw new BunkerException("This date is already marked as a holiday !",context);
				
				stringVector.clear();
				stringVector.add("'"+sdf.format(date)+"'");
				//String[] values = {"'"+sdf.format(date)+"'"};
				schedule.insertIntoTable("holi", stringVector);
				
			}//markHoliday-end
			
			
			//------------------------------------------------
			
			
			/**
			* Unmarks a Date as Holiday.<br>
			* The String parameters need to be in Date format.
			* @param date The date which is to be marked as a holiday
			* @throws BunkerException
			* @throws SQLiteException
			* @see Date
			* @see Calendar
			* @deprecated Use blanks instead
			*/	
			public  synchronized  void unmarkHoliday(Date date) throws BunkerException, SQLiteException{ //tested OK
				if(!schedule.valueExists("datex", "'"+sdf.format(date)+"'", "holi")) throw new BunkerException("The date isn't marked as a holiday !",context);
				
				//String dates = sdf.format(date);
				schedule.deleteFromTable("holi", "where datex = Datetime('"+sdf.format(date)+"')");
				
			}//unmarkHoliday-end
			
			
			//------------------------------------------------
			
			
			/**
			* Unmarks a Date as Holiday.<br>
			* The String parameters need to be in in format yyyy-MM-dd HH:mm:ss
			* @param date The date which is to be marked as a holiday<br>
			* @throws BunkerException
			* @throws SQLiteException
			* @see SimpleDateFormat
			* @deprecated Use blanks instead
			*/	
			public  synchronized  void unmarkHoliday(String date) throws BunkerException, SQLiteException{ //tested OK
				if(!schedule.valueExists("datex", "'"+date+"'", "holi")) throw new BunkerException("The date isn't marked as a holiday !",context);
				
			     schedule.deleteFromTable("holi", "where datex = Datetime('"+date+"')");
				
			}//unmarkHoliday-end
			
			
			//------------------------------------------------
			
			
			
		
		//Term
			/**
			* Returns the start of the Term
			* <br>Returns a null if the term is not defined
			* @return Date The Start Date of the term set in this Term
			* @throws ParseException
			*/	
			public  synchronized  Date getStartOfTerm() throws ParseException{
				Cursor c = schedule.rawQuery("select start from term");
				if(c.moveToFirst()) {String temp = c.getString(0); c.close(); return sdf.parse(temp);}
				c.close();
				return null;
			}
			/**
			* Returns the end of the Term
			* <br>Returns a null if the term is not defined
			* @return The End Date of the term set in this Term
			* @throws ParseException
			*/	
			public  synchronized  Date getEndOfTerm() throws ParseException{
				Cursor c = schedule.rawQuery("select end from term");
				if(c.moveToFirst()) {
					Date end = sdf.parse(c.getString(0));
					c.close();
					return end;
					}
				c.close();
				return null;
			}
			
			/**
			* Sets the Start of the Term for this Schedule
			* @param start The Start Date of the term to be set
			* @param flag If true, setTerm will check any overwrite<br>
			* If false, setTerm will set the term will set the dates without any concerns
			* @throws ParseException
			* @throws BunkerException
			* @throws SQLiteException
			*/	
			public  synchronized  void setStartOfTerm(Date start, boolean flag) throws BunkerException, SQLiteException, ParseException{
				Cursor c;
				c = schedule.rawQuery("select end from term");
				if(c.moveToFirst()){
						Date end = sdf.parse(c.getString(0));
						schedule.standards.setTerm(start, end, flag);
				}
				c.close();
				
			}
			/**
			* Sets the End of the Term for this Schedule
			* @param end The End Date of the term to be set
			* @param flag If true, setTerm will check any overwrite<br>
			* If false, setTerm will set the term will set the dates without any concerns
			* @throws ParseException
			* @throws BunkerException
			* @throws SQLiteException
			*/
			public  synchronized  void setEndOfTerm(Date end, boolean flag) throws BunkerException, SQLiteException, ParseException{
				Cursor c;
				c = schedule.rawQuery("select start from term");
				if(c.moveToFirst()){
						Date start = sdf.parse(c.getString(0));
						schedule.standards.setTerm(start, end, flag);
				}
				c.close();
			}
			/**
			* Sets the Start and End of the Term for this Schedule
			* @param start The Start Date of the term to be set
			* @param end The end Date of the term to be set
			* @param flag If true, setTerm will check any overwrite<br>
			* If false, setTerm will set the term will set the dates without any concerns
			* @throws ParseException
			* @throws BunkerException
			* @throws SQLiteException
			*/
			public  synchronized  void setTerm(Date start, Date end, boolean flag) throws BunkerException, SQLiteException, ParseException{//flag:false = overide check //test:pending
				//{check if bounds are ok}
				Cursor c,c2;
				String wname, mname;
				Calendar x = Calendar.getInstance();
				Calendar x2 = Calendar.getInstance();
				int progress = 0;
				if(schedule.start!=null&&schedule.end!=null) if(start.equals(schedule.start)&&end.equals(schedule.end)) throw new BunkerException("Terms are already set",context);
				if(start.compareTo(end)>=0){ throw new BunkerException("end cannot be before start",context);}
				long diffDays = (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24);
				if(diffDays<=5){ throw new BunkerException("Terms has to be of atleast 1 week",context);}
				
				//schedule.dropTable(schedule.stats.selectRange(schedule.start, schedule.end));
				if(schedule.start!=null)
				schedule.stats.deleteCustomRange(schedule.stats.selectRange(schedule.start, schedule.end,false));
				//System.out.println("/ "+schedule.stats.selectRange(schedule.start, schedule.end));
				if(flag){
				/*
 				//detecting data loss
				for(int i = 0;i<7;i++){
					c = schedule.rawQuery("select min(datenm),max(datenm) from "+days[i]+"");
					
					if(c.moveToFirst()){
						
							c2 = schedule.rawQuery("select refDate from "+days[i]+" where datenm = '"+c.getString(0)+"'");
							if(c2.moveToFirst()) trial = c2.getString(0); else trial = "blank";
							if((((sdf.parse(c.getString(0)).before(start))&(!trial.equals("blank")))||sdf.parse(c.getString(1)).after(end)))
							throw new BunkerException("You migt lose some data mate!!!",context);
						
					}
					c.close();
				}
				 */
				}

				progress++;
				Intent intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				if(schedule.start!=null&&schedule.end!=null){
					if(start.compareTo(schedule.start)<0&&end.compareTo(schedule.start)<0){
						x.setTime(end);
						x.add(Calendar.DATE, 1);
						while(x.getTime().compareTo(schedule.end)<=0){
							schedule.meta.deleteRecord(x.getTime());
							x.add(Calendar.DATE, 1);
						}
						
					}
					else if(start.compareTo(schedule.end)>0&&end.compareTo(schedule.end)>0){
						x.setTime(schedule.start);
						while(x.getTime().compareTo(end)<=0){
							schedule.meta.deleteRecord(x.getTime());
							x.add(Calendar.DATE, 1);
						}
					}
					else{
						
						
					x.setTime(schedule.start);
					while(x.getTime().compareTo(start)<0){
						schedule.meta.deleteRecord(x.getTime());
						x.add(Calendar.DATE, 1);
					}
					
					
					
					x.setTime(end);
					x.add(Calendar.DATE, 1);
					while(x.getTime().compareTo(schedule.end)<=0){
						schedule.meta.deleteRecord(x.getTime());
						x.add(Calendar.DATE, 1);
					}
					
					
					}
				
				}
				progress++;
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				
				
				//delete end blanks to expand to new end
				if(schedule.end!=null){
				x2.setTime(schedule.end);
				x2.add(Calendar.DATE, 1);
				for(int i=0;i<7;i++){
					if(schedule.exists("select datenm from "+days[x2.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+sdf.format(x2.getTime())+"' and refDate = 'blank'"))
						schedule.execQuery("delete from "+days[x2.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+sdf.format(x2.getTime())+"' and refDate = 'blank'");
					x2.add(Calendar.DATE, 1);
				}
				}
				progress++;
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				if(schedule.end!=null){
				x2.setTime(schedule.end);
				while(x2.getTime().compareTo(end)<=0)
				{
					wname = schedule.stats.selectWeekly(x2.getTime());
					ScheduleDatabase.this.stats.addToPipeline(wname);
					mname = schedule.stats.selectMonthly(x2.getTime());
					ScheduleDatabase.this.stats.addToPipeline(mname);
					x2.add(Calendar.DATE, 7);
				}
				progress++;
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				x2.setTime(end);
				while(x2.getTime().compareTo(schedule.end)<=0)
				{
					wname = schedule.stats.selectWeekly(x2.getTime());
					ScheduleDatabase.this.stats.addToPipeline(wname);
					mname = schedule.stats.selectMonthly(x2.getTime());
					ScheduleDatabase.this.stats.addToPipeline(mname);
					x2.add(Calendar.DATE, 7);
				}
				progress++;
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				}
				if(schedule.start!=null){
					x2.setTime(schedule.start);
					while(x2.getTime().compareTo(start)<=0)
					{
						wname = schedule.stats.selectWeekly(x2.getTime());
						ScheduleDatabase.this.stats.addToPipeline(wname);
						mname = schedule.stats.selectMonthly(x2.getTime());
						ScheduleDatabase.this.stats.addToPipeline(mname);
						x2.add(Calendar.DATE, 7);
					}
					x2.setTime(start);
					while(x2.getTime().compareTo(schedule.start)<=0)
					{
						wname = schedule.stats.selectWeekly(x2.getTime());
						ScheduleDatabase.this.stats.addToPipeline(wname);
						mname = schedule.stats.selectMonthly(x2.getTime());
						ScheduleDatabase.this.stats.addToPipeline(mname);
						x2.add(Calendar.DATE, 7);
					}
					
					}

				progress++;
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				schedule.deleteFromTable("term", "");
				//if(!trial.equals("")) return trial;
				stringVector.clear();
				stringVector.add("'"+sdf.format(start)+"'");
				stringVector.add("'"+sdf.format(end)+"'");
				//String[] values = {"'"+sdf.format(start)+"'","'"+sdf.format(end)+"'"};
				schedule.insertIntoTable("term", stringVector);
				//if(!trial.equals("")) return trial;
				
				progress++;
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				
				
				
				
				
				//String temp;
				//Calendar x2 = Calendar.getInstance();
				//x2.setTime(end);
				//x2.add(Calendar.DATE, 1);
				for(int i=0;i<7;i++){
					
					
					
					x.setTime(start);
					//>insert default blanks or previous
					//if start empty
						//if min before start
							//copy min to start
						//else
							//insert blank
					//Toast.makeText(context, ""+(x.getTime().toString()), Toast.LENGTH_LONG).show();
					c = schedule.rawQuery("select * from "+days[x.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+sdf.format(x.getTime())+"'");
					if(!c.moveToFirst()){//start doesn't exist
						c.close();
						//c = schedule.rawQuery("select min(datenm) from "+days[x.get(Calendar.DAY_OF_WEEK)-1]+"");
						c = schedule.rawQuery("select refDate from "+days[x.get(Calendar.DAY_OF_WEEK)-1]+" where datenm < '"+sdf.format(x.getTime())+"' order by datenm desc");
						if(c.moveToFirst()){
							
								//schedule.meta.insertIntoIndex(start, c.getString(0), 0);
								if(!c.getString(0).equals("blank"))
								schedule.meta.nudgeStructureNames(c.getString(0), true);
								stringVector.clear();
								stringVector.add("'"+sdf.format(x.getTime())+"'");
								stringVector.add("'"+c.getString(0)+"'");
								//String[] valuesx = {"'"+sdf.format(x.getTime())+"'","'"+c.getString(0)+"'"};
								schedule.insertIntoTable(days[x.get(Calendar.DAY_OF_WEEK)-1], stringVector);
							
								
							
						}else{//insert blank
							//System.out.println("smallers do not exist else");
							schedule.meta.insertBlankIntoIndex(x.getTime(), 0);
							//schedule.meta.insertBlankIntoIndex(start, days[x.get(Calendar.DAY_OF_WEEK)-1]);
							//String[] valuesx = {"'"+sdf.format(x.getTime())+"'","'blank'"};
							//schedule.insertIntoTable(days[x.get(Calendar.DAY_OF_WEEK)-1], valuesx);
						}
						c.close();
					}
					c.close();
					
					//>nudge relevant structureNames...
					c = schedule.rawQuery("select datenm, refDate from "+days[x.get(Calendar.DAY_OF_WEEK)-1]+" where datenm < '"+sdf.format(x.getTime())+"' or datenm > '"+sdf.format(end)+"'");
					if(c.moveToFirst()){
						do{
							if(!c.getString(1).equals("blank")){
							//schedule.meta.deleteRecord(sdf.parse(c.getString(0)));
							schedule.meta.nudgeStructureNames(c.getString(1), false);
							}
							
							wname = schedule.stats.selectWeekly(sdf.parse(c.getString(0)));
							ScheduleDatabase.this.stats.addToPipeline(wname);
							mname = schedule.stats.selectMonthly(sdf.parse(c.getString(0)));
							ScheduleDatabase.this.stats.addToPipeline(mname);
							
							c2 = schedule.rawQuery("select rname from ranges where start <= '"+sdf.format(sdf.parse(c.getString(0)))+"' and end >= '"+sdf.format(sdf.parse(c.getString(0)))+"' and rname not like '%fixed%'");
							if(c2.moveToFirst()){
								do{
									//System.out.println(c2.getString(0));
									
									ScheduleDatabase.this.stats.addToPipeline(c2.getString(0));
								}
								while(c2.moveToNext()); 
							}
							c2.close();

							
							
							
							
						}while(c.moveToNext());
					}
					c.close();
					
					//delete all outers from indexes
					schedule.deleteFromTable(days[x.get(Calendar.DAY_OF_WEEK)-1], "where datenm < '"+sdf.format(x.getTime())+"' or datenm > '"+sdf.format(end)+"'");
					//schedule.deleteFromTable(days[x2.get(Calendar.DAY_OF_WEEK)-1], "where datenm = '"+end+"'");
					//schedule.execQuery("insert into "+days[x2.get(Calendar.DAY_OF_WEEK)-1]+" values('"+sdf.format(x2.getTime())+"','blank')");
					//increment date
					//x.setTime(start);
					x.add(Calendar.DATE, 1);
					//x2.add(Calendar.DATE, 1);
					//end = x2.getTime();
					//start = x.getTime();
					
					progress++;
					 intentUpdate = new Intent();
					intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
					intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
					intentUpdate.putExtra("perc", (int)progress);
					ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				}
				//insert blanks at ends
					x2.setTime(end);
					x2.add(Calendar.DATE, 1);
					//Calendar xstart = Calendar.getInstance();
					//xstart.setTime(start);
					for(int i=0;i<7;i++){
						//schedule.execQuery("delete from "+days[x2.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+sdf.format(x2.getTime())+"' and refDate = 'blank'");
						if(!schedule.exists("select datenm from "+days[x2.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+sdf.format(x2.getTime())+"'"))
							schedule.execQuery("insert into "+days[x2.get(Calendar.DAY_OF_WEEK)-1]+" values('"+sdf.format(x2.getTime())+"','blank')");
						
						
						x2.add(Calendar.DATE, 1);
						//xstart.add(Calendar.DATE, 1);
					}
				
				progress++;
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				schedule.start = start;
				schedule.end = end;
				stats.createCustomRange(start, end, false);
				stats.addToPipeline(stats.getOverallRange());
				progress++;
				
				 intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				intentUpdate.putExtra("perc", (int)progress);
				ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
				System.out.println("toundxer :"+progress); 
				
				
				
				if(!flag){
					Calendar caltrav = Calendar.getInstance();
					caltrav.setTime(schedule.start);
					for(int i=0; i<7; i++){
						if(!schedule.exists("select datenm from "+days[caltrav.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+sdf.format(caltrav.getTime())+"'"))
							schedule.execQuery("insert into "+days[caltrav.get(Calendar.DAY_OF_WEEK)-1]+" values('"+sdf.format(caltrav.getTime())+"','blank')");
						
						caltrav.add(Calendar.DATE, 1);
					}
				}
				
			}//setTerm-end

	}//TableOfStandards-end
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------	
	
	/**
	* Meta
	* The class covers all standard methods to manipulate Meta Data related schema of the database
	* <br>The class defines the following Entities of a Schedule
	* <br><br>
	* > Indexes<br>
	* > Structures<br>
	* > Records<br>
	* > Tags<br>
	* > Albums<br>
	* > Files<br>
	* @author Roger Cores
	* @version 1.0 | untested | prerealease
	*/
	public class Meta{
			//Integer
			public final static int ABSENT = 0;
			public final static int PRESENT = 1;
			public final static int NOT_TOUCHED = 2;
			public final static int DONT_CARE = 3;
		
		
		
		
		
		
			//Methods
			//Indexes(dateName) //completed - test: pending
			/**
			* Creates an Index.<br>
			* Each schedule comprise basically consists of max 7 indexes.<br>
			* <br>
			* > Sunday<br>
			* > Monday<br>
			* > Tuesday<br>
			* > Wednesday<br>
			* > Thursday<br>
			* > Friday<br>
			* > Saturday<br><br>
			* @param name The name of the index to be created.<br>
			* the string value ranges from 'sun' to 'mon'.
			* @throws BunkerException
			* @throws SQLiteException
			*/
			public  synchronized  void createIndex(String name) throws BunkerException, SQLiteException{ // tested
				
				schedule.createTable(name, indexDef);
				
			}//createIndex-end
			
			//------------------------------------------------
			/**
			* Count weeks between start and end
			* @param start The start from which weeks should be counted
			* @param end The end until which weeks should be counted
			 * @throws BunkerException 
			*/
			public   int countWeeks(Date start, Date end) throws BunkerException{
				if(end.compareTo(start)<0) throw new BunkerException("start should be before end",context);
				Calendar x = Calendar.getInstance(), x1 = Calendar.getInstance();
				//System.out.println("start : "+start+" end : "+end);
				x.setTime(end);
				x1.setTime(start);
				while(x.get(Calendar.DAY_OF_WEEK)!=x1.get(Calendar.DAY_OF_WEEK)){
					x.add(Calendar.DATE, -1);
				}
				long diffDays = (x.getTime().getTime() - x1.getTime().getTime()) / (1000 * 60 * 60 * 24);
				return ((int) (diffDays+1)/7);
			}
			/**
			* Insert structure into a specific index for days starting at a date
			* <br>
			* structure can be added for max 7 days
			* @param date The date at which insertion should start
			* @param structureName The Structure Name that should be inserted
			* @param days The number of days it should be inserted for
			* @throws BunkerException
			* @throws SQLiteException
			* @throws ParseException
			*/
			public  synchronized  void insertIntoIndexForDays(Date date,String structureName, int days) throws BunkerException, SQLiteException, ParseException{
				Calendar x;
				Cursor c;
				if(days>6){
				
					throw new BunkerException("only 6 days allowed",context);
				}
				
				x = Calendar.getInstance();
				x.setTime(date);
				String wname = schedule.stats.selectWeekly(x.getTime()), mname = schedule.stats.selectMonthly(x.getTime());
				
				for(int i=0;i<days;i++){
					insertIntoIndex(x.getTime(),structureName,0, false);
						wname = schedule.stats.selectWeekly(x.getTime());
						ScheduleDatabase.this.stats.addToPipeline(wname);
						mname = schedule.stats.selectMonthly(x.getTime());
						ScheduleDatabase.this.stats.addToPipeline(mname);
						
						c = schedule.rawQuery("select rname from ranges where start <= '"+sdf.format(x.getTime())+"' and end >= '"+sdf.format(x.getTime())+"' and name not like '%fixed%'");
						if(c.moveToFirst()){
							do{
								ScheduleDatabase.this.stats.addToPipeline(c.getString(0));
							}
							while(c.moveToNext()); 
						}
						c.close();
						x.add(Calendar.DATE, 1);
				}
				
				
			}//insertIntoIndexForDays-end
			
			//------------------------------------------------
			
			/**
			* Insert blank into a specific index for days starting at a date
			* <br>
			* structure can be added for max 7 days
			* @param date The date at which insertion should start
			* @param days The number of days it should be inserted for
			* @throws BunkerException
			* @throws SQLiteException
			* @throws ParseException
			*/
			public  synchronized  void insertBlankIntoIndexForDays(Date date, int days) throws BunkerException, SQLiteException, ParseException{
				insertIntoIndexForDays(date,"blank",days);
			}//insertBlankIntoIndexForDays-end
			
			//------------------------------------------------
			
			/**
			* Insert a blank into index for specified weeks
			* @param date The date at which insertion should start
			* @param weeks Number of weeks blank should span for
			* @throws BunkerException
			* @throws SQLiteException
			* @throws ParseException
			*/
			public  synchronized  void insertBlankIntoIndex(Date date, int weeks) throws BunkerException, SQLiteException, ParseException{ // tested
				insertIntoIndex(date,"blank",weeks, true);
			}//insertBlankIntoIndex-end
			
			
			//------------------------------------------------
			/**
			* Insert a blank into a specific date and index<br>
			* The methods should not be used, Its was build for setTerm<br>
			* This is becuase it doesn't nudge the replaced structureName
			* @param date The date at which insertion should start
			* @param daysx The index name
			* @throws BunkerException
			* @throws SQLiteException
			*/
			public  synchronized  void insertBlankIntoIndex(Date date,String daysx) throws BunkerException, SQLiteException{
				Cursor c;
				//extract string format from date
				String dates = sdf.format(date);
				//check if theres blank after this date, if yes, delete it
				c = schedule.rawQuery("select datenm, refDate from "+daysx+" where datenm > '"+sdf.format(date)+"' order by datenm");
				//Toast.makeText(context, (daysx+ " " + sdf.format(date)), Toast.LENGTH_LONG).show();
				if(c.moveToFirst()){
					if(c.getString(1).equals("blank")) {
						schedule.deleteFromTable(daysx, "where datenm = '"+c.getString(0)+"'");
					}
				}
				c.close();
				//insert blank
				stringVector.clear();
				stringVector.add("'"+dates+"'");
				stringVector.add("'"+"blank"+"'");
				//String[] valuesx = {"'"+dates+"'","'"+"blank"+"'"};
				schedule.insertIntoTable(days[x.get(Calendar.DAY_OF_WEEK)-1], stringVector);
				
			}
			
			/**
			* This method is the foundation of Indexes module<br>
			* It inserts a structure to a Index
			* @param date The date at which insertion should start
			* @param structureName The structure name
			* @param refreshLogic It decides if insertion should perform a validation of stats.<br>
			* usually the value should be 'true'
			* @throws BunkerException
			* @throws SQLiteException
			* @throws ParseException
			*/
			public  synchronized  String insertIntoIndex(Date date, String structureName, int weeks, boolean refreshLogic) throws BunkerException, SQLiteException, ParseException{ // tested
				Cursor c;
				Calendar x;
				//init Variables
				String dates = sdf.format(date);
				String backName = null;
				String name;
				x = Calendar.getInstance();
				x.setTime(date);
				name = days[x.get(Calendar.DAY_OF_WEEK)-1];
				//add weeks
				x.add(Calendar.DATE, 7*weeks);
				Date right = x.getTime();
				x.setTime(right);
				//add one week
				x.add(Calendar.DATE, 7);
				Date rightNext = x.getTime();
				Date start,end;
				float progress = 0;
				float total = (weeks*2)+2;
				c = schedule.rawQuery("select * from term");
				if(c.moveToFirst()){
					
						start = sdf.parse(c.getString(0));
						end = sdf.parse(c.getString(1));
						if(date.before(start)||right.after(end)){
							//System.out.println("date : "+date);
							//System.out.println("start : "+start);
							throw new BunkerException("MAte, you got outta terms....",context);
						}
						if(countWeeks(date,standards.getEndOfTerm())<weeks){ throw new BunkerException("MAte, you got outta terms....",context); }
					
				}
				c.close();
				
				c = schedule.rawQuery("select refDate from "+name+" where datenm <= '"+sdf.format(date)+"' order by datenm desc");
				if(c.moveToFirst()){
					backName = c.getString(0);
				}
				c.close();
				
				
				if(backName!=null)
					if(backName.equals("blank")&&structureName.equals("blank")){
						
							//throw new BunkerException("Its already blank mate!!!",context);
						
					}
				
				
				backName = null;
				c = schedule.rawQuery("select refDate from "+name+" where datenm <= '"+sdf.format(right)+"' order by datenm desc");
				if(c.moveToFirst()){
					backName = c.getString(0);
				}
				c.close();
				
				
				
				
				
				//create structure
				if(!structureName.equals("blank")){
				schedule.createTable("labsdecore"+structureName, structureDef);
				schedule.createTrigger(triggerDefInsert, "labsdecore"+structureName,"i");
				schedule.createTrigger(triggerDefUpdate, "labsdecore"+structureName,"u");
				schedule.createTrigger(triggerDefInsert1, "labsdecore"+structureName,"i");
				schedule.createTrigger(triggerDefUpdate1, "labsdecore"+structureName,"u");
				
				
				x.setTime(date);
				while(x.getTime().compareTo(right)<=0){
					meta.deleteRecord(x.getTime());
					meta.createRecord(x.getTime(),"labsdecore"+structureName);
					System.out.println("toundxer "+(progress/total*100));

					Intent intentUpdate = new Intent();
					intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
					intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
					intentUpdate.putExtra("perc", (int)(progress/total*100));
					ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
					progress++;
					
					x.add(Calendar.DATE, 7); 
				}
				
				}
				else{
					x.setTime(date);
					while(x.getTime().compareTo(right)<=0){
						meta.deleteRecord(x.getTime());

						Intent intentUpdate = new Intent();
						intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
						intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
						intentUpdate.putExtra("perc", (int)(progress/total*100));
						ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
						System.out.println("toundxer "+(progress/total*100));
						progress++;
						x.add(Calendar.DATE, 7);
					}
				}
				
				System.out.println("toundxer count"+weeks);
				
				
				//update last if not exists and backName is not null
				c = schedule.rawQuery("select refDate from "+name+" where datenm = '"+sdf.format(rightNext)+"'");
				if(!c.moveToFirst()&&backName!=null){
					System.out.println("In Here >  backName "+backName+" ");
					//schedule.deleteFromTable(name, "where datenm = '"+sdf.format(rightNext)+"'");	//delete none
					stringVector.clear();
					stringVector.add("'"+sdf.format(rightNext)+"'");
					stringVector.add("'"+backName+"'");
					//String[] values = {"'"+sdf.format(rightNext)+"'","'"+backName+"'"};
					
					schedule.insertIntoTable(name, stringVector);											//insert one
					//if(!trial.equals("")) return trial;
					
					if(!backName.equals("blank")){
					nudgeStructureNames(backName,true);
					//if(!trial.equals("")) return trial;
					}
				}
				c.close();
				//nudge the structure name
				c = schedule.rawQuery("select refDate from "+name+" where datenm = '"+dates+"'");
				if(c.moveToFirst()){
					if(!c.getString(0).equals("blank")){
					nudgeStructureNames(c.getString(0),false);
					//if(!trial.equals("")) return trial;
					}
				}
				c.close();
				//delete if exists and replace with new
				schedule.deleteFromTable(name, "where datenm = '"+dates+"'");	//delete one//-
				//if(!trial.equals("")) return trial;
				stringVector.clear();
				stringVector.add("'"+dates+"'");
				stringVector.add("'"+structureName+"'");
				//String[] values = {"'"+dates+"'","'"+structureName+"'"};
				schedule.insertIntoTable(name, stringVector);							//insert one//-
				//if(!trial.equals("")) return trial;
				//nudge the structure name
				if(!structureName.equals("blank")){
				nudgeStructureNames(structureName,true);
				//if(!trial.equals("")) return trial;
				}
				
				c = schedule.rawQuery("select refDate from "+name+" where datenm > '"+dates+"' and datenm <= '"+sdf.format(right)+"'");
				if(c.moveToFirst()){
					//nudge structure names
					do{
						if(!c.getString(0).equals("blank")){
						nudgeStructureNames(c.getString(0),false);
						//if(!trial.equals("")) return trial;
						}
					}while(c.moveToNext());
				}
				c.close();
				//delete intermidiate
				schedule.deleteFromTable(name, "where datenm > '"+dates+"' and datenm <= '"+sdf.format(right)+"'");	//delete many
				if(refreshLogic){
				x.setTime(date);
				String wname = "",mname = "";
				while(x.getTime().compareTo(rightNext)<=0){ 
						System.out.println("toundxer "+(progress/total*100));
						Intent intentUpdate = new Intent();
						intentUpdate.setAction("com.vosaye.bunkr.UPDATESTR");
						intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
						intentUpdate.putExtra("perc", (int)(progress/total*100));
						ScheduleDatabase.this.context.sendBroadcast(intentUpdate);
						progress++;
						wname = schedule.stats.selectWeekly(x.getTime());
						ScheduleDatabase.this.stats.addToPipeline(wname);
						mname = schedule.stats.selectMonthly(x.getTime());
						ScheduleDatabase.this.stats.addToPipeline(mname);
						
 						c = schedule.rawQuery("select rname from ranges where start <= '"+sdf.format(x.getTime())+"' and end >= '"+sdf.format(x.getTime())+"' and rname not like '%fixed%'");
						if(c.moveToFirst()){
							do{

								ScheduleDatabase.this.stats.addToPipeline(c.getString(0));
							} 
							while(c.moveToNext()); 
						}
						c.close();

						x.add(Calendar.DATE, 7);
				}
				}
				return "labsdecore"+structureName; 
				
			}//insertIntoIndex-end
			
			//------------------------------------------------
			
			
			

			/**
			* Nudge is required to track structureNames
			* @param name the structure name that is to be nudged
			* @param insdel true = insert and false = delete
			* @throws BunkerException
			* @throws SQLiteException
			*/
			public  synchronized  void nudgeStructureNames(String name, boolean insdel) throws BunkerException, SQLiteException{ //Garbage Collector for Extra StructureNames
				boolean flag = false;
				Cursor c = schedule.rawQuery("select name from structureNames where name = '"+name+"'");
				if(c.moveToFirst()){
					flag = true;
				}
				c.close();
				if(insdel){
					if(flag){
						//incr
						//update structureNames set countz = ((select countz from structureNames where name = 'anystring')+1) where name = 'anystring';
						
						schedule.execQuery("update structureNames set countz = ((select countz from structureNames where name = '"+name+"')+1) where name = '"+name+"'");
						
					}
					else{
						//insert default 1
						stringVector.clear();
						stringVector.add("'"+name+"'");
						stringVector.add("1");
						//String[] values = {"'"+name+"'","1"};
						schedule.insertIntoTable("structureNames", stringVector);
						//if(!trial.equals("")) return trial;
					}
				}
				else{
					if(flag){
						//decr, if 0 delete
						
						schedule.execQuery("update structureNames set countz = ((select countz from structureNames where name = '"+name+"')-1) where name = '"+name+"'");
						
						c = schedule.rawQuery("select countz from structureNames where name = '"+name+"'");
						if(c.moveToFirst()){
							if(c.getInt(0)==0){
								schedule.deleteFromTable("structureNames", "where name = '"+name+"'");
								//if(!trial.equals("")) {return trial;}
								schedule.dropTable("labsdecore"+name);
								//if(!trial.equals("")) {return trial;}
							}
						}
						c.close();
						
						
					}
					else{
						//decr //not possible
						System.out.println("anarth ho gaya be...");
						//Toast.makeText(context, "anarth ho gaya be...", Toast.LENGTH_LONG).show();
					}
				}
				
				
			}//nudgeStructure-end
			
			
			
			//------------------------------------------------
			
			
			
			/**
			* Selects structure nam at a perticular date
			* @param date The date from which structure name should be extracted.
			* @throws BunkerException
			* @throws SQLiteException
			*/
			public  synchronized String selectFromIndex(Date date) throws BunkerException, SQLiteException{ //tested OK
				String name;
				Calendar x = Calendar.getInstance();
				x.setTime(date);
				name = days[x.get(Calendar.DAY_OF_WEEK)-1];
				Cursor c = schedule.rawQuery("select refDate from "+name+" where datenm <= '"+sdf.format(date)+"' order by datenm desc");
				if(c.moveToFirst()){
					String temp = c.getString(0);
					c.close();
					return "labsdecore"+(temp);
				}
				c.close();
				return "labsdecoreblank";
			}//selectFromIndex-end
			
			
			//------------------------------------------------
			
			
			/*
 			//untested....
			/**
			* The method checks redundancy at a date in an index
			* @param date The date at which redundancy should be checked
			* @throws BunkerException
			* @throws SQLiteException
			
			public void checkRedundancy(Date date) throws SQLiteException, BunkerException{
				Cursor c;
				String refDate = "";
				x.setTime(date);
				c = rawQuery("select refDate from "+days[x.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+sdf.format(date)+"'");
				if(c.moveToFirst()){
					refDate = c.getString(0);
					c.close();
					c = rawQuery("select datenm, refDate from "+days[x.get(Calendar.DAY_OF_WEEK)-1]+" where datenm > '"+sdf.format(date)+"'");
					if(c.moveToFirst()){
						if(refDate.equals(c.getString(1))){
							schedule.meta.nudgeStructureNames(refDate, false);
							deleteFromTable(days[x.get(Calendar.DAY_OF_WEEK)-1], "where datenm = '"+c.getString(0)+"'");
						}
					}
					c.close();
					c= rawQuery("select datenm, refDate from "+days[x.get(Calendar.DAY_OF_WEEK)-1]+" where datenm < '"+sdf.format(date)+"'");
					if(c.moveToFirst()){
						if(refDate.equals(c.getString(1))){
							schedule.meta.nudgeStructureNames(refDate, false);
							deleteFromTable(days[x.get(Calendar.DAY_OF_WEEK)-1],"where datenm = '"+sdf.format(date)+"'");
						}
					}
				}
			}
			 */
			
		
		
			//DayStructure(mins,IDrel,mindur)
			/**
			* Creates a pseudostructure<br>
			* These structures are used for temporary transactions.
			* @return the name of the pseudostructure name that was created.
			* @throws SQLiteException
			*/
			public  synchronized  String createPseudoStructure() throws SQLiteException{// tested
				
				String structure = this.getUUID("pseudo");
				schedule.createTable(structure, pseudoStructureDef);

				createTrigger(triggerDefInsert, structure,"i");
				createTrigger(triggerDefUpdate, structure,"u");
				createTrigger(triggerDefInsert1, structure,"i");
				createTrigger(triggerDefUpdate1, structure,"u");
				return structure;
			}
			/**Generates a unique table name with the specified prefix.
			 * The prefix could be 'labsdecore' or 'pseudo'
			 * @param prefix
			 * @return
			 */
			public  synchronized  String getUUID(String prefix){
				String uuid = "";
				do{
					uuid = UUID.randomUUID().toString().replaceAll("-", "");
				}
				while(schedule.tableExists(prefix+uuid));
				return prefix+uuid;
			}
			/**
			* Deletes a structure.
			* @param date The structures will be deleted at this date
			* @throws SQLiteException
			 * @throws BunkerException 
			*/
			public  synchronized  void deleteStructure(String name) throws SQLiteException, BunkerException{// tested
				if(!schedule.tableExists(name)) throw new BunkerException("Structure doesn't exist",context);
				schedule.dropTable(name);
			}
			
			//structure methods
			/*
 			public void insertIntoStructure(String name, int mins, int IDrel, int duration) throws SQLiteException{// tested
				String[] values = {""+mins,""+IDrel,""+duration};
				schedule.insertIntoTable(name, values);
			}
*/
			/**
			* Deletes a session from a structure
			* @param name The structure name.
			* @param mins Mins at which a structure should be deleted.
			* @throws SQLiteException
			 * @throws BunkerException 
			*/
			public  synchronized  void deleteFromStructure(String name, int mins) throws SQLiteException, BunkerException{// tested
				if(!schedule.tableExists(name)) throw new BunkerException("The structure doesn't exist",context);
				if(!schedule.valueExists("mins", mins+"", name)) throw new BunkerException("The value to be deleted doesn't exist",context);
				schedule.deleteFromTable(name, "where mins = "+mins);
			}
			/**
			* Updates a structure.<br>
			* It relocates a session within a structure.
			* @param name The structure name.
			* @param omins Old mins
			* @param mins new mins
			* @param IDrel The Id of the session to be relocated
			* @param duration The duration to be set
			* @throws SQLiteException
			 * @throws BunkerException 
			*/
			public  synchronized  void updateStructure(String name,int omins, int mins, int IDrel, int duration) throws SQLiteException, BunkerException{// tested
				if(!schedule.tableExists(name)) throw new BunkerException("The structure doesn't exist",context);
				if(!schedule.valueExists("mins", omins+"", name)) throw new BunkerException("The value to be updated doesn't exist",context);
				
				schedule.execQuery("update "+name+" set mins = "+mins+", IDrel = "+IDrel+", duration = "+duration+" where mins = "+omins+"");
			}
			
			//pseudo structure methods
			/**
			* Add a session to pseudo structure
			* @param name the pseudo structure name.
			* @param mins mins at which it should be inserted.
			* @param IDrel Session Id
			* @param duration the duration of this session
			* @throws SQLiteException
			 * @throws BunkerException 
			*/
			public  synchronized  void insertIntoPseudoStructure(String name, int mins, int IDrel, int duration) throws SQLiteException, BunkerException{
				if(!schedule.tableExists(name)) throw new BunkerException("The structure doesn't exist",context);
				if(schedule.valueExists("mins", mins+"", name)) throw new BunkerException("The value mins already exists",context);
				stringVector.clear();
				stringVector.add(""+mins);
				stringVector.add(""+IDrel);
				stringVector.add(""+duration);
				stringVector.add(""+2);
				//String[] values = {""+mins,""+IDrel,""+duration,""+0};
				schedule.insertIntoTable(name, stringVector);
			}
			/**
			* Copies structure and records to pseudo structure<br>
			* This new pseudostructre is used for temporary transactions.
			* @param name the pseudo structure name.
			* @param sname the structure name.
			* @param date Date at from which structure and records should be copied.
			 * @throws BunkerException 
			*/
			public  synchronized  void copyToPseudoStructure(String name, String sname, Date date) throws BunkerException{
				if(!schedule.tableExists(name)||!schedule.tableExists(sname)) throw new BunkerException("Structure , Record or both do not exist in the database",context);
				//insert into name select s.mins, s.IDrel, s.duration, r.attendance from sname s, rname r
				if(sname.equals("labsdecoreblank")) return;
				//String dates = sdf.format(date);
				String rname = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				
				String query = "insert into "+name+" select s.mins, s.IDrel, s.duration, r.attendance from "+sname+" s, "+rname+" r where s.mins = r.mins";
				execQuery(query);
			}
			//select s.mins, s.IDrel, s.duration, r.attendance from labsdecore394a93b2305e4992afa336d08c85240d s, record20130101000000 r where s.mins = r.mins;
			//copy structure
			
			
			/**
			 * copy structure without records
			 * @param source The source from which structure should be copied
			 * @param destination The destination at which it should be copied
			 * @throws SQLiteException
			 * @throws BunkerException 
			 */
			public  synchronized  void copyStructure(String source, String destination) throws SQLiteException, BunkerException{
				if(!schedule.tableExists(source)||!schedule.tableExists(destination)) throw new BunkerException("Source , destination or both structure do not exist",context);
				schedule.execQuery("insert into "+destination+" select mins, IDrel, duration from "+source);
				
			}
			
			/**
			 * validates the whole pipeline and clears the pipeline. This method was found unacceptable and was deprecated. Use the table 'pipeline' with Validator Service.
			 * @throws BunkerException
			 * @throws SQLiteException
			 * @throws ParseException
			 * @deprecated 
			 */
			public  synchronized  void validatePipeline() throws BunkerException, SQLiteException, ParseException{
				if(validatePipeline.isEmpty()) return;
				int i = 0;
				while(i<validatePipeline.size()){
					if(validatePipeline.elementAt(i).contains("week")){
						schedule.stats.validateWeekly(validatePipeline.elementAt(i));
						validatePipeline.remove(i);
					}
					else{
						i++;
					}
				}
				i = 0;
				while(i<validatePipeline.size()){
					if(validatePipeline.elementAt(i).contains("month")){
						schedule.stats.validateMonthly(validatePipeline.elementAt(i));
						validatePipeline.remove(i);
					}
					else{
						i++;
					}
				}
				/*
 i = 0;
				while(i<validatePipeline.size()){
					if(validatePipeline.elementAt(i).contains("range")&&!validatePipeline.elementAt(i).contains("fixed")){
						//schedule.stats.validateRange(validatePipeline.elementAt(i));
						validatePipeline.remove(i);
					}
					else{
						i++;
					}
				}
*/
				validatePipeline.clear();
				//schedule.stats.validateOverall();
			}
			//create pseudo
			//load joined structure and record data from specified day
			//set events (insert, update, delete, modify structure)
			//copy structure and record back
			
			
		
			//DayRecord(mins,attendance)
			/** creates a record
			 * @param date
			 * @param targetTableName
			 * @return
			 * @throws SQLiteException
			 * @throws BunkerException 
			 */
			public synchronized  String createRecord(Date date, String targetTableName) throws SQLiteException, BunkerException{
				if(!schedule.tableExists(targetTableName)) throw new BunkerException("Target table name doesn't exists",context);
				//String dates = sdf.format(date);
				String name = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				String[] recdef = new String[3];
				recdef = recordDef.clone();	
				recdef[2] = recdef[2].replaceAll("/", targetTableName);
				schedule.createTable(name, recdef); 
				return name;
			}
			/** selects a record
			 * @param date
			 * @return
			 */
			public  synchronized String selectRecord(Date date){
				//String dates = sdf.format(date);
				//String name = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				return "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
			}
			/** deletes a record
			 * @param date
			 * @throws BunkerException 
			 */
			public synchronized  void deleteRecord(Date date){
				//String dates = sdf.format(date);
				String name = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				//if(!schedule.tableExists(name)) throw new BunkerException("Record doesn't exist",context);
				schedule.dropTable(name);
			}
			/** deletes all tuples from a record
			 * @param date
			 * @throws SQLiteException
			 * @throws BunkerException 
			 */
			public synchronized  void flushRecord(Date date) throws SQLiteException, BunkerException{
				//String dates = sdf.format(date);
				String name = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				if(!schedule.tableExists(name)) throw new BunkerException("Record doesn't exist",context);
				schedule.deleteFromTable(name, "");
			}
			/** updates a record with new values
			 * @param date
			 * @param mins
			 * @param attendance
			 * @throws SQLiteException
			 * @throws BunkerException
			 */
			public  synchronized  void updateRecord(Date date, int mins, int attendance) throws SQLiteException, BunkerException{
				
				Cursor c;
				//String dates = sdf.format(date);
				System.out.println("Vosayen : 1");
				String name = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				if(!schedule.tableExists(name)) 
					if(schedule.meta.selectFromIndex(date).equals("labsdecoreblank")||schedule.stats.inBlackHole(date)) throw new BunkerException("The record is either in a blackHole or is over a blank structure",context);
					

				System.out.println("Vosayen : 2");
				schedule.execQuery("update "+name+" set attendance = "+attendance+" where mins = "+mins);
				System.out.println("Vosayen : 3");
				name = schedule.stats.selectMonthly(date);
				System.out.println("Vosayen : 4");
				
				try{
					ScheduleDatabase.this.execQueryReplica("insert into pipeline values('"+name+"');");
					
					
					Date start = null, end = null;
					c = schedule.rawQueryReplica("select start, end from term");
					if(c.moveToFirst()){
						start = sdf.parse(c.getString(0));
						end = sdf.parse(c.getString(1));
					}
					c.close();
					String str = "range" + sdf.format(start).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "") + sdf.format(end).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
					ScheduleDatabase.this.execQueryReplica("insert into pipeline values('"+str+"');");
					
					
				}
				catch(Exception e){
					e.printStackTrace(); 
				}
				
				
				System.out.println("Vosayen : 5");
				name = schedule.stats.selectWeekly(date);
				System.out.println("Vosayen : 6");

				try{
					ScheduleDatabase.this.execQueryReplica("insert into pipeline values('"+name+"');");
					Date start = null, end = null;
					c = schedule.rawQueryReplica("select start, end from term");
					if(c.moveToFirst()){
						start = sdf.parse(c.getString(0));
						end = sdf.parse(c.getString(1));
					}
					c.close();
					String str = "range" + sdf.format(start).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "") + sdf.format(end).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
					ScheduleDatabase.this.execQueryReplica("insert into pipeline values('"+str+"');");
					
					
					
				}
				catch(Exception e){
					e.printStackTrace(); 
				}
				
				c = schedule.rawQuery("select rname from ranges where start <= '"+sdf.format(date)+"' and end >= '"+sdf.format(date)+"' and rname not like '%fixed%'");
				if(c.moveToFirst()){
					do{
						try{
						ScheduleDatabase.this.execQueryReplica("insert into pipeline values('"+c.getString(0)+"');");
						}catch(Exception e){}
					}
					while(c.moveToNext()); 
				}
				c.close();
				System.out.println("Vosayen : 7");
				
 				

				System.out.println("Vosayen : 8");
				
			}
			/** sets the attendance for specified date, mins.
			 * @param date
			 * @param mins
			 * @param condition
			 * @throws SQLiteException
			 * @throws BunkerException
			 */
			public  synchronized  void setAttendance(Date date, int mins, int condition) throws SQLiteException, BunkerException{
				updateRecord(date,mins, condition);
			}
			/**
			 * @param pseudo
			 * @param date
			 * @throws SQLiteException
			 * @throws BunkerException
			 */
			public synchronized  void copyRecordWithAttendance(String pseudo, Date date) throws SQLiteException, BunkerException{
				if(!schedule.tableExists(pseudo)) throw new BunkerException("The pseudo structure specified doesn't exist",context);
				//String dates = sdf.format(date);
				//String name = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				flushRecord(date);
				//makeRecord(date);
				schedule.execQuery("insert into "+"record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "")+" select mins, attendance from "+pseudo);
			}
			/**
			 * @param pseudo
			 * @param date
			 * @throws SQLiteException
			 * @throws BunkerException 
			 */
			public synchronized  void copyRecord(String pseudo, Date date) throws SQLiteException, BunkerException{
				if(!schedule.tableExists(pseudo)) throw new BunkerException("The pseudo structure specified doesn't exist",context);
				//String dates = sdf.format(date);
				String name = "record"+sdf.format(date).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
				flushRecord(date);
				//makeRecord(date);
				schedule.execQuery("insert into "+name+" select mins, attendance from "+pseudo);
				schedule.execQuery("update "+name+" set attendance = 2");
			}
			
			/**
			 * @param pseudoTableName
			 * @param dt
			 * @param count
			 * @throws BunkerException 
			 * @throws SQLiteException 
			 */
			public synchronized  void copyRecordBack(String pseudoTableName, Date dt, int count) throws SQLiteException, BunkerException{
				Calendar cal = Calendar.getInstance();
				cal.setTime(dt);
				//schedule.meta.makeRecord(cal.getTime());
				schedule.meta.copyRecordWithAttendance(pseudoTableName, cal.getTime());
				cal.add(Calendar.DATE, 7);
				for(int i=0;i<count;i++){
					//schedule.meta.makeRecord(cal.getTime());
					schedule.meta.copyRecord(pseudoTableName, cal.getTime());
					cal.add(Calendar.DATE, 7); 
				}
			}
			
			
			/**
			 * @param pseudoTableName
			 * @param dt
			 * @param count
			 * @throws BunkerException 
			 * @throws SQLiteException 
			 */
			public synchronized  void copyRecordBackForDays(String pseudoTableName, Date dt, int count) throws SQLiteException, BunkerException{
				Calendar cal = Calendar.getInstance();
				cal.setTime(dt);
				schedule.meta.copyRecordWithAttendance(pseudoTableName, cal.getTime());
				cal.add(Calendar.DATE, 1);
				for(int i=0;i<count;i++){
					schedule.meta.copyRecord(pseudoTableName, cal.getTime());
					cal.add(Calendar.DATE, 1);
				}
			}
			
			/**
			 * @param date
			 * @throws SQLiteException
			 * @throws BunkerException
			 */
			public synchronized  void makeRecord(Date date) throws SQLiteException, BunkerException{
				if(schedule.meta.selectFromIndex(date).equals("labsdecoreblank"))
					return;
				schedule.meta.deleteRecord(date);
				schedule.meta.createRecord(date, schedule.meta.selectFromIndex(date));
				
				schedule.execQuery("insert into "+schedule.meta.selectRecord(date)+" (mins) select mins from "+selectFromIndex(date)+";");
				
			}
			
			//Tags
			/**
			 * @param date
			 * @throws BunkerException
			 */
			public  synchronized  void createTagContainer(Date date)throws BunkerException {
				String name = selectTagContainer(date);
				if(schedule.tableExists(name)) throw new BunkerException("Tag container already exists",context);
				schedule.createTable(name, tagContainerDef);
			}
			/**
			 * @param date
			 * @throws BunkerException 
			 */
			public  synchronized  void deleteTagContainer(Date date) throws BunkerException{
				if(!schedule.tableExists(selectTagContainer(date))) throw new BunkerException("Tag Container doesn't exist",context);
				if(!schedule.tableExists(name)) throw new BunkerException("Tag container doesn't exists",context);
				schedule.dropTable(selectTagContainer(date));
			}
			/**
			 * @param date
			 * @return
			 */
			public  synchronized  String selectTagContainer(Date date){
				return "tagcontainer"+sdf.format(date).replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "");
			}
			/**
			 * @param date
			 * @param tagName
			 * @return
			 */
			public  synchronized  String selectTag(Date date,String tagName){
				return "tag"+sdf.format(date).replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "")+tagName;
			}
			/**
			 * @param tagName
			 * @return
			 * @throws BunkerException
			 */
			public  synchronized  String extractTagName(String tagName) throws BunkerException{
				if(tagName.length()<12||tagName.contains("album")||!tagName.contains("tag")) throw new BunkerException("Invalid tagName",context);
				return tagName.substring(11, tagName.length());
			}
			
			/**
			 * @param albumName
			 * @param tagName
			 * @return
			 * @throws BunkerException
			 */
			public  synchronized  String extractAlbumName(String albumName, String tagName) throws BunkerException{
				if(albumName.length()<12||!albumName.contains("album")) throw new BunkerException("Invalid albumName",context);
				return albumName.substring(11+tagName.length(), albumName.length());
				
			}
			/**
			 * @param date
			 * @param tagName
			 * @param albumName
			 * @return
			 */
			public  synchronized  String selectAlbum(Date date, String tagName, String albumName){
				return "album"+sdf.format(date).replaceAll(":", "").replaceAll("-", "").replaceAll(" ", "")+tagName+albumName;
			}
			/**
			 * @param date
			 * @param mins
			 * @param name
			 * @throws BunkerException
			 */
			public  synchronized  void createTag(Date date, int mins, String name) throws BunkerException{
				if(!schedule.tableExists(selectTagContainer(date))) throw new BunkerException("Tag Container doesn't exist",context);
				//if(schedule.rawQuery("select tagname from "+selectTagContainer(date)+" where tagname = '"+name+"'").moveToFirst()) throw new BunkerException("Tag already exists", context);
				if(schedule.valueExists("tagname", "'"+name+"'", selectTagContainer(date))) throw new BunkerException("Tag already exists", context);
				//createTagContainer(date);
				stringVector.clear();
				stringVector.add(""+mins);
				stringVector.add("'"+name+"'");
				schedule.insertIntoTable(selectTagContainer(date), stringVector);
				schedule.createTable(selectTag(date,name), tagDef);
				//
			}
			/**
			 * @param date
			 * @param name
			 * @throws BunkerException
			 */
			public  synchronized  void deleteTag(Date date, String name) throws BunkerException{
				if(!schedule.tableExists(selectTagContainer(date))) throw new BunkerException("Tag Container doesn't exist",context);
				//createTagContainer(date);
				//if(!schedule.rawQuery("select tagname from "+selectTagContainer(date)+" where tagname = '"+selectTag(date,name)+"'").moveToFirst()) throw new BunkerException("Tag doesn't exists", context);
				if(!schedule.valueExists("tagname", "'"+name+"'", selectTagContainer(date))) throw new BunkerException("Tag doesn't exists", context);
				schedule.deleteFromTable(selectTagContainer(date), "where tagname = '"+name+"'");
				schedule.dropTable(selectTag(date,name));
				
			}
			/**
			 * @param date
			 * @param name
			 * @param mins
			 * @throws BunkerException
			 */
			public  synchronized  void updateTag(Date date, String name, int mins)throws BunkerException {
				//createTagContainer(date);
				if(!schedule.valueExists("tagname", "'"+name+"'", selectTagContainer(date))) throw new BunkerException("Tag doesn't exists", context);
				schedule.execQuery("update "+selectTagContainer(date)+" set mins = "+mins+" where tagname = '"+name+"'");
			}
			
			/**
			 * @throws BunkerException 
			 * 
			 */
			public  synchronized  void renameTag(Date date, String oname, String nname) throws BunkerException{
				if(!schedule.tableExists(selectTagContainer(date))) throw new BunkerException("Tag Container doesn't exist",context);
				if(schedule.valueExists("tagname", "'"+nname+"'", selectTagContainer(date))) throw new BunkerException("Tag with name "+nname+" already exists", context);
				if(!schedule.valueExists("tagname", "'"+oname+"'", selectTagContainer(date))) throw new BunkerException("Tag with name "+oname+" doesn't exists", context);
				schedule.execQuery("update "+selectTagContainer(date)+" set tagname = '"+nname+"' where tagname = '"+oname+"'");
				schedule.renameTable(selectTag(date,oname), selectTag(date,nname));
			}
			/**
			 * @param date
			 * @param tagName
			 * @param albumName
			 * @throws BunkerException
			 */
			public  synchronized  void createAlbum(Date date,String tagName, String albumName) throws BunkerException{
				if(!schedule.tableExists(selectTagContainer(date))) throw new BunkerException("Tag Container doesn't exist",context);
				if(!schedule.tableExists(selectTag(date,tagName))) throw new BunkerException("Tag doesn't exist",context);
				//if(schedule.rawQuery("select name from "+selectTag(date,tagName)+" where name = '"+albumName+"'").moveToFirst()) throw new BunkerException("",context);
				if(schedule.valueExists("name","'"+albumName+"'", selectTag(date,tagName))) throw new BunkerException("",context);
				stringVector.clear();
				stringVector.add("'"+albumName+"'");
				schedule.insertIntoTable(selectTag(date,tagName), stringVector);
				schedule.createTable(selectAlbum(date,tagName,albumName), tagDef);
			}
			/**
			 * @param date
			 * @param tagName
			 * @param name
			 * @throws BunkerException
			 */
			public  synchronized  void deleteAlbum(Date date,String tagName, String name) throws BunkerException{
				if(!schedule.tableExists(selectTagContainer(date))) throw new BunkerException("Tag Container doesn't exist",context);
				if(!schedule.tableExists(selectTag(date,tagName))) throw new BunkerException("Tag doesn't exist",context);
				//if(!schedule.rawQuery("select name from "+selectAlbum(date,tagName,name)).moveToFirst()){throw new BunkerException("Album doesn't exist",context);}
				if(!schedule.valueExists("name","'"+name+"'", selectTag(date,tagName))) throw new BunkerException("Album doesn't exist",context);
				//if(!schedule.valueExists("name", , selectAlbum(date,tagName,name))) throw new BunkerException("Album doesn't exist",context);
				schedule.dropTable(selectAlbum(date,tagName,name));
				schedule.deleteFromTable(selectTag(date,tagName), "where name = '"+name+"'");
			}
			/**
			 * @param path
			 * @param container
			 * @throws BunkerException
			 */
			public  synchronized  void addFile(String path, String container)throws BunkerException {
				if(!schedule.tableExists(container)) throw new BunkerException("The target table doesn't exist",context);
				if(schedule.valueExists("name", "'"+path+"'", container)) throw new BunkerException("path already exists in the specified container",context);
				stringVector.clear();
				stringVector.add("'"+path+"'");
				schedule.insertIntoTable(container, stringVector);
			}
			/**
			 * @param path
			 * @param container
			 * @throws BunkerException
			 */
			public  synchronized  void removeFile(String path, String container)throws BunkerException {
				if(!schedule.tableExists(container)) throw new BunkerException("The target table doesn't exist",context);
				if(!schedule.valueExists("name", "'"+path+"'", container)) throw new BunkerException("path doesn't exist in the specified container",context);
				stringVector.clear();
				stringVector.add("'"+path+"'");
				schedule.deleteFromTable(container, "where name = '"+path+"'");
			}
	}
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------------------------	
	
	
	/**
	 * @author Roger Cores
	 *
	 */
	public class Stats{
		//weeks(datenm)
		//MetaWeekly(IDrel,attendance,total)
		/**
		 * @param date
		 * @throws BunkerException
		 */
		public   synchronized void createWeekly(Date date) throws BunkerException{
			//String name = selectWeekly(date);
			schedule.createTable(selectWeekly(date), statDef);
		}

		/**
		 * @param date
		 */
		public  synchronized  void deleteWeekly(Date date){
			//String name = selectWeekly(date);
			schedule.dropTable(selectWeekly(date));
		}
		/**
		 * @param date
		 * @return
		 */
		public    String selectWeekly(Date date){
			Calendar x = Calendar.getInstance();
			x.setTime(date);
			if(x.get(Calendar.DAY_OF_WEEK)>1){
				x.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			}else{
				x.add(Calendar.DATE, -6);
			}
			//String name = "week"+sdf.format(x.getTime()).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
			return "week"+sdf.format(x.getTime()).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
		}
		
		
		
		/**
		 * @param name
		 * @throws BunkerException
		 */
		public  synchronized  void createWeekly(String name) throws BunkerException{
			schedule.createTable(name, statDef);
		}
		/**
		 * @param name
		 */
		public  synchronized  void deleteWeekly(String name){
			schedule.dropTable(name);
		}
		
		
		/** Produces a query to access information of a week or month along with enclosed BlackHoles
		 * @param date
		 * @param weekormonth
		 * @return
		 */
		public synchronized  String makeQuery(Date date, boolean weekormonth){
			String name,query;
			Date end;
			Cursor c;
			Calendar x;
			x = Calendar.getInstance();
			x.setTime(date);
			if(weekormonth){
				name = schedule.stats.selectWeekly(date);
				x.add(Calendar.DATE, 6);
			}
			else{
				name = schedule.stats.selectMonthly(date);
				x.add(Calendar.MONTH, 1);
				x.add(Calendar.DATE, -1);
			}
			end = x.getTime();
			query = "select * from "+name+" UNION ALL ";
			
			c = schedule.rawQuery("select rname from ranges where start >= '"+sdf.format(date)+"' and end <= '"+sdf.format(end)+"' and rname like '%fixed%'");
			
			if(c.moveToFirst()){
				do{
					//IDrel, attendance, total
					//	   , 		   , 0      if percentage
					//	   ,		   , number if total
					
					//String query1 = " select IDrel, attendance, total from "+c.getString(0)+" where total != 0 ";
					//String query2 = " select IDrel, attendance, 100 as total from "+c.getString(0)+" where total == 0 ";
					//query1 = query1 + " UNION ALL " + query2 + " UNION ALL ";
				
					query = query + "(select IDrel, attendance, total from "+c.getString(0)+" where total != 0 " + " UNION ALL select IDrel, attendance, 100 as total from "+c.getString(0)+" where total == 0) UNION ALL ";
				
				}
				while(c.moveToNext());
			}
			
			query = "select IDrel, sum(attendance) as attendance, sum(total) as total from ("+query+") group by IDrel";
			
			
			// select * from weekormonth
			// UNION ALL select [] from blackHoles
			
			return query;
		}
		
		/**
		 * @param start
		 * @param end
		 * @return
		 * @throws SQLiteException
		 * @throws BunkerException
		 * @throws ParseException
		 */
		public synchronized  String buildQuery(Date start, Date end) throws SQLiteException, BunkerException, ParseException{
			int dcount,count,nmonths;
			
			Calendar x1 = Calendar.getInstance(), x2 = Calendar.getInstance();
			x1.setTime(start);
			x2.setTime(end);
			Cursor c;
			
			//long startTime = start.getTime();
			//long endTime = end.getTime();
			//long diffTime = end.getTime() - start.getTime();
			//long diffDays = (end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24);
			dcount = (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24) + 1);
			if(dcount<=1) return "";
			//System.out.println(dcount);
			
			nmonths = x2.get(Calendar.MONTH) - x1.get(Calendar.MONTH);
			//System.out.println("nmonths : "+nmonths);
			String query = "(", dquery = "(",name = "";
			//start month, use dcount if =0 or =1, use count if >=2
			count = 0;
			if(nmonths<=1){count = dcount;} else if(nmonths>=2){count = x1.getActualMaximum(Calendar.DAY_OF_MONTH)-(x1.get(Calendar.DATE)-1); dcount = dcount - count;}
			do{
				//System.out.println("First Month : "+sdf.format(x1.getTime()));
				//System.out.println("count : "+count);
				if(x1.get(Calendar.DATE)==1&&count>=x1.getActualMaximum(Calendar.DAY_OF_MONTH)){
					//System.out.println("Taking Month : "+sdf.format(x1.getTime()));
					
					name = schedule.stats.selectMonthly(x1.getTime());
					c = schedule.rawQuery("select name from sqlite_master where type = 'table' and name = '"+name+"'");
					if(c.moveToFirst()){
						query = query + "select * from "+name+" UNION ALL ";
					}
					c.close();
					count = count - x1.getActualMaximum(Calendar.DAY_OF_MONTH); x1.add(Calendar.MONTH, 1);//System.out.println("after month add "+x1.getTime());
					/*add a month and dec count by days of the month, add a month*/
				}
				else if(x1.get(Calendar.DAY_OF_WEEK)==2&&count>=7){
					
					name = schedule.stats.selectWeekly(x1.getTime());
					c = schedule.rawQuery("select name from sqlite_master where type = 'table' and name = '"+name+"'");
					if(c.moveToFirst()){
						query = query + "select * from "+name+" UNION ALL ";
					}
					c.close();
					x1.add(Calendar.DATE, 7); count -= 7;
					
				/*add a week and dec count by 7, add a week*/}
				else{
					name = schedule.meta.selectFromIndex(x1.getTime());
					if(!name.equals("labsdecore")&&!schedule.stats.inBlackHole(x1.getTime())&&!schedule.tableExists(schedule.meta.selectRecord(x1.getTime()))) schedule.meta.makeRecord(x1.getTime());
					if(((!name.equals("labsdecoreblank"))&&(x1.getTime().compareTo(schedule.start)>=0&&x1.getTime().compareTo(schedule.end)<=0)) && !schedule.stats.inBlackHole(x1.getTime())){
							dquery = dquery + "select s.IDrel, r.attendance from "+schedule.meta.selectFromIndex(x1.getTime())+" s, "+schedule.meta.selectRecord(x1.getTime())+" r where s.mins = r.mins AND r.attendance != 3 UNION ALL ";
						
					}
					
					
					
					x1.add(Calendar.DATE, 1); count--;
				/*add date and dec count by 1, add a date*/}
			}
			while(count>0);
			
			//intermideate months, if nmonths >=2
			if(nmonths>=2)
			for(int i=0; i<nmonths-1; i++){
				
				name = schedule.stats.selectMonthly(x1.getTime());
				c = schedule.rawQuery("select name from sqlite_master where type = 'table' and name = '"+name+"'");
				if(c.moveToFirst()){
					query = query + "select * from "+name+" UNION ALL ";
				}
				c.close();
				
				
				
				//System.out.println("Taking Month : "+sdf.format(x1.getTime()));
				//take month at i, and add month to date
				dcount = dcount - x1.getActualMaximum(Calendar.DAY_OF_MONTH);
				x1.add(Calendar.MONTH, 1);
				
			}
			
			
			//last month, if nmonths >=2
			if(nmonths>=2)
			do{
				if(x1.get(Calendar.DAY_OF_WEEK)==2&&dcount>=7){
					
					
					name = schedule.stats.selectWeekly(x1.getTime());
					c = schedule.rawQuery("select name from sqlite_master where type = 'table' and name = '"+name+"'");
					if(c.moveToFirst()){
						query = query + "select * from "+name+" UNION ALL ";
					}	
					c.close();
					x1.add(Calendar.DATE, 7); dcount -= 7;
					
					/*add a week and dec count by 7, add a week*/}
				else{
					
					name = schedule.meta.selectFromIndex(x1.getTime());
					if(!name.equals("labsdecore")&&!schedule.stats.inBlackHole(x1.getTime())&&!schedule.tableExists(schedule.meta.selectRecord(x1.getTime()))&&!schedule.tableExists(schedule.meta.selectRecord(x1.getTime()))) schedule.meta.makeRecord(x1.getTime());
					
					if(((!name.equals("labsdecoreblank"))&&(x1.getTime().compareTo(schedule.start)>=0&&x1.getTime().compareTo(schedule.end)<=0)) && !schedule.stats.inBlackHole(x1.getTime())){
							dquery = dquery + "select s.IDrel, r.attendance from "+schedule.meta.selectFromIndex(x1.getTime())+" s, "+schedule.meta.selectRecord(x1.getTime())+" r where s.mins = r.mins AND r.attendance != 3 UNION ALL ";
						
					} 
					x1.add(Calendar.DATE, 1); dcount--;
					
					
						/*add date and dec count by 1, add a date*/}
			}
			while(dcount>0);
			
			
			//traverse blackHoles and add'em
			if(schedule.start.equals(start)&&schedule.end.equals(end))
			c = schedule.rawQuery("select rname from ranges where rname like '%fixed%'");
			else
			c = schedule.rawQuery("select rname from ranges where start >= '"+sdf.format(start)+"' and end <= '"+sdf.format(end)+"' and rname like '%fixed%'");

			if(c.moveToFirst()){
				do{
					//IDrel, attendance, total
					//	   , 		   , 0      if percentage
					//	   ,		   , number if total
					
					//String query1 = " select IDrel, attendance, total from "+c.getString(0)+" where total != 0 ";
					//String query2 = " select IDrel, attendance, 100 as total from "+c.getString(0)+" where total == 0 ";
					//query1 = query1 + " UNION ALL " + query2 + " UNION ALL ";
				
					query = query + " select IDrel, attendance, total from "+c.getString(0)+" where total != 0 " + " UNION ALL select IDrel, attendance, 100 as total from "+c.getString(0)+" where total == 0 UNION ALL ";
				
				} 
				while(c.moveToNext());
				
			}
			c.close();
			if(!query.equals("(")){
				//query =  query.substring(0, query.length()-11) + ")";
				query = "select IDrel, sum(attendance) as attendance, sum(total) as total from ("+query.substring(0, query.length()-11) + ")"+") group by IDrel";
			}
			if(!dquery.equals("(")){
				dquery =  dquery.substring(0, dquery.length()-11) + ")";
			
				//String attendance = "(select IDrel, sum(attendance) as attendance from "+dquery+"where attendance = 1 group by IDrel)";

				//String total = "(select IDrel, count(attendance) as total from "+dquery+" group by IDrel)";

				//String join = "select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+dquery+"where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+dquery+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+dquery+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+dquery+"where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;";
				schedule.execQuery("insert into xtemp (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+dquery+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2  as attendance from "+dquery+" where attendance = 2 group by IDrel) group by IDrel) a left join ((select IDrel, count(attendance) as total from "+dquery+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+dquery+" group by IDrel)) a left join (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+dquery+" where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2  as attendance from "+dquery+" where attendance = 2 group by IDrel) group by IDrel) b on a.IDrel = b.IDrel where b.IDrel is null;");
				
				schedule.execQuery("update xtemp set attendance = 0 where attendance is null");
			}
			//System.gc();
			return query;
		}
		/**
		 * @param name
		 * @param start
		 * @param end
		 */
		public synchronized  void insertIntoRanges(String name, Date start, Date end){
			stringVector.clear();
			stringVector.add("'"+name+"'");
			stringVector.add("'"+sdf.format(start)+"'");
			stringVector.add("'"+sdf.format(end)+"'");
			//String[] values = {"'"+name+"'","'"+sdf.format(start)+"'","'"+sdf.format(end)+"'"};
			schedule.insertIntoTable("ranges", stringVector);
		}
		/**
		 * @param name
		 * @throws ParseException 
		 */
		public synchronized  void deleteFromRanges(String name){
			schedule.deleteFromTable("ranges", "where rname = '"+name+"'");
		}
		/**
		 * @param start
		 * @param end
		 */
		public synchronized  void deleteFromRanges(Date start, Date end){
			schedule.deleteFromTable("ranges", "start = '"+sdf.format(start)+"' and end = '"+sdf.format(end)+"'");
		}
		/**
		 * @param name
		 * @return
		 * @throws ParseException
		 */
		public synchronized  Date getStart(String name) throws ParseException{
			Cursor c = schedule.rawQuery("select start from ranges where rname = '"+name+"'");
			if(c.moveToFirst()) {
				String temp = c.getString(0);
				c.close();
				return sdf.parse(temp);
				
			}
			c.close();
			return null;
		}
		/**
		 * @param name
		 * @return
		 * @throws ParseException
		 */
		public synchronized  Date getEnd(String name) throws ParseException{
			Cursor c = schedule.rawQuery("select end from ranges where rname = '"+name+"'");
			if(c.moveToFirst()) {Date temp = sdf.parse(c.getString(0)); c.close(); return temp;}
			c.close();
			return null;
		}
		
		public synchronized  void setStart(String name, Date start) throws ParseException, BunkerException{
			Date e;
			boolean blackHole;
			if(name.contains("fixed")) blackHole = true; else blackHole = false;
			//s = this.getStart(name);
			e = this.getEnd(name);
			if(this.rangeExists(start, e)){throw new BunkerException("Range already exists",context);}
			this.deleteCustomRange(name);
			this.createCustomRange(start, e, blackHole);
		}
		
		public synchronized  void setEnd(String name, Date end) throws ParseException, BunkerException{
			Date s;
			boolean blackHole;
			if(name.contains("fixed")) blackHole = true; else blackHole = false;
			s = this.getStart(name);
			//e = this.getEnd(name);
			if(this.rangeExists(s, end)){throw new BunkerException("Range already exists",context);}
			this.deleteCustomRange(name);
			this.createCustomRange(s, end, blackHole);
		}
		 
		/** Creates a CustomRange, a blackHole or a normal range<br>
		 *  while creating normal range it needs to be validated after pipeline has been cleared<br>
		 *  or before opening contents of this range.
		 * @param start
		 * @param end
		 * @return boolean false if need a conversion <br> true if creation was successful.
		 * @throws BunkerException
		 * @throws SQLiteException
		 * @throws ParseException
		 */
		public synchronized  boolean createCustomRange(Date start, Date end, boolean blackHole) throws BunkerException, SQLiteException, ParseException{
			if(start.compareTo(end)>0) throw new BunkerException("Start should be before End",context);
			String name = selectRange(start,end,blackHole);
			//if(schedule.tableExists(name)) throw new BunkerException("Range already exists", context);
			if(schedule.stats.rangeExists(start, end)!=null)
				if(blackHole==schedule.stats.rangeExists(start, end)){
					throw new BunkerException("Range already exists", context);
				}
				else{
					return false; 
				}
			
			if(blackHole){
				Cursor c = schedule.rawQuery("select rname from ranges where rname like '%fixed%' and not ((start<'"+sdf.format(start)+"' and end<'"+sdf.format(start)+"') or (start>'"+sdf.format(end)+"' and end>'"+sdf.format(end)+"'))");
				if(c.moveToFirst())
					throw new BunkerException("Can't overwrite black hole with black hole",context);
				c.close();
				
				
				
				
				schedule.createTable(name , statDef);
				insertIntoRanges(name,start,end);
				Calendar x = Calendar.getInstance();
				x.setTime(start);
				do{
					schedule.meta.deleteRecord(x.getTime());
					x.add(Calendar.DATE, 1);
				}
				while(x.getTime().compareTo(end)<=0);
				

				collectAll(start,end);
			}
			else{
			//stats.deleteFromRanges(name);
			schedule.createTable(name , statDef);
			insertIntoRanges(name,start,end);
			schedule.stats.addToPipeline(name);
			//this.validateRange(name);
			//Cursor c;
			/*
 String query2 = stats.buildQuery(start, end);
			//String query2 = "(";
			String query = "select IDrel, sum(attendance) as attendance, sum(total)as total from (select * from xtemp ";
			if(!query2.equals("("))
				query = query + "UNION ALL "+query2+") group by IDrel";
			else query = query + ") group by IDrel";
			schedule.deleteFromTable(name, "");
			schedule.execQuery("insert into "+name+" (IDrel, attendance, total) "+query+"");
			c = schedule.rawQuery("select IDrel from "+name+"");
			if(!c.moveToFirst()){schedule.stats.deleteMonthly(name);}
			schedule.deleteFromTable("xtemp", "");
*/
			
			}
			
			//collect all from start to end
			
			
			return true;
		}
		/** Converts a range from blackHole to normal range<br>
		 *  or a normal range to blackHole<br>
		 *  if blackHole is converted to normal range, it has to be validated<br>
		 *  after pipeline is cleared.
		 * @param start
		 * @param end
		 * @throws BunkerException
		 * @throws SQLiteException
		 * @throws ParseException
		 */
		public synchronized  void convertRange(Date start, Date end) throws BunkerException, SQLiteException, ParseException{
			Cursor c = schedule.rawQuery("select rname from ranges where start = '"+sdf.format(start)+"' and end = '"+sdf.format(end)+"';");
			if(c.moveToFirst()){
				if(c.getString(0).contains("fixed")){
					schedule.deleteFromTable("ranges", "where rname = '"+c.getString(0)+"'");
					schedule.dropTable(c.getString(0));
					schedule.stats.createCustomRange(start, end, false);
				}
				else{
					schedule.deleteFromTable("ranges", "where rname = '"+c.getString(0)+"'");
					schedule.dropTable(c.getString(0));
					schedule.stats.createCustomRange(start, end, true);
				}
			}
			else{
				throw new BunkerException("Range doesn't exist, cannot be converted.",context);
			}
		}
		
		/**
		 * @param name
		 * @throws ParseException 
		 * @throws BunkerException 
		 * @throws SQLiteException 
		 */
		public synchronized  void deleteCustomRange(String name) throws ParseException, SQLiteException, BunkerException{
			//if name contains fixed
			//collect all form start to end
			if(name.contains("fixed")){
				collectAll(stats.getStart(name),stats.getEnd(name));
				
			}
			
			schedule.dropTable(name);
			deleteFromRanges(name);
		}
		/**
		 * @param start
		 * @param end
		 * @throws ParseException
		 * @throws BunkerException 
		 * @throws SQLiteException 
		 */
		public synchronized  void deleteCustomRange(Date start, Date end) throws ParseException, SQLiteException, BunkerException{
			//if name contains fixed
			//collect all from start to end
			boolean blackHole = false;
			Cursor c = schedule.rawQuery("select rname from ranges where start = '"+sdf.format(start)+"' and end = '"+sdf.format(end)+"'");
			if(c.moveToFirst())
				if(c.getString(0).contains("fixed")) blackHole = true;
				else blackHole = false;
			if(blackHole){
			collectAll(start,end);
			
			
			}
			
			
			
			String name = selectRange(start,end,blackHole);
			//System.out.println(name);
			schedule.dropTable(name);
			deleteFromRanges(name);
		}
		public synchronized  void insertIntoBlackHole(String rname, int IDrel, int attendance, int total) throws ParseException, BunkerException{
			
			//if rname contains fixed
			//collect all from start to end

			
			
			if(rname.contains("fixed"))
			collectAll(stats.getStart(rname),stats.getEnd(rname));
			else throw new BunkerException("Cannot manipulate non blackHole ranges !",context);
			
			stringVector.clear();
			stringVector.add(""+IDrel);
			stringVector.add(""+attendance);
			stringVector.add(""+total);
			schedule.insertIntoTable(rname, stringVector);
			
		}
		public synchronized  void deleteFromBlackHole(String rname, int IDrel) throws ParseException, BunkerException{
			
			
			
			//if rname contains fixed
			//collect all from start to end
			if(rname.contains("fixed"))
			collectAll(stats.getStart(rname),stats.getEnd(rname));
			else throw new BunkerException("Cannot manipulate non blackHole ranges !",context);
			
			
			
			schedule.deleteFromTable(rname, "where IDrel = "+IDrel);
		}
		/** Collect all weeklys and monthlys spanning between the provided dates
		 * @param start
		 * @param end
		 * @throws BunkerException 
		 * @throws SQLiteException 
		 */
		public synchronized  void collectAll(Date start, Date end) throws SQLiteException, BunkerException{
			//Toast.makeText(context, "collecting", Toast.LENGTH_LONG).show();
			Calendar x = Calendar.getInstance();
			x.setTime(start);
			do{
				
				

				ScheduleDatabase.this.stats.addToPipeline(schedule.stats.selectWeekly(x.getTime()));

				ScheduleDatabase.this.stats.addToPipeline(schedule.stats.selectMonthly(x.getTime()));
				
				x.add(Calendar.DATE, 7);
			}
			while(x.getTime().compareTo(end)<=0);
			
			
 			Cursor c = schedule.rawQuery("select rname from ranges where rname not like '%fixed%' and not ((start<'"+sdf.format(start)+"' and end<'"+sdf.format(start)+"') or (start>'"+sdf.format(end)+"' and end>'"+sdf.format(end)+"'))");
			if(c.moveToFirst())
				do{
					schedule.stats.addToPipeline(c.getString(0));
				}
				while(c.moveToNext());
			c.close();
			 
		}
		/** Validates a range
		 * @param start
		 * @param end
		 * @throws BunkerException
		 * @throws SQLiteException
		 * @throws ParseException
		 */
		public synchronized  void validateRange(Date start, Date end) throws BunkerException, SQLiteException, ParseException{
			String name = "";
			Cursor c = schedule.rawQuery("select rname from ranges where start = '"+sdf.format(start)+"' and end = '"+sdf.format(end)+"'");
			if(c.moveToFirst())
				name = c.getString(0);
			else throw new BunkerException("Range doesn't exist",context);
			c.close();
			
			if(name.contains("fixed")) throw new BunkerException("Cannot manipulate blackHole range",context);
			
			String query2 = stats.buildQuery(start, end);
			//String query2 = "(";
			String query = "select IDrel, sum(attendance) as attendance, sum(total)as total from (select * from xtemp ";
			if(!query2.equals("("))
				query = query + "UNION ALL "+query2+") group by IDrel";
			else query = query + ") group by IDrel";
			schedule.deleteFromTable(name, "");
			schedule.execQuery("insert into "+name+" (IDrel, attendance, total) "+query+"");
			schedule.deleteFromTable("xtemp", "");
			/*
 			stats.deleteCustomRange(start, end, false);
			stats.deleteFromRanges(stats.selectRange(start, end,false));
			stats.createCustomRange(start, end,false);
			 */
			
		}
		/** Validates a range
		 * @param name
		 * @throws ParseException
		 * @throws BunkerException
		 */
		public synchronized  void validateRange(String name) throws ParseException, BunkerException {
			
			
			if(name==null) return;

 			Date start = getStart(name);
			Date end = getEnd(name);
			//System.out.println("Vosaye: "+name+" "+sdf.format(start)+" - "+sdf.format(end));
			if(name.contains("fixed")) throw new BunkerException("Cannot manipulate non blackHole range",context);
			
			String query2 = stats.buildQuery(start, end);
			//String query2 = "(";
			String query = "select IDrel, sum(attendance) as attendance, sum(total)as total from (select * from xtemp ";
			if(!query2.equals("(")&&!query2.equals(""))
				query = query + "UNION ALL "+query2+") group by IDrel";
			else query = query + ") group by IDrel";
			schedule.deleteFromTable(name, "");
			schedule.execQuery("insert into "+name+" (IDrel, attendance, total) "+query+"");
			schedule.deleteFromTable("xtemp", "");
		}
		
		
		/** Checks if a range exists
		 * @param start
		 * @param end
		 * @return null : if range doesn't exist
		 * <br> true if a blackHole on this range
		 * <br> false if a normal range exists
		 */
		public synchronized  Boolean rangeExists(Date start, Date end){
			Cursor c = schedule.rawQuery("select rname from ranges where start = '"+sdf.format(start)+"' and end = '"+sdf.format(end)+"';");
			if(c.moveToFirst()){
				if(c.getString(0).contains("fixed")){c.close(); return true;}
				else{c.close(); return false;}
			}
			c.close();
			return null;
		}
		
		/**
		 * @param date
		 * @return
		 */
		public synchronized  boolean inBlackHole(Date date){
			Cursor c = schedule.rawQuery("select rname from ranges where start <= '"+sdf.format(date)+"' and end >= '"+sdf.format(date)+"' and rname like '%fixed%';");
			if(c.moveToFirst()){
				c.close();
				return true;
			}
			c.close();
			return false;
		}
		
		public synchronized  String getBlackHole(Date date){
			Cursor c = schedule.rawQuery("select rname from ranges where start <= '"+sdf.format(date)+"' and end >= '"+sdf.format(date)+"' and rname like '%fixed%';");
			if(c.moveToFirst()){
				String name = c.getString(0);
				c.close();
				return name;
			}
			c.close();
			return "";
		}
		
		/**
		 * @param date
		 * @return
		 */
		public synchronized  boolean inRange(Date date){
			Cursor c = schedule.rawQuery("select rname from ranges where start <= '"+sdf.format(date)+"' and end >= '"+sdf.format(date)+"' and rname not like '%fixed%';");
			if(c.moveToFirst()){
				return true;
			}
			return false;
		}
		
		
		
		
		public synchronized  boolean inBracket(Date start, Date end){
			Cursor c = schedule.rawQuery("select rnanme from ranges where start >= '"+sdf.format(start)+"' and end <= '"+sdf.format(end)+"' and rname like '%fixed%';");
			if(c.moveToFirst()){
				return true;
			}
			return false;
		}

		/**
		 * @throws ParseException
		 * @throws BunkerException
		 */
		public synchronized  void validateOverall() throws ParseException, BunkerException{
			Date start, end;
			Cursor c = schedule.rawQuery("select start, end from term");
			if(c.moveToFirst()){
				start = sdf.parse(c.getString(0));
				end = sdf.parse(c.getString(1));
			}else{throw new BunkerException("No terms set yet", context);}
			c.close();
			//System.out.println(sdf.format(end));
			validateRange(start, end);
		}
		/**
		 * @return
		 * @throws ParseException
		 * @throws BunkerException
		 */
		public  synchronized String getOverallRange() throws ParseException, BunkerException{
			Date start, end;
			Cursor c = schedule.rawQuery("select start, end from term");
			if(c.moveToFirst()){
				start = sdf.parse(c.getString(0));
				end = sdf.parse(c.getString(1));
			}else{c.close();throw new BunkerException("No terms set yet", context);}
			c.close();
			return stats.selectRange(start, end,false);
		}
		
		public synchronized String getTodayRange() throws ParseException, BunkerException{
			Calendar cal = Calendar.getInstance(); 
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.add(Calendar.DATE, -1);
			return stats.selectRange(start, cal.getTime(),false);
		}
		/**
		 * @param start
		 * @param end
		 * @return
		 */
		public synchronized  String selectRange(Date start, Date end, boolean blackHole){
			//String name = "range" + sdf.format(start).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "") + sdf.format(end).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
			//System.out.println(name+" "+name.length());
			if(blackHole)
			return "range" + sdf.format(start).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "") + sdf.format(end).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "")+"fixed";
			else
				return "range" + sdf.format(start).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "") + sdf.format(end).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
			//return "axis";
		}
		
		
		
		
		/**
		 * @deprecated
		 * @param date
		 * @throws BunkerException
		 * @throws SQLiteException
		 * @throws ParseException
		 */
		public synchronized  void validateWeekly(Date date) throws BunkerException, SQLiteException, ParseException{
			//create weekly if not exists
			schedule.stats.createWeekly(date);
			//select weekly name, set calendar
			Calendar x = Calendar.getInstance();
			x.setTime(date);
			if(x.get(Calendar.DAY_OF_WEEK)>1){
				x.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
			}else{
				x.add(Calendar.DATE, -6);
			}
			String name = "week"+sdf.format(x.getTime()).replaceAll("-", "").replaceAll(":", "").replaceAll(" ", "");
			//query
			String query = "(";
			//flush weekly
			schedule.stats.flushWeekly(date);
			//build query
			for(int i = 0; i<7; i++){
				//add select UNION ALL to query
				//(select s.IDrel, r.attendance from labsdecore981f5336b74f408a9e51314855ba1c0d s, record20130102000000 r where s.mins = r.mins AND r.attendance != 3)
				if((!schedule.meta.selectFromIndex(x.getTime()).equals("labsdecoreblank"))&&(x.getTime().compareTo(schedule.start)>=0&&x.getTime().compareTo(schedule.end)<=0))
				query = query + "select s.IDrel, r.attendance from "+schedule.meta.selectFromIndex(x.getTime())+" s, "+schedule.meta.selectRecord(x.getTime())+" r where s.mins = r.mins AND r.attendance != 3 UNION ALL ";
				
				
				x.add(Calendar.DATE, 1);
			}
			
			
			if(!query.equals("(")){
				query = query.substring(0, query.length()-11)+ ")";
				//query = query + ")";
				//String attendance = "(select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)";
				//String total = "(select IDrel, count(attendance) as total from "+query+" group by IDrel)";
				//String join = "select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;";
				schedule.execQuery("insert into "+name+" (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;");
				schedule.execQuery("update "+name+" set attendance = 0 where attendance is null");
			}
			if(!schedule.rawQuery("select IDrel from "+name+"").moveToFirst()){schedule.stats.deleteWeekly(date);}
			//refine query
			//insert into
			//System.gc();
		}
		
		/**
		 * @param name
		 * @param type
		 * @return
		 * @throws ParseException
		 */
		public synchronized  Date getDate(String name, String type) throws ParseException{
			String date = name.substring(type.length());
			date = date.substring(0, 4) +"-"+ date.substring(4, 6) +"-"+ date.substring(6, 8) + " 00:00:00";
			return sdf.parse(date); 
		}
		
		
		public synchronized  boolean validateWeekly(String name) throws ParseException, BunkerException{
			Calendar x = Calendar.getInstance();
			x.setTime(this.getDate(name, "week"));
			Cursor c;
			String structure,record;
			
			schedule.createTable("xtempr", statDef);
			schedule.deleteFromTable("xtempr", "");
			
			String dummyName = "xtempr";
			
			StringBuilder exists = new StringBuilder("select IDrel from "+dummyName+" where IDrel = ");//only the value needs to be appended
			
			StringBuilder insertInto = new StringBuilder("insert into "+dummyName+" values(");
			String insertInto1 = ",", insertInto2 = ");";
			
			StringBuilder query = new StringBuilder("select s.IDrel, r.attendance from ");
			String query1 = " s, ", query2 = " r where s.mins = r.mins;";
			
			StringBuilder update = new StringBuilder("update "+dummyName+" set attendance = attendance + ");
			String update1 = " , total = total + ",update2 = " where IDrel = ";
			
			
			int IDrel,attendance,total=0;
			
			//schedule.createTable("xtemp", statDef);
			//flush in case theres something
			//schedule.deleteFromTable("xtemp", "");
			
			for(int i=0;i<7;i++){
				structure = schedule.meta.selectFromIndex(x.getTime());
				record = schedule.meta.selectRecord(x.getTime());
				if(((!structure.equals("labsdecoreblank"))&&x.getTime().compareTo(schedule.start)>=0&&x.getTime().compareTo(schedule.end)<=0)&& !schedule.stats.inBlackHole(x.getTime())){
					if(!schedule.tableExists(schedule.meta.selectRecord(x.getTime()))) {System.out.println("Creating "+record);System.out.println("Creating "+schedule.meta.selectRecord(x.getTime())); System.out.println(sdf.format(x.getTime()));schedule.meta.makeRecord(x.getTime());}
					if(schedule.tableExists(schedule.meta.selectRecord(x.getTime()))){ 
						//record = schedule.meta.selectRecord(x.getTime());
						//temp = query.indexOf("#");
						//query.replace(temp, temp+1, structure);
						//temp = query.indexOf("@");
						//query.replace(temp, temp+1, record);
						query.append(" "+structure);
						query.append(query1);
						query.append(schedule.meta.selectRecord(x.getTime()));
						System.out.println(schedule.meta.selectRecord(x.getTime()));
						query.append(query2);
						c = schedule.rawQuery(query.toString()); 
						
						if(c.moveToFirst()){
							do{
								IDrel = c.getInt(0);
								attendance = c.getInt(1);
								//exists.replaceAll("?", ""+IDrel);
								exists.append(""+IDrel);
								if(schedule.exists(exists.toString())){
									if(attendance == 1||attendance == 2){
										total = 1;
										update.append(" "+1);
										update.append(update1);
										update.append(" "+total);
										update.append(update2);
										update.append(" "+IDrel);
										schedule.execQuery(update.toString());
										update.delete(update.indexOf("+")+1, update.length()).append(" ");
									}
									if(attendance == 0){
										attendance = 0;
										total = 1;
										update.append(" "+attendance);
										update.append(update1);
										update.append(" "+total);
										update.append(update2);
										update.append(" "+IDrel);
										schedule.execQuery(update.toString());
										update.delete(update.indexOf("+")+1, update.length()).append(" ");
									}
								}
								else{
								
								
									if(attendance == 0){
										attendance = 0;
										total = 1;
										insertInto.append(" "+IDrel);
										insertInto.append(insertInto1);
										insertInto.append(""+attendance);
										insertInto.append(insertInto1);
										insertInto.append(""+total);
										insertInto.append(insertInto2);
										schedule.execQuery(insertInto.toString());
										insertInto.delete(insertInto.indexOf("(")+1, insertInto.length()).append(" ");
									}
									else if(attendance == 1||attendance == 2){
										total = 1;
										insertInto.append(" "+IDrel);
										insertInto.append(insertInto1);
										insertInto.append(""+1);
										insertInto.append(insertInto1);
										insertInto.append(""+total);
										insertInto.append(insertInto2);
										schedule.execQuery(insertInto.toString());
										insertInto.delete(insertInto.indexOf("(")+1, insertInto.length()).append(" ");
									}
								
								
								
								}
								exists.delete(exists.indexOf("=")+1, exists.length()).append(" ");
							
							}
							while(c.moveToNext());
						}
						c.close();
						query.delete(33, query.length());
					}
					else{
							schedule.dropTable(dummyName);
							schedule.stats.createWeekly(name);
							schedule.stats.flushWeekly(name);
							schedule.stats.deleteWeekly(name);
							return false;
					}
					
				}
				
				x.add(Calendar.DATE, 1);
			}
			schedule.stats.createWeekly(name);
			schedule.stats.flushWeekly(name);
			schedule.execQuery("insert into "+name+" select * from "+dummyName+";");
			schedule.dropTable(dummyName);
			if(!schedule.exists("select * from "+name))
				schedule.dropTable(name);
			
			return true;
			
		}
		/**
		 * @deprecated
		 * @param name
		 * @throws BunkerException
		 * @throws ParseException
		 */
		public synchronized  void validateWeeklys(String name) throws BunkerException, ParseException{
			//System.out.println("validating week : "+name);
			Calendar x = Calendar.getInstance();
			x.setTime(this.getDate(name, "week"));
			schedule.stats.createWeekly(name);
			//query
			StringBuilder query = new StringBuilder("(");
			StringBuilder query2 = new StringBuilder("insert into ");
			
			//query.append("(");
			//query2.append("insert into ");
			//flush weekly
			schedule.stats.flushWeekly(name);
			//build query
			for(int i = 0; i<7; i++){
				//add select UNION ALL to query
				//(select s.IDrel, r.attendance from labsdecore981f5336b74f408a9e51314855ba1c0d s, record20130102000000 r where s.mins = r.mins AND r.attendance != 3)
				if(!schedule.meta.selectFromIndex(x.getTime()).equals("labsdecore")&&!schedule.stats.inBlackHole(x.getTime())&&!schedule.tableExists(schedule.meta.selectRecord(x.getTime()))) schedule.meta.makeRecord(x.getTime());
				if(((!schedule.meta.selectFromIndex(x.getTime()).equals("labsdecoreblank"))&&x.getTime().compareTo(schedule.start)>=0&&x.getTime().compareTo(schedule.end)<=0)&& !schedule.stats.inBlackHole(x.getTime()))
				query.append("select s.IDrel, r.attendance from "+schedule.meta.selectFromIndex(x.getTime())+" s, "+schedule.meta.selectRecord(x.getTime())+" r where s.mins = r.mins AND r.attendance != 3 UNION ALL ");
				x.add(Calendar.DATE, 1);
			}
			
			//System.out.println(query);
			if(!query.toString().equals("(")){
				query.delete(query.length()-11, query.length());
				query.append(")");
				//query = query.substring(0, query.length()-11)+ ")";
				//query = query + ")";
				//String attendance = "(select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)";
				
				//String total = "(select IDrel, count(attendance) as total from "+query+" group by IDrel)";
				
				//String join = "select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;";
				query2 = new StringBuilder("insert into ");
				query2.append(name);
				query2.append(" (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from ");
				query2.append(query);
				query2.append(" where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from ");
				query2.append(query);
				query2.append(" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from ");
				query2.append(query);
				query2.append(" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from ");
				query2.append(query);
				query2.append(" where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;");
				//query2.append("");
				
				//System.out.println(query2.toString());
				schedule.execQuery(query2.toString());
				/*
 schedule.execQuery("insert into "
				+name+
				" (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "
				+query+
				"where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "
				+query+
				" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "
				+query+
				" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "
				+query+
				"where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;");
*/
				schedule.execQuery("update "+name+" set attendance = 0 where attendance is null");
			}
			Cursor c = schedule.rawQuery("select IDrel from "+name+"");
			if(!c.moveToFirst()){schedule.stats.deleteWeekly(name);}
			c.close();
			//refine query
			//insert into
			//System.gc();
			//query.setLength(0);
			//query2.setLength(0);
			//System.gc();
			query = null;
			query2 = null;
		}
		
		
		
		/**
		 * @param date
		 */
		public synchronized  void flushWeekly(Date date){
			schedule.deleteFromTable(selectWeekly(date), "");
		}
		/**
		 * @param name
		 */
		public  synchronized void flushWeekly(String name){
			schedule.deleteFromTable(name, "");
		}
		
		/**
		 * @param date
		 */
		public synchronized  void createMonthly(Date date){
			//String name = selectMonthly(date);
			schedule.createTable(selectMonthly(date), statDef);
		}
		/**
		 * @param date
		 */
		public synchronized  void deleteMonthly(Date date){
			//String name = selectMonthly(date);
			schedule.dropTable(selectMonthly(date));
		}
		
		/**
		 * @param name
		 */
		public synchronized  void createMonthly(String name){
			schedule.createTable(name, statDef);
		}
		/**
		 * @param name
		 */
		public synchronized  void deleteMonthly(String name){
			schedule.dropTable(name);
		}
		
		
		
		/**
		 * @param date
		 * @return
		 */
		public   String selectMonthly(Date date){
			Calendar x = Calendar.getInstance();
			x.setTime(date);
			x.set(Calendar.DAY_OF_MONTH, 1);
			//String name = "month"+sdf.format(x.getTime()).replaceAll("-","").replaceAll(":", "").replaceAll(" ", "");
			return "month"+sdf.format(x.getTime()).replaceAll("-","").replaceAll(":", "").replaceAll(" ", "");
		}
		
		
		
		
		
		/**
		 * @param name
		 * @throws SQLiteException
		 * @throws BunkerException
		 * @throws ParseException
		 */
		public synchronized  boolean validateMonthly(String name) throws SQLiteException, BunkerException, ParseException{
			Calendar x = Calendar.getInstance();
			x.setTime(this.getDate(name, "month"));
			schedule.stats.createMonthly(name);
			schedule.createTable("xtemp", statDef);
			schedule.deleteFromTable("xtemp", "");
			int count = x.getActualMaximum(Calendar.DAY_OF_MONTH);
			String query = "(",weekname,query2 = "(";
			Cursor c;

			System.out.println("Vosaye :: "+name);
			do{
				if(x.get(Calendar.DAY_OF_WEEK)==2&&count>=7){
					weekname = schedule.stats.selectWeekly(x.getTime());
					c = schedule.rawQuery("select name from sqlite_master where type = 'table' and name = '"+weekname+"'");
					if(c.moveToFirst()){
						query2 = query2 + "select * from "+weekname+" UNION ALL ";
						System.out.println("Vosaye :: "+weekname);
					}
					c.close();
					count = count - 7;
					x.add(Calendar.DATE, 7);
					
				}
				else{
					weekname = schedule.meta.selectFromIndex(x.getTime());
					if(!schedule.meta.selectFromIndex(x.getTime()).equals("labsdecore")&&!schedule.stats.inBlackHole(x.getTime())&&!schedule.tableExists(schedule.meta.selectRecord(x.getTime()))) schedule.meta.makeRecord(x.getTime());
					if(((!weekname.equals("labsdecoreblank"))&&(x.getTime().compareTo(schedule.start)>=0&&x.getTime().compareTo(schedule.end)<=0))&& (!schedule.stats.inBlackHole(x.getTime())) ){
							if((schedule.tableExists(schedule.meta.selectRecord(x.getTime()))))
							{
								System.out.println("Vosaye :: "+schedule.meta.selectRecord(x.getTime()));
								query = query + "select s.IDrel, r.attendance from "+schedule.meta.selectFromIndex(x.getTime())+" s, "+schedule.meta.selectRecord(x.getTime())+" r where s.mins = r.mins AND r.attendance != 3 UNION ALL ";
							}
							else {
								schedule.stats.flushMonthly(name);
								schedule.stats.deleteMonthly(name);
								return false;
							}
					}
					count--;
					x.add(Calendar.DATE, 1);
				}
			}while(count>0);
			
			if(!query.equals("(")){
				query = query.substring(0, query.length()-11)+ ")";
				//query = query + ")";
				//String attendance = "(select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)";
				
				//String total = "(select IDrel, count(attendance) as total from "+query+" group by IDrel)";
				
				//String join = "select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;";
				schedule.deleteFromTable("xtemp", "");
				//old query //schedule.execQuery("insert into xtemp (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 or attendance = 2 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 or attendance = 2 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;");
				schedule.execQuery("insert into xtemp (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2  as attendance from "+query+"where attendance = 2 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2  as attendance from "+query+"where attendance = 2 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;");
				schedule.execQuery("update xtemp set attendance = 0 where attendance is null");
			}
			if(!query2.equals("(")){
				//query2 = query2.substring(0, query2.length()-11)+ ")";
				//query2 = query2 + ")";
				query2 = "select IDrel, sum(attendance) as attendance, sum(total) as total from ("+query2.substring(0, query2.length()-11)+ ")"+") group by IDrel";
			}
			query = "select IDrel, sum(attendance) as attendance, sum(total)as total from (select * from xtemp ";
			if(!query2.equals("("))
				query = query + "UNION ALL "+query2+") group by IDrel";
			else query = query + ") group by IDrel";
			
			schedule.stats.flushMonthly(name);
			schedule.execQuery("insert into "+name+" (IDrel, attendance, total) "+query+"");
			schedule.deleteFromTable("xtemp", "");
			c = schedule.rawQuery("select IDrel from "+name+"");
			if(!c.moveToFirst()){schedule.stats.deleteMonthly(name);}
			c.close();
			System.out.println("Vosaye :: \n\n");
			System.out.println("Vosaye :: \n\n");
			System.out.println("Vosaye :: \n\n");
			System.out.println("Vosaye :: \n\n");
			System.out.println("Vosaye :: \n\n");
			System.out.println("Vosaye :: \n\n");
			System.out.println("Vosaye :: \n\n");
			return true;
			//collect weeks
			//collect days
			//System.gc();
		}
		
		/**
		 * @param date
		 */
		public synchronized  void flushMonthly(Date date){
			String name = selectMonthly(date);
			schedule.deleteFromTable(name, "");
		}
		/**
		 * @param name
		 */
		public  synchronized void flushMonthly(String name){
			schedule.deleteFromTable(name, "");
		}
		
		public  void addToPipeline(String name){
			try{
				ScheduleDatabase.this.execQuery("insert into pipeline values('"+name+"');");
				ScheduleDatabase.this.execQuery("insert into pipeline values('"+schedule.stats.getOverallRange()+"');");
				
				if(!ValidatorService.STARTED)
				context.startService(new Intent(context,ValidatorService.class));
				
			}
			catch(Exception e){
				//e.printStackTrace(); 
			}
		}
		public synchronized void deleteFromPipeline(String name){
			try{
				ScheduleDatabase.this.execQuery("delete from pipeline where name = '"+name+"';");
			}
			catch(Exception e){}
		}
		public synchronized String popFromPipeline(){
			Cursor c = ScheduleDatabase.this.rawQuery("select * from pipeline where name is not null and name not like '%range%' ORDER BY ROWID");
			if(c.moveToFirst()){
				String temp = c.getString(0);
				c.close();
				//ScheduleDatabase.this.deleteFromTable("pipeline", "where name = '"+temp+"'");
				return temp;
			}
			else{
				c.close();
				return null;
			}
		}
		public synchronized String popWeekFromPipeline(){
			Cursor c = ScheduleDatabase.this.rawQuery("select * from pipeline where name like 'week%' and name is not null ORDER BY ROWID");
			if(c.moveToFirst()){
				String temp = c.getString(0);
				c.close();
				//ScheduleDatabase.this.deleteFromTable("pipeline", "where name = '"+temp+"'");
				return temp;
			}
			else{
				c.close();
				return null;
			}
		}
		public synchronized String peakWeekFromPipeline(){
			Cursor c = ScheduleDatabase.this.rawQuery("select * from pipeline where name like 'week%' and name is not null ORDER BY ROWID");
			if(c.moveToFirst()){
				String temp = c.getString(0);
				c.close();
				return temp;
			}
			else{
				c.close();
				return null;
			}
		}
		public synchronized int countWeekFromPipeline(){
			Cursor c = ScheduleDatabase.this.rawQuery("select count(*) from pipeline where name like 'week%' and name is not null ORDER BY ROWID");
			if(c.moveToFirst()){
				int temp = c.getInt(0);
				c.close();
				return temp;
			}
			else{
				c.close();
				return 0;
			}
			
		}
		public synchronized int countMonthFromPipeline(){
			
			Cursor c = ScheduleDatabase.this.rawQuery("select count(*) from pipeline where name like 'month%' and name is not null ORDER BY ROWID");
			if(c.moveToFirst()){
				int temp = c.getInt(0);
				c.close();
				return temp;
			}
			else{
				c.close();
				return 0;
			}
		}
		public synchronized int countElementsFromPipeline(){
			
			Cursor c = ScheduleDatabase.this.rawQuery("select count(*) from pipeline where name not like '%null%' and name not like '%range%';");
			if(c.moveToFirst()){
				int temp = c.getInt(0);
				c.close();
				return temp;
			}
			else{
				c.close();
				return 0;
			}
		}
		public synchronized String popMonthFromPipeline(){
			Cursor c = ScheduleDatabase.this.rawQuery("select * from pipeline where name like 'month%' and name is not null and name not like '%null%' ORDER BY ROWID");
			if(c.moveToFirst()){
				String temp = c.getString(0);
				c.close();
				ScheduleDatabase.this.deleteFromTable("pipeline", "where name = '"+temp+"'");
				return temp;
			}
			else{
				c.close();
				return null;
			}
		}
		public synchronized boolean isPipelineEmpty(){
			Cursor c = ScheduleDatabase.this.rawQuery("select * from pipeline where name is not null and name not like '%null%' and name not like '%range%'");
			if(c.moveToFirst()){
				c.close();
				return false;
			}
			else{
				c.close();
				return true;
				}
		}
		public synchronized boolean pipelineHasRanges(){
			Cursor c = ScheduleDatabase.this.rawQuery("select * from pipeline where name is not null and name not like '%null%' and name like '%range%' and name not like '%fixed%'");
			if(c.moveToFirst()){
				c.close();
				return true;
			}
			else{
				c.close();
				return false;
				}
		}
		public synchronized String popRangeFromPipeline(){
			Cursor c = ScheduleDatabase.this.rawQuery("select * from pipeline where name like '%range%' and name is not null and name not like '%null%' ORDER BY ROWID");
			if(c.moveToFirst()){
				String temp = c.getString(0);
				c.close();
				if(!schedule.tableExists(temp)) {
					ScheduleDatabase.this.deleteFromTable("pipeline", "where name = '"+temp+"'");
					return null;
				}
				ScheduleDatabase.this.deleteFromTable("pipeline", "where name = '"+temp+"'");
				return temp;
			}
			else{
				c.close();
				return null;
			}
		}
		
		public synchronized  int countRangesFromPipeline(){
			
			Cursor c = ScheduleDatabase.this.rawQuery("select count(*) from pipeline where name not like '%null%' and name like '%range%' and name not null;");
			if(c.moveToFirst()){
				int temp = c.getInt(0);
				c.close();
				return temp;
			}
			else{
				c.close();
				return 0;
			}
		}
		
		
		//Special Features
	}//stats-end 
	
	
}//ScheduleDatabase-end
