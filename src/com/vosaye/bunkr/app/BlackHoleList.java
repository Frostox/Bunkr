package com.vosaye.bunkr.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.ScheduleListActivity.MyBroadcastReceiver;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.customviews.CalendarView;
import com.vosaye.bunkr.customviews.CustomListView;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.ValidatorService;



public class BlackHoleList extends SherlockFragmentActivity implements OnClickListener {
	Button start, end, create;
	ListView list;
	BlackHoleAdapter blackholeadapter;
	BunKar bunker;
	ScheduleDatabase sched;
	Cursor c;
	DatePicker datePicker;
	SimpleDateFormat sdf;
	SimpleDateFormat sdf3;
	SimpleDateFormat sdf2;
	

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_black_hole);
		bunker = (BunKar) this.getApplication();
		sdf = bunker.sdf2;
		sdf3 = bunker.sdf3;
		sdf2 = bunker.sdf;
		sched = bunker.getDatabase(bunker.name);
		ActionBar action = this.getSupportActionBar();
	    action.show();
	    action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Ranges" + "</font>"));
        action.setDisplayHomeAsUpEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        BunKar.count++;
        action.setSubtitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+bunker.name + "</font>"));
		
		start = (Button) this.findViewById(R.id.blkhole_start);
		end = (Button) this.findViewById(R.id.blkhole_end);
		try {
			Calendar tmp = Calendar.getInstance();
			tmp.setTime(sdf2.parse(this.getIntent().getStringExtra("start")));
			start.setText(sdf.format(tmp.getTime()));
			tmp.add(Calendar.DATE, 7);
			end.setText(sdf.format(tmp.getTime()));
			
		} catch (NullPointerException e1) {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY,0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			start.setText(sdf.format(cal.getTime()));
			cal.add(Calendar.DATE, 7);
			end.setText(sdf.format(cal.getTime()));
			//e1.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list = (ListView) this.findViewById(R.id.blkhole_list);
		
		create = (Button) this.findViewById(R.id.blkhole_create);
		c = sched.rawQuery("select 1 as _id, rname, start, end from ranges where rname like '%fixed%';");
		blackholeadapter = new BlackHoleAdapter(this,R.layout.activity_black_hole_listitem,c,new String[]{"_id"},new int[]{R.id.black_hole_start});
		list.setAdapter(blackholeadapter);
		create.setOnClickListener(this);
		/*
 list.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView startt, endt;
				startt = (TextView) view.findViewById(R.id.black_hole_start);
				endt = (TextView) view.findViewById(R.id.black_hole_end);
				
				try {
					Date start = sdf.parse(startt.getText().toString());
					String name = sched.stats.getBlackHole(start);
	        		Intent intent = new Intent(BlackHoleList.this, BlackHoleActivity.class);
	        		intent.putExtra("name", name);
	        		BlackHoleList.this.startActivity(intent);
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}});
*/
		start.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(BlackHoleList.this);
				builder.setTitle("Select the Date");
				View view = getLayoutInflater().inflate(R.layout.date_picker_dialog,null);
				builder.setView(view);
				datePicker = (DatePicker) view.findViewById(R.id.date_picker_dialog_datePicker1);

				Calendar start = Calendar.getInstance(), end = Calendar.getInstance();
				//start.setTime(sched.start);
				end.setTime(sched.end);
				try {
					start.setTime(sdf.parse(BlackHoleList.this.start.getText().toString()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				datePicker.updateDate(start.get(Calendar.YEAR), start.get(Calendar.MONDAY), start.get(Calendar.DAY_OF_MONTH));
				builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}});
				builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Calendar temp = Calendar.getInstance();
						temp.set(Calendar.HOUR_OF_DAY, 0);
						temp.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
						temp.set(Calendar.MONTH, datePicker.getMonth());
						temp.set(Calendar.YEAR, datePicker.getYear());
						if(temp.before(sched.start)||temp.after(sched.end)){
							
						}else{
							BlackHoleList.this.start.setText(sdf.format(temp.getTime()));
						}
					}
					});
				builder.show();
				
				
			}});
				
			
		
		end.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(BlackHoleList.this);
				builder.setTitle("Select the Date");
				View view = getLayoutInflater().inflate(R.layout.date_picker_dialog,null);
				builder.setView(view);
				datePicker = (DatePicker) view.findViewById(R.id.date_picker_dialog_datePicker1);

				Calendar start = Calendar.getInstance(), end = Calendar.getInstance();
				//start.setTime(sched.start);
				end.setTime(sched.end);
				try {
					start.setTime(sdf.parse(BlackHoleList.this.end.getText().toString()));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				datePicker.updateDate(start.get(Calendar.YEAR), start.get(Calendar.MONDAY), start.get(Calendar.DAY_OF_MONTH));
				builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}});
				builder.setPositiveButton("Set", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Calendar temp = Calendar.getInstance();
						temp.set(Calendar.HOUR_OF_DAY, 0);
						temp.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
						temp.set(Calendar.MONTH, datePicker.getMonth());
						temp.set(Calendar.YEAR, datePicker.getYear());
						if(temp.before(sched.start)||temp.after(sched.end)){
							
						}else{
							BlackHoleList.this.end.setText(sdf.format(temp.getTime()));
						}
					}
					});
				builder.show();
				
			}
		});
		
		 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.black_hole_list, menu);
		return true;
	}
	
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
	
	class BlackHoleAdapter extends SimpleCursorAdapter{

		@SuppressWarnings("deprecation")
		public BlackHoleAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			
		}
		TextView startt, endt;
		Button del;
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView==null)
				convertView = getLayoutInflater().inflate(R.layout.activity_black_hole_listitem, null);
			c.moveToPosition(position);
			
			
			
			startt = (TextView) convertView.findViewById(R.id.black_hole_start);
			endt = (TextView) convertView.findViewById(R.id.black_hole_end);
			del = (Button) convertView.findViewById(R.id.black_hole_delete);
			convertView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					try {
						Date start = sdf.parse(startt.getText().toString());
						String name = sched.stats.getBlackHole(start);
		        		Intent intent = new Intent(BlackHoleList.this, BlackHoleActivity.class);
		        		intent.putExtra("name", name);
		        		BlackHoleList.this.startActivity(intent);
		        		
		        		//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}});
			try {
				startt.setText(sdf.format(sdf2.parse(c.getString(2))));
				endt.setText(sdf.format(sdf2.parse(c.getString(3))));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			del.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					
					
					AlertDialog.Builder builder = new AlertDialog.Builder(BlackHoleList.this);
					builder.setMessage("Do you really wanna delete this Range?");
					builder.setTitle("Warning");
					builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}});
					builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							BackgroundDBase bg = new BackgroundDBase();
							bg.OP = true;
							bg.execute();
						
						}});
					//authenticator.schedules.deleteSchedule(textView.getText().toString());
					//c.requery();
					//CAdapter.notifyDataSetChanged();
					dialog = builder.create();
					dialog.show();
				
				
					
					
				}
			});
			
			return convertView;
		}
		//
		class BackgroundDBase extends AsyncTask<String,Void,String>{
			boolean OP;
			@Override
			protected String doInBackground(String... params) {
				
				MaintenanceManager.HALT = true;
				if(bunker.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
					while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
						//wait for manager to be halted by this thread...
					}
				}
				
				ValidatorService.HALT = true;
					
				while(ValidatorService.status == ValidatorService.BUSY){
					if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
						BlackHoleList.this.startService(new Intent(BlackHoleList.this, ValidatorService.class));
				}
				Cursor c = bunker.settings.rawQuery("select max(id) from labsdecore"+bunker.name.replaceAll(" ", "_")+" where id > 0;");
				if(c.moveToFirst()){
					if(c.getString(0)==null){
					//sched.close();
					bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
					//sched.open();
					}
				}else {
					//sched.close();
					bunker.tempBackupDbase(bunker.name,  Calendar.getInstance().getTime(), "Semester opened");
					//sched.open();
				}
				c.close();
				sched.beginTransaction();
				if(OP){
					try {
						
						
						
						Calendar start = Calendar.getInstance(), end = Calendar.getInstance();
						start.setTime(sdf.parse(startt.getText().toString()));
						end.setTime(sdf.parse(endt.getText().toString()));
						sched.stats.deleteCustomRange(start.getTime(), end.getTime());
						sched.commit();
						//sched.close();
						bunker.tempBackupDbase(bunker.name, Calendar.getInstance().getTime(), "Deleted Range at");
						//sched.open();
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
					finally{
						//sched.open();
						if(sched.getDatabase().inTransaction()) sched.rollback();
					}
				}
				else{
					try {
						Calendar startx = Calendar.getInstance(), endx = Calendar.getInstance();
						startx.setTime(sdf.parse(BlackHoleList.this.start.getText().toString()));
						endx.setTime(sdf.parse(BlackHoleList.this.end.getText().toString()));
						sched.stats.createCustomRange(startx.getTime(), endx.getTime(), true);
						sched.commit();
						//sched.close();
						bunker.tempBackupDbase(bunker.name, Calendar.getInstance().getTime(), "Created Range at");
						//sched.open();
						
						
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLiteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BunkerException e) {

						error = e.getMessage();
						e.printStackTrace();
					} finally { 
						//sched.open();
						if(sched.getDatabase().inTransaction()) sched.rollback();}
					
				}
				
				Intent intentUpdate = new Intent();
				intentUpdate.setAction("com.vosaye.bunkr.BlackHoleRefresh");
				intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
				sendBroadcast(intentUpdate);
				ValidatorService.HALT = false;
				return null;
			}
			public void onPostExecute(String s){
				c = sched.rawQuery("select 1 as _id, rname, start, end from ranges where rname like '%fixed%';");
				blackholeadapter = new BlackHoleAdapter(BlackHoleList.this,R.layout.activity_black_hole_listitem,c,new String[]{"_id"},new int[]{R.id.black_hole_start});
				list.setAdapter(blackholeadapter);
				if(error!=null) {Toast.makeText(BlackHoleList.this, error, Toast.LENGTH_LONG).show();error = null;}
			}
			String error = null;
		}
	}

	@Override
	public void onClick(View v) {
		
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(BlackHoleList.this);
		builder.setMessage("Do you really wanna add a Range here?");
		builder.setTitle("Warning");
		builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.dismiss();
			}});
		builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				BlackHoleAdapter.BackgroundDBase bg = blackholeadapter.new BackgroundDBase();
				bg.OP = false;
				bg.execute();
			
			}});
		dialog = builder.create();
		dialog.show();

		
	}
	
	public void onPause(){
		super.onPause();
		ValidatorService.FREEFLOW = true;
		//c.close();
	}
	public void onResume(){
		super.onResume();
		ValidatorService.FREEFLOW = true;
    	ValidatorService.FOCUSED = true;
    	
    	/*
 c = sched.rawQuery("select 1 as _id, rname, start, end from ranges where rname like '%fixed%';");
		blackholeadapter.changeCursor(c);
		list.setAdapter(blackholeadapter);
*/
		
    	//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
		
	}
	AlertDialog dialog;
	public void onDestroy(){
		super.onDestroy();
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
		
		c.close();
		
	}

}
