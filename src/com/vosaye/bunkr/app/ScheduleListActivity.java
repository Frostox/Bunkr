package com.vosaye.bunkr.app;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;

import android.view.KeyEvent;

import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import net.sqlcipher.Cursor;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.Here.MyBroadcastReceiver;
import com.vosaye.bunkr.app.Here.MyBroadcastReceiverRefresh;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.files.FileHandler;
import com.vosaye.bunkr.services.ValidatorService;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ScheduleListActivity extends Activity {
	ActionBar action; 
	BunKar bunker;
	AutoCompleteTextView schedulenm;
	Button submit;
	AuthDatabase auth;
	public Cursor c;
	public ListView listView;
	public InputMethodManager mngr;
	ScheduleScrollListAdapter adapter;
	Vector<String> str = new Vector<String>();
	String suggestions[] = {""};
	String strtext = "";
	JSONParser jParser = new JSONParser();
	JSONArray items = null;
    MyBroadcastReceiver mBroadcast;
    MyBroadcastReceiverRefresh mBroadcastRefresh;
	Toast toast;
	
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);
        action = this.getActionBar();
        action.show();
        BunKar.count++;
        System.out.println("Vosayen : "+BunKar.count);
        action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "  Manage Semesters" + "</font>"));
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        bunker = (BunKar) this.getApplication();
        schedulenm = (AutoCompleteTextView) this.findViewById(R.id.sched_editText1);
        submit = (Button) this.findViewById(R.id.sched_button1);
        listView = (ListView) this.findViewById(R.id.sched_listView1);
        auth = bunker.settings;
        mngr = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        c = auth.rawQuery("select 1 as _id, name from schedules");
        adapter = new ScheduleScrollListAdapter(this,R.layout.schedule_list,c,new String[] {"name"},new int[]{R.id.sch_list_textView1});
        listView.setAdapter(adapter);
        

        mBroadcast = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATE");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.registerReceiver(mBroadcast, intentFilter);
		
		
		mBroadcastRefresh = new MyBroadcastReceiverRefresh();
		intentFilter = new IntentFilter("com.vosaye.bunkr.REFRESH");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.registerReceiver(mBroadcastRefresh, intentFilter);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN); 
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, suggestions);
        
        schedulenm.setAdapter(adapter);
        schedulenm.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
            	if(str.contains(schedulenm.getText().toString())){
            		downloadable = true;
            		schedulenm.dismissDropDown();
            	}
            	else
            		downloadable = false;
                return false;
            }
        });
        
        schedulenm.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
            	if(str.contains(schedulenm.getText().toString()))
            		downloadable = true;
            	else
            			downloadable = false;
                schedulenm.dismissDropDown();
                
            }
        });
        
        schedulenm.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            	
                
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            	
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
            	strtext = schedulenm.getText().toString();
				
        		new Search().execute();	
                // TODO Auto-generated method stub
            }
        });
    
    }
    
    public void onDestroy(){
    	super.onDestroy();
    	this.unregisterReceiver(mBroadcast);
    	this.unregisterReceiver(mBroadcastRefresh);
    	BunKar.count--;
    	if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
    	c.close();
    }
    
    AlertDialog dialog = null;
    boolean downloadable = false;
    public void onCreateSchedule(View v){
    	final String schedulenm;
		schedulenm = this.schedulenm.getText().toString();
		
		if(downloadable){
			
			
			mngr.hideSoftInputFromWindow(this.schedulenm.getWindowToken(), 0);
			final String str[] = schedulenm.split(" - ");
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Save As");
			builder.setTitle("Download Semester "+str[0]);
			builder.setCancelable(true);
			View view = ScheduleListActivity.this.getLayoutInflater().inflate(R.layout.edit_prompt_subject,null);
			builder.setView(view);
			final EditText nme = (EditText) view.findViewById(R.id.editpromptsubject_editText1);
			nme.setText(str[0]);
			if(auth.exists("select * from schedules where name = '"+nme.getText().toString().trim()+"'")){
				builder.setMessage("This Semester Exists! Wanna replace?");
				
			}
			else if(nme.getText().toString().length()<5||!StringUtils.isAlphanumericSpace(nme.getText().toString())){
				builder.setMessage("Name should be at least 5 chars in length. Only alphanumeric names are allowed");
					
			}
			else{
				builder.setMessage("Name your semester");
			}
			nme.setSelection(nme.getText().toString().trim().length());
			
			builder.setPositiveButton("Save", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					String name = nme.getText().toString().trim();
					if(name.length()<5||!StringUtils.isAlphanumericSpace(name)){
						Toast.makeText(ScheduleListActivity.this, "Invalid semester name. Downloading cancelled", Toast.LENGTH_LONG).show();
						return;
					}

					mngr.hideSoftInputFromWindow(nme.getWindowToken(), 0);
					new Download(schedulenm,name).execute();
					
				}});
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					
					
				}});
			nme.setHint("name your semester");
			nme.addTextChangedListener(new TextWatcher() {
	            @Override
	            public void onTextChanged(CharSequence s, int start, int before, int count) {
	            	
	                
	            }

	            @Override
	            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	            	
	                // TODO Auto-generated method stub
	            }

	            @Override
	            public void afterTextChanged(Editable s) {
	            	try{
	    				if(auth.exists("select * from schedules where name = '"+nme.getText().toString().trim()+"'")){
	    					if(dialog!=null)
	    					dialog.setMessage("This Semester Exists! Wanna replace?");
	    					
	    				}
	    				else if(nme.getText().toString().trim().length()<5||!StringUtils.isAlphanumericSpace(nme.getText().toString().trim())){
	    					if(dialog!=null)
		    					dialog.setMessage("Name should be at least 5 chars in length. Only alphanumeric names are allowed");
		    					
	    				}
	    				else{
	    					if(dialog!=null)
	    					dialog.setMessage("Name your semester");
	    				}
	    			}
	    			catch(Exception e){e.printStackTrace();}
	                // TODO Auto-generated method stub
	            }
	        });
			
			
			
			
			dialog = builder.show();

			nme.requestFocus();
			
			
			return;
		}
		//check if downloadable,
		//if downloadable download and return
		//else go ahead and create
		
		if(schedulenm.equals("")) {
			toast.setText("please enter a value");
			toast.show();
			
		}
		else if(schedulenm.startsWith(" ")){
			toast.setText("can't begin with a space");
			toast.show();
			
		}
		else if(!StringUtils.isAlphanumericSpace(schedulenm)){
			toast.setText("name should be alpha numeric");
			toast.show();

		}
		else if(schedulenm.length()<4){
			toast.setText("atleast 4 characters needed");
			toast.show();

		}
		else if(schedulenm.equalsIgnoreCase("NONE")){
			toast.setText("Invalid name, choose another");
			toast.show();
		}
		else{
			
				try {
					
					if(auth.valueExists("name","'"+schedulenm+"'", "schedules")){
						toast.setText("Schedule already exists");
						toast.show();

					}
					else{
					//bunker.haltNotifications();
					auth.schedules.newSchedule(schedulenm);
					bunker.createSchedule(schedulenm);
					bunker.name = schedulenm;
					//((Bunker)context.getApplication()).createSchedule(schedulenm);
					this.schedulenm.setText("");
					mngr.hideSoftInputFromWindow(this.schedulenm.getWindowToken(), 0);
					
 					c.requery();
					adapter.notifyDataSetChanged();
					
					AsyncTask<?, ?, ?> waiter = new AsyncTask<Object, Object, Object>(){

						@Override
						protected Object doInBackground(Object... arg0) {
							try {
								Thread.sleep(1500);
								
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							
							return null;
						}
						
						protected void onPostExecute(Object result) {
							
						}
						
					};
					//waiter.execute();

					
					}
				} catch (Exception e) {
					//toast.setText("Unexpected Error :"+e.getMessage());
					//toast.show();
					
				}
				
			
		}
		
		
		
    }
    
    
    
    class ScheduleScrollListAdapter extends SimpleCursorAdapter{
		@SuppressWarnings("deprecation")
		public ScheduleScrollListAdapter(Context scheduleScrollList, int layout, Cursor c,String[] from, int[] to) {
			super(scheduleScrollList, layout, c, from, to);
			
		}
		
	
		public View getView(final int position, View  view, ViewGroup viewGroup){
			if(c.moveToPosition(position)){
			if(view==null)view = LayoutInflater.from(ScheduleListActivity.this).inflate(R.layout.schedule_list, null);
			final ToggleButton notification = (ToggleButton) view.findViewById(R.id.sch_list_notificationActive);
			notification.setChecked(true);
			Button delete = (Button) view.findViewById(R.id.sch_list_button1);
			final TextView schName = (TextView) view.findViewById(R.id.sch_list_textView1);
			final TextView schPer = (TextView) view.findViewById(R.id.sch_list_textView2);
			schName.setText(c.getString(1)); 
			
			Cursor cc = auth.rawQuery("select currentPerc from schedules where name = '"+schName.getText()+"';");
			if(cc.moveToFirst())
				schPer.setText(""+String.format("%.2f", cc.getFloat(0))+" %");
			cc.close();
			
			LinearLayout layout= (LinearLayout) view.findViewById(R.id.sch_list_layout);
			final CheckBox defaulter = (CheckBox) view.findViewById(R.id.sch_list_defaulter);
			Cursor cx = auth.rawQuery("select value from settings where name = 'default'");
			if(cx.moveToFirst())
				if(cx.getString(0).equals(c.getString(1)))
					defaulter.setChecked(true);
				else defaulter.setChecked(false);
			cx.close();
			layout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					
					
					
					try {
						((BunKar) ScheduleListActivity.this.getApplication()).name = schName.getText().toString();
						
						
						Cursor cx = auth.rawQuery("select value from settings where name = 'default' and value not like 'NONE'");
						if(cx.moveToFirst()){
							((BunKar) ScheduleListActivity.this.getApplication()).name = cx.getString(0);
							com.vosaye.bunkr.base.ScheduleDatabase db = (bunker.getDatabase(bunker.name));
							Calendar checker = Calendar.getInstance();
							checker.setTime(db.start);
							try {
								if(!db.exists("select * from session")||db.standards.getEndOfTerm()==null){
									//SchedulesList.this.finish();
								if(!ValidatorService.STARTED)
									ScheduleListActivity.this.startService(new Intent(ScheduleListActivity.this,ValidatorService.class));
									cx.close();
									startActivityForResult(new Intent(ScheduleListActivity.this.getBaseContext(),TOS.class),0);
									
									//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
									
								}
								else if(!db.exists("select null from "+db.days[checker.get(Calendar.DAY_OF_WEEK)-1]+" where datenm = '"+bunker.sdf.format(checker.getTime())+"'")){
									if(!ValidatorService.STARTED)
										ScheduleListActivity.this.startService(new Intent(ScheduleListActivity.this,ValidatorService.class));
										cx.close();
										startActivityForResult(new Intent(ScheduleListActivity.this.getBaseContext(),EditAllStructures.class),0);
										
										//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
										
								}
								else{
									//this.finish();
									ValidatorService.FOCUSED = true;
									ValidatorService.FREEFLOW = false;
									cx.close();

									if(!ValidatorService.STARTED)
										ScheduleListActivity.this.startService(new Intent(ScheduleListActivity.this,ValidatorService.class));
									Intent it = new Intent(ScheduleListActivity.this.getBaseContext(),Here.class);
									it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ScheduleListActivity.this.finish();
									startActivityForResult(it,0);
									
									//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
									
						            ////overridependingTransition( R.anim.slide_activity_in, R.anim.slide_activity_out );
									//Toast.makeText(ScheduleListActivity.this, "Entry Point", Toast.LENGTH_LONG).show();
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
						else{
							com.vosaye.bunkr.base.ScheduleDatabase db = (bunker.getDatabase(bunker.name));
							try {
								Calendar checker = Calendar.getInstance();
								checker.setTime(db.standards.getStartOfTerm());
								if(!db.exists("select * from session")||db.standards.getEndOfTerm()==null){
									if(!ValidatorService.STARTED)
										ScheduleListActivity.this.startService(new Intent(ScheduleListActivity.this,ValidatorService.class));
									cx.close();
									startActivityForResult(new Intent(ScheduleListActivity.this.getBaseContext(),TOS.class),0);
									
									//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
									
								}

								
								else{
									//this.finish();
									ValidatorService.FOCUSED = true;
									ValidatorService.FREEFLOW = false;
									
									cx.close();

									if(!ValidatorService.STARTED)
										ScheduleListActivity.this.startService(new Intent(ScheduleListActivity.this,ValidatorService.class));
									Intent it = new Intent(ScheduleListActivity.this.getBaseContext(),Here.class);
									it.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									ScheduleListActivity.this.finish();
									startActivityForResult(it,0);
									
									////overridependingTransition( R.anim.slide_activity_in, R.anim.slide_activity_out );
									//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
									
									//Toast.makeText(ScheduleListActivity.this, "Entry Point", Toast.LENGTH_LONG).show();
								}
							} catch (ParseException e) {
								e.printStackTrace();
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						Toast.makeText(ScheduleListActivity.this, "Too quick! Click again.", Toast.LENGTH_SHORT).show();
						e.printStackTrace();
					}
					
					//Intent i = new Intent(ScheduleListActivity.this,);
					//ScheduleListActivity.this.startActivityForResult(i);
					
				}});
			defaulter.setOnClickListener(new OnClickListener(){

				
				@Override
				public void onClick(View arg0) {
					Cursor cx = auth.rawQuery("select value from settings where name = 'default'");
					if(cx.moveToFirst()){
						auth.execQuery("update settings set value = '"+schName.getText().toString()+"' where name = 'default'");
					}
					else{
						auth.execQuery("insert into settings values('default','"+schName.getText()+"')");
					}
					cx.close();
					c.requery();
					adapter.notifyDataSetChanged();
					listView.invalidate();
				}});
			try {
				if(!auth.schedules.isNotificationActive(schName.getText().toString())){
					notification.setChecked(false);
				}
			} catch (BunkerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace(); 
			}
			notification.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					try {
						auth.schedules.setNotification(schName.getText().toString(), notification.isChecked());
					} catch (BunkerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}});
			
			delete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleListActivity.this);
					builder.setMessage("Do you really wanna delete the schedule '"+schName.getText().toString()+"'\nOnce deleted, it cannot be brought back !");
					builder.setTitle("Warning");
					builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}});
					builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
							try{
							//((BunKar) ScheduleListActivity.this.getApplication()).haltNotifications();
							auth.schedules.deleteSchedule(schName.getText().toString());
							((BunKar) ScheduleListActivity.this.getApplication()).deleteSchedule(schName.getText().toString());
							c.requery();
							adapter.notifyDataSetChanged();
							//((Bunker)context.getApplication()).deleteSchedule(schName.getText().toString());
							FileHandler.deleteFile(ScheduleListActivity.this.getDatabasePath(schName.getText().toString()));
							
							
							
							arg0.dismiss();
							
							}
							catch(Exception e){
								
								e.printStackTrace();
							}
							c.requery();
							adapter.notifyDataSetChanged();
						}});
					//authenticator.schedules.deleteSchedule(textView.getText().toString());
					//c.requery();
					//CAdapter.notifyDataSetChanged();
					builder.create();
					builder.show();
				}});
			
			
			}
			return view;
			
		}
	
	
	}
    boolean pressedOnce = false;
    public void onBackPressed(){
    	
    		
    			if(pressedOnce){
    				//close databases
    				//exit
    				this.setResult(BunKar.RESULT_CLOSE_ALL);
    				this.finish();
    			}
    			pressedOnce = true;
    			Toast.makeText(this, "Press 'back' again to exit!", Toast.LENGTH_SHORT).show();
    			new Handler().postDelayed(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						pressedOnce = false;
					}},2000);
    		
    	
    	
    		
    }
    
    public void onPause(){ 
		super.onPause();
		ValidatorService.FREEFLOW = true;
	}
	public void onResume(){
		super.onResume();
		ValidatorService.FREEFLOW = false;
    	ValidatorService.FOCUSED = false;
    	if(adapter!=null)
    	adapter.notifyDataSetChanged();
    	
	}
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	       MenuInflater inflater = getMenuInflater();
	       inflater.inflate(R.menu.schedule_list, (Menu) menu);
	       return super.onCreateOptionsMenu(menu);
	    }
	 
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        // Handle item selection
	        if(item.getItemId()==R.id.action_settings){
	        	Intent intent = new Intent(this, Settings.class);
	        	intent.putExtra("focused", false);
	        	this.startActivityForResult(intent,0);
	        	
	        	//overridependingTransition(R.anim.fade_in, R.anim.fade_out);
				
	        	return true;
	        }else if (item.getItemId() == R.id.action_help) {
	        	Intent i = new Intent(this,HelpActivity.class);
				i.putExtra("explicit", "true");
				this.startActivity(i);
				
				return true;
			}
			return false;
			
	 }
	 public class MyBroadcastReceiver extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  int perc = intent.getIntExtra("perc", 0);
			  if(perc==-1){
				  c.requery();
				  adapter.notifyDataSetChanged();
				  System.out.println("changed");
			  
			  }
		 }
		  
	 }
	 
	 public class MyBroadcastReceiverRefresh extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  c.requery();
			 adapter.notifyDataSetChanged();
			 System.out.println("changed");
			  
		  }
	 }
	 
	 @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     switch(resultCode)
	     {
	     case BunKar.RESULT_CLOSE_ALL:
	         setResult(BunKar.RESULT_CLOSE_ALL);
	         finish();
	     }
	     super.onActivityResult(requestCode, resultCode, data);
	 }
	 class Search extends AsyncTask{
			String urlServer = "http://vosaye.hol.es/search.php";
			String checkDownloadable = "http://vosaye.hol.es/checkifdownloadable.php";
			
			String responseString = "";
			HttpResponse response;
			JSONObject json;
			@Override
			protected Object doInBackground(Object... params) {
				try
				{
					
					List<NameValuePair> msg = new ArrayList<NameValuePair>();
					
					try{
					String strx[] = strtext.split(" - ");
	                msg.add(new BasicNameValuePair("name", strx[0]));
	                msg.add(new BasicNameValuePair("tags", strx[1]));

	                // getting product details by making HTTP request
	                // Note that product details url will use GET request
	                json = jParser.makeHttpRequest(
	                        checkDownloadable, "GET", msg);
	                
	                
	                if(json.getInt("success")==1){
	                	downloadable = true;
	                }else downloadable = false;
					}catch(Exception e){e.printStackTrace();}
	                
	                
	                
					
					msg = new ArrayList<NameValuePair>();
	                msg.add(new BasicNameValuePair("searchQuery", strtext));

	                // getting product details by making HTTP request
	                // Note that product details url will use GET request
	                json = jParser.makeHttpRequest(
	                        urlServer, "GET", msg);
	                
	                
	                
	                //Log.d("searching done", ""+json.toString());
	                
	                
					
				}
				catch (Exception ex)
				{
				    //Exception handling
					ex.printStackTrace();
				}
				return null;
			}
			
			protected void onPostExecute(Object e){
				try {
					
					System.out.println("vosaye : "+json);
					str.clear();
					if(json.getInt("success")==1){
						items = json.getJSONArray("db");
						
						for(int i=0; i<items.length(); i++){
							JSONObject c = items.getJSONObject(i);
							
							str.add(c.getString("name")+" - "+c.getString("tag"));
						}
					}

				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				
				suggestions = new String[str.size()];
				for(int i=0; i<str.size();i++){
					suggestions[i] = str.elementAt(i);
				}
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(ScheduleListActivity.this,
		                android.R.layout.simple_dropdown_item_1line, suggestions);
				schedulenm.setAdapter(adapter);
		        adapter.notifyDataSetChanged();
		        //schedulenm.showDropDown();
			}
			
		}
	 
	 class Download extends AsyncTask{
		 AlertDialog dialog;
		 AlertDialog.Builder builder;
		 String nametag = "";
		 ProgressBar pb;
		 long filesize = 0;
		 int len = 0;
		 long filecompleted = 0;
		 String newname;
		 byte[] buffer = new byte[32767];
		 String url = "http://vosaye.hol.es/download.php?searchQuery=";
		 public Download(String nametag, String newname){
			 this.nametag = nametag;
			 this.newname = newname;
		 }
		 @Override
		 protected Object doInBackground(Object... params) {
			 
			 	try{
			 	String strx[] = nametag.split(" - ");
			 	
			 	DefaultHttpClient client = new DefaultHttpClient();
	            HttpGet httpGet = new HttpGet(url+strx[0]+strx[1]);
	            
	            try {
	                HttpResponse execute = client.execute(httpGet);
	                InputStream content = execute.getEntity().getContent();

	                filesize = execute.getEntity().getContentLength();
	                
	                ScheduleDatabase old = bunker.getDatabase(newname);
	                if(old!=null) {
	                	old.close();
	                	auth.schedules.deleteSchedule(old.getName());
	                	bunker.deleteSchedule(newname);
	                }
	                
	                
	                String path = ScheduleListActivity.this.getApplicationInfo().dataDir;
	        		path = path+"/databases";
	                File file = new File(path+"/"+newname); 
	                if(file.exists()) file.delete();
	                FileOutputStream fileOutput = new FileOutputStream(file);

	                 while ((len = content.read(buffer, 0, 32767)) > 0) {
	                     fileOutput.write(buffer, 0, len);
	                     filecompleted += len;
	                     Thread.sleep(100);
	                     pb.setProgress((int) (((float)filecompleted/(float)filesize)*100));
	                     System.out.println("Vosaye : "+(int) (((float)filecompleted/(float)filesize)*100)+"    "+(((float)filecompleted/(float)filesize)*100));
	                 }

	                fileOutput.close();
	                
	                auth.schedules.newSchedule(newname);
					bunker.createSchedule(newname);
					ScheduleDatabase sd = bunker.getDatabase(newname);
					sd.downloaded = true;
					
	                
					

	            } catch (Exception e) {
	                e.printStackTrace();
	            }
		        
			 	}catch(Exception e){
			 		e.printStackTrace();
			 		Toast.makeText(ScheduleListActivity.this, "Downloading Failed", Toast.LENGTH_SHORT).show();
			 		
			 	}
			 return null;
		 }
		 protected void onPostExecute(Object e){
	 		 dialog.dismiss();
	 		c.requery();
			adapter.notifyDataSetChanged();
			
	 	 }
	 	 protected void onPreExecute(){ 
	 		 builder = new AlertDialog.Builder(ScheduleListActivity.this);
	 		 builder.setCancelable(false);
	 		 builder.setTitle("Downloading");
	 		 builder.setMessage("Downloading and setting up your semester!");
	 		 View view = ScheduleListActivity.this.getLayoutInflater().inflate(R.layout.download_progress, null);
	 		 builder.setView(view);
	 		 pb = (ProgressBar) view.findViewById(R.id.progressBar1);
	 		 pb.setProgress(0);
	 		 pb.setMax(100);
	 		 dialog = builder.show();
	 	 }
	 
	 
	 
	 }
	 	
	 	
}
