package com.vosaye.bunkr.app;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.externalpackages.Pager_Adapter;
import com.vosaye.bunkr.externalpackages.ScrollingTabs_Adapter;
import com.vosaye.bunkr.externalpackages.master.ScrollingTabsView;
import com.vosaye.bunkr.fragments.Today;
import com.vosaye.bunkr.services.ValidatorService;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class BaseActivity extends SherlockFragmentActivity {

    

    private ViewPager mPager;
	private Pager_Adapter mPagerAdapter;
	BunKar bunker;
	private ScrollingTabsView mScrollingTabs;
	private ScrollingTabs_Adapter mScrollingTabsAdapter;
	Today today;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_tos);
        ActionBar action = this.getSupportActionBar();
        action.show();
        bunker = (BunKar) this.getApplication();
        action.setTitle(" Home");
        action.setSubtitle("  "+bunker.name);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#87CEFF")));
        setContentView(R.layout.viewpager_scrollingtab_layout);
		
		this.initialisePaging();
		mScrollingTabs = (ScrollingTabsView) findViewById(R.id.viewpager_scrolling_tabs);
		mScrollingTabs.act = this;
		mScrollingTabsAdapter = new ScrollingTabs_Adapter(this);
		mScrollingTabsAdapter.setStringsTitles("Today", "Stats", "Calendar");
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mPager);
		
    }
    private com.vosaye.bunkr.fragments.Calendar calendar;
    private void initialisePaging()
	{
    	calendar = (com.vosaye.bunkr.fragments.Calendar) Fragment.instantiate(this, com.vosaye.bunkr.fragments.Calendar.class.getName());
		mPager = (ViewPager) findViewById(R.id.viewpager_scrolling_pager);
		List<Fragment> fragments = new Vector<Fragment>();
		Bundle bundle = new Bundle();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		bundle.putString("date", BunKar.sdf.format(cal.getTime()));
		today = new Today();
		today.setArguments(bundle);
		fragments.add(today);
		fragments.add(Fragment.instantiate(this, com.vosaye.bunkr.fragments.Stats.class.getName()));
		fragments.add(calendar);
		this.mPagerAdapter = new Pager_Adapter(super.getSupportFragmentManager(), fragments);
		
		
		mPager.setAdapter(mPagerAdapter);

		
		mPager.setCurrentItem(1);
		mPager.setPageMargin(1);
	}
    public void onPause(){
    	super.onPause();
    	ValidatorService.FREEFLOW = true;
    	//Toast.makeText(this, "paused base activity", Toast.LENGTH_LONG).show();
    }
    public void onResume(){
    	super.onResume();
    	
    	ValidatorService.FREEFLOW = false;
    	ValidatorService.FOCUSED = true;
    	//Toast.makeText(this, "resumed", Toast.LENGTH_LONG).show();
    	calendar.printCalendarView();
    	Calendar cal = Calendar.getInstance();
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		today.sched = bunker.getDatabase(bunker.name);
		
    	today.setDate(cal.getTime());
    }
    boolean pressedOnce = false;
    public void onBackPressed(){
    	String base = (String) this.getIntent().getCharSequenceExtra("base");
    	if(base!=null){
    		if(base.equals("yes")){
    			if(pressedOnce){
    				//close databases
    				//exit
    				System.exit(0);
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
    	}
    	else this.finish();
    		
    }
    
    
}

