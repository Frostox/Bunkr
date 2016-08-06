package com.vosaye.bunkr.fragments;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.Here;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.events.ScrollListListener;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.ValidatorService;

public class Stats   extends Fragment implements ScrollListListener, OnClickListener{
	RelativeLayout layout, header;
	ProgressBar pg;
	TextView percCompleted;
	MyBroadcastReceiver mbroadcast;
	MyBroadcastReceiverRefresh refreshBroadcast;
	RadioGroup rg;
	RadioButton sub, typ, none;
	ExpandableListView list;
	ExpAdapter listAdapter;
	Cursor c;
	BunKar bunkr;
	ScheduleDatabase sched;
	AuthDatabase settings;
	
	@Override
	public void onDone(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}
	private void hideView(final View view){
		    view.setVisibility(View.GONE);
	}
	
	
	private void showView(final View view){
	    view.setVisibility(View.VISIBLE);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		
		if (container == null) 
		{
			return null;
		}

		bunkr = (BunKar) this.getActivity().getApplication();
		sched = bunkr.getDatabase(bunkr.name);
		settings = bunkr.settings;
		layout = (RelativeLayout) inflater.inflate(R.layout.activity_main, container, false);
		header = (RelativeLayout) layout.findViewById(R.id.act_main_stats_list_relativeLayout1);
		rg = (RadioGroup) layout.findViewById(R.id.act_main_stats_list_radiogrp);
		sub = (RadioButton) layout.findViewById(R.id.act_main_stats_list_radioButton1);
		typ = (RadioButton) layout.findViewById(R.id.act_main_stats_list_radioButton2);
		none = (RadioButton) layout.findViewById(R.id.act_main_stats_list_radioButton3);
		list = (ExpandableListView) layout.findViewById(R.id.act_main_stats_list_expandableListView1);
		
		list.setOnGroupExpandListener(new OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

            	for(int i=0; i<listAdapter.getGroupCount();i++){
                 	if(i!=groupPosition)
                 		list.collapseGroup(i);
                 	
                 }

                    
                   

                
            }
        });
		
		list.setOnGroupClickListener(new OnGroupClickListener(){

			@Override
			public boolean onGroupClick(ExpandableListView arg0, View grp,
					int groupPosition, long arg3) {
				 // TODO Auto-generated method stub
				return false;
			}});
		/*
 			try {
			Calendar cal = Calendar.getInstance(); 
			int mins = cal.get(Calendar.HOUR_OF_DAY) * 60;
			mins = mins + cal.get(Calendar.MINUTE);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			sched.createTable("todaytemp", sched.statDef);
			sched.deleteFromTable("todaytemp", "");
			String query = "(";
			if((!sched.stats.inBlackHole(cal.getTime()))&&(!sched.meta.selectFromIndex(cal.getTime()).equals("labsdecoreblank"))&&((cal.getTime().compareTo(sched.start)>=0&&cal.getTime().compareTo(sched.end)<=0)))
			query = "(select s.IDrel, r.attendance from "+sched.meta.selectFromIndex(cal.getTime())+" s, "+sched.meta.selectRecord(cal.getTime())+" r where s.mins = r.mins and r.attendance != 3 and (s.mins + s.duration) <= "+mins+") ";
			
			if(!query.equals("(")){
				sched.execQuery("insert into todaytemp (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join ((select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel)) b on a.IDrel = b.IDrel where b.IDrel is null;");
				sched.execQuery("update todaytemp set attendance = 0 where attendance is null");
			}
			if(sched.tableExists(sched.stats.getTodayRange())){
			if(sched.isEmpty(sched.stats.getTodayRange())&&sched.isEmpty("todaytemp")){
				query = "select 1 as _id, c.IDrel as IDrel, b.subjname, b.typname, 0 as attendance, 0 as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from  session b, "+sched.stats.getOverallRange()+" c where c.IDrel = b.sessionID ";
				
			}
			else
			//query = "select 1 as _id, a.IDrel as IDrel, b.subjname, b.typname, sum(a.attendance) as attendance, sum(a.total) as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from (select IDrel, sum(attendance) as attendance, sum(total) as total from (select * from todaytemp UNION ALL select * from "+sched.stats.getTodayRange()+") group by IDrel ) a, session b, "+sched.stats.getOverallRange()+" c where a.IDrel = b.sessionID and a.IDrel = c.IDrel ";
			query = "select 1 as _id, IDrel, b.subjname as subjname, b.typname as typname, sum(attendance) as attendance, sum(total) as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select a.IDrel as IDrel, sum(a.attendance) as attendance, sum(a.total) as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from (select IDrel, sum(attendance) as attendance, sum(total) as total from (select * from todaytemp UNION ALL select * from "+sched.stats.getTodayRange()+") group by IDrel ) a, "+sched.stats.getOverallRange()+" c where a.IDrel = c.IDrel group by IDrel UNION ALL select IDrel, 0 as attendance, 0 as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select b.IDrel as IDrel, sum(b.attendance) as oattendance, sum(b.total) as ototal from (select IDrel from "+sched.stats.getOverallRange()+" a except select IDrel from (select IDrel from todaytemp UNION ALL select IDrel from "+sched.stats.getTodayRange()+" ) b) a, "+sched.stats.getOverallRange()+" b where a.IDrel = b.IDrel group by IDrel) group by IDrel) a, session b where a.IDrel = b.sessionID ";
			if(rg.getCheckedRadioButtonId()==sub.getId()){
				query = query + " group by b.subjname";
			}else if(rg.getCheckedRadioButtonId()==typ.getId()){
				query = query + " group by b.typname";
			}else{
				query = query + " group by IDrel";
			}
			if(c!=null)
			c.close();
			c = sched.rawQuery(query);
			
			
			listAdapter = new ExpAdapter(c,this.getActivity());
			list.setAdapter(listAdapter);
			}
			
			
		} catch (BunkerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		sub.setOnClickListener(this);
		typ.setOnClickListener(this);
		none.setOnClickListener(this);
		percCompleted = (TextView) layout.findViewById(R.id.act_main_stats_list_textView2);
		percCompleted.setText("0 %");
		pg = (ProgressBar) layout.findViewById(R.id.act_main_stats_list_progressBar1);
		pg.setMax(100);
		pg.setProgress(0);
		Stats.this.hideView(header);
		
		mbroadcast = new MyBroadcastReceiver();
		IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATE");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.getActivity().registerReceiver(mbroadcast, intentFilter);
		
		
		refreshBroadcast = new MyBroadcastReceiverRefresh();
		intentFilter = new IntentFilter("com.vosaye.bunkr.REFRESH");
		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
		this.getActivity().registerReceiver(refreshBroadcast, intentFilter);
		
		RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		lp.addRule(RelativeLayout.ABOVE, R.id.act_main_stats_list_opt);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		list.setLayoutParams(lp);
		return layout;
	}
	public void onStart(){
		super.onStart();
		bunkr = (BunKar) this.getActivity().getApplication();
		sched = bunkr.getDatabase(bunkr.name);
		listAdapter = new ExpAdapter(null,this.getActivity());
		list.setAdapter(listAdapter);
		this.onGroupByChanged();
	}
	
	public void onResume(){
		super.onResume();
		
	}
	
	public void onPause(){
		super.onPause();
		//c.close();
	}
	
	public class MyBroadcastReceiver extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  
			  int perc = intent.getIntExtra("perc", 0);
			  if(perc==-1){
				  pg.setProgress(100);
				  Stats.this.hideView(header);
				  RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
				  lp.addRule(RelativeLayout.ABOVE, R.id.act_main_stats_list_opt);
				  lp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				  list.setLayoutParams(lp);
				  onGroupByChanged();
			  }
			  else{
				  Stats.this.showView(header);
				  RelativeLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
				  lp.addRule(RelativeLayout.ABOVE, R.id.act_main_stats_list_opt);
				  lp.addRule(RelativeLayout.BELOW, header.getId());
				  list.setLayoutParams(lp);
				  pg.setProgress(perc);
				  percCompleted.setText(perc+" %");
				  ValidatorService.FREEFLOW = true;
			  }
			  
		  }
		 }
	
	public class MyBroadcastReceiverRefresh extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  onGroupByChanged();
			  
		  }
		 }
	
	public void onDestroy(){
		super.onDestroy();
		this.getActivity().unregisterReceiver(mbroadcast);
		this.getActivity().unregisterReceiver(refreshBroadcast);
		if(c!=null)
		c.close();
	}

	@Override
	public void onClick(View arg0) {
		this.onGroupByChanged();
	}
	
	public void onGroupByChanged(){
		try {
			if(listAdapter!=null)
			listAdapter.setGroupCursor(null);
			Calendar cal = Calendar.getInstance(); 
			int mins = cal.get(Calendar.HOUR_OF_DAY) * 60;
			mins = mins + cal.get(Calendar.MINUTE);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			sched.createTable("todaytemp", sched.statDef);
			sched.deleteFromTable("todaytemp", "");
			String query = "(";
			if((!sched.stats.inBlackHole(cal.getTime()))&&(!sched.meta.selectFromIndex(cal.getTime()).equals("labsdecoreblank"))&&((cal.getTime().compareTo(sched.start)>=0&&cal.getTime().compareTo(sched.end)<=0)))
			query = "(select s.IDrel, r.attendance from "+sched.meta.selectFromIndex(cal.getTime())+" s, "+sched.meta.selectRecord(cal.getTime())+" r where s.mins = r.mins and r.attendance != 3 and (s.mins) <= "+mins+") ";
			
			if(!query.equals("(")){
				System.out.println("Vosaye :: inhere");
				sched.execQuery("insert into todaytemp (IDrel, attendance, total) select a.IDrel, a.attendance, b.total from (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel) group by IDrel) a left join ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) b on a.IDrel = b.IDrel UNION ALL select a.IDrel, b.attendance, a.total from ((select IDrel, count(attendance) as total from "+query+" group by IDrel)) a left join (select IDrel, sum(attendance) as attendance from (select IDrel, sum(attendance) as attendance from "+query+"where attendance = 1 group by IDrel UNION ALL select IDrel, sum(attendance)/2 as attendance from "+query+"where attendance = 2 group by IDrel) group by IDrel) b on a.IDrel = b.IDrel where b.IDrel is null;");
				if(sched.isEmpty("todaytemp")) System.out.println("Vosaye :: Empty"); else System.out.println("Vosaye :: Nope");
				sched.execQuery("update todaytemp set attendance = 0 where attendance is null");
			}
			query = "(";
			if(sched.tableExists(sched.stats.getTodayRange())){
			if(sched.isEmpty(sched.stats.getTodayRange())&&sched.isEmpty("todaytemp")){
				query = "select 1 as _id, c.IDrel as IDrel, b.subjname, b.typname, 0 as attendance, 0 as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from  session b, "+sched.stats.getOverallRange()+" c where c.IDrel = b.sessionID ";
				
			}
			else
			query = "select 1 as _id, IDrel, b.subjname as subjname, b.typname as typname, sum(attendance) as attendance, sum(total) as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select a.IDrel as IDrel, sum(a.attendance) as attendance, sum(a.total) as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from (select IDrel, sum(attendance) as attendance, sum(total) as total from (select * from todaytemp UNION ALL select * from "+sched.stats.getTodayRange()+") group by IDrel ) a, "+sched.stats.getOverallRange()+" c where a.IDrel = c.IDrel group by IDrel UNION ALL select IDrel, 0 as attendance, 0 as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select b.IDrel as IDrel, sum(b.attendance) as oattendance, sum(b.total) as ototal from (select IDrel from "+sched.stats.getOverallRange()+" a except select IDrel from (select IDrel from todaytemp UNION ALL select IDrel from "+sched.stats.getTodayRange()+" ) b) a, "+sched.stats.getOverallRange()+" b where a.IDrel = b.IDrel group by IDrel) group by IDrel) a, session b where a.IDrel = b.sessionID ";
			
			}
			else {
				query = "select 1 as _id, IDrel, b.subjname as subjname, b.typname as typname, sum(attendance) as attendance, sum(total) as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select a.IDrel as IDrel, sum(a.attendance) as attendance, sum(a.total) as total, sum(c.attendance) as oattendance, sum(c.total) as ototal from (select IDrel, sum(attendance) as attendance, sum(total) as total from (select * from todaytemp) group by IDrel ) a, "+sched.stats.getOverallRange()+" c where a.IDrel = c.IDrel group by IDrel UNION ALL select IDrel, 0 as attendance, 0 as total, sum(oattendance) as oattendance, sum(ototal) as ototal from (select b.IDrel as IDrel, sum(b.attendance) as oattendance, sum(b.total) as ototal from (select IDrel from "+sched.stats.getOverallRange()+" a except select IDrel from (select IDrel from todaytemp) b) a, "+sched.stats.getOverallRange()+" b where a.IDrel = b.IDrel group by IDrel) group by IDrel) a, session b where a.IDrel = b.sessionID ";
				
			}
			
			
			if(rg.getCheckedRadioButtonId()==sub.getId()){
				query = query + " group by b.subjname";
			}else if(rg.getCheckedRadioButtonId()==typ.getId()){
				query = query + " group by b.typname";
			}else{
				query = query + " group by IDrel";
			}
			if(c!=null)
			c.close();
			c = sched.rawQuery(query);
			if(listAdapter!=null){
			listAdapter.setGroupCursor(c);
			listAdapter.notifyDataSetChanged();}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	final class ExpAdapter extends CursorTreeAdapter {
        LayoutInflater mInflator;

        public ExpAdapter(Cursor cursor, Context context) {
            super(cursor, context);
            mInflator = LayoutInflater.from(context);
        }

        @Override
        protected void bindChildView(View view, Context context, Cursor cursor, boolean isLastChild) {
        	System.out.println("Vosayes : Binding Child");
        	
        	
        	final TextView title = (TextView) view.findViewById(R.id.stats_list_child_textView1);
        	final TextView bunkabletitle = (TextView) view.findViewById(R.id.stats_list_child_textView2);
        	final TextView bunkable = (TextView) view.findViewById(R.id.stats_list_child_textView3);
        	final TextView attendabletitle = (TextView) view.findViewById(R.id.stats_list_child_textView4);
        	final TextView attendable = (TextView) view.findViewById(R.id.stats_list_child_textView5);
        	final TextView none = (TextView) view.findViewById(R.id.stats_list_child_textView7);
        	final SeekBar sbar = (SeekBar) view.findViewById(R.id.stats_list_child_seekBar1);
        	final TextView seekabletitle = (TextView) view.findViewById(R.id.stats_list_child_textView8);
        	final TextView seekable = (TextView) view.findViewById(R.id.stats_list_child_textView9);
        	final TextView nonetitle = (TextView) view.findViewById(R.id.stats_list_child_textView6);
        	//final TextView info = (TextView) view.findViewById(R.id.stats_list_child_textView10);
        	final RelativeLayout seekbarContainer = (RelativeLayout) view.findViewById(R.id.stats_list_child_seekBarContainer);
        	
        	final float currentatt = c.getFloat(4);
        	final float overalltotal = c.getFloat(7);
        	final float remaining = c.getFloat(7) - c.getFloat(5);
        	if(rg.getCheckedRadioButtonId()==sub.getId()){
        		title.setText((int)remaining+" "+c.getString(2)+" are left");
            	
			}else if(rg.getCheckedRadioButtonId()==typ.getId()){
				title.setText((int)remaining+" "+c.getString(3)+" are left");
	        	
			}else{
        		title.setText((int)remaining+" "+c.getString(2)+" - "+c.getString(3)+" are left");
	        	
			}
        	if(remaining==0){
        		bunkabletitle.setVisibility(View.GONE);
        		bunkable.setVisibility(View.GONE);
        		attendabletitle.setVisibility(View.GONE);
        		attendable.setVisibility(View.GONE);
        		none.setVisibility(View.GONE);
        		nonetitle.setVisibility(View.GONE);
        		seekabletitle.setVisibility(View.GONE);
        		seekable.setVisibility(View.GONE);
        		sbar.setVisibility(View.GONE);
        		//info.setVisibility(View.GONE);
        		return;
        		
        	}
        	else if(remaining==1){
        		bunkabletitle.setVisibility(View.VISIBLE);
        		bunkable.setVisibility(View.VISIBLE);
        		attendabletitle.setVisibility(View.VISIBLE);
        		attendable.setVisibility(View.VISIBLE);
        		
        		seekabletitle.setVisibility(View.GONE);
        		seekable.setVisibility(View.GONE);
        		sbar.setVisibility(View.GONE);
        		//info.setVisibility(View.GONE);
        		nonetitle.setVisibility(View.GONE);
        		none.setVisibility(View.GONE);
        		
        	}
        	else if(remaining==2){
        		bunkabletitle.setVisibility(View.VISIBLE);
        		bunkable.setVisibility(View.VISIBLE);
        		attendabletitle.setVisibility(View.VISIBLE);
        		attendable.setVisibility(View.VISIBLE);
        		none.setVisibility(View.VISIBLE);
        		nonetitle.setVisibility(View.VISIBLE);
        		
        		seekabletitle.setVisibility(View.GONE);
        		seekable.setVisibility(View.GONE);
        		sbar.setVisibility(View.GONE);
        		//info.setVisibility(View.GONE);
        		
        	}
        	else {
        		bunkabletitle.setVisibility(View.VISIBLE);
        		bunkable.setVisibility(View.VISIBLE);
        		attendabletitle.setVisibility(View.VISIBLE);
        		attendable.setVisibility(View.VISIBLE);
        		none.setVisibility(View.VISIBLE);
        		nonetitle.setVisibility(View.VISIBLE);
        		seekabletitle.setVisibility(View.VISIBLE);
        		seekable.setVisibility(View.VISIBLE);
        		sbar.setVisibility(View.VISIBLE);
        		//info.setVisibility(View.VISIBLE);
        	}
        	
        	float unotarget = (1/c.getFloat(7))*100;
        	float shouldAttend = (float) Math.ceil(settings.schedules.getCutoff(bunkr.name)/unotarget);
        	int canBunk = 0;
        	
        	
        	if(remaining!=0){
            	if(shouldAttend > (c.getFloat(4)+remaining)){
            		canBunk = 1;
            	}
            	else {
            		if(shouldAttend <= c.getFloat(4)){
            			canBunk = (int) remaining;
            		} else {
            			
            				canBunk = (int) (remaining - (shouldAttend - c.getFloat(4)));
            			
            		}
            	}
            	
            }
        	
        	
        	if(remaining==0){
        		bunkabletitle.setText("-");
            	bunkable.setText("-");
            	
        	}
        	else if(remaining!=0&&canBunk==0){
        		bunkabletitle.setText("If you bunk 1");
        		bunkable.setText(""+String.format("%.2f", (((((c.getFloat(4)+remaining-1)/c.getFloat(7))*100))))+"% ");
        	
        	}
        	else{
        		bunkabletitle.setText("If you bunk "+canBunk);
        		bunkable.setText(""+String.format("%.2f", (((((c.getFloat(4)+remaining-canBunk)/c.getFloat(7))*100))))+"% ");
        	}
        	
        	if(remaining!=0){
        		if(remaining==1)
        		attendabletitle.setText("If you attend "+(int)remaining);
        		else
            	attendabletitle.setText("If you attend all "+(int)remaining);
        		attendable.setText(""+String.format("%.2f", (((((c.getFloat(4)+remaining)/c.getFloat(7))*100))))+"% ");
        	}
        	else{
        		attendabletitle.setText("-");
            	attendable.setText("-");
        	}
        	
        	if(remaining==1) return;
        	none.setText(""+String.format("%.2f", (((((c.getFloat(4))/c.getFloat(7))*100))))+"% ");
        	if(remaining==2) return;
        	sbar.setMax((int)remaining);
        	
        	sbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

				@Override
				public void onProgressChanged(SeekBar seekBar, int progress,
						boolean fromUser) {
					seekabletitle.setText("If you bunk "+progress);
					seekable.setText(""+String.format("%.2f", (((((currentatt+remaining-progress)/overalltotal)*100))))+"% ");
				}

				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {
					// TODO Auto-generated method stub
					
				}});

        	sbar.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent arg1) {

                    if(arg1.getAction() == MotionEvent.ACTION_DOWN || arg1.getAction() == MotionEvent.ACTION_MOVE)
                    {
                    list.requestDisallowInterceptTouchEvent(true);

                    }
                    return false;
                }
            });
        	sbar.setProgress(0);
        	seekabletitle.setText("If you bunk __");
        	seekable.setText("-");
        	
        }
        
        
        @Override
        protected void bindGroupView(View view, Context context, Cursor cursor, boolean isExpanded) {
        	System.out.println("Vosayes : Binding Group");
        	
        	final TextView subname = (TextView) view.findViewById(R.id.stats_list_textView1);
        	final TextView typname = (TextView) view.findViewById(R.id.stats_list_textView2);
        	final TextView perc = (TextView) view.findViewById(R.id.stats_list_textView3);
        	final TextView remain = (TextView) view.findViewById(R.id.stats_list_textView4);
            
            
            if(rg.getCheckedRadioButtonId()==sub.getId()){
            	String typ = cursor.getString(2);
				for(int i=1; i<=4-typ.length();i++)
					typ=typ + " ";
				subname.setText(typ);
	            typname.setText("");
			}else if(rg.getCheckedRadioButtonId()==typ.getId()){
				String typ = cursor.getString(3);
				for(int i=1; i<=4-typ.length();i++)
					typ+=" ";
				subname.setText(typ);
	            typname.setText("");
			}else{
				String typ = cursor.getString(2);
				for(int i=1; i<=4-typ.length();i++)
					typ+=" ";
				subname.setText(typ);
				typ = cursor.getString(3);
				for(int i=1; i<=4-typ.length();i++)
					typ+=" ";
	            typname.setText(typ);
			}
            
            float temp = (cursor.getFloat(4)/cursor.getFloat(5))*100;
            if(cursor.getFloat(5)==0) temp = 0;
            perc.setText(""+String.format("%.2f", ((temp)))+"% ");
            if(temp<settings.schedules.getCutoff(bunkr.name))
            	perc.setTextColor(getResources().getColor(R.color.orrange));
            else perc.setTextColor(Color.parseColor("#006400"));
            
            remain.setTextColor(Color.BLACK);
    		
            float remaining = cursor.getFloat(7) - cursor.getFloat(5);
            if(remaining!=0){
            	int canBunk = 0;
            	float unotarget = (1/cursor.getFloat(7))*100;
            	float shouldAttend = (float) Math.ceil(settings.schedules.getCutoff(bunkr.name)/unotarget);
            	if(shouldAttend > (cursor.getFloat(4)+remaining)){
            		remain.setTextColor(getResources().getColor(R.color.orrange));
            		canBunk = (int) remaining;
            	}
            	else {
            		remain.setTextColor(Color.parseColor("#006400"));
            		if(shouldAttend <= cursor.getFloat(4)){
            			canBunk = (int) remaining;
            		} else {
            			
            				canBunk = (int) (remaining - (shouldAttend - cursor.getFloat(4)));
                		
            		}
            	}
            	remain.setText(""+canBunk+" ");
            
            }
            else remain.setText(""+0+" ");
            
            
            
            
            
        }

        @Override
        protected Cursor getChildrenCursor(Cursor groupCursor) {
        	
            Cursor c = sched.rawQuery("select 1 as _id, 'uno'");
            return c;
        }

        @Override
        protected View newChildView(Context context, Cursor cursor, boolean isLastChild, ViewGroup parent) {
            View mView = mInflator.inflate(R.layout.stats_list_child, null);
            return mView;
        }

        @Override
        protected View newGroupView(Context context, Cursor cursor, boolean isExpanded, ViewGroup parent) {
            View mView = mInflator.inflate(R.layout.stats_list_item, null);
            
            return mView;
        }
        
        

    }
	
}


