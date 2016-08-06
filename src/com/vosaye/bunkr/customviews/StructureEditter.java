package com.vosaye.bunkr.customviews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.customviews.TypeScrollList.TypeScrollListAdapter;
import com.vosaye.bunkr.exception.BunkerException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;




public class StructureEditter{
	Activity context;
	String pseudoStructureName;
	RelativeLayout mainContainer;
	boolean saved = true;
	Spinner sub,typ;
	Button start, dur, add;
	int starttime = 480, durtime = 60;
	public ListView list;
	Toast toast;
	public ScheduleDatabase sched;
	AuthDatabase auth;
	BunKar bunker;
	Cursor c, subC, typC;
	public ListAdapter listAdapter;
	

	java.text.DecimalFormat nft = new java.text.DecimalFormat("#00.###");
	SimpleCursorAdapter forSub, forTyp;
	
	public void setSaved(boolean saved){
		this.saved = saved;
	}
	
	public boolean getSaved(){
		return saved;
	}
	
	public void updateList(){
		c = sched.rawQuery("select 1 as _id, p.mins as mins, p.IDrel as IDrel, p.duration as duration, s.subjname as sub, s.typname as typ  from "+this.pseudoStructureName+" p, session s where s.sessionID = p.IDrel order by p.mins;");
		listAdapter.changeCursor(c);
		list.setAdapter(listAdapter);
		
		subC = sched.rawQuery("select 1 as _id, name from subject");
		forSub.changeCursor(subC);
		sub.setAdapter(forSub);
		typC = sched.rawQuery("select 1 as _id, name from type");
		forTyp.changeCursor(typC);
		typ.setAdapter(forTyp);
		
	}
	
	public void closeList(){
		subC.close();
		typC.close();
		c.close();
	}
	
	public StructureEditter(Activity context, String pseudoStructureName){
		this.context = context;
		this.pseudoStructureName = pseudoStructureName;
		onCreate();
	}
	
	
	public String getPseudoStructureName(){
		return this.pseudoStructureName;
	}
	
	public void test(String name){
		add.setText(name);
	}
	
	public void onCreate(){
		mainContainer = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.str_editter_main, null);
		sub = (Spinner) mainContainer.findViewById(R.id.str_spinner2);
		typ = (Spinner) mainContainer.findViewById(R.id.str_spinner1);
		start = (Button) mainContainer.findViewById(R.id.str_button1);
		dur = (Button) mainContainer.findViewById(R.id.str_button2);
		add = (Button) mainContainer.findViewById(R.id.str_button3);
		list = (ListView) mainContainer.findViewById(R.id.str_listView1);
		
		
		toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
		bunker = (BunKar) context.getApplication();
		sched = bunker.getDatabase(bunker.name);
		auth = bunker.settings;
		if(pseudoStructureName.equals(""))
		pseudoStructureName = sched.meta.createPseudoStructure();
		 
		c = sched.rawQuery("select 1 as _id, p.mins as mins, p.IDrel as IDrel, p.duration as duration, s.subjname as sub, s.typname as typ  from "+this.pseudoStructureName+" p, session s where s.sessionID = p.IDrel order by p.mins;");
		listAdapter = new ListAdapter(context,R.layout.str_editter_listitem,c,new String[] {"sub"},new int[]{R.id.str_listitem_textView1});
		list.setAdapter(listAdapter);
		
		subC = sched.rawQuery("select 1 as _id, name from subject");
		forSub = new SimpleCursorAdapter(context,R.layout.str_editter_spinnertext,subC,new String[] {"name"},new int[]{R.id.str_editter_spinnertext_textView1});
		sub.setAdapter(forSub);
		typC = sched.rawQuery("select 1 as _id, name from type");
		forTyp = new SimpleCursorAdapter(context,R.layout.str_editter_spinnertext, typC,new String[] {"name"},new int[]{R.id.str_editter_spinnertext_textView1});
		typ.setAdapter(forTyp);
		
		typ.setOnItemSelectedListener(new OnItemSelectedListener(){
			
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				Cursor ctemp = (Cursor) typ.getSelectedItem();
				String tempstr = (ctemp.getString(ctemp.getColumnIndex("name")));
				//ctemp.close();
				Cursor temp = sched.rawQuery("select mins from type where name = '"+tempstr+"'");
				if(temp.moveToFirst())
					do{
						
						durtime = temp.getInt(0);
						dur.setText("Duration :\n"+durtime/60+" hrs "+durtime%60+" mins");
						
					}while(temp.moveToNext());
				temp.close();

				
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
				
			}});
		
		dur.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				OnTimeSetListener ol = new OnTimeSetListener(){

					@Override
					public void onTimeSet(TimePicker tp, int hrs, int mins) {
						if(hrs>4||(hrs==0&&mins==0)) {
							toast.setText("Duration cannot be more than 4 hrs or equal to 0");
							toast.show();
						}
						else{
						durtime = hrs*60+mins;
						dur.setText("Duration :\n"+nft.format(hrs)+":"+nft.format(mins)+"");
						}
						
					}};
					
				CustomTimePickerDialog tp = new CustomTimePickerDialog(context, ol, durtime/60, durtime%60, true, true, 0, 0, 60, 4);
				
				tp.show();
				
			}});
		
		start.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				OnTimeSetListener ol = new OnTimeSetListener(){

					@Override
					public void onTimeSet(TimePicker tp, int hrs, int mins) {
						starttime = hrs*60+mins;
						Calendar date;
						date = Calendar.getInstance();
						date.set(Calendar.HOUR_OF_DAY, hrs);
						date.set(Calendar.MINUTE, mins);
						SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
						start.setText("Starts at :\n"+sdf.format(date.getTime())); 
						
					}};
					
				CustomTimePickerDialog tp = new CustomTimePickerDialog(context, ol, starttime/60, starttime%60, false);
				tp.show();
			}});
		
		add.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				String subn,typn;
				int IDrel = -1;
				Cursor ctemp = (Cursor) typ.getSelectedItem();
				typn  = (ctemp.getString(ctemp.getColumnIndex("name")));
				//ctemp.close();
				ctemp = (Cursor) sub.getSelectedItem();
				subn  = (ctemp.getString(ctemp.getColumnIndex("name")));
				
				ctemp = sched.rawQuery("select sessionID from session where subjname = '"+subn+"' and typname = '"+typn+"'");
				if(ctemp.moveToFirst())
					IDrel = ctemp.getInt(0);
				ctemp.close();
				if(IDrel!=-1)
					try {
						sched.meta.insertIntoPseudoStructure(pseudoStructureName, starttime, IDrel, durtime);
						c.requery();
						listAdapter.notifyDataSetChanged();
						saved = false;
						starttime = starttime + durtime;
						Calendar date;
						date = Calendar.getInstance();
						date.set(Calendar.HOUR_OF_DAY, starttime/60);
						date.set(Calendar.MINUTE, starttime%60);
						SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
						start.setText("Starts at :\n"+sdf.format(date.getTime())); 
						
						toast.setText("inserted successfully.....");
						toast.show();
						
					} catch (SQLiteException e) {
						toast.setText(""+e.getMessage().substring(0,e.getMessage().lastIndexOf(":")));
						toast.show();
						e.printStackTrace();
					} catch (BunkerException e) {
						toast.setText(""+e.getMessage());
						toast.show();
						e.printStackTrace();
					}
					
				
			}});
		
		
	}
	
	public View getView(){
		return mainContainer;
	}
	
	public class ListAdapter extends SimpleCursorAdapter{

		@SuppressWarnings("deprecation")
		public ListAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			
		}
		
		
		public View getView(int position, View convertView, ViewGroup parent){
			c.moveToPosition(position);
			if(convertView==null) convertView = LayoutInflater.from(context).inflate(R.layout.str_editter_listitem,null);
			
			System.out.println("Vosayes : getting");
			final TextView subname,typname,start,dur;
			Button del;
			subname = (TextView) convertView.findViewById(R.id.str_listitem_textView1);
			typname = (TextView) convertView.findViewById(R.id.str_listitem_textView2);
			start = (TextView) convertView.findViewById(R.id.str_listitem_textView3);
			dur = (TextView) convertView.findViewById(R.id.str_listitem_textView4);
			del = (Button) convertView.findViewById(R.id.str_listitem_button1);
			
			subname.setText(c.getString(4));
			typname.setText(c.getString(5));
			
			Calendar date = Calendar.getInstance();
			date.set(Calendar.HOUR_OF_DAY, c.getInt(1)/60);
			date.set(Calendar.MINUTE, c.getInt(1)%60);
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
			start.setText(""+sdf.format(date.getTime())); 
			dur.setText(c.getInt(3)/60+" hrs "+c.getInt(3)%60+" mins");
			
			del.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a",Locale.ENGLISH);
					try {
						Date date = sdf.parse(start.getText().toString());
						Calendar cal = Calendar.getInstance();
						cal.setTime(date);
						int hrs = cal.get(Calendar.HOUR_OF_DAY);
						int mins = cal.get(Calendar.MINUTE);
						mins = hrs*60+mins;
						sched.meta.deleteFromStructure(pseudoStructureName, mins);
						saved = false;
						
					} catch (ParseException e) {
						//toast.setText(e.getMessage().substring(0,e.getMessage().lastIndexOf(":")));
						//toast.show();
						e.printStackTrace();
					} catch (SQLiteException e) {
						toast.setText(e.getMessage().substring(0,e.getMessage().lastIndexOf(":")));
						toast.show();
						e.printStackTrace();
					} catch (BunkerException e) {
						toast.setText(e.getMessage());
						toast.show();
						e.printStackTrace();
					}
					c.requery();
					listAdapter.notifyDataSetChanged();
				}});
			
			
			
			
			
			return convertView;
		}
		
		
	}
	public void notifyData(){
		c.requery();
		listAdapter.notifyDataSetChanged();
		
	}
}


