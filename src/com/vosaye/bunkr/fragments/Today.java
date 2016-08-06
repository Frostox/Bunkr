package com.vosaye.bunkr.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.EditAllStructures;
import com.vosaye.bunkr.app.StructureActivity;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.customviews.Timeline;
import com.vosaye.bunkr.events.ScrollListListener;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.fragments.Stats.MyBroadcastReceiver;

public class Today   extends Fragment implements ScrollListListener{
	BunKar bunker;
	public ScheduleDatabase sched;
	String str = "";
	Timeline timeline;
	boolean locked = false;
	Date date;
	MyBroadcastReceiver mbroadcast;
	
	@Override
	public void onDone(View view) {
		// TODO Auto-generated method stub
		
	}
	
	

	@Override
	public void onError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}
	
	public void onResume(){
		super.onResume();
		if(sched==null) sched = bunker.getDatabase(bunker.name);
		this.inflate();
	}
	
	public void closeList(){
		timeline.closeList();
	}
	
	public void onPause(){
		super.onPause();
		//timeline.closeList();
		
		
	}
	
	
	
	public void setDate(Date date){
		this.date = date;
		if(timeline!=null){
			try {
				Cursor c = null;
				if(sched.stats.inBlackHole(date)&&!sched.meta.selectFromIndex(date).equals("labsdecoreblank")){
					c = sched.rawQuery("select str.mins, ses.subjname, ses.typname, str.duration, 1 as attendance from "
					+sched.meta.selectFromIndex(date)+
					" str, session ses "
					+" where ses.sessionID = str.IDrel;");
				}
				else if(!sched.meta.selectFromIndex(date).equals("labsdecoreblank"))
					
					
					c = sched.rawQuery("select str.mins, ses.subjname, ses.typname, str.duration, rec.attendance from "
					+sched.meta.selectFromIndex(date)+
					" str, session ses, "
					+sched.meta.selectRecord(date)+
					" rec where str.mins = rec.mins and ses.sessionID = str.IDrel;");
				
				timeline.setCursor(c);
				timeline.inflate();
				 
			} catch (SQLiteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BunkerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void setCursor(Cursor c){
		if(timeline!=null)
			timeline.setCursor(c);
	}
	
	public void inflate(){
		if(timeline!=null){
			timeline.inflate();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		bunker = (BunKar) this.getActivity().getApplication();
		sched = bunker.getDatabase(bunker.name);
		
		Cursor c = null;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
		
		String strdate = this.getArguments().getString("date");
		try {
			cal.setTime(sdf.parse(strdate));
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		
		
		Date date = cal.getTime();
		try {
			if(sched.stats.inBlackHole(date)&&!sched.meta.selectFromIndex(date).equals("labsdecoreblank")){
				c = sched.rawQuery("select str.mins, ses.subjname, ses.typname, str.duration, 1 as attendance from "
				+sched.meta.selectFromIndex(date)+
				" str, session ses "
				+" where ses.sessionID = str.IDrel;");
			}
			else if(!sched.meta.selectFromIndex(date).equals("labsdecoreblank")){
				
				if(!sched.tableExists(sched.meta.selectRecord(date))) sched.meta.makeRecord(date);
				c = sched.rawQuery("select str.mins, ses.subjname, ses.typname, str.duration, rec.attendance from "
				+sched.meta.selectFromIndex(date)+
				" str, session ses, "
				+sched.meta.selectRecord(date)+
				" rec where str.mins = rec.mins and ses.sessionID = str.IDrel;");
				}
			 
		} catch (SQLiteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BunkerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		LinearLayout current = (LinearLayout) this.getActivity().getLayoutInflater().inflate(R.layout.today, null);
		timeline = (Timeline) current.findViewById(R.id.today_timeline);
		ScrollView scroll = (ScrollView) current.findViewById(R.id.today_scrollView1);
		timeline.setActivity(this.getActivity(),date);
		//Toast.makeText(this.getActivity(), "cont here is "+c.getCount(),Toast.LENGTH_LONG).show();
		timeline.setCursor(c);
		timeline.scroll = scroll;
		timeline.setWillNotDraw(false);
		timeline.inflate();
		timeline.setLocked(locked);
		if(stract!=null)
			stract.invalidateOptionsMenu();
		
		mbroadcast = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.REFRESH");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.getActivity().registerReceiver(mbroadcast, intentFilter);
		
		
		
		return current;
	}
	StructureActivity stract;
	public void setStrActivity(StructureActivity str){
		stract = str;
	}
	public void setLocked(boolean locked){
		if(timeline!=null){
		timeline.setLocked(locked);
		this.locked = locked;
		
		}
		else 

			this.locked = locked;
	}
	public boolean getLocked(){
		if(timeline!=null)
		return timeline.getLocked();
		else return false;
	}
	
	public void onDestroy(){
		super.onDestroy();
		this.getActivity().unregisterReceiver(mbroadcast);
		
		timeline.closeList();
	}
	
	
	public class MyBroadcastReceiver extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  timeline.inflate();
			  
		  }
		 }
}
