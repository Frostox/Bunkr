package com.vosaye.bunkr.app;

import java.util.List;
import java.util.Vector;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.externalpackages.Pager_Adapter;
import com.vosaye.bunkr.externalpackages.ScrollingTabs_Adapter;
import com.vosaye.bunkr.externalpackages.master.ScrollingTabsView;
import com.vosaye.bunkr.fragments.Terms;
import com.vosaye.bunkr.services.ValidatorService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager; 
import android.text.Html;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class TOS extends SherlockFragmentActivity {
	
	private ViewPager mPager;
	private Pager_Adapter mPagerAdapter;
	BunKar bunker;
	private ScrollingTabsView mScrollingTabs;
	private ScrollingTabs_Adapter mScrollingTabsAdapter;
	public ScheduleDatabase scheduleDB;
	public Toast toast; 
    @SuppressLint("ShowToast")
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_tos);
        ActionBar action = this.getSupportActionBar();
        action.show();
        action.setTitle(" Fill up Details"); 
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2c3e50")));
        BunKar.count++; 
        setContentView(R.layout.activity_tos);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		this.initialisePaging();
		mScrollingTabs = (ScrollingTabsView) findViewById(R.id.viewpager_scrolling_tabs);
		mScrollingTabs.setTos(this);
		mScrollingTabsAdapter = new ScrollingTabs_Adapter(this);
		mScrollingTabs.setAdapter(mScrollingTabsAdapter);
		mScrollingTabs.setViewPager(mPager);
		bunker = ((BunKar) this.getApplication());
		action.setSubtitle(" "+bunker.name);
		toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		scheduleDB = bunker.getDatabase(bunker.name);
    }

    public void onDone(){
    	if(!scheduleDB.exists("select name from subject")){
			//plz setup subject first
			toast.setText("Please set up subjects first");
			toast.show();
		}
		else if(!scheduleDB.exists("select name from type")){
			//plz setup type first
			toast.setText("Please set up type first");
			toast.show();
		}
		else if(!scheduleDB.exists("select * from term")){
			//plz setup term first
			toast.setText("Please set up terms first");
			toast.show();
		}
		else{
			scheduleDB.execQuery("insert into session (subjname, typname) select s.name, t.name from subject s, type t except select subjname, typname from session");
			
			terms.setDisabled();
			Intent i = new Intent(this, EditAllStructures.class);
			this.startActivityForResult(i,0);
			overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
			
		}
    }
    Terms terms;
    private void initialisePaging()
	{
		mPager = (ViewPager) findViewById(R.id.viewpager_scrolling_pager);
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(Fragment.instantiate(this, com.vosaye.bunkr.fragments.Subjects.class.getName()));
		fragments.add(Fragment.instantiate(this, com.vosaye.bunkr.fragments.Types.class.getName()));
		terms = (Terms) Fragment.instantiate(this, com.vosaye.bunkr.fragments.Terms.class.getName());
		terms.setTOS(this);
		fragments.add(terms);
		this.mPagerAdapter = new Pager_Adapter(super.getSupportFragmentManager(), fragments);
		mPager.setAdapter(mPagerAdapter);
		mPager.setCurrentItem(0);
		mPager.setOffscreenPageLimit(3);
		mPager.setPageMargin(1); 
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
       if(mPager.getCurrentItem()!=2)
    	   inflater.inflate(R.menu.tos_menu,  menu);
       else
    	   inflater.inflate(R.menu.tos_menu_last,  menu);
       return super.onCreateOptionsMenu(menu);
    }
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if(itemId == R.id.next_button){
			if(mPager.getCurrentItem()!=2){
				if(!scheduleDB.exists("select name from subject")&&mPager.getCurrentItem()==0){
					//plz setup subject first
					toast.setText("Please set up subjects first");
					toast.show();
				}else if(!scheduleDB.exists("select name from type")&&mPager.getCurrentItem()==1){
					//plz setup type first
					toast.setText("Please set up type first");
					toast.show();
				}
				
				else
				mPager.setCurrentItem(mPager.getCurrentItem()+1);
			}
				
			return true;
		} else if(itemId == android.R.id.home){
			this.setResult(1);
			finish();
			return true;
		}
		
		else {
			return super.onOptionsItemSelected(item);
		}
    }
    public void onBackPressed(){
    	this.setResult(1);
    	this.finish();
    		
    }
    
    public void onPause(){
		super.onPause();
		ValidatorService.FREEFLOW = true;
	}
	public void onResume(){
		super.onResume();
		ValidatorService.FREEFLOW = true;
    	ValidatorService.FOCUSED = true;
	}
    public void onDestroy(){
    	super.onDestroy();
    	if(scheduleDB.getDatabase().inTransaction())
    		scheduleDB.rollback();
    	ValidatorService.HALT = false;
    	BunKar.count--;
    	if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
    }
   
	 
    @Override
	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	     switch(resultCode)
	     {
	     case 1:
	    	 setResult(1);
	    	 finish();
	    	 
	     case BunKar.RESULT_CLOSE_ALL:
	    	 setResult(BunKar.RESULT_CLOSE_ALL);
	    	 finish();
	     }
	     super.onActivityResult(requestCode, resultCode, data);
	 }
	 
    
}
