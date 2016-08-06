package com.vosaye.bunkr.app;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.R.drawable;
import com.vosaye.bunkr.R.id;
import com.vosaye.bunkr.R.layout;
import com.vosaye.bunkr.R.menu;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CursorTreeAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class About extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		ActionBar action = this.getActionBar();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.action));
		action.setTitle(" Bunkr");
		BunKar.count++;

		listDataHeader.add("Sqlite Cipher Copyright (c) Zetetic LLC All rights reserved.");
		listDataHeader.add("Android, Copyright (c) Google and others. All rights reserved.");
		listDataHeader.add("International Components for Unicode (ICU), Copyright (c) 1995-2012 International Business Machines Corporation and others. All rights reserved.");
		
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.about, menu);
		return true;
	}

	ArrayList<String> listDataHeader = new ArrayList<String>();
	class ExpandableListAdapter extends BaseExpandableListAdapter {
		 
	    private Context _context;
	    private List<String> _listDataHeader; // header titles
	    // child data in format of header title, child title
	    private HashMap<String, List<String>> _listDataChild;
	 
	    public ExpandableListAdapter(Context context, List<String> listDataHeader,
	            HashMap<String, List<String>> listChildData) {
	        this._context = context;
	        this._listDataHeader = listDataHeader;
	        this._listDataChild = listChildData;
	    }
	 
	    @Override
	    public Object getChild(int groupPosition, int childPosititon) {
	        return "";
	    }
	 
	    @Override
	    public long getChildId(int groupPosition, int childPosition) {
	        return childPosition;
	    }
	 
	    @Override
	    public View getChildView(int groupPosition, final int childPosition,
	            boolean isLastChild, View convertView, ViewGroup parent) {
	 
	        
	        if (convertView == null) {
	            convertView = About.this.getLayoutInflater().inflate(R.layout.license_item, null);
	        }
	        TextView txt = (TextView) convertView.findViewById(R.id.license_item_textView1);
	        if(groupPosition==0){
	        	txt.setText(R.string.license_sqlitecipher);
	        }
	        else if(groupPosition==1){

	        	txt.setText(R.string.license_android);
	        }
	        else{

	        	txt.setText(R.string.license_icu);
	        }
	        
	 
	        return convertView;
	    }
	 
	    @Override
	    public int getChildrenCount(int groupPosition) {
	        return 1;
	    }
	 
	    @Override
	    public Object getGroup(int groupPosition) {
	        return this._listDataHeader.get(groupPosition);
	    }
	 
	    @Override
	    public int getGroupCount() {
	        return this._listDataHeader.size();
	    }
	 
	    @Override
	    public long getGroupId(int groupPosition) {
	        return groupPosition;
	    }
	 
	    @Override
	    public View getGroupView(int groupPosition, boolean isExpanded,  View convertView, ViewGroup parent) {
	        if (convertView == null) {
	            convertView = About.this.getLayoutInflater().inflate(R.layout.license_item, null);
	        }
	        TextView txt = (TextView) convertView.findViewById(R.id.license_item_textView1);
	        txt.setText(_listDataHeader.get(groupPosition));
	        
	 
	        return convertView;
	    }
	 
	    @Override
	    public boolean hasStableIds() {
	        return false;
	    }
	 
	    @Override
	    public boolean isChildSelectable(int groupPosition, int childPosition) {
	        return false;
	    }
	}
	
	
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_License) {
			//alert dialog//
			
			
			View view = this.getLayoutInflater().inflate(R.layout.license, null);
			ExpandableListView list = (ExpandableListView) view.findViewById(R.id.license_expandableListView1);
			ExpandableListAdapter adapter = new ExpandableListAdapter(this,this.listDataHeader,null);
			list.setAdapter(adapter);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(true);
			builder.setTitle("License");
			builder.setView(view);
			builder.show();
			
			return true;
		}
		else if(id == R.id.action_fb){
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.facebook.com/vosbunkr"));
			startActivity(browserIntent);
			return true;
		}
		else if(id == R.id.action_in){
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://in.linkedin.com/pub/roger-cores/70/504/73"));
			startActivity(browserIntent);
			return true;
		}
		else if(id == android.R.id.home){
			this.setResult(1);
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onDestroy(){
		super.onDestroy();
		BunKar.count--;
		if(BunKar.count==0){
    		((BunKar) this.getApplication()).deleteAllCache();
    	}
	}
}
