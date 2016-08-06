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
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.TextView.OnEditorActionListener;

public class TypeScrollList extends ScrollView  implements OnClickListener, OnEditorActionListener{
	LayoutInflater inflater;
	ScrollListListener typeList;
	Activity context;
	LinearLayout  header, listContainer, typeListUI;
	ScrollView mainContainer;
	public CustomListView listView;
	AuthDatabase auth;
	ScheduleDatabase scheduleDB;
	BunKar bunker;
	AlphaAnimation fadeIn;

	java.text.DecimalFormat nft = new java.text.DecimalFormat("#00.###");
	EditText typName;
	//EditText mins, hrs;
	Button submit;
	//Button minsMinus, minsPlus, hrsMinus, hrsPlus;
	Button duration;
	int durtime = 60;
	Toast toast;
	TypeScrollListAdapter adapter;
	Cursor c;
	
	InputMethodManager mngr;
	
	public void closeList(){
		c.close();
	}
	
	public void updateList(){
		c = scheduleDB.rawQuery("select 1 as _id, name from type");
		adapter.changeCursor(c);;
		listView.setAdapter(adapter);
		
	}
	
	
	
	public TypeScrollList(Activity context, LayoutInflater inflater) {
		super(context);
		this.inflater = inflater;
		this.context = context;
		onCreate();
	}
	public void onCreate(){
		mainContainer = (ScrollView) inflater.inflate(R.layout.tos_list, null);
		header = (LinearLayout) mainContainer.findViewById(R.id.toslist_header);
		listContainer = (LinearLayout) mainContainer.findViewById(R.id.toslist_listContainer);
		bunker = ((BunKar) context.getApplication());
		auth = bunker.settings;
		scheduleDB = bunker.getDatabase(bunker.name);
		listView = new CustomListView(context);
		typeListUI = (LinearLayout) inflater.inflate(R.layout.type_ui, null);
		header.addView(typeListUI);
		listContainer.addView(listView);
		fadeIn = new AlphaAnimation(0.0f , 1.0f );
		mngr = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		toast = Toast.makeText(this.context, "", Toast.LENGTH_SHORT);
		submit = (Button) typeListUI.findViewById(R.id.typ_button1);
		typName = (EditText) typeListUI.findViewById(R.id.typ_editText2);
		duration = (Button) typeListUI.findViewById(R.id.typ_duration);
		
		duration.setOnClickListener(new OnClickListener(){

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
						duration.setText(""+nft.format(hrs)+":"+nft.format(mins)+" hrs");
						}
						
						
						
					}};
					
					
					CustomTimePickerDialog tp = new CustomTimePickerDialog(context, ol, durtime/60, durtime%60, true, true, 0, 0, 60, 4);
					tp.setTitle("Set Duration");
					tp.show();
			}});
		/*
 TextWatcher mTextWatcherx = new TextWatcher() {
			String string;
	        @Override
	        public void beforeTextChanged(CharSequence s, int arg1, int arg2,int arg3) {
	            // YOU STRING BEFORE CHANGE
	        	string = typName.getText().toString();
	        	
	        }
	        @Override
	        public void onTextChanged(CharSequence s, int start, int before,int count) {
	            if(typName.getText().toString().length()>=6){
	            	typName.setText(string);
	            	typName.setSelection(5);
	            }
	        }
	        @Override
	        public void afterTextChanged(Editable s) {
	        	//AFTER CHANGED
	        }
			
			
	    };
	    //typName.addTextChangedListener(mTextWatcherx);
*/
		InputFilter[] FilterArray = new InputFilter[1];
		FilterArray[0] = new InputFilter.LengthFilter(5);
		typName.setFilters(FilterArray);
		//hrs = (EditText) typeListUI.findViewById(R.id.typ_EditText01);
		//mins = (EditText) typeListUI.findViewById(R.id.typ_editText3);
		//hrsMinus = (Button) typeListUI.findViewById(R.id.typ_minsMinus);
		//hrsPlus = (Button) typeListUI.findViewById(R.id.typ_minsPlus);
		//minsMinus = (Button) typeListUI.findViewById(R.id.typ_hrsMinus);
		//minsPlus = (Button) typeListUI.findViewById(R.id.typ_hrsPlus);
		
		/*
 TextWatcher mTextWatcher = new TextWatcher() {
			String string;
	        @Override
	        public void beforeTextChanged(CharSequence s, int arg1, int arg2,int arg3) {
	            // YOU STRING BEFORE CHANGE
	        	string = mins.getText().toString();
	        }
	        @Override
	        public void onTextChanged(CharSequence s, int start, int before,int count) {
	              // CHARS INPUT BY USER
	        	  try{
	        		  if(Integer.parseInt(mins.getText().toString())>59||Integer.parseInt(mins.getText().toString())<0){
	        			  mins.setText(string);
	        			  mins.setSelection(mins.getText().toString().length());
	        		  }
	        	  }
	        	  catch(NumberFormatException e){}
	        }
	        @Override
	        public void afterTextChanged(Editable s) {
	        	//AFTER CHANGED
	        }
			
			
	    };
		mins.addTextChangedListener(mTextWatcher);
		TextWatcher mTextWatcherHrs = new TextWatcher() {
			String string;
	        @Override
	        public void beforeTextChanged(CharSequence s, int arg1, int arg2,int arg3) {
	            // YOU STRING BEFORE CHANGE
	        	string = hrs.getText().toString();
	        	
	        }
	        @Override
	        public void onTextChanged(CharSequence s, int start, int before,int count) {
	              // CHARS INPUT BY USER
	        	try{
	        		  if(Integer.parseInt(hrs.getText().toString())>10||Integer.parseInt(hrs.getText().toString())<0){
	        			  hrs.setText(string);
	        			  hrs.setSelection(hrs.getText().toString().length());
	        		  }
	        	  }
	        	  catch(NumberFormatException e){}
	        }
	        @Override
	        public void afterTextChanged(Editable s) {
	        	//AFTER CHANGED
	        }
			
			
	    };
		hrs.addTextChangedListener(mTextWatcherHrs);
		minsMinus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				try{
				if(StringUtils.isNumeric(mins.getText().toString())){
					if(Integer.parseInt(mins.getText().toString())==0) mins.setText("59");
					else
					mins.setText(""+(Integer.parseInt(mins.getText().toString())-1));
				}
				else{
					mins.setText("0");
				}
				}
				catch(NumberFormatException e){
					
				}
			}});
		minsPlus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				try{
				if(StringUtils.isNumeric(mins.getText().toString())){
					if(Integer.parseInt(mins.getText().toString())==59) mins.setText("0");
					else
					mins.setText(""+(Integer.parseInt(mins.getText().toString())+1));
				}
				else{
					mins.setText("0");
				}
				}
				catch(NumberFormatException e){}
			}});
		hrsMinus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				try{
				if(StringUtils.isNumeric(hrs.getText().toString())){
					if(Integer.parseInt(hrs.getText().toString())==0) hrs.setText("10");
					else
					hrs.setText(""+(Integer.parseInt(hrs.getText().toString())-1));
				}
				else{
					hrs.setText("0");
				}
				}
				catch(NumberFormatException e){}
			}});
		hrsPlus.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				try{
				if(StringUtils.isNumeric(hrs.getText().toString())){
					if(Integer.parseInt(hrs.getText().toString())==10) hrs.setText("0");
					else
					hrs.setText(""+(Integer.parseInt(hrs.getText().toString())+1));
				}
				else{
					hrs.setText("0");
				}
				}
				catch(NumberFormatException e){}
			}});
*/
		c = scheduleDB.rawQuery("select 1 as _id, name from type");
		adapter = new TypeScrollListAdapter(context,R.layout.schedule_list,c,new String[] {"name"},new int[]{R.id.sch_list_textView1});
		listView.setAdapter(adapter);
		CustomListView.setListViewHeightBasedOnChildren(listView);
		
		submit.setOnClickListener(this);
		//mins.setOnEditorActionListener(this);
		
	}
	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		onClick(arg0);
		return false;
	}

	@Override
	public void onClick(View arg0) {
		String type;
		type = typName.getText().toString().trim();
		//typemins = mins.getText().toString();
		//typehrs = hrs.getText().toString();
		if(!StringUtils.isAlphanumericSpace(type)){
			//
			//notify.setText("type name should be alphanumeric");
			//
			toast.setText("type name should be alphanumeric");
			toast.show();
		}
		else if(type.length()<2){
			//
			//notify.setText("Atleast 2 chars needed");
			//
			toast.setText("Atleast 2 chars needed");
			toast.show();
		}
		else{
			try {
				
				if(scheduleDB.valueExists("name","'"+type+"'", "subject")){
					toast.setText("A subject with name already exists");
					toast.show();
					//
					//
					//notify.setText("Type Already exists");
					//
				}else if(scheduleDB.valueExists("name","'"+type+"'", "type")){
					toast.setText("Type already exists");
					toast.show();
					//
					//
					//notify.setText("Type Already exists");
					//
				}
				else{
					if(durtime<5){
						toast.setText("Atleast 5 mins needed");
						toast.show();
						//notify.setText("Atleast 5 mins needed");
						
						
					}else{
				scheduleDB.standards.addTyp(type,durtime);
				typName.setText("");
				//mins.setText("0");
				//hrs.setText("0");
				mngr.hideSoftInputFromWindow(typName.getWindowToken(), 0);
				c.requery();
				adapter.notifyDataSetChanged();
				CustomListView.setListViewHeightBasedOnChildren(listView);
				typeList.onDone(TypeScrollList.this.getView());}
				}
			} catch (BunkerException e) {
				//toast.setText("Unexpected Error :"+e.getMessage().substring(0,e.getMessage().indexOf(":")));
				//toast.show();
				e.printStackTrace();
			}
		}
	}
	public void setOnDoneListener(ScrollListListener typeList) {
		this.typeList = typeList;
		
	}
	public View getView() {
		return mainContainer;
	}
	
	class TypeScrollListAdapter extends SimpleCursorAdapter{

		@SuppressWarnings("deprecation")
		public TypeScrollListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
			super(context, layout, c, from, to);
			
		}
		public View getView(int position, View convertView, ViewGroup parent){
			c.moveToPosition(position);
			if(convertView==null)convertView = LayoutInflater.from(context).inflate(R.layout.schedule_list, null);
			final ToggleButton notification = (ToggleButton) convertView.findViewById(R.id.sch_list_notificationActive);
			//((LinearLayout) notification.getParent()).removeView(notification);
			((LinearLayout)convertView).removeView(notification);
			Button delete = (Button) convertView.findViewById(R.id.sch_list_button1);
			final TextView schName = (TextView) convertView.findViewById(R.id.sch_list_textView1),schPer = (TextView) convertView.findViewById(R.id.sch_list_textView2);
			schName.setText(c.getString(1));
			Cursor cx = scheduleDB.rawQuery("select mins from type where name = '"+schName.getText().toString()+"'");
			if(cx.moveToFirst())
				//schPer.setText(Integer.parseInt(cx.getString(0))+"");
				schPer.setText(""+(Integer.parseInt(cx.getString(0))/60)+" hrs "+(Integer.parseInt(cx.getString(0))%60)+" mins");
			cx.close();
			LinearLayout layout= (LinearLayout) convertView.findViewById(R.id.sch_list_layout);
			final CheckBox defaulter = (CheckBox) convertView.findViewById(R.id.sch_list_defaulter);
			((LinearLayout) convertView).removeView(defaulter);
			delete.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Do you really wanna delete the Type '"+schName.getText().toString()+"'\nOnce deleted, it cannot be brought back !");
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
								scheduleDB.standards.deleteTyp(schName.getText().toString());
							} catch (SQLiteException e) {
								toast.setText(e.getMessage().substring(0,e.getMessage().indexOf(":")));
								toast.show();
								e.printStackTrace();
							} catch (BunkerException e) {

								toast.setText(e.getMessage().substring(0,e.getMessage().indexOf(":")));
								toast.show();
								e.printStackTrace();
							}
							c.requery();
							adapter.notifyDataSetChanged();
							arg0.dismiss();
							CustomListView.setListViewHeightBasedOnChildren(listView);
							typeList.onDone(mainContainer);
						
						}});
					//authenticator.schedules.deleteSchedule(textView.getText().toString());
					//c.requery();
					//CAdapter.notifyDataSetChanged();
					builder.create();
					builder.show();
				}});
			
			layout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View vx) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setMessage("Edit Type");
					final View v = LayoutInflater.from(context).inflate(R.layout.edit_prompt_type, null);
					builder.setView(v);
					final EditText subname = (EditText) v.findViewById(R.id.editprompttype_editText1);
					final EditText mins = (EditText) v.findViewById(R.id.editprompttype_mins);
					final EditText hrs = (EditText) v.findViewById(R.id.editprompttype_hrs);
					subname.setText(schName.getText());
					subname.setSelection(subname.getText().toString().length());
					Cursor cx = scheduleDB.rawQuery("select mins from type where name = '"+schName.getText().toString()+"'");
					if(cx.moveToFirst()){
						hrs.setText((Integer.parseInt(cx.getString(0))/60)+"");
						mins.setText((Integer.parseInt(cx.getString(0))%60)+"");
					}
					cx.close();
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							String name = schName.getText().toString();
							String nname = subname.getText().toString();
							if(nname.equals("")) Toast.makeText(context, "No Value", Toast.LENGTH_SHORT).show();
							else if(nname.length()<2) Toast.makeText(context, "atleast 2 characters needed", Toast.LENGTH_SHORT).show();
							else if(mins.getText().toString().equals("")||hrs.getText().toString().equals("")){Toast.makeText(context, "No Values", Toast.LENGTH_LONG).show();}
							else
								
							try {
								
								scheduleDB.execQuery("update type set mins = "+((Integer.parseInt(hrs.getText().toString())*60)+Integer.parseInt(mins.getText().toString()))+" where name = '"+name+"'");
								if(!nname.equals(schName.getText().toString()))
								scheduleDB.standards.renameTyp(name, nname);
								
								c.requery();
								adapter.notifyDataSetChanged();
								CustomListView.setListViewHeightBasedOnChildren(listView);
								typeList.onDone(TypeScrollList.this.getView());
								
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
			
			return convertView;
		}


	}

}
