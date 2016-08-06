package com.vosaye.bunkr.app;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import net.sqlcipher.Cursor;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.R.drawable;
import com.vosaye.bunkr.R.id;
import com.vosaye.bunkr.R.layout;
import com.vosaye.bunkr.R.menu;
import com.vosaye.bunkr.R.string;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.externalpackages.master.ScrollingTabsView;
import com.vosaye.bunkr.fragments.Stats;
import com.vosaye.bunkr.fragments.Today;
import com.vosaye.bunkr.fragments.Stats.MyBroadcastReceiver;
import com.vosaye.bunkr.services.ValidatorService;

import android.app.AlertDialog;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
 
public class Here extends SherlockFragmentActivity implements TabListener {
	MenuAdapter adapter;
	DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBar action;
    ActionBarDrawerToggle mDrawerToggle;
    String selected = "";
    static float percentNow = 0.0f; 
    BunKar bunker;
    AuthDatabase auth;
    ScheduleDatabase sched;
    Today today;
	Tab tab1, tab2, tab3;
	
    MyBroadcastReceiver mBroadcast;
    MyBroadcastReceiverRefresh mBroadcastRefresh;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_here);
		bunker = (BunKar) this.getApplication();
		auth = bunker.settings;
		sched = bunker.getDatabase(bunker.name);
		BunKar.count++;
		action = this.getSupportActionBar();
		action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Stats" + "</font>"));
		action.setSubtitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+bunker.name + "</font>"));
		//action.setIcon(R.drawable.ic_drawer1);
        action.show();
        getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action));
        getSupportActionBar().setStackedBackgroundDrawable(this.getResources().getDrawable(R.drawable.actiontab));
        action.setDisplayHomeAsUpEnabled(true);
        action.setHomeButtonEnabled(true);
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        tab1 = getActionBar().newTab().setText("Today").setTabListener(this);

        
        getActionBar().addTab(tab1);
        tab2 = getActionBar().newTab().setText("Stats").setTabListener(this);
        
        getActionBar().addTab(tab2);
        tab3 = getActionBar().newTab().setText("Calendar").setTabListener(this);
        
        getActionBar().addTab(tab3);
        
        mDrawerLayout = (DrawerLayout) this.findViewById(R.id.here_drawer_layouts);
        mDrawerList = (ListView) this.findViewById(R.id.here_listview_drawers);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.here, R.string.drawer_open,
                R.string.drawer_close) { 
 
            public void onDrawerClosed(View view) {
                // TODO Auto-generated method stub
                //getSupportActionBar().setTitle(" "+selected+"    ");
                action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;"+ selected + "</font>"));

                super.onDrawerClosed(view);
            }
 
            public void onDrawerOpened(View drawerView) {
                // TODO Auto-generated method stub
                // Set the title on the action when drawer open
                //getSupportActionBar().setTitle(" Menu    ");
                action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;Menu" + "</font>"));
        		
                super.onDrawerOpened(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        String[] values = new String[] { "Header","Divider",
                "Today", "Stats", "Calendar", "Divider", "Subjects",
                "Types", "Terms", "Divider", "Edit Whole Schedule","Ranges","Settings", "Close Semester", "Log Out and Exit", "Divider" };

            final ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < values.length; ++i) {
              list.add(values[i]);
            }
            adapter = new MenuAdapter(this,
                android.R.layout.simple_list_item_1, list);
            mDrawerList.setAdapter(adapter);
            
            
            if (savedInstanceState == null) {
                selectItem(3);
            }
            
            mBroadcast = new MyBroadcastReceiver();
    		IntentFilter intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATE");
    		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
    		this.registerReceiver(mBroadcast, intentFilter);
    		
    		
    		mBroadcastRefresh = new MyBroadcastReceiverRefresh();
    		intentFilter = new IntentFilter("com.vosaye.bunkr.UPDATE");
    		intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
    		this.registerReceiver(mBroadcastRefresh, intentFilter);
    		
    		
	}
	
	public void onStart(){
		super.onStart();
		
	}
	
	public void onDestroy(){
		super.onDestroy();
		this.unregisterReceiver(mBroadcast);
		this.unregisterReceiver(mBroadcastRefresh);
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if(!adapter.getItem(position).equals("Divider")&&!adapter.getItem(position).equals("Header")&&!adapter.getItem(position).equals("Header"))
			selectItem(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.here, menu);
		return true;
	}

	
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
 
        if (item.getItemId() == android.R.id.home) {
 
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
            	mDrawerList.setSelectionAfterHeaderView();
                mDrawerLayout.openDrawer(mDrawerList);
            }
            return true;
        }
        int id = item.getItemId();
		if (id == R.id.action_settings) {
			ValidatorService.FREEFLOW = true;
        	Intent intent = new Intent(this,Settings.class);
        	intent.putExtra("focused", true);

            mDrawerLayout.closeDrawer(mDrawerList);
        	Here.this.startActivity(intent);
        	
        	//overridependingTransition(R.anim.fade_in, R.anim.fade_out);
			return true;
		} else if (id == R.id.action_help) {

			ValidatorService.FREEFLOW = true;
			Intent i = new Intent(this,HelpActivity.class);
			i.putExtra("explicit", "true");
			this.startActivity(i);
			
			return true;
		}
 
        return super.onOptionsItemSelected(item);
    }
	
	private void selectItem(int position) {
		 
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // Locate Position

		
        if(adapter.getItem(position).equals("Today")){
        	ValidatorService.FREEFLOW = true;
        	Bundle bundle = new Bundle();
    		Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.HOUR_OF_DAY, 0);
    		bundle.putString("date", BunKar.sdf.format(cal.getTime()));
    		today = new Today();
    		today.setArguments(bundle);
    		ft.replace(R.id.here_content_frame, today);
            
    		tab1.setText("Today");
    		tab2.setText("Stats");
    		tab3.setText("Calendar");
    		tab1.select();
        }
        else if(adapter.getItem(position).equals("Stats")){

        	ValidatorService.FREEFLOW = false;
        	
        	ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Stats.class.getName()));
        	tab1.setText("Today");
    		tab2.setText("Stats");
    		tab3.setText("Calendar");
    		tab2.select();
        }
        else if(adapter.getItem(position).equals("Calendar")){
        	ValidatorService.FREEFLOW = false;
        	ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Calendar.class.getName()));
        	tab1.setText("Today");
    		tab2.setText("Stats");
    		tab3.setText("Calendar");
    		tab3.select();
        }
        else if(adapter.getItem(position).equals("Subjects")){

        	ValidatorService.FREEFLOW = true;
        	ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Subjects.class.getName()));
        	tab1.setText("Subjects");
    		tab2.setText("Types");
    		tab3.setText("Terms");
    		tab1.select();
        
        }
        else if(adapter.getItem(position).equals("Types")){

        	ValidatorService.FREEFLOW = true;
        	ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Types.class.getName()));
        	tab1.setText("Subjects");
    		tab2.setText("Types");
    		tab3.setText("Terms");
    		tab2.select();
        }
        else if(adapter.getItem(position).equals("Terms")){

        	ValidatorService.FREEFLOW = true; 
        	ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Terms.class.getName()));
        	tab1.setText("Subjects");
    		tab2.setText("Types");
    		tab3.setText("Terms");
    		tab3.select();
        }
        else if(adapter.getItem(position).equals("Settings")){ 
        	ValidatorService.FREEFLOW = true;
        	Intent intent = new Intent(this,Settings.class);
        	intent.putExtra("focused", true);

            mDrawerLayout.closeDrawer(mDrawerList);
        	Here.this.startActivity(intent);
        	
        	//overridependingTransition(R.anim.fade_in, R.anim.fade_out);
            return;
        }
        else if(adapter.getItem(position).equals("Close Semester")){
        	this.finish();
        	ValidatorService.FOCUSED = false;
        	//delete all cache
        	Cursor c = auth.rawQuery("select filename from labsdecore"+bunker.name.replaceAll(" ", "_")+" where id > 0;");
        	if(c.moveToFirst())
        		do{
        			new File(c.getString(0)).delete();
        		}
        		while(c.moveToNext());
        	auth.deleteFromTable("labsdecore"+bunker.name.replaceAll(" ", "_"), "where id > 0");
            mDrawerLayout.closeDrawer(mDrawerList);
        	this.startActivity(new Intent(Here.this, ScheduleListActivity.class));
        	
        	//overridependingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        }
        else if(adapter.getItem(position).equals("Ranges")){
        	ValidatorService.FREEFLOW = true;
        	adapter.notifyDataSetChanged();
            // Close drawer
            mDrawerLayout.closeDrawer(mDrawerList);
            

        	this.startActivity(new Intent(Here.this, BlackHoleList.class));
        	
        	//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
            return;
        }
        else if(adapter.getItem(position).equals("Log Out and Exit")){
        	this.finish();
        	ValidatorService.FOCUSED = false;
        }
        else if(adapter.getItem(position).equals("Edit Whole Schedule")){
        	ValidatorService.FREEFLOW = false;
        	adapter.notifyDataSetChanged();
            // Close drawer
            mDrawerLayout.closeDrawer(mDrawerList);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Info");
            builder.setMessage("Editting entire structures will wipe out all existing schedules for the entire semester. Do you want to continue editting?");
            builder.setPositiveButton("That Be True!", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Here.this.startActivity(new Intent(Here.this, EditAllStructures.class));
					
					//overridependingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
		            dialog.dismiss();
					
				}});
            builder.setNegativeButton("Nay, Belay That!", new OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}});
        	
            builder.create().show();

            mDrawerLayout.closeDrawer(mDrawerList);
            return;
        }
        
        ft.commit();
        mDrawerList.setItemChecked(position, true);
 
        // Get the title followed by the position
        //action.setTitle(" "+adapter.getItem(position)+"    ");
        
        action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;" +adapter.getItem(position)+ "</font>"));
		
        selected = adapter.getItem(position);
        adapter.notifyDataSetChanged();
        // Close drawer
        mDrawerLayout.closeDrawer(mDrawerList);
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
	    	if(adapter.getItem(position).equals("Divider")||adapter.getItem(position).equals("Header"))
	    		return false;
	    	return true;
	    }
	    
	    public View getView(int position, View view, ViewGroup parent){
	    	
	    	
	    	if(this.getItem(position).equals("Header")){
	        	view = Here.this.getLayoutInflater().inflate(R.layout.menu_item_header, null);
	        	TextView schedNme = (TextView) view.findViewById(R.id.menu_item_header_textView1);
	        	schedNme.setText(bunker.name);
	        	TextView percent = (TextView) view.findViewById(R.id.menu_item_header_textView2);
	        	Cursor c = auth.rawQuery("select currentPerc from schedules where name = '"+bunker.name+"';");
	        	if(c.moveToFirst())
	        		Here.percentNow = c.getFloat(0);
	        	percent.setText(""+String.format("%.2f", Here.percentNow)+" %");
	        	c.close();
	        	return view;
	        	
	        }
	    	TextView sample = null;
	    	if(view!=null)
	    	sample = (TextView) view.findViewById(R.id.menu_item_header_textView1);
	    	
	    	if(view==null||sample!=null) view = Here.this.getLayoutInflater().inflate(R.layout.menu_item, null);
	    	TextView menuItem = (TextView) view.findViewById(R.id.menu_item_textView1s);
	    	menuItem.setText(this.getItem(position));
	    	ImageView image = (ImageView) view.findViewById(R.id.menu_item_imageView1s);
	    	TextView divider = (TextView) view.findViewById(R.id.menu_item_dividers);
	    	ViewGroup.LayoutParams params = divider.getLayoutParams();
	    	params.height = (int) (Here.this.getResources().getDisplayMetrics().density * 30);
	    	divider.setLayoutParams(params);
	    	if(this.getItem(position).equals("Divider")||this.getItem(position).equals("ADD")){
	    		image.setVisibility(View.GONE);
	        	menuItem.setVisibility(View.GONE);
	        	view.setEnabled(false);
	        	
	        	params = divider.getLayoutParams();
		    	params.height = (int) (Here.this.getResources().getDisplayMetrics().density * 10);
		    	divider.setLayoutParams(params);
	    	}
	    	
	        else if(this.getItem(position).equals(selected)){
	    	image.setVisibility(View.VISIBLE);
        	menuItem.setVisibility(View.VISIBLE);
        	view.setEnabled(true);
	    	menuItem.setTypeface(menuItem.getTypeface(), Typeface.BOLD);
	    	}
	        else{
	        	image.setVisibility(View.INVISIBLE);
	        	menuItem.setVisibility(View.VISIBLE);
	        	menuItem.setTypeface(Typeface.DEFAULT);
	        	view.setEnabled(true);
		    	
	        }
	    	
	    	
	    	
	    	
	    	return view;
	    }

	    @Override
	    public boolean hasStableIds() {
	      return true;
	    }

	  }
	
	public void onResume(){
		super.onResume();
		ValidatorService.FOCUSED = true;
		if(today!=null){
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0); 
			cal.set(Calendar.HOUR_OF_DAY, 0);
			
			today.setDate(cal.getTime());
		}
		if(selected.equals("Calendar")) 
			ValidatorService.FREEFLOW = false;
		else
			ValidatorService.FREEFLOW = true; 
	}
	public void onPause(){
    	super.onPause();
    	ValidatorService.FREEFLOW = true;
    	
    	//Toast.makeText(this, "paused base activity", Toast.LENGTH_LONG).show();
    }
	
	public class MyBroadcastReceiver extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			  int perc = intent.getIntExtra("perc", 0);
			  if(perc==-1){
				  	
				  	Here.this.adapter.notifyDataSetChanged();
			  
			  }
		 }
		  
	}

	@Override
	public void onTabSelected(Tab tab, android.app.FragmentTransaction ftt) {
		// TODO Auto-generated method stub
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		try{
		if(tab.getText().equals("Today")){
			ValidatorService.FREEFLOW = true;
			Bundle bundle = new Bundle();
    		Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MINUTE, 0);
    		cal.set(Calendar.HOUR_OF_DAY, 0);
    		bundle.putString("date", BunKar.sdf.format(cal.getTime()));
    		today = new Today();
    		today.setArguments(bundle);
    		ft.replace(R.id.here_content_frame, today);
			selected = "Today";
			adapter.notifyDataSetChanged();
		}else if(tab.getText().equals("Stats")){
			selected = "Stats";
			ValidatorService.FREEFLOW = false;
			ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Stats.class.getName()));
			adapter.notifyDataSetChanged();
			
		}else if(tab.getText().equals("Calendar")){
			selected = "Calendar";
			ValidatorService.FREEFLOW = false;
			ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Calendar.class.getName()));

			adapter.notifyDataSetChanged();
			
		}else if(tab.getText().equals("Subjects")){
			selected = "Subjects";
			ValidatorService.FREEFLOW = true;
			ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Subjects.class.getName()));
       

			adapter.notifyDataSetChanged();
		}else if(tab.getText().equals("Types")){
			selected = "Types";
			ValidatorService.FREEFLOW = true;
			ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Types.class.getName()));
       

			adapter.notifyDataSetChanged();
		}else if(tab.getText().equals("Terms")){
			selected = "Terms";
			ValidatorService.FREEFLOW = true;
			ft.replace(R.id.here_content_frame, Fragment.instantiate(this, com.vosaye.bunkr.fragments.Terms.class.getName()));
       

			adapter.notifyDataSetChanged();
		}
		if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        }
        catch(NullPointerException e){} 
        finally{action.setTitle(Html.fromHtml("<font color=\"#eeeeee\">" + "&nbsp;" + selected+ "</font>"));
		ft.commit();}
		
	}

	

	@Override
	public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}
	
	public class MyBroadcastReceiverRefresh extends BroadcastReceiver {

		  @Override
		  public void onReceive(Context context, Intent intent) {
			 adapter.notifyDataSetChanged();
			  
		  }
		 }
	
	boolean pressedOnce = false;
    public void onBackPressed(){
    	
    		
    			if(pressedOnce){
    				//close databases
    				//exit
    				Cursor c = auth.rawQuery("select filename from labsdecore"+bunker.name.replaceAll(" ", "_")+" where id > 0;");
    	        	if(c.moveToFirst())
    	        		do{
    	        			new File(c.getString(0)).delete();
    	        		}
    	        		while(c.moveToNext());
    	        	auth.deleteFromTable("labsdecore"+bunker.name.replaceAll(" ", "_"), "where id > 0");
    	        	setResult(BunKar.RESULT_CLOSE_ALL);
    	        	finish();
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
    
    @Override
    public boolean onKeyDown(int keycode, KeyEvent e) {
        switch(keycode) {
            case KeyEvent.KEYCODE_MENU:
            	if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                } else {
                	mDrawerList.setSelectionAfterHeaderView();
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
        }

        return super.onKeyDown(keycode, e);
    }
}
