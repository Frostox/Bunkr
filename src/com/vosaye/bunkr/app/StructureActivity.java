package com.vosaye.bunkr.app;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import org.apache.commons.lang3.StringUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.WeekDayStructure.MyBroadcastReceiver;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.fragments.StructureEditterFragment;
import com.vosaye.bunkr.fragments.Today;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.ValidatorService;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.annotation.TargetApi;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;

public class StructureActivity extends SherlockFragmentActivity implements TabListener{
	 	String pseudoStructureName = "";
	 	SimpleDateFormat sdf;
	 	SimpleDateFormat sdf2;
	 	Date date;
		BunKar bunker;
		ScheduleDatabase sched;
		AlertDialog dialog_forsetter;
		StructureEditterFragment sef;
		int weeks = 0;
		int position = 0;
		boolean locked = false;
		Date start;
		Today today;
		Menu menu;
		Tab tab1,tab2;
	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	       // setContentView(R.layout.activity_tos);
	        ActionBar action = this.getSupportActionBar();
	        action.show();
	        BunKar.count++;
	        //action.setTitle("  Bunk Kar na!");
	        setContentView(R.layout.activity_structure);
			bunker = (BunKar) this.getApplication();
			sdf = BunKar.sdf;
			sdf2 = BunKar.sdf3;
			sched = bunker.getDatabase(bunker.name);
			try {
				start = sdf.parse(this.getIntent().getStringExtra("date"));
				weeks = Integer.parseInt(this.getIntent().getStringExtra("count"));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			android.app.ActionBar theAction = getActionBar();
			theAction.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	        tab1 = theAction.newTab().setText("Today").setTabListener(this);
	        theAction.addTab(tab1);
	        
	        if(weeks!=-1){
	        	tab2 = getActionBar().newTab().setText("Stats").setTabListener(this);
	        	theAction.addTab(tab2);
		        tab2.setText("Edit Time Table");
		       // tab1.setText("Time Table");
	        }
	        tab1.setText("Attendance");
	        
			
	        
	        getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action));
	        getSupportActionBar().setStackedBackgroundDrawable(this.getResources().getDrawable(R.drawable.actiontab));
	        
	        getActionBar().setDisplayHomeAsUpEnabled(true);
	        
			
			action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;" +sdf2.format(start)+ "</font>"));
	        
	        action.setSubtitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+bunker.name + "</font>"));
			
			mBroadcast = new MyBroadcastReceiver();
			IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATESTR");
			intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
			registerReceiver(mBroadcast, intentFilter);
			tab1.select();
			Bundle bundle = new Bundle();
			bundle.putString("date", this.getIntent().getStringExtra("date"));
			today = new Today();
			today.setStrActivity(this);
			System.out.println("Vosayye : 1 "+today.getLocked());
			
			
			
			
			today.setArguments(bundle);
			
			android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			
			try{
	        	ft.replace(R.id.act_str_content_frame, today);
	        }
	        catch(Exception e){e.printStackTrace();}
	        finally{ft.commit();}
			
			this.invalidateOptionsMenu();
			
			
	    }
	    public void onStart(){
	    	super.onStart();
	    	
	    	try{
	    	Calendar cal = Calendar.getInstance();
			Calendar tom = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.DATE, -1);
			tom.set(Calendar.HOUR_OF_DAY, 0);
			tom.set(Calendar.MINUTE, 0);
			tom.set(Calendar.SECOND, 0);
			tom.add(Calendar.DATE, 2);
			if(start.before(cal.getTime())||start.after(tom.getTime())){
    			today.setLocked(true);
    			locked = true;
    			today.inflate(); 
    		}
			else{
				today.setLocked(false);
				locked = false;
				today.inflate();
			}
			System.out.println("Vosayye : 2 "+today.getLocked());
			
	    	} catch(Exception e){}
			invalidateOptionsMenu();
	    }
	    private void initialisePaging()
		{
			List<Fragment> fragments = new Vector<Fragment>();
			Bundle bundle = new Bundle();
			bundle.putString("date", this.getIntent().getStringExtra("date"));
			today = new Today();
			today.setArguments(bundle);
			fragments.add(today);
			
			if(weeks!=-1){
				bundle = new Bundle();
				pseudoStructureName = sched.meta.createPseudoStructure();
				try {
					date = sdf.parse(this.getIntent().getStringExtra("date"));
					if(!sched.meta.selectFromIndex(date).equals("labsdecoreblank"))
					sched.meta.copyToPseudoStructure(pseudoStructureName, sched.meta.selectFromIndex(date), date);
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
				bundle.putString("pseudoStructureName", pseudoStructureName);
				sef = new StructureEditterFragment();
				sef.setArguments(bundle);
				//fragments.add(Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName()));
				fragments.add(sef);
			}
			else{
				today.setLocked(true);
			}
			
			
			
			
			
			
		}
	    
	    
	    
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	selectMenu(position,menu);
	    	this.menu = menu;
	        return super.onCreateOptionsMenu(menu);
	    }
	    
	    @Override
	    public boolean onPrepareOptionsMenu(Menu menu) {
	    	selectMenu(position,menu);
	        return super.onPrepareOptionsMenu(menu);
	    
	    }
	    
	    public void selectMenu(int position, Menu menu){
	    	menu.clear();
	    	com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
	    	
	    	
	    	
	    	
	    	if(position == 0&&weeks!=-1){
	    		inflater.inflate(R.menu.attendance, (com.actionbarsherlock.view.Menu) menu);
	    		MenuItem mi = menu.findItem(R.id.att_locker);
	    		
	    		if(today!=null){
	    		
				
				if(today.getLocked()){
					mi.setIcon(R.drawable.login);
        			mi.setTitle("Unock");
				}
				else{
					mi.setIcon(R.drawable.un);
        			mi.setTitle("Lock");
				}
				
				
	    		//Toast.makeText(this, "at "+position, Toast.LENGTH_SHORT).show();
	    		}
	    	}else if(position == 1){
	    		//Toast.makeText(this, "at "+position, Toast.LENGTH_SHORT).show();
    			inflater.inflate(R.menu.structure, (com.actionbarsherlock.view.Menu) menu);
    		}
	    	
	    	
	    }
	    
	    ProgressBar bar;
	    public boolean onOptionsItemSelected(MenuItem item) {
	        int itemId = item.getItemId();
			if (itemId == R.id.str_done_button) {
				AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(this);
				builder_forsetter.setTitle("Updating Table");
				View view = this.getLayoutInflater().inflate(R.layout.setting_term_wait_dialog, null);
				TextView title, subtitle;
				title = (TextView) view.findViewById(R.id.atos_textView1);
				subtitle = (TextView) view.findViewById(R.id.atos_textView2);
				bar = (ProgressBar) view.findViewById(R.id.atos_progressBar1);
				bar.setMax(100);
				bar.setProgress(0);
				title.setText("Please wait a moment");
				subtitle.setText("");
				builder_forsetter.setView(view);
				builder_forsetter.setCancelable(false);
				dialog_forsetter = builder_forsetter.create();
				dialog_forsetter.show();
				BackgroundStructureEditter bg = new BackgroundStructureEditter(this);
				bg.execute();
				return true;
			} else if (itemId == R.id.str_blank) {
				sched.deleteFromTable(this.pseudoStructureName, "");
				sef.notifyData();
				sef.setSaved(false);
				return true;
			} else if (itemId == R.id.str_op) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose number of weeks");
				View v = this.getLayoutInflater().inflate(R.layout.picker, null);
				builder.setView(v);
				final Button b1,b2,b3;
				final EditText txt;
				b1 = (Button) v.findViewById(R.id.picker_plus);
				b2 = (Button) v.findViewById(R.id.picker_minus);
				b3 = (Button) v.findViewById(R.id.picker_end);
				txt = (EditText) v.findViewById(R.id.picker_text);
				txt.setText((StructureActivity.this.weeks+1)+"");
				TextWatcher mTextWatchertxt = new TextWatcher() {
					String string;
				    @Override
				    public void beforeTextChanged(CharSequence s, int arg1, int arg2,int arg3) {
				        // YOU STRING BEFORE CHANGE
				    	string = txt.getText().toString();
				    	txt.setSelection(txt.getText().toString().length());
				    }
				    @Override
				    public void onTextChanged(CharSequence s, int start, int before,int count) {
				          // CHARS INPUT BY USER
				    	try{
				    		  if(Integer.parseInt(txt.getText().toString())>(sched.meta.countWeeks(StructureActivity.this.start, sched.end)+1)||Integer.parseInt(txt.getText().toString())<1){
				    			  //txt.setText(Html.fromHtml("<font color=\"#eeeeee\">" + string + "</font>"));
				    			  txt.setText(string);
				    		  }
				    	  }
				    	  catch(NumberFormatException e){} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }
				    @Override
				    public void afterTextChanged(Editable s) {
				    	//AFTER CHANGED
				    }
					
					
				};
				txt.addTextChangedListener(mTextWatchertxt);
				b3.setOnClickListener(new View.OnClickListener(){

					@Override
					public void onClick(View v) {
						try {
							StructureActivity.this.weeks = sched.meta.countWeeks(start, sched.end);
							txt.setText((StructureActivity.this.weeks+1)+"");
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}

					
					
				});
				b1.setOnClickListener(new View.OnClickListener(){

					
					@Override
					public void onClick(View v) {
						try{
							
							
							
							
						if(!StringUtils.isNumeric(txt.getText())){
							txt.setText("1");
						}
						else if(Integer.parseInt(txt.getText().toString())>=(sched.meta.countWeeks(start, sched.end))+1){
							
						}
						else{
							int i = Integer.parseInt(txt.getText().toString());
							i++;
							txt.setText(i+"");
						}
						}
						catch(BunkerException e){e.printStackTrace();}
					}});
				b2.setOnClickListener(new View.OnClickListener(){

					
					@Override
					public void onClick(View v) {
						if(!StringUtils.isNumeric(txt.getText())){
							txt.setText("1");
						}
						else if(Integer.parseInt(txt.getText().toString())<=1){
						}
						else{
							int i = Integer.parseInt(txt.getText().toString());
							i--;
							txt.setText(i+"");
						}
					}});
				builder.setPositiveButton("Set", new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(Integer.parseInt(txt.getText().toString())<1){
							Toast.makeText(StructureActivity.this, "Number of weeks cant be less than 1", Toast.LENGTH_LONG).show();
							dialog.dismiss();
							return;
						}
						StructureActivity.this.weeks = Integer.parseInt(txt.getText().toString())-1;
						MenuItem m = menu.findItem(R.id.str_op);
						
						//m.setTitle((StructureActivity.this.weeks+1)+" "+((StructureActivity.this.weeks+1) <= 1 ? "Week" : "Weeks"));
						m.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + ((StructureActivity.this.weeks+1)+" "+((StructureActivity.this.weeks+1) <= 1 ? "Week" : "Weeks")) + "</font>"));
						dialog.dismiss();
					}});
				builder.setNegativeButton("Cancel", new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}});
				builder.show();
				return true;
			} else if (itemId == R.id.att_locker) {
				if(today.getLocked()){
					//item.setIcon(R.drawable.unlock);
					//item.setTitle("Lock");
					today.setLocked(false);
					locked = today.getLocked();
					invalidateOptionsMenu();
					today.inflate();
				}
				else{
					//item.setIcon(R.drawable.lock);
					//item.setTitle("Unlock");
					today.setLocked(true);
					locked = today.getLocked();
					invalidateOptionsMenu();
					today.inflate();
				}
				return true;
			} else if(itemId == android.R.id.home){
				setResult(1);
				finish();
				return true;
			}
			else {
				return super.onOptionsItemSelected(item);
			}
	        
	    } 
	    
	    
	    
	    
	    
	    
	    
	    
	    class BackgroundStructureEditter extends AsyncTask<String,Void,String>{
	    	
	    	StructureActivity edit;
	    	public BackgroundStructureEditter(StructureActivity edit){
	    		this.edit = edit;
	    	}
	    	
			@Override
			protected String doInBackground(String... params) {
				
	        	Calendar cal = Calendar.getInstance();
				cal.setTime(edit.date);
				
				MaintenanceManager.HALT = true;
				if(bunker.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
					while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
						//wait for manager to be halted by this thread...
					}
				}
				
				ValidatorService.HALT = true;
				 
				while(ValidatorService.status == ValidatorService.BUSY){
					if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
						startService(new Intent(StructureActivity.this,ValidatorService.class));
				}
				
				Cursor c = bunker.settings.rawQuery("select max(id) from labsdecore"+bunker.name.replaceAll(" ", "_")+" where id > 0;");
				if(c.moveToFirst()){
					if(c.getString(0)==null){
					//sched.close();
					bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
					//sched.open();
					}
				}
				else {
					//sched.close();
					bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
					//sched.open();
				}
				c.close();
				sched.beginTransaction();
				
				
					//Toast.makeText(EditAllStructures.this, ""+sdf.format(cal.getTime()), Toast.LENGTH_LONG).show();
					try {
						if(!sched.isEmpty(edit.pseudoStructureName)){
							String name = sched.meta.insertIntoIndex(cal.getTime(), sched.meta.getUUID(""), weeks, true);
							if(edit.pseudoStructureName!=null){
								sched.meta.copyStructure(edit.pseudoStructureName, name);
								sched.meta.copyRecordBack(edit.pseudoStructureName, cal.getTime(), weeks);
								Calendar cal2 = cal;
								cal2.add(Calendar.DATE, weeks*7);
								sched.stats.collectAll(cal.getTime(), cal2.getTime());
							}
						}
						else{
							StructureActivity.this.sched.meta.insertBlankIntoIndex(cal.getTime(), weeks);
						}
						//sched.dropTable(edit.pseudoStructureName);
						sched.commit();
						//sched.close();
						bunker.tempBackupDbase(bunker.name, Calendar.getInstance().getTime(), "Editted schedule for "+sdf2.format(cal.getTime()));
						//sched.open();
						ValidatorService.HALT = false;
					} catch (SQLiteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BunkerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally{
						if(sched.getDatabase().inTransaction()) sched.rollback();
						ValidatorService.HALT = false;
					}
				return null;
			}
			protected void onPostExecute(String param){
				dialog_forsetter.dismiss();
				edit.sef.updateList();
	    		
	    		edit.sef.setSaved(true);
				
	    		//StructureActivity.this.finish();
				//startActivity(new Intent(StructureActivity.this, Here.class));
				
			}
	    }




	    public void onDestroy(){
	    	super.onDestroy();
	    	if(!pseudoStructureName.equals(""))
	    	sched.dropTable(StructureActivity.this.pseudoStructureName);
	    	BunKar.count--;
	    	if(BunKar.count==0){
	    		((BunKar) this.getApplication()).deleteAllCache();
	    	}
	    	this.unregisterReceiver(this.mBroadcast);
	    	
	    	if(sef!=null) sef.closeList();
	    	if(today!=null) today.closeList();
	    }
		
		public void onPause(){
			super.onPause();
			//if(sef!=null) sef.closeList();
			ValidatorService.FREEFLOW = false;
		}
		public void onResume(){
			super.onResume();
			//if(sef!=null) sef.updateList();
			ValidatorService.FREEFLOW = false;
	    	ValidatorService.FOCUSED = true;

	    	//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
		}
		
		MyBroadcastReceiver mBroadcast;
		public class MyBroadcastReceiver extends BroadcastReceiver {

			  @Override
			  public void onReceive(Context context, Intent intent) {
				  int perc = intent.getIntExtra("perc", 0);
				  if(bar!=null)
					  bar.setProgress(perc);
				  
			  }
			 }



		
		@Override
		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			if(tab.getText().equals("Attendance")){
				position = 0;
				if(sef!=null) sef.closeList();
				sef = null;
				
				Bundle bundle = new Bundle();
				bundle.putString("date", this.getIntent().getStringExtra("date"));
				today = new Today();

				today.setStrActivity(this);
				
				
				
				today.setArguments(bundle);
				
				
				android.support.v4.app.FragmentTransaction ftt = getSupportFragmentManager().beginTransaction();
				ftt.setCustomAnimations(R.anim.enter_from_left,
		                R.anim.exit_to_right);
				try{
		        	ftt.replace(R.id.act_str_content_frame, today);
		        }
		        catch(Exception e){e.printStackTrace();}
		        finally{ftt.commit();this.invalidateOptionsMenu();}
				System.out.println("Vosayye : 4 "+today.getLocked());
				today.setLocked(locked);
			}
			else{
				position = 1;
				
				if(today!=null){
				locked = today.getLocked();
				System.out.println("Vosayye : 3 "+today.getLocked());
				today.closeList();
				
				}
				
				android.support.v4.app.FragmentTransaction ftt = getSupportFragmentManager().beginTransaction();
				today = null;
				sef = new StructureEditterFragment();
				Bundle bundle = new Bundle();
				pseudoStructureName = sched.meta.createPseudoStructure();
				try {
					date = sdf.parse(this.getIntent().getStringExtra("date"));
					if(!sched.meta.selectFromIndex(date).equals("labsdecoreblank"))
					sched.meta.copyToPseudoStructure(pseudoStructureName, sched.meta.selectFromIndex(date), date);
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
				bundle.putString("pseudoStructureName", pseudoStructureName);
				sef = new StructureEditterFragment();
				sef.setArguments(bundle);
				ftt.setCustomAnimations(R.anim.enter_from_right,
		                R.anim.exit_to_left);
				try{
		        	ftt.replace(R.id.act_str_content_frame, sef);
		        }
		        catch(Exception e){e.printStackTrace();}
		        finally{ftt.commit();}
			}
			
			this.invalidateOptionsMenu();
			
			if(today!=null){
			today.setLocked(locked);
			today.inflate();
			}
		}

		@Override
		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// TODO Auto-generated method stub
			if(tab.getText().equals("Attendance"))
				position = 0;
			else
				position = 1;
			
			this.invalidateOptionsMenu();
			
		}
		
		
}
