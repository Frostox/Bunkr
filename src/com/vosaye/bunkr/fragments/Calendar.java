package com.vosaye.bunkr.fragments;



import java.text.SimpleDateFormat;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;









import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.BlackHoleActivity;
import com.vosaye.bunkr.app.StructureActivity;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.customviews.CalendarView;
import com.vosaye.bunkr.events.ScrollListListener;

public class Calendar   extends Fragment implements ScrollListListener{
	public SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
	BunKar bunker;
	ScheduleDatabase sched;
	@Override
	public void onDone(View view) {
		// TODO Auto-generated method stub
		
	}

	public void onResume(){
		super.onResume();
		if(sched==null){
			sched = bunker.getDatabase(bunker.name);
			calendarView.sched = bunker.getDatabase(bunker.name);
		}
		printCalendarView();
	}
	@Override
	public void onError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}
	private CalendarView calendarView;
	public CalendarView getCalendarView(){
		return calendarView;
	}
	
	public void printCalendarView(){
		if(calendarView!=null)
		calendarView.adapter.printMonth();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		LinearLayout current = null;
		bunker = ((BunKar)Calendar.this.getActivity().getApplication());
		sched = bunker.getDatabase(bunker.name);
		
			current = (LinearLayout) this.getActivity().getLayoutInflater().inflate(R.layout.base_calendar, null);
			calendarView = new CalendarView(this.getActivity(),this.getActivity().getLayoutInflater());
			calendarView.setOnDoneListener(new ScrollListListener(){

				@Override
				public void onDone(View view) {
					
					java.util.Calendar selected = java.util.Calendar.getInstance();
					selected.setTime(calendarView.selected);
					if(sched.stats.inBlackHole(selected.getTime())){
						//go to blackhole activity
						String name = sched.stats.getBlackHole(selected.getTime());
						Intent intent = new Intent(Calendar.this.getActivity(),BlackHoleActivity.class);
						intent.putExtra("name", name);
						Calendar.this.getActivity().startActivity(intent);
					}
					else{
						Intent intent = new Intent(Calendar.this.getActivity(),StructureActivity.class);
						intent.putExtra("date", sdf.format(selected.getTime()));
						intent.putExtra("count", "0");
						//one day - structure with records
						Calendar.this.getActivity().startActivity(intent);
					}
				}

				@Override
				public void onError(String errorMessage) {
					
					
				}});
			current.addView(calendarView.getView());
		
		
		return current;
	}
	
}
