package com.vosaye.bunkr.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sqlcipher.Cursor;

import org.apache.commons.lang3.StringUtils;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.R.id;
import com.vosaye.bunkr.R.layout;
import com.vosaye.bunkr.R.menu;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.Uploader;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {
	boolean focused = false;
	ListView listView;
	MenuAdapter adapter;
	ActionBar action;
	BunKar bunker;
	ScheduleDatabase sdbase;
	AuthDatabase settings;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		listView = (ListView) this.findViewById(R.id.act_settings_list1);
		bunker = (BunKar) this.getApplication();
		action = this.getActionBar();
		BunKar.count++;
		action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Settings" + "</font>"));
        
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        action.setDisplayHomeAsUpEnabled(true);
        action.show();
        sdbase = bunker.getDatabase(bunker.name);
        settings = bunker.settings;
		String[] values = new String[] { "Auto Login", "Default Semester", "Change Password","About" };
		String[] valuesx = new String[] {"Cut off","Backups","Restore","Notifications","Upload Time Table","Auto Login", "Default Semester", "Change Password","About"};
		focused = this.getIntent().getBooleanExtra("focused",false);
		
			if(!focused){
				final ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < values.length; ++i) {
					list.add(values[i]);
				}
				adapter = new MenuAdapter(this,
						android.R.layout.simple_list_item_1, list);
				listView.setAdapter(adapter);
			}
			else{
				action.setSubtitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+ bunker.name + "</font>"));
				final ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < valuesx.length; ++i) {
					list.add(valuesx[i]);
				}
				adapter = new MenuAdapter(this,
						android.R.layout.simple_list_item_1, list);
				listView.setAdapter(adapter);
			
				
			}
			
			listView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					if(adapter.getItem(position).equals("Cut off")){
						AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(Settings.this);
						builder_forsetter.setTitle("Choose a Semester");
						View viewx = Settings.this.getLayoutInflater().inflate(R.layout.settings_cutoff, null);
						builder_forsetter.setView(viewx);
						
						final SeekBar seek = (SeekBar) viewx.findViewById(R.id.settings_cutoff_seekBar1s);
						final TextView percnt = (TextView) viewx.findViewById(R.id.settings_cutoff_textView2s);
						seek.setMax(90); 
						Cursor c = settings.rawQuery("select cutoff from schedules where name = '"+bunker.name+"';");
						if(c.moveToFirst()){
							seek.setProgress(c.getInt(0)-10);
							percnt.setText(""+c.getInt(0)+"%");
						}
						c.close();
						builder_forsetter.setPositiveButton("Set", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								settings.execQuery("update schedules set cutoff = "+(seek.getProgress()+10)+" where name = '"+bunker.name+"';");
								adapter.notifyDataSetChanged();
								dialog.dismiss();
							}
						});
						
						builder_forsetter.setNegativeButton("Belay that!", new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								
								dialog.dismiss();
							}
						});
						
						seek.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

							@Override
							public void onProgressChanged(SeekBar seekBar,
									int progress, boolean fromUser) {
								// TODO Auto-generated method stub
								percnt.setText((progress+10)+"%");
							}

							@Override
							public void onStartTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								
							}

							@Override
							public void onStopTrackingTouch(SeekBar seekBar) {
								// TODO Auto-generated method stub
								
							}});
						builder_forsetter.show();
					}
					else if(adapter.getItem(position).equals("Upload Time Table")){
						AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
						builder.setTitle("Do you wanna upload your time table?");
						builder.setMessage("Help your class-mates to bunkr!");
			    		View v = Settings.this.getLayoutInflater().inflate(R.layout.upload, null);
			    		builder.setView(v);
			    		final EditText tags = (EditText) v.findViewById(R.id.editText1);
			    		tags.setHorizontallyScrolling(false);
			    		tags.setLines(5);
			    		builder.setPositiveButton("Upload", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog, int which) {
								if(tags.getText().toString().equals("")||tags.getText().toString().length()>140){
									Toast.makeText(Settings.this, "Tags cant be blank and they should be maximum 140 characters. Upload canceled", Toast.LENGTH_LONG).show();
									return;
								}else if(!StringUtils.isAlphanumericSpace(tags.getText().toString())){
									Toast.makeText(Settings.this, "Tags should be alphanumeric. Upload canceled", Toast.LENGTH_LONG).show();
									return;
								}
								bunker.tagForUpload = tags.getText().toString();
								
								if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.Uploader"))
									startService(new Intent(Settings.this,Uploader.class));
								
								dialog.dismiss();
								
							}});
			    		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								
							}});
			    		builder.setCancelable(true);
						builder.show();
					}
					else if(adapter.getItem(position).equals("Default Semester")){
						AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(Settings.this);
						builder_forsetter.setTitle("Choose a Semester");
						Cursor c = settings.rawQuery("select name from schedules;");
						ArrayList<String> schedulesList = new ArrayList<String>();
						if(c.moveToFirst())
							do{
								schedulesList.add(c.getString(0));
							}
							while(c.moveToNext());
						c.close();
						schedulesList.add("NONE");
						final String[] sch = schedulesList.toArray(new String[schedulesList.size()]);
						builder_forsetter.setItems(sch, new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int item) {
						    	if(!settings.exists("select name from settings where name = 'default'"))
						    	settings.execQuery("insert into settings values('default','"+sch[item]+"')");
						    	else
						    	settings.execQuery("update settings set value = '"+sch[item]+"' where name = 'default'");
						    	adapter.notifyDataSetChanged();
						    	dialog.dismiss();
						    }
						});
						builder_forsetter.show();
					}else if(adapter.getItem(position).equals("Change Password")){
						AlertDialog.Builder builder_forsetter = new AlertDialog.Builder(Settings.this);
						builder_forsetter.setTitle("Change Password");
						View viewx = Settings.this.getLayoutInflater().inflate(R.layout.settings_change_pass, null);
						final EditText oldpass = (EditText) viewx.findViewById(R.id.settings_change_pass_oldpass);
						final EditText newpass = (EditText) viewx.findViewById(R.id.settings_change_pass_newpass);
						final EditText retype = (EditText) viewx.findViewById(R.id.settings_change_pass_retype);
						builder_forsetter.setView(viewx);
						builder_forsetter.setPositiveButton("Set", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								
								if(settings.authentication.login("labsdecore", oldpass.getText().toString())){
									if(newpass.getText().toString().equals(retype.getText().toString())&&StringUtils.isAlphanumeric(newpass.getText().toString())&&newpass.getText().toString().length()>=5){
										settings.authentication.register("labsdecore", newpass.getText().toString());
										Toast.makeText(Settings.this, "Password Changed!", Toast.LENGTH_SHORT).show();
										dialog.dismiss();
									} else Toast.makeText(Settings.this, "Password should be alphanumeric, atleast 5 characters and both passwords should match", Toast.LENGTH_LONG).show();
								}
								else Toast.makeText(Settings.this, "Current password is wrong", Toast.LENGTH_SHORT).show();
								
							}});
						builder_forsetter.setNegativeButton("Belay That!", new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
								
							}});
						builder_forsetter.show();
						
					} else if(adapter.getItem(position).equals("Restore")){
						Cursor c = settings.rawQuery("select id from labsdecore"+bunker.name.replaceAll(" ", "_")+";");
			    		if(c.moveToFirst()){
			    			Settings.this.startActivity(new Intent(Settings.this,Backups.class));
			    			
			    			//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
							
			    		}
			    		c.close();
						
						
						
					} else if(adapter.getItem(position).equals("Backups")){
						int temp = 0;
						Cursor csx = settings.rawQuery("select backups from schedules where name = '"+bunker.name+"'");
						if(csx.moveToFirst())
							if(csx.getInt(0)==0) temp = 1;
							else temp = 0;
						csx.close();
						settings.execQuery("update schedules set backups = "+temp+" where name = '"+bunker.name+"'");
						adapter.notifyDataSetChanged();
					} else if(adapter.getItem(position).equals("Notifications")){
						try {
							boolean temp = false;
							Cursor csx = settings.rawQuery("select type from schedules where name = '"+bunker.name+"'");
							if(csx.moveToFirst())
								if(csx.getString(0).equals("true"))
									temp = false;
								else
									temp = true;
							csx.close();
							settings.schedules.setNotification(bunker.name, temp);
							adapter.notifyDataSetChanged();
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if(adapter.getItem(position).equals("Auto Login")){
						boolean temp = false;
						Cursor csx = settings.rawQuery("select value from settings where name = 'autologin';");
						if(csx.moveToFirst())
							if(csx.getString(0).equals("true"))
								temp = false;
							else temp = true;
						csx.close();
						settings.execQuery("update settings set value = '"+temp+"' where name = 'autologin';");
						adapter.notifyDataSetChanged();
					} else if(adapter.getItem(position).equals("About")){
						Settings.this.startActivity(new Intent(Settings.this, About.class));
						
					}
					
					
				}});
			
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}else if(id == android.R.id.home){
			this.setResult(1);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	
	
	
	private class MenuAdapter extends ArrayAdapter<String> {

	    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

	    public MenuAdapter(Context context, int textViewResourceId,
	        List<String> objects) {
	      super(context, textViewResourceId, objects);
	      for (int i = 0; i < objects.size(); ++i) {
	        mIdMap.put(objects.get(i), i);
	      }
	    }

	    @Override
	    public long getItemId(int position) {
	      String item = getItem(position);
	      return mIdMap.get(item);
	    }

	    public boolean isEnabled(int position){
	    	
	    	return true;
	    }
	    
	    public View getView(int position, View view, ViewGroup parent){
	    	
	    	if(view==null) view = Settings.this.getLayoutInflater().inflate(R.layout.settings_item, null);
	    	final int pos = position;
	    	TextView title = (TextView) view.findViewById(R.id.settings_item_textView1);
	    	TextView subtitle = (TextView) view.findViewById(R.id.settings_item_textView2);
	    	TextView choice = (TextView) view.findViewById(R.id.settings_item_textView3s);
	    	final CheckBox check = (CheckBox) view.findViewById(R.id.settings_item_checkBox1s);
	    	title.setText(this.getItem(position));
	    	title.setVisibility(View.VISIBLE);
	    	subtitle.setVisibility(View.VISIBLE);
	    	choice.setVisibility(View.VISIBLE);
	    	check.setVisibility(View.VISIBLE);
	    	view.setEnabled(true);
	    	check.setEnabled(true);
	    	if(this.getItem(position).equals("Auto Login")){
	    		choice.setVisibility(View.GONE);
	    		subtitle.setText("Bunkr wouln't ask for passwords");
	    		Cursor c = settings.rawQuery("select value from settings where name = 'autologin'");
	    		if(c.moveToFirst())
	    			if(c.getString(0).equals("true")){
	    				check.setChecked(true);
	    			}
	    			else{
	    				check.setChecked(false);
	    			}
	    		else{
	    			check.setChecked(false);
	    		}
	    		c.close();
	    	}
	    	else if(this.getItem(position).equals("Default Semester")){
	    		check.setVisibility(View.GONE);
	    		subtitle.setText("Selected semester will be auto opened");
	    		Cursor c = settings.rawQuery("select value from settings where name = 'default'");
	    		if(c.moveToFirst())
	    			choice.setText(""+c.getString(0));
	    		else
	    			choice.setText("None");
	    		c.close();
	    	}
	    	else if(this.getItem(position).equals("Upload Time Table")){
	    		check.setVisibility(View.GONE);
	    		choice.setVisibility(View.GONE);
	    		subtitle.setText("Help your classmates to bunkr!");
	    		
	    	}
	    	else if(this.getItem(position).equals("Restore")){
	    		check.setVisibility(View.GONE);
	    		choice.setVisibility(View.GONE);
	    		Cursor c = settings.rawQuery("select id from labsdecore"+bunker.name.replaceAll(" ", "_")+";");
	    		if(!c.moveToFirst()){
	    			view.setEnabled(false);
	    			subtitle.setText("No Backups Found!");
	    		}else subtitle.setText("Restore to earlier backed up version of this semester");
	    		c.close();
	    		
	    		
	    	}
	    	else if(this.getItem(position).equals("Change Password")){
	    		check.setVisibility(View.GONE);
	    		choice.setVisibility(View.GONE);
	    		subtitle.setText("Update your credentials");
	    		
	    	}
	    	else if(this.getItem(position).equals("Cut off")){
	    		check.setVisibility(View.GONE);
	    		subtitle.setText("Target attendance percentage");
	    		Cursor c = settings.rawQuery("select cutoff from schedules where name = '"+bunker.name+"';");
	    		if(c.moveToFirst())
	    			choice.setText(""+c.getInt(0)+" %");
	    		c.close();
	    	} 
	    	else if(this.getItem(position).equals("Backups")){
	    		choice.setVisibility(View.GONE);
	    		subtitle.setText("If activated, Bunkr will keep bakups");
	    		Cursor c = settings.rawQuery("select backups from schedules where name = '"+bunker.name+"';");
	    		if(c.moveToFirst())
	    			if(c.getInt(0)==1)
	    				check.setChecked(true);
	    			else check.setChecked(false);
	    		else check.setChecked(false);
	    		c.close();
	    		
	    	} 
	    	else if(this.getItem(position).equals("About")){
	    		choice.setVisibility(View.GONE);
	    		check.setVisibility(View.GONE);
	    		subtitle.setVisibility(View.INVISIBLE);
	    		
	    	}
	    	else if(this.getItem(position).equals("Notifications")){
	    		choice.setVisibility(View.GONE);
	    		subtitle.setText("Bunkr will ask you to register your attendance from time to time");
	    		boolean rtact = false;
	    		Cursor c = settings.rawQuery("select rt from schedules where name = '"+bunker.name+"';");
	    		if(c.moveToFirst())
	    			if(c.getInt(0)==0)
	    				rtact = false;
	    			else rtact = true;
	    		else rtact = false;
	    		c.close();
	    		
	    		if(!rtact) {view.setEnabled(false); check.setEnabled(false);}
	    		else {
	    			c = settings.rawQuery("select type from schedules where name = '"+bunker.name+"';");
		    		if(c.moveToFirst())
		    			if(c.getString(0).equals("false"))
		    				check.setChecked(false);
		    			else check.setChecked(true);
		    		else check.setChecked(false);
		    		c.close();
	    		}
	    	}
	    	
	        check.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(MenuAdapter.this.getItem(pos).equals("Auto Login")){
						settings.execQuery("update settings set value = '"+check.isChecked()+"' where name = 'autologin';");
					}
					else if(MenuAdapter.this.getItem(pos).equals("Backups")){
						int backups = 0;
						if(check.isChecked()) backups = 1;
						settings.execQuery("update schedules set backups = "+backups+" where name = '"+bunker.name+"'");
					
					}
					else if(MenuAdapter.this.getItem(pos).equals("Notifications")){
						
 						try {
							settings.schedules.setNotification(bunker.name, check.isChecked());
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
				}});
	        
	        
	    	
	    	
	    	
	    	
	    	
	    	return view;
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }
	
	public void onBackPressed(){
		this.setResult(1);
		this.finish();
	}
	
	public void onDestroy(){
		super.onDestroy();
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
		
	}
	
	
}
