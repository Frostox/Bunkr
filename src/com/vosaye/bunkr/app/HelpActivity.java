package com.vosaye.bunkr.app;

import java.util.List;
import java.util.Vector;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.R.id;
import com.vosaye.bunkr.R.layout;
import com.vosaye.bunkr.R.menu;
import com.vosaye.bunkr.externalpackages.Pager_Adapter;
import com.vosaye.bunkr.fragments.StatsDay;
import com.vosaye.bunkr.fragments.Terms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class HelpActivity extends SherlockFragmentActivity implements OnPageChangeListener {
	private ViewPager mPager;
	//private Pager_Adapter mPagerAdapter;
	private MyPagerAdapter adapter;
	private Button prev, next;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		prev = (Button) this.findViewById(R.id.button1);
		next = (Button) this.findViewById(R.id.button2);
		prev.setEnabled(false);
		ActionBar action = this.getSupportActionBar();
        action.show();
        action.setTitle(" How to Bunkr?");
        if((this.getIntent().getStringExtra("explicit").equals("true")))
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action));
		
		initializePaging();
		BunKar.count++;
        
	}
	
	int ids[] = {R.drawable.s1,R.drawable.s2,R.drawable.s3,R.drawable.s4,R.drawable.s5,R.drawable.s10,R.drawable.s11,R.drawable.s14,R.drawable.s15,R.drawable.s16,R.drawable.s17,R.drawable.s19,R.drawable.s20,R.drawable.s18,R.drawable.s21,R.drawable.s22,R.drawable.rangescreen,R.drawable.s25,R.drawable.iconmain_light};
	String texts[][] = {
			{"Semesters","Create and delete semesters","Once created, click to set it up!"},
			{"Fill up Basic Details", "Once semester is created, fill up details such as subjects, terms etc.", ""},
			{"Types", "And yes, types of subjects imply lectures, practicals etc.", ""},
			{"Terms", "You can set your term dates.", "Dont worry if you dont know them for sure, just set approximate and you can change them later."},
			{"Time Table", "Create your Time table here.","Slide left and right to select day."},
			{"Stats", "After creating your Time table, you'll be taken to the stats","Here you'll manage your bunks"},
			{"Stats", "Once refreshing completes, you'll have your stats till today!","You can group'em by Subject, type or none."},
			{"Stats", "Click on subjects to view additional stats",""},
			{"Today", "Click on the 'Today' tab to see today's time table","And remember, checked button means attended, cross means bunked and circle means cancled."},
			{"Calendar", "Click on the 'Calendar' tab to see calendar","Days with time table will appear in light color. Days which do not have schedules and holidays will appear in dark color."},
			{"Calendar", "Long press on a date to get options menu","Click insert blank to insert a holiday! You can also copy and paste schedules from one date to another!"},
			{"Open a date", "In calendar, click on a date to open it",""},
			{"Open a date", "Click on the 'Edit TimeTable' tab to change the time table for this date.","Also set the amount of weeks this Time table should be applied to."},
			{"Edit All Mondays, Tuesdays etc.", "In calendar, click on day name. For eg. sun, mon etc.","When you are done creating time table, click on the 'save' button."},
			{"Menu", "Use the menu to navigate.",""},
			{"Ranges", "What if you didn't put in your attendance in Bunkr for about a month? You don't remember when you attended, when you bunked!","Well, you're in luck if you can access your college defaulters. Just create a range and fill in your percentage for the month!"},
			{"Ranges", "Open the range to add your defaulter information to it.","Always create rages on past dates."},
			{"Backups", "Bunkr keeps daily and weekly backups.","It also keeps session backups which are cleared when you exit the app!"},
			{"Happy Bunking!", "",""}
			};
	public void initializePaging(){
		mPager = (ViewPager) findViewById(R.id.viewpager_scrolling_pager);

		mPager.setOffscreenPageLimit(3);
		/*
 List<Fragment> fragments = new Vector<Fragment>();
		StatsDay temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s1, "Semesters", "Create and delete semesters", "Once created, click to set it up!");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s2, "Fill up Basic Details", "Once semester is created, fill up details such as subjects, terms etc.", "");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s3, "Types", "And yes, types of subjects imply lectures, practicals etc.", "");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s4, "Terms", "You can set your term dates.", "Dont worry if you dont know them for sure, just set approximate and you can change them later.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s5, "Time Table", "Create your Time table here.","Slide left and right to select day.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s10, "Stats", "After creating your Time table, you'll be taken to the stats","Here you'll manage your bunks");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s11, "Stats", "Once refreshing completes, you'll have your stats till today!","You can group'em by Subject, type or none.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s14, "Stats", "Click on subjects to view additional stats","");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s15, "Today", "Click on the 'Today' tab to see today's time table","And remember, checked button means attended, cross means bunked and circle means cancled.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s16, "Calendar", "Click on the 'Calendar' tab to see calendar","From here you can manage attendance, time table changes etc.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s17, "Calendar", "Long press on a date to get options menu","");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s19, "Open a date", "In calendar, click on a date to open it","");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s20, "Open a date", "Click on the 'Edit TimeTable' tab to change the time table for this date.","Also set the amount of weeks this Time table should be applied to.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s18, "Edit All Mondays, Tuesdays etc.", "In calendar, click on day name. For eg. sun, mon etc.","When you are done creating time table, click on the 'save' button.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s21, "Menu", "Use the menu to navigate.","");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s22, "Ranges", "What if you didn't put in your attendance in Bunkr for about a month? You don't remember when you attended, when you bunked!","Well, you're in luck if you can access your college defaulters. Just create a range and fill in your percentage for the month!");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.rangescreen, "Ranges", "Open the range to add your defaulter information to it.","Always create rages on past dates.");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.s25, "Backups", "Bunkr keeps daily and weekly backups.","It also keeps session backups which are cleared when you exit the app!");
		fragments.add(temp);
		temp = (StatsDay) Fragment.instantiate(this, com.vosaye.bunkr.fragments.StatsDay.class.getName());
		temp.setContent(R.drawable.iconmain_light, "Happy Bunking!", "","");
		fragments.add(temp);
		this.mPagerAdapter = new Pager_Adapter(super.getSupportFragmentManager(), fragments);
*/
		adapter = new MyPagerAdapter();
		mPager.setAdapter(adapter);
		mPager.setCurrentItem(0);
		mPager.setOffscreenPageLimit(3);
		mPager.setPageMargin(1);
		
		mPager.setOnPageChangeListener(this);
	}

	
	public void onNext(View v){
		if(!(mPager.getCurrentItem()>=adapter.getCount()-1)){
			mPager.setCurrentItem(mPager.getCurrentItem()+1);
			
			
		}else{
			
			if(this.getIntent().getStringExtra("explicit").equals("true")){
				setResult(1);
				finish();
			}
			else{
				this.finish();
				this.startActivity(new Intent(this, ScheduleListActivity.class));
				
			}
		}
	}
	
	public void onBack(View v){
		if(mPager.getCurrentItem()!=0){
			mPager.setCurrentItem(mPager.getCurrentItem()-1);
			
			
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(!this.getIntent().getStringExtra("explicit").equals("true"))
		getSupportMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.next_button) {
			this.finish();
			this.startActivity(new Intent(this, ScheduleListActivity.class));
			
			return true;
		}else if(id == android.R.id.home){
			this.setResult(1);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onBackPressed(){
		this.setResult(1);
		finish();
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		if(position==0){
			next.setEnabled(true);
			prev.setEnabled(false);
		}
		else if(position==adapter.getCount()-1){
			next.setEnabled(true);
			next.setText("Finish");
			prev.setEnabled(true);
		}
		else{
			next.setText("Next");
			next.setEnabled(true);
			prev.setEnabled(true);
		}
		
	}
	public void onDestroy(){
		super.onDestroy();
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
	}
	
	
	
	public class MyPagerAdapter extends PagerAdapter {
	    public int getCount() {
	        return 19;
	    }
	    public Object instantiateItem(View collection, int position) {
	        LayoutInflater inflater = (LayoutInflater) collection.getContext()
	                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        
	        View view = inflater.inflate(R.layout.help_image, null);
	        TextView title, sub, info;
	    	ImageView image;
	    	
	    	info = (TextView) view.findViewById(R.id.help_image_textView1);
			info.setText(texts[position][2]);
			sub = (TextView) view.findViewById(R.id.help_image_textView2);
			sub.setText(texts[position][1]);
			title = (TextView) view.findViewById(R.id.help_image_textView3);
			title.setText(texts[position][0]);
			
			image = (ImageView) view.findViewById(R.id.help_image_imageView1);
			
			image.setImageBitmap(BitmapFactory.decodeResource(getResources(),
		            ids[position]));
	        
	        
	        ((ViewPager) collection).addView(view, 0);
	        return view;
	    }
	    @Override
	    public void destroyItem(View collection, int position, Object o) {
	        View view = (View)o;
	        ImageView imgView = (ImageView) view.findViewById(R.id.help_image_imageView1);
	        BitmapDrawable bmpDrawable = (BitmapDrawable) imgView.getDrawable();
	        if (bmpDrawable != null && bmpDrawable.getBitmap() != null) {
	                // This is the important part
	                bmpDrawable.getBitmap().recycle();
	        }
	        ((ViewPager) collection).removeView(view);
	        view = null;
	    }

	    @Override
	    public boolean isViewFromObject(View arg0, Object arg1) {
	        return arg0 == ((View) arg1);
	    }
	    @Override
	    public Parcelable saveState() {
	        return null;
	    }
	}
	
}
