package com.vosaye.bunkr.base;


import java.util.Stack;
import java.util.Vector;

import android.content.Context;
import net.sqlcipher.Cursor;
import net.sqlcipher.database.*;
import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;
/** 
 *  The class serves as a interface between the database and android app.
 *  <br>
 *  This class include all lower level, non-logic, non-app related methods for <br>
 *  database operations.<br>
 *  The class needs to be instantiated using the provided constructor.
 *  <br>
 *  The methods onCreate, onOpen, onClose can be overridden for specific applications.
 *  <br>
 *  The class doesn't use preparedStatements because it uses complex dynamically built queries.
 * @author Roger Cores
 * @version 1.01
 * 
 */
public class BunkerDatabase extends SQLiteOpenHelper { //tested OK
	SQLiteDatabase dbase;
	Cursor dbaseC;
	String name;
	Context context;
	//private String key = "Holiness is in right action, and courage on behalf of those who cannot defend themselves.";
	protected String key = "Holiness is in right actions and courage on behalf of those who cannot defend themselves";
	/** The stack contains savepoints<br>
	 *  The stack is cleared every transaction commit or rollback
	 * 
	 */
	public Stack<String> savepoints;
	
	public   String  getName(){return name;}
	/**
	 * Constructs a BunkerDatabase.
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public BunkerDatabase(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.name = name;
		this.context = context;
		savepoints = new Stack<String>();
		//dbase = this.openWritable(); 
		SQLiteDatabase.loadLibs(context);
	}

	
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public  void onCreate(SQLiteDatabase arg0) { //OK
		
	}
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onOpen(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public  void onOpen(SQLiteDatabase db){ //OK
		if(!db.isReadOnly()){
			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public   void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) { //OK
		
	}
	
	/**
	 * This method returns true if a select query returns atleast one result, which should not be null.<br>
	 * else it returns false. The method is used to check if a query return any valid result.
	 * @param query
	 * @return
	 */
	public synchronized  boolean exists(String query){
		Cursor c = this.rawQuery(query);
		if(c.moveToFirst())
			if(c.getString(0)!= null){ 
				c.close();
				return true;
			}
		c.close();
		return false;
	}
	
	
	
	/**
	 * This method is used to construct and fire a select query.
	 * The method should not be used, instead the query should be 
	 * executed directly using rawQuery.
	 * @deprecated
	 * @param tableNames
	 * @param columnNames
	 * @param whereCondition
	 * @return
	 */
	public synchronized  Cursor selectQuery(String tableNames[],String columnNames[],String whereCondition){ //tested OK 
		String query = "select ";
		query = buildQuery(query,columnNames);
		query = query + "from ";
		query = buildQuery(query,tableNames);
		query = query + " " + whereCondition;
		return this.rawQuery(query);
	}
	/**
	 * This method inserts values to specified table
	 * @param tableName
	 * @param values
	 * @throws SQLiteException
	 */
	public synchronized  void insertIntoTable(String tableName,String values[]) throws SQLiteException{ //tested OK
		String query = "insert into "+tableName+" values(";
		query = buildQuery(query,values);
		query = query + ")";this.execQuery(query);
		
	}
	/**
	 * The method creates a new table with the specified definitions
	 * @param tableName
	 * @param definitions
	 * @throws SQLiteException
	 */
	public synchronized  void createTable(String tableName,String definitions[]) throws SQLiteException{ //tested OK
		String query = "create table if not exists "+tableName+" (";
		query = buildQuery(query,definitions);
		query = query + ")";
		this.execQuery(query);
		
	}
	
	public synchronized  SQLiteDatabase getDatabase(){
		return dbase;
	}
	
	/**This method inserts values to specified table
	 * @param tableName
	 * @param values
	 * @throws SQLiteException
	 */
	public synchronized  void insertIntoTable(String tableName,Vector<String> values) throws SQLiteException{ //tested OK
		String query = "insert into "+tableName+" values(";
		query = buildQuery(query,values);
		query = query + ")";this.execQuery(query);
		
	}
	/**The method creates a new table with the specified definitions
	 * @param tableName
	 * @param definitions
	 * @throws SQLiteException
	 */
	public synchronized  void createTable(String tableName,Vector<String> definitions) throws SQLiteException{ //tested OK
		String query = "create table if not exists "+tableName+" (";
		query = buildQuery(query,definitions);
		query = query + ")";
		this.execQuery(query);
		
	}
	
	
	
	/** Drops a specified table
	 * @param tableName
	 * @throws SQLiteException
	 */
	public synchronized void dropTable(String tableName) throws SQLiteException{ //tested OK
		String query = "drop table if exists "+tableName+";";
		this.execQuery(query);
	}
	/** The method is app-specific and logic-specific to 'bunker'
	 *  It is used to construct and create trigger on structures.
	 * @param query
	 * @param name
	 * @param purpose
	 * @throws SQLiteException
	 */
	public synchronized  void createTrigger(String query, String name, String purpose) throws SQLiteException{
		query = query.replaceAll("7", name+purpose).replaceAll("/", name);
		this.execQuery(query);
		
	}
	
 	/** Begins a transactions
	 */
	 
	public   void beginTransaction(){
		this.dbase.beginTransaction();
		
		
	}
	
	/**
	 * This method is used to construct and execute update queries when only one value is supposed to be set.
	 * Use execQuery instead if a query needs more than one attirbute to be set.
	 * @param columnName
	 * @param value
	 * @param tableName
	 * @param whereCondition
	 * @throws SQLiteException
	 */
	public synchronized  void set(String columnName,String value,String tableName,String whereCondition) throws SQLiteException{ //tested OK
		String query = "update "+tableName+" set "+columnName+"="+value+" "+whereCondition+";";
		this.execQuery(query);
	}
	/**
	 * The methods renames a table from old name to a new name specified.
	 * @param tableName
	 * @param newname
	 * @throws SQLiteException
	 */
	public synchronized  void renameTable(String tableName, String newname) throws SQLiteException{ //tested OK
		String query = "alter table "+tableName+" rename to "+newname;
		this.execQuery(query);
		
	}
	/** deletes tuples from table based on the conditions specified in 'where'
	 * @param tableName
	 * @param whereCondition
	 * @throws SQLiteException
	 */
	public synchronized  void deleteFromTable(String tableName,String whereCondition) throws SQLiteException{ //tested OK
		String query = "delete from "+tableName+" "+whereCondition+";";
		this.execQuery(query);
	}
	
	/**
	 * rolls back the current transaction.
	 * 
	 */
	
 	public   void rollback(){
		this.dbase.endTransaction();
		savepoints.clear();
	}

	/**
	 * commits the current transaction.
	 */
	
 	public   void commit(){
		this.dbase.setTransactionSuccessful();
		this.dbase.endTransaction();
	}

	
	
 	public void savePoint(String name){
		this.execQuery("savepoint "+name);
	}

	
 	public void rollbackTo(String name){
		if(savepoints.contains(name)){
			while(!savepoints.peek().equals(name)){
				savepoints.pop();
			}
			savepoints.pop();
			this.execQuery("rollback to savepoint "+name);
		}
	}
	public void rollbackToPrev(){
		if(!savepoints.empty()) this.execQuery(savepoints.pop());
	}

	/**The method constructs a query. This method should not be used directly<br>
	 * This method is used by other methods of BunkerDatabase to construct queries<br>
	 * to be fired to the database in question.
	 * @param query
	 * @param str
	 * @return
	 */
	public  synchronized String buildQuery(String query, String str[]){ //tested OK
		for(int i=0;i<str.length-1;i++){
			query = query + str[i] +",";
		}
		query = query + str[str.length-1]+" ";
		return query;
	}
	/**he method constructs a query. This method should not be used directly<br>
	 * This method is used by other methods of BunkerDatabase to construct queries<br>
	 * to be fired to the database in question.
	 * @param query
	 * @param str
	 * @return
	 */
	public synchronized  String buildQuery(String query, Vector<String> str){ //tested OK
		for(int i=0;i<str.size()-1;i++){
			query = query + str.elementAt(i) +",";
		}
		query = query + str.elementAt(str.size()-1)+" ";
		return query;
	}
	
 	/**opens a writable database.
 	 * @return
 	 */
 	public  synchronized SQLiteDatabase openWritable(){ //OK
 		SQLiteDatabase dbase = this.getWritableDatabase(key);
		return dbase;
	}
	/**opens a readable database.
	 * @return
	 */
	public synchronized  SQLiteDatabase openReadable(){ //OK
 		SQLiteDatabase dbase = this.getWritableDatabase(key);
		return dbase;
	}

	
 	/**rawQuery fires a select query to the database and returns a cursor<br>
 	 * which contains all the data.
 	 * @param query
 	 * @return
 	 */
 	public synchronized  Cursor rawQuery(String query){ //tested OK
	Cursor dbaseC = null; 
	if(dbase==null)
		dbase = this.openWritable();

		try {
			dbaseC = dbase.rawQuery(query, null);
			return dbaseC;
		} catch (Exception e) {
			//Toast.makeText(context, "error "+e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		return null;
	}
 	
 	public Cursor rawQueryReplica(String query){
 		Cursor dbaseC = null; 
 		if(dbase==null)
 			dbase = this.openWritable();

 			try {
 				dbaseC = dbase.rawQuery(query, null);
 				return dbaseC;
 			} catch (Exception e) {
 				//Toast.makeText(context, "error "+e.getMessage(), Toast.LENGTH_LONG).show();
 				e.printStackTrace();
 			}
 			return null;
 	}
 	
 	/**Returns true if a value exists in specified attribute in specified table.<br>
 	 * else returns false.
 	 * @param attribute
 	 * @param value
 	 * @param table
 	 * @return
 	 */
 	public synchronized  boolean valueExists(String attribute,String value, String table){
 		Cursor c = this.rawQuery("select "+attribute+" from "+table+" where "+attribute+" = "+value+"");
 		if(c.moveToFirst()){
 			c.close();
 			return true;
 		}
 		c.close();
 		return false;
 	}
 	
 	public synchronized boolean isEmpty(String table){
 		Cursor c = this.rawQuery("select * from "+table+";");
 		if(c.moveToFirst()){
 			c.close();
 			return false;
 		}
 		c.close();
 		return true;
 	}
 	
 	public synchronized  boolean recordExists(String attribute, String table){
 		Cursor c = this.rawQuery("select "+attribute+" from "+table);
 		if(c.moveToFirst()){
 			c.close();
 			return true;
 		}
 		c.close();
 		return false;
 	}
 	
	/**The method executes a query. Usually queries other than select queries.
	 * @param query
	 * @throws SQLiteException
	 */
	public synchronized  void execQuery(String query)throws SQLiteException { //tested OK
		
		if(dbase==null)
		dbase = this.openWritable();

		dbase.execSQL(query);
	}
	
	public void execQueryReplica(String query) throws SQLiteException{
		if(dbase==null)
			dbase = this.openWritable();

			dbase.execSQL(query);
		
	}
	/** returns true if specified table exists.
	 * @param name
	 * @return
	 * @throws SQLiteException
	 */
	public synchronized  boolean tableExists(String name)throws SQLiteException {
		Cursor c = rawQuery("select name from sqlite_master where type = 'table' and name = '"+name+"'");
		if(c.moveToFirst()){
			c.close();
			return true;
		}
		c.close();
		return false;
	}
	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#close()
	 */
	public  synchronized void close(){ //tested OK
		if(dbase!=null)
		dbase.close();
	}
	/**The method sets database for this class.
	 * Should not be explicitly called, it should be used only in onCreate of classes that inherit this class.
	 * @param dbase
	 */
	public  synchronized void setDbase(SQLiteDatabase dbase){ //OK
		this.dbase=dbase;
	}

	
}
