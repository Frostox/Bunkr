package com.vosaye.bunkr.customviews;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import org.apache.commons.lang3.StringUtils;












import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.base.AuthDatabase;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.events.ScrollListListener;
import com.vosaye.bunkr.exception.BunkerException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.TextView.OnEditorActionListener;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;

public class SubjectScrollList extends ScrollView implements OnClickListener, OnEditorActionListener{
	LayoutInflater inflater;
	BunKar bunker;
	Activity context;
	public ScheduleDatabase scheduleDB;
	ScrollView mainContainer;
	AuthDatabase auth;
	public CustomListView listView;
	LinearLayout header, listContainer, subjectListUi;
	public AlphaAnimation fadeIn;
	public EditText scheduleName;
	Button submit;
	Toast toast;
	public InputMethodManager mngr;
	SubjectScrollListAdapter adapter;
	ScrollListListener scroll;
	public Cursor c;
	public SubjectScrollList(Activity context, LayoutInflater inflater) {
		super(context);
		this.inflater = inflater;
		this.context = context;
		onCreate();
	}
	
	public void closeList(){
		c.close();
	}
	
	public void updateList(){
		c = scheduleDB.rawQuery("select 1 as _id, name from subject");
		adapter.changeCursor(c);
		listView.setAdapter(adapter);
		
	}
	
	public void setOnDoneListener(ScrollListListener object){
		this.scroll = object;
	}
	public void onCreate(){
		mainContainer = (ScrollView) inflater.inflate(R.layout.tos_list, null);
		bunker = ((BunKar) context.getApplication());
		auth = bunker.settings;
		scheduleDB = bunker.getDatabase(bunker.name);
		listView = new CustomListView(context);
		header = (LinearLayout) mainContainer.findViewById(R.id.toslist_header);
		listContainer = (LinearLayout) mainContainer.findViewById(R.id.toslist_listContainer);
		listContainer.removeAllViews();
		listContainer.addView(listView);
		subjectListUi = (LinearLayout) inflater.inflate(R.layout.schedule_list_ui, null);
		header.removeAllViews();
		header.addView(subjectListUi);
		fadeIn = new AlphaAnimation(0.0f , 1.0f );
		scheduleName = (EditText) subjectListUi.findViewById(R.id.asch_editText1);
		
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(5);
		scheduleName.setFilters(FilterArray);
		
		/*
 TextWatcher mTextWatcher = new TextWatcher() {
			String string;
	        @Override
	        public void beforeTextChanged(CharSequence s, int arg1, int arg2,int arg3) {
	            // YOU STRING BEFORE CHANGE
	        	string = scheduleName.getText().toString();
	        	
	        }
	        @Override
	        public void onTextChanged(CharSequence s, int start, int before,int count) {
	            if(scheduleName.getText().toString().length()>=6){
	            	scheduleName.setText(string);
	            	scheduleName.setSelection(5);
	            }
	        }
	        @Override
	        public void afterTextChanged(Editable s) {
	        	//AFTER CHANGED
	        }
			
			
	    };
	    //scheduleName.addTextChangedListener(mTextWatcher); 
*/ 
		((TextView) subjectListUi.findViewById(R.id.asch_textView1)).setText("Create a Subject :");
		submit = (Button) subjectListUi.findViewById(R.id.asch_button1);
		toast = Toast.makeText(this.getContext(), "", Toast.LENGTH_SHORT);
		mngr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		submit.setOnClickListener(this);
		scheduleName.setOnEditorActionListener(this);
		listView.setDividerHeight(2);
		c = scheduleDB.rawQuery("select 1 as _id, name from subject");
		adapter = new SubjectScrollListAdapter(context,R.layout.schedule_list,c,new String[] {"name"},new int[]{R.id.sch_list_textView1});
		listView.setAdapter(adapter);
		CustomListView.setListViewHeightBasedOnChildren(listView);
	}

	public ScrollView getView(){
		return mainContainer;
	}
	
	@Override
	public void onClick(View arg0) {
		String schedulenm;
		schedulenm = scheduleName.getText().toString().trim();
		if(schedulenm.equals("")) {
			toast.setText("please enter a value");
			toast.show();
			
		}
		else if(schedulenm.startsWith(" ")){
			toast.setText("can't begin with a space");
			toast.show();
			
			
		}
		else if(!StringUtils.isAlphanumericSpace(schedulenm)){
			toast.setText("name should be alpha numeric");
			toast.show();
			
			
		}
		else if(schedulenm.length()<2){
			toast.setText("atleast 2 characters needed");
			toast.show();
			
			
		}
		else{
			
				try {
					
					if(scheduleDB.valueExists("name","'"+schedulenm+"'", "subject")){
						toast.setText("Subject already exists");
						toast.show();
						
						
						
					}
					else{
					scheduleDB.standards.addSub(schedulenm);
					scheduleName.setText("");
					mngr.hideSoftInputFromWindow(scheduleName.getWindowToken(), 0);
					c.requery();
					adapter.notifyDataSetChanged();
					listView.setListViewHeightBasedOnChildren(listView);
					scroll.onDone(this.getView());
					}
				} catch (BunkerException e) {
					toast.setText("Unexpected Error :"+e.getMessage());
					toast.show();
					e.printStackTrace();
				}
			
		}
		
	}

	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		onClick(arg0);
		return false;
	}

	
	
	class SubjectScrollListAdapter extends SimpleCursorAdapter{

		public SubjectScrollListAdapter(Context context, int layout, Cursor c,String[] from, int[] to) {
			super(context, layout, c, from, to);
			
		}
		public View getView(final int position, View convertView, ViewGroup parent){
			c.moveToPosition(position);
			if(convertView==null)convertView = LayoutInflater.from(context).inflate(R.layout.schedule_list, null);
			final ToggleButton notification = (ToggleButton) convertView.findViewById(R.id.sch_list_notificationActive);
			//((LinearLayout) notification.getParent()).removeView(notification);
			((LinearLayout)convertView).removeView(notification);
			Button delete = (Button) convertView.findViewById(R.id.sch_list_button1);
			final TextView schName = (TextView) convertView.findViewById(R.id.sch_list_textView1),schPer = (TextView) convertView.findViewById(R.id.sch_list_textView2);
			schName.setText(c.getString(1));
			schPer.setText("");
			LinearLayout layout= (LinearLayout) convertView.findViewById(R.id.sch_list_layout);
			final CheckBox defaulter = (CheckBox) convertView.findViewById(R.id.sch_list_defaulter);
			((LinearLayout) convertView).removeView(defaulter);
			
			layout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Edit Subject");
					final View v = LayoutInflater.from(context).inflate(R.layout.edit_prompt_subject, null);
					builder.setView(v);
					final EditText subname = (EditText) v.findViewById(R.id.editpromptsubject_editText1);
					subname.setText(schName.getText().toString());
					subname.setSelection(subname.getText().toString().length());
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String name = schName.getText().toString();
							String nname = subname.getText().toString();
							if(nname.equals("")) Toast.makeText(context, "No Value", Toast.LENGTH_SHORT).show();
							else if(nname.length()<2) Toast.makeText(context, "atleast 2 characters needed", Toast.LENGTH_SHORT).show();
							else
							try {
								scheduleDB.standards.renameSub(name, nname);
								c.requery();
								adapter.notifyDataSetChanged();
								listView.setListViewHeightBasedOnChildren(listView);
								scroll.onDone(mainContainer);
							} catch (SQLiteException e) {
								// TODO Auto-generated catch block
								//Toast.makeText(sch, e.getMessage().substring(0,e.getMessage().indexOf(":")), Toast.LENGTH_SHORT);
								e.printStackTrace();
							} catch (BunkerException e) {
								// TODO Auto-generated catch block
								//Toast.makeText(sch, e.getMessage().substring(0,e.getMessage().indexOf(":")), Toast.LENGTH_SHORT);
								e.printStackTrace();
							}
							mngr.hideSoftInputFromWindow(subname.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
							
							dialog.dismiss();
						}});
					
					builder.setNegativeButton("Nay", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							mngr.hideSoftInputFromWindow(subname.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
							dialog.dismiss();
						}});
					
					builder.show();
					
					
					
				}});
			
			
			delete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Do you really wanna delete the Subject '"+schName.getText().toString()+"'\nOnce deleted, it cannot be brought back !");
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
								scheduleDB.standards.deleteSub(schName.getText().toString());
							} catch (SQLiteException e) {
								toast.setText(""+e.getMessage().substring(0,e.getMessage().indexOf(":")));
								toast.show();
								
								e.printStackTrace();
							} catch (BunkerException e) {
								toast.setText(""+e.getMessage().substring(0,e.getMessage().indexOf(":")));
								toast.show();
								
								e.printStackTrace();
							}
							c.requery();
							adapter.notifyDataSetChanged();
							
							arg0.dismiss();
						}});
					//authenticator.schedules.deleteSchedule(textView.getText().toString());
					//c.requery();
					//CAdapter.notifyDataSetChanged();
					builder.create();
					builder.show();
					CustomListView.setListViewHeightBasedOnChildren(listView);
				}});
			
			
			return convertView;
		}
		
	}
}
