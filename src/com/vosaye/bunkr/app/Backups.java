package com.vosaye.bunkr.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sqlcipher.Cursor;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.R.id;
import com.vosaye.bunkr.R.layout;
import com.vosaye.bunkr.R.menu;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.customviews.TermSetter;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.ValidatorService;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class Backups extends Activity {

	ActionBar action;
	BunKar bunker;
	ScheduleDatabase sdbase; 
	AlertDialog.Builder builder_forsetter;
	AlertDialog alert;
	AuthDatabase settings;
	ListView list;
	View per = null, cache = null;
	Cursor c;
	BackupsAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backups);
		bunker = (BunKar) this.getApplication();
		action = this.getActionBar();
		action.setTitle(" Backups    ");
		action.setSubtitle(" "+bunker.name);
        
		BunKar.count++;
		
		
		action.show();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action));
        
		sdbase = bunker.getDatabase(bunker.name);
        settings = bunker.settings;
        c = settings.rawQuery("select 1 as _id, id, name, datex from (select id, name, datex from labsdecore"+bunker.name.replaceAll(" ", "_")+" UNION ALL select -3 as id, 'x' as name, 'x' as datex UNION ALL select 0 as id, 'x' as name, 'x' as datex) order by id");
        adapter = new BackupsAdapter(this,R.layout.backups_list_item,c,new String[] {"name"},new int[]{R.id.backups_list_item_textView1});
		list = (ListView) this.findViewById(R.id.activity_backups_listView1);
        list.setAdapter(adapter);
        
        list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final int pos = position;
				builder_forsetter = new AlertDialog.Builder(Backups.this);
				builder_forsetter.setTitle("Warning!");
				builder_forsetter.setMessage("Do you really wanna restore the semester to selected backup? This action cannot be reverted");
				builder_forsetter.setPositiveButton("That be true!", new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						c.moveToPosition(pos);
						dialog.dismiss();
						AlertDialog.Builder waithere = new AlertDialog.Builder(Backups.this);
						waithere.setTitle("Restoring Semester");
						waithere.setMessage("Please do not close Bunkr. Closing Bunkr while it restores your semester might corrupt your semester.");
						alert = waithere.show();
						new BackupsRestorer(bunker.name,c.getInt(1),bunker).execute("");
						
					}});
				
				builder_forsetter.setNegativeButton("Belay that!", new OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}});
				builder_forsetter.show();
			}});
        
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.backups, menu);
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
	
	class BackupsAdapter extends SimpleCursorAdapter{

		public BackupsAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to, int flags) {
			super(context, layout, c, from, to, flags);
			// TODO Auto-generated constructor stub
		}

		public BackupsAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			// TODO Auto-generated constructor stub
		}
		
		public View getView(final int position, View convertView, ViewGroup parent){
			c.moveToPosition(position);
			if(convertView==null) convertView = Backups.this.getLayoutInflater().inflate(R.layout.backups_list_item, null);
			TextView title = (TextView) convertView.findViewById(R.id.backups_list_item_textView1);
			TextView subtitle = (TextView) convertView.findViewById(R.id.backups_list_item_textView2);
			TextView header = (TextView) convertView.findViewById(R.id.backups_list_item_textView3);
			title.setVisibility(View.VISIBLE);
			subtitle.setVisibility(View.VISIBLE);
			header.setVisibility(View.VISIBLE);
			if(c.getInt(1)==-3||c.getInt(1)==0){
				title.setVisibility(View.GONE);
				subtitle.setVisibility(View.GONE);
				if(c.getInt(1)==-3){
					per = convertView;
					header.setText("Permenant");
				}else{
					cache = convertView;
					header.setText("Cache");
				}
				
				return convertView;
			}else header.setVisibility(View.GONE);
			
			title.setText(c.getString(2));
			try {
				subtitle.setText(BunKar.sdf2.format(bunker.sdf.parse(c.getString(3)))+" "+BunKar.sdftime.format(bunker.sdf.parse(c.getString(3))));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return convertView;
		}
		
		public boolean isEnabled(int position){
			c.moveToPosition(position);
			if(c.getInt(1)==-3||c.getInt(1)==0){return false;}
			return true;
		}
		
		
	}
	
	class BackupsRestorer extends AsyncTask<String,Void,String>{
		String name;
		int id;
		BunKar bunker;
		public BackupsRestorer(String name, int id, BunKar bunker){
			super();
			this.name = name;
			this.id = id;
			this.bunker = bunker;
		}

		@Override
		protected String doInBackground(String... params) {
			
			try{
			ValidatorService.HALT = true;
			
			while(ValidatorService.status = ValidatorService.BUSY){
				if(!ValidatorService.STARTED)
					Backups.this.startService(new Intent(Backups.this,ValidatorService.class));
			}
			MaintenanceManager.HALT = true;
			if(bunker.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
				while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
					//wait for manager to be halted by this thread...
				}
			}
			
			System.out.println("Vossaye bnkr : "+name);
			
			bunker.reStore(name, id);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				
				ValidatorService.HALT = false;
				MaintenanceManager.HALT = false;
			}
			return "";
		}
		
		protected void onPostExecute(String param){
			alert.dismiss();
			c = settings.rawQuery("select 1 as _id, id, name, datex from (select id, name, datex from labsdecore"+bunker.name.replaceAll(" ", "_")+" UNION ALL select -3 as id, 'x' as name, 'x' as datex UNION ALL select 0 as id, 'x' as name, 'x' as datex) order by id");
	        adapter = new BackupsAdapter(Backups.this,R.layout.backups_list_item,c,new String[] {"name"},new int[]{R.id.backups_list_item_textView1});
			list.setAdapter(adapter);
	        
			Backups.this.startService(new Intent(Backups.this,ValidatorService.class));
		}
		
	}
	
	public void onDestroy(){
		super.onDestroy();
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
		c.close();
	}
	
	public void onResume(){
		super.onResume();
		
		/*
 c = settings.rawQuery("select 1 as _id, id, name, datex from (select id, name, datex from labsdecore"+bunker.name.replaceAll(" ", "_")+" UNION ALL select -3 as id, 'x' as name, 'x' as datex UNION ALL select 0 as id, 'x' as name, 'x' as datex) order by id");
		adapter.changeCursor(c);
		list.setAdapter(adapter);
*/
        
	}
	public void onPause(){
		super.onPause();
		//c.close();
	}
	
}
