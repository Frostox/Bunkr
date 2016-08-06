package com.vosaye.bunkr.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import net.sqlcipher.Cursor;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.customviews.StructureEditter.ListAdapter;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.ValidatorService;



public class BlackHoleActivity extends SherlockFragmentActivity {
	InputMethodManager mngr;
	
	BunKar bunker;
	ScheduleDatabase sched;
	Date startOfRange, endOfRange;
	String name;
	Spinner sub,typ;
	EditText att,total;
	Button add;
	Cursor c;
	BlackHoleActivityAdapter adapter;
	ListView list;

	SimpleDateFormat sdfx = new SimpleDateFormat("dd MMM yy",Locale.ENGLISH);
	SimpleCursorAdapter forSub, forTyp;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_black_hole_list);
		
		ActionBar action = this.getSupportActionBar();
        action.show();
        
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        action.setDisplayHomeAsUpEnabled(true);
		BunKar.count++;
		bunker = (BunKar) this.getApplication();
		sched = bunker.getDatabase(bunker.name);
		name = this.getIntent().getStringExtra("name");
		mngr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		
		try {
			startOfRange = sched.stats.getStart(name);
			endOfRange = sched.stats.getEnd(name);
			action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Range " +sdfx.format(startOfRange)+" - "+sdfx.format(endOfRange)+ "</font>"));
		        
			action.setSubtitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+bunker.name + "</font>"));
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		sub = (Spinner) this.findViewById(R.id.abhl_sub);
		typ = (Spinner) this.findViewById(R.id.abhl_typ);
		att = (EditText) this.findViewById(R.id.abhl_att);
		total = (EditText) this.findViewById(R.id.abhl_total);
		add = (Button) this.findViewById(R.id.abhl_add);
		list = (ListView) this.findViewById(R.id.abhl_list);
		c = sched.rawQuery("select 1 as _id, r.IDrel as IDrel, r.attendance as attendance, r.total, s.subjname, s.typname as total from "+name+" r, session s where s.sessionID = r.IDrel;");
		
		adapter = new BlackHoleActivityAdapter(this,R.layout.abhl_item,c,new String[] {"IDrel"},new int[]{R.id.abhli_sub});
		list.setAdapter(adapter);
		mngr.hideSoftInputFromWindow(att.getApplicationWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
		
		
		
		forSub = new SimpleCursorAdapter(this,R.layout.str_editter_spinnertext,sched.rawQuery("select 1 as _id, name from subject"),new String[] {"name"},new int[]{R.id.str_editter_spinnertext_textView1});
		sub.setAdapter(forSub);
		forTyp = new SimpleCursorAdapter(this,R.layout.str_editter_spinnertext,sched.rawQuery("select 1 as _id, name from type"),new String[] {"name"},new int[]{R.id.str_editter_spinnertext_textView1});
		typ.setAdapter(forTyp);
		
		
		
		
		
		
	    
	    
	    add.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try{
					int atti = Integer.parseInt(att.getText().toString());
					int totali = Integer.parseInt(total.getText().toString());
					if(atti>totali){

						Toast.makeText(BlackHoleActivity.this, "Attendance cannot be more than total", Toast.LENGTH_LONG).show();
						return;
					}
					Cursor ctemp = (Cursor) typ.getSelectedItem();
					String typn  = (ctemp.getString(ctemp.getColumnIndex("name")));
					ctemp = (Cursor) sub.getSelectedItem();
					String subn  = (ctemp.getString(ctemp.getColumnIndex("name")));
					int IDrel = -1;
					
					ctemp = sched.rawQuery("select sessionID from session where subjname = '"+subn+"' and typname = '"+typn+"'");
					if(ctemp.moveToFirst())
						IDrel = ctemp.getInt(0);
					
					if(IDrel!=-1){
						
						if(sched.valueExists("IDrel", ""+IDrel, name)){
							Toast.makeText(BlackHoleActivity.this, "The session is already added", Toast.LENGTH_LONG).show();
							
							return;
						}
						
						sched.stats.insertIntoBlackHole(name, IDrel, atti, totali);
						att.setText("");
						total.setText("");
						c.requery();
						adapter.notifyDataSetChanged();
					}
					
				}
				catch(NumberFormatException e){
					Toast.makeText(BlackHoleActivity.this, "Please insert the values", Toast.LENGTH_LONG).show();
					return;
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BunkerException e) {
					// TODO Auto-generated catch block
					Toast.makeText(BlackHoleActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				mngr.hideSoftInputFromWindow(total.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
				mngr.hideSoftInputFromWindow(att.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
				
				
			}
		});
		
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.black_hole, menu);
		return true;
	}
	
	
	
	public class BlackHoleActivityAdapter extends SimpleCursorAdapter{

		@SuppressWarnings("deprecation")
		public BlackHoleActivityAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			
		}
		
		public View getView(int position, View convertView, ViewGroup parent){
			c.moveToPosition(position);
			if(convertView==null) convertView = BlackHoleActivity.this.getLayoutInflater().inflate(R.layout.abhl_item, null);
			TextView sub,typ,perc;
			TextView atte,tot;
			Button del;
			
			sub = (TextView) convertView.findViewById(R.id.abhli_sub);
			typ = (TextView) convertView.findViewById(R.id.abhli_typ);
			perc = (TextView) convertView.findViewById(R.id.abhli_perc);
			atte = (TextView) convertView.findViewById(R.id.abhli_att);
			tot = (TextView) convertView.findViewById(R.id.abhli_total);
			del = (Button) convertView.findViewById(R.id.abhli_del);
			
			final float IDrel, attendance, totals;
			IDrel = c.getInt(1);
			attendance = c.getInt(2);
			totals = c.getInt(3);
			
			sub.setText(c.getString(4));
			typ.setText(c.getString(5));
			
			atte.setText((int)attendance+"");
			tot.setText((int)totals+"");
			perc.setText(""+String.format("%.2f", ((attendance/totals)*100))+" %");
			
			del.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					
					
					
					
					
					AlertDialog.Builder builder = new AlertDialog.Builder(BlackHoleActivity.this);
					builder.setMessage("Do you really wanna delete it?");
					builder.setTitle("Warning");
					builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
						}});
					builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

						@SuppressWarnings("deprecation")
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							try {
								
								sched.stats.deleteFromBlackHole(name, (int) IDrel);
								c.requery();
								adapter.notifyDataSetChanged();
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (BunkerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							arg0.dismiss();
							
						
						}});
					//authenticator.schedules.deleteSchedule(textView.getText().toString());
					//c.requery();
					//CAdapter.notifyDataSetChanged();
					builder.create();
					builder.show();
				}
			});
			
			
			
			
			return convertView;
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
	public void onPause(){
		super.onPause();
		ValidatorService.FREEFLOW = true;
		//c.close();
	}
	public void onResume(){
		super.onResume();
		ValidatorService.FREEFLOW = true;
    	ValidatorService.FOCUSED = true;
    	mngr.hideSoftInputFromWindow(att.getApplicationWindowToken(),InputMethodManager.HIDE_IMPLICIT_ONLY);
		
    	/*
 c = sched.rawQuery("select 1 as _id, r.IDrel as IDrel, r.attendance as attendance, r.total, s.subjname, s.typname as total from "+name+" r, session s where s.sessionID = r.IDrel;");
		adapter.changeCursor(c);
		list.setAdapter(adapter);
*/
		
    	//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
	}
}
