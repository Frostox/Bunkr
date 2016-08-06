package com.vosaye.bunkr.customviews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import com.vosaye.bunkr.app.BlackHoleActivity;
import com.vosaye.bunkr.app.BlackHoleList;
import com.vosaye.bunkr.app.StructureActivity;
import com.vosaye.bunkr.app.WeekDayStructure;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.events.ScrollListListener;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.ValidatorService;
import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;



public class CalendarView implements OnClickListener{
	public Date selected;
	SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy",Locale.ENGLISH);
	SimpleDateFormat sdf3 = new SimpleDateFormat("dd MMMM yy",Locale.ENGLISH);
	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
	private TextView currentMonth;
	private ImageView prevMonth;
	private ImageView nextMonth;
	private GridView calendarView;
	public GridCellAdapter adapter;
	private Calendar _calendar;
	private int month, year;
	private static final String dateTemplate = "MMMM yyyy";
	private String[] weekDays = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	private WeekDaysAdapter weekDaysAdapter;
	private GridView weekDaysGridView;
	private Activity context;
	private LayoutInflater inflater;
	private LinearLayout mainContainer;
	BunKar BunKar;
	public ScheduleDatabase sched;
	String months[] = {"January","February","March","April","May","June","July","August","September","October","November","December"};
	
	ScrollListListener event;
	/** Called when the activity is first created. */
	public CalendarView(Activity context, LayoutInflater inflater){
		this.context = context;
		this.inflater = inflater;
		onCreate();
	}
	public void setOnDoneListener(ScrollListListener event){
		this.event = event;
	}
	public void onCreate() {
		BunKar = (BunKar) context.getApplication();
		sched = BunKar.getDatabase(BunKar.name);
		mainContainer = (LinearLayout) inflater.inflate(R.layout.my_calendar_view, null);
		
		//setContentView(R.layout.my_calendar_view);

		_calendar = Calendar.getInstance(Locale.getDefault());
		month = _calendar.get(Calendar.MONTH) + 1;
		year = _calendar.get(Calendar.YEAR);
		

		

		prevMonth = (ImageView) mainContainer.findViewById(R.id.prevMonth);
		prevMonth.setOnClickListener(this);

		currentMonth = (TextView) mainContainer.findViewById(R.id.currentMonth);
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		

		nextMonth = (ImageView) mainContainer.findViewById(R.id.nextMonth);
		nextMonth.setOnClickListener(this);

		calendarView = (GridView) mainContainer.findViewById(R.id.calendar);

		// Initialised 
		adapter = new GridCellAdapter(context.getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
		
		
		weekDaysGridView = (GridView) mainContainer.findViewById(R.id.calendarheader);
		weekDaysAdapter = new WeekDaysAdapter(context,R.layout.calendar_item, weekDays);
		weekDaysGridView.setAdapter(weekDaysAdapter);
		
		currentMonth.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				
				builder.setTitle("Select a Month");
				LinearLayout layout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.calendar_goto, null);
				builder.setView(layout);
				final Spinner spinMonth = (Spinner) layout.findViewById(R.id.calendargoto_spinner1);
				final Spinner spinYear = (Spinner) layout.findViewById(R.id.calendargoto_spinner2);
				ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context,   android.R.layout.simple_spinner_item, months);
				spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
				spinMonth.setAdapter(spinnerArrayAdapter);
				
				Calendar cal = Calendar.getInstance();
				try {
					Date start = sched.standards.getStartOfTerm();
					Date end = sched.standards.getEndOfTerm();
					cal.setTime(start);
					int startYear = cal.get(Calendar.YEAR);
					cal.setTime(end);
					int endYear = cal.get(Calendar.YEAR);
					Vector<String> v = new Vector<String>();
					while(startYear<=endYear){
						v.add(startYear+"");
						startYear++;
					}
					Object[] years = v.toArray();
					ArrayAdapter<Object> spinnerArrayAdapter2 = new ArrayAdapter<Object>(context,   android.R.layout.simple_spinner_item, years);
					spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
					spinYear.setAdapter(spinnerArrayAdapter2);
					
					
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
					}});
				builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						Toast.makeText(context, ""+spinMonth.getSelectedItem()+" "+spinYear.getSelectedItem(), Toast.LENGTH_LONG).show();
						setGridCellAdapterToDate(spinMonth.getSelectedItemPosition()+1, Integer.parseInt((String)spinYear.getSelectedItem()));
						month = spinMonth.getSelectedItemPosition()+1;
						year = Integer.parseInt((String)spinYear.getSelectedItem());
						arg0.dismiss();
					}});
				//authenticator.schedules.deleteSchedule(textView.getText().toString());
				//c.requery();
				//CAdapter.notifyDataSetChanged();
				builder.create();
				builder.show();
			}});
	}

	/**
	 * 
	 * @param month
	 * @param year 
	 */
	private void setGridCellAdapterToDate(int month, int year) {
		adapter = new GridCellAdapter(context.getApplicationContext(),
				R.id.calendar_day_gridcell, month, year);
		_calendar.set(year, month - 1, _calendar.get(Calendar.DAY_OF_MONTH));
		currentMonth.setText(DateFormat.format(dateTemplate,
				_calendar.getTime()));
		adapter.notifyDataSetChanged();
		calendarView.setAdapter(adapter);
	}

	public View getView(){
		return mainContainer;
	}
	
	@Override
	public void onClick(View v) {
		if (v == prevMonth) {
			if (month <= 1) {
				month = 12;
				year--;
			} else {
				month--;
			}
			
			setGridCellAdapterToDate(month, year);
		}
		if (v == nextMonth) {
			if (month > 11) {
				month = 1;
				year++;
			} else {
				month++;
			}
			
			setGridCellAdapterToDate(month, year);
		}

	}

	

	// Inner Class
	public class GridCellAdapter extends BaseAdapter {
		private static final String tag = "GridCellAdapter";
		private final Context _context;

		private final List<String> list;
		private static final int DAY_OFFSET = 1;
		private final String[] weekdays = new String[] { "Sun", "Mon", "Tue",
				"Wed", "Thu", "Fri", "Sat" };
		
		private final int[] daysOfMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
				31, 30, 31 };
		private int daysInMonth;
		private int currentDayOfMonth;
		private int currentWeekDay;
		//private Button gridcell;
		//private final int[] colors = {R.color.sky, R.color.lightgreen};
		//private int colorPointer = 0;
		//private TextView num_events_per_day;
		//private final HashMap<String, Integer> eventsPerMonthMap;
		private final SimpleDateFormat dateFormatter = new SimpleDateFormat(
				"dd-MMM-yyyy",Locale.ENGLISH);

		// Days in Current Month
		public GridCellAdapter(Context context, int textViewResourceId,
				int month, int year) {
			super();
			this._context = context;
			this.list = new ArrayList<String>();
			
			Calendar calendar = Calendar.getInstance();
			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
			
			// Print Month
			printMonth(month, year);

			// Find Number of Events
			//eventsPerMonthMap = findNumberOfEventsPerMonth(year, month);
		}

		private String getMonthAsString(int i) {
			return months[i];
		}

		private String getWeekDayAsString(int i) {
			return weekdays[i];
		}

		private int getNumberOfDaysOfMonth(int i) {
			return daysOfMonth[i];
		}

		public String getItem(int position) {
			return list.get(position);
		}

		@Override
		public int getCount() {
			return list.size();
		}
		
		public void printMonth(){
			list.clear();
			Calendar calendar = Calendar.getInstance();
			setCurrentDayOfMonth(calendar.get(Calendar.DAY_OF_MONTH));
			setCurrentWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
			
			// Print Month
			printMonth(month, year);
		}

		
		/**
		 * Prints Month
		 * 
		 * @param mm
		 * @param yy
		 */
		public void printMonth(int mm, int yy) {
			int trailingSpaces = 0;
			int daysInPrevMonth = 0;
			int prevMonth = 0;
			int prevYear = 0;
			int nextMonth = 0;
			int nextYear = 0;

			int currentMonth = mm - 1;
			String currentMonthName = getMonthAsString(currentMonth);
			daysInMonth = getNumberOfDaysOfMonth(currentMonth);

			
			GregorianCalendar cal = new GregorianCalendar(yy, currentMonth, 1);
			Log.d(tag, "Gregorian Calendar:= " + cal.getTime().toString());

			if (currentMonth == 11) {
				prevMonth = currentMonth - 1;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 0;
				prevYear = yy;
				nextYear = yy + 1;
				
			} else if (currentMonth == 0) {
				prevMonth = 11;
				prevYear = yy - 1;
				nextYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				nextMonth = 1;
				Log.d(tag, "**--> PrevYear: " + prevYear + " PrevMonth:"
						+ prevMonth + " NextMonth: " + nextMonth
						+ " NextYear: " + nextYear);
			} else {
				prevMonth = currentMonth - 1;
				nextMonth = currentMonth + 1;
				nextYear = yy;
				prevYear = yy;
				daysInPrevMonth = getNumberOfDaysOfMonth(prevMonth);
				
			}

			int currentWeekDay = cal.get(Calendar.DAY_OF_WEEK) - 1;
			trailingSpaces = currentWeekDay;

			

			if (cal.isLeapYear(cal.get(Calendar.YEAR)))
				if (mm == 2)
					++daysInMonth;
				else if (mm == 3)
					++daysInPrevMonth;

			// Trailing Month days
			for (int i = 0; i < trailingSpaces; i++) {
				
				list.add(String
						.valueOf((daysInPrevMonth - trailingSpaces + DAY_OFFSET)
								+ i)
						+ "-GREY"
						+ "-"
						+ getMonthAsString(prevMonth)
						+ "-"
						+ prevYear);
			}

			// Current Month Days
			for (int i = 1; i <= daysInMonth; i++) {
				
				if (i == getCurrentDayOfMonth()) {
					list.add(String.valueOf(i) + "-BLUE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				} else {
					list.add(String.valueOf(i) + "-WHITE" + "-"
							+ getMonthAsString(currentMonth) + "-" + yy);
				}
			}

			// Leading Month days
			for (int i = 0; i < list.size() % 7; i++) {
				
				list.add(String.valueOf(i + 1) + "-GREY" + "-"
						+ getMonthAsString(nextMonth) + "-" + nextYear);
			}
			if(adapter!=null)
			adapter.notifyDataSetChanged();
			
		}

		/**
		 * NOTE: YOU NEED TO IMPLEMENT THIS PART Given the YEAR, MONTH, retrieve
		 * ALL entries from a SQLite database for that month. Iterate over the
		 * List of All entries, and get the dateCreated, which is converted into
		 * day.
		 * 
		 * @param year
		 * @param month
		 * @return
		 */
		private HashMap<String, Integer> findNumberOfEventsPerMonth(int year,
				int month) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();

			return map;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		
		class AlertDialogForOptions extends AlertDialog{
			public TextView title;
			public Date date;
			class BackgroundApplier extends AsyncTask<String,Void,String>{
				public int OP;
				@Override
				protected String doInBackground(String... params) {
					
					MaintenanceManager.HALT = true;
					if(BunKar.isMyServiceRunning("com.vosaye.bunkr.services.MaintenanceManager")){
						while(MaintenanceManager.STATUS == MaintenanceManager.BUSY){
							//wait for manager to be halted by this thread...
						}
					}
					
					
					
					
					ValidatorService.HALT = true;
					
					while(ValidatorService.status == ValidatorService.BUSY){
						if(!BunKar.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
							context.startService(new Intent(context,ValidatorService.class));
					}
					Cursor c = BunKar.settings.rawQuery("select max(id) from labsdecore"+BunKar.name.replaceAll(" ", "_")+" where id > 0;");
					if(c.moveToFirst()){
						if(c.getString(0)==null){
						//sched.close();
						BunKar.tempBackupDbase(BunKar.name,  Calendar.getInstance().getTime(), "Semester opened");
						//sched.open();
						}
					}else {
						//sched.close();
						BunKar.tempBackupDbase(BunKar.name,  Calendar.getInstance().getTime(), "Semester opened");
						//sched.open();
					}
					c.close();
					sched.beginTransaction();
					if(OP==0){
						try {
							sched.meta.insertBlankIntoIndex(date, 0);
							sched.commit();
							//sched.close();
							BunKar.tempBackupDbase(BunKar.name, Calendar.getInstance().getTime(), "Range created at "+sdf3.format(date));
							//sched.open();
							ValidatorService.HALT = false;
						} catch (SQLiteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} finally{
							if(sched.getDatabase().inTransaction()) sched.rollback();
							
							ValidatorService.HALT = false;
						}
					}
					else if(OP == 1){
						String sname;
						try {
							sname = sched.meta.insertIntoIndex(date, sched.meta.getUUID(""), 0, true);
							sched.meta.copyStructure(BunKar.pseudoStructureNameClipBoard,sname );
							sched.meta.copyRecord(BunKar.pseudoStructureNameClipBoard, date);
							sched.dropTable(BunKar.pseudoStructureNameClipBoard);
							BunKar.pseudoStructureNameClipBoard = " "; 
							sched.stats.addToPipeline(sched.stats.selectMonthly(date));
							sched.stats.addToPipeline(sched.stats.selectWeekly(date));
							sched.commit();
							
							//sched.close();
							BunKar.tempBackupDbase(BunKar.name, Calendar.getInstance().getTime(), "Editted Schedule for "+sdf3.format(date));
							//sched.open();
							ValidatorService.HALT = false;
						} catch (SQLiteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							if(sched.getDatabase().inTransaction()) sched.rollback();
							ValidatorService.HALT = false;
						}
					}
					else if(OP == 2){
						try {
							sched.stats.deleteCustomRange(sched.stats.getStart(sched.stats.getBlackHole(date)),sched.stats.getEnd(sched.stats.getBlackHole(date)));

							sched.commit();
							//sched.close();
							BunKar.tempBackupDbase(BunKar.name, Calendar.getInstance().getTime(), "Range deleted at "+sdf3.format(date));
							//sched.open();
							ValidatorService.HALT = false;
						} catch (SQLiteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}finally{
							if(sched.getDatabase().inTransaction()) sched.rollback();
							ValidatorService.HALT = false;
						}
					}
					ValidatorService.HALT = false;
					return null;
				}
				protected void onPostExecute(String param){
					if(OP==1)Toast.makeText(CalendarView.this.context, "Pasted "+BunKar.pseudoStructureNameClipBoard, Toast.LENGTH_LONG).show();
					
					CalendarView.this.adapter.printMonth();
					alert.dismiss();
				}
			
			
			}
			AlertDialog.Builder builder;
			AlertDialog dialog;
			AlertDialog alert;
			AlertDialog.Builder builderOfDialog = new AlertDialog.Builder(context);
			
			protected AlertDialogForOptions(Context context, Date datex) {
				super(context);
				setTitle("");
				this.date = datex;
				LinearLayout layout = (LinearLayout) CalendarView.this.context.getLayoutInflater().inflate(R.layout.cal_popupfordate, null);
				setView(layout);
				ListView list = (ListView) layout.findViewById(R.id.cal_popupfordatelistView1);
				title = (TextView) layout.findViewById(R.id.cal_popupfordate_title);
				String options[];
				builderOfDialog.setTitle("Info");
				builderOfDialog.setMessage("Applying changes");
				
				if(sched.stats.inBlackHole(datex)){
					options = new String[2];
					options[0] = "Delete Range";
					options[1] = "Open Range";
						//{"insert blank","insert black hole","copy","paste","open"};
				}
				else{
					options = null;
					try {
						if(!sched.meta.selectFromIndex(datex).equals("labsdecoreblank")){
						
						options = new String[7];
						options[0] = "Insert Blank";
						options[1] = "Insert Range";
						options[2] = "Copy";
						options[3] = "Paste";
						options[4] = "Open Structure";
						options[5] = "Set whole day attended";
						options[6] = "Set whole day bunked";
						} else {

							options = new String[5];
							options[0] = "Insert Blank";
							options[1] = "Insert Range";
							options[2] = "Copy";
							options[3] = "Paste";
							options[4] = "Open Structure";
						}
						
					} catch (SQLiteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (BunkerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally{
					}

					
				}
				
				//final String options[] = {"insert blank","insert black hole","copy","paste","open"};
				ArrayAdapter<String> adapt = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, options);
				list.setAdapter(adapt);
				
				list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				    public void onItemClick(AdapterView parent, View v, int position, long id){
				    	builder = new AlertDialog.Builder(CalendarView.this.context);
				        switch(position){
				        case 0:
				        	if(!sched.stats.inBlackHole(date)){
							builder.setMessage("Do you really wanna insert blank at "+sdf.format(date)+" \nPrevious contents cannot be brought back once blank is inserted");
							builder.setTitle("Warning");
							builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
								}});
							builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
									alert = builderOfDialog.create();
									alert.show();
									BackgroundApplier bg = new BackgroundApplier();
									bg.OP = 0;
									bg.execute();
								
								}});
							dialog = builder.create();
							dialog.show();
				        	}
				        	else{
				        		
				        		builder.setMessage("Do you really wanna delete this Range");
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
										arg0.dismiss();
										alert = builderOfDialog.create();
										alert.show();
										
										
										BackgroundApplier bg = new BackgroundApplier();
										bg.OP = 2;
										bg.execute();
										
									
									}});
								dialog = builder.create();
								dialog.show();
				        	}
				        break;
				        case 1:
				        	if(!sched.stats.inBlackHole(date)){
				        		Intent intent = new Intent(CalendarView.this.context,BlackHoleList.class);
				        		intent.putExtra("start", sdf2.format(date));
				        		CalendarView.this.context.startActivity(intent);
				        		
				        	}else{
				        		String name = sched.stats.getBlackHole(date);
				        		Intent intent = new Intent(CalendarView.this.context, BlackHoleActivity.class);
				        		intent.putExtra("name", name);
				        		CalendarView.this.context.startActivity(intent);
				        		
				        	}
				        break;
				        case 2:
				        	
				        	try {
				        		String pseudoName = sched.meta.createPseudoStructure();
				        		if(!sched.meta.selectFromIndex(date).equals("labsdecoreblank")){
								sched.meta.copyToPseudoStructure(pseudoName, sched.meta.selectFromIndex(date), date);
								CalendarView.this.BunKar.pseudoStructureNameClipBoard = pseudoName;
								Toast.makeText(CalendarView.this.context, "Copied to clipboard "+BunKar.pseudoStructureNameClipBoard, Toast.LENGTH_LONG).show();
				        		
								
				        		
				        		}
				        		else Toast.makeText(CalendarView.this.context, "Cannot copy blank", Toast.LENGTH_LONG).show();
							} catch (SQLiteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (BunkerException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
				        	
				        break;
				        case 3:
				        	if(BunKar.pseudoStructureNameClipBoard.equals(" ")){
				        		Toast.makeText(CalendarView.this.context, "Copy before pasting", Toast.LENGTH_LONG).show();
				        	}
				        	else{
				        		Toast.makeText(CalendarView.this.context, "Pasted "+BunKar.pseudoStructureNameClipBoard, Toast.LENGTH_LONG).show();
								
				        	builder = new AlertDialog.Builder(CalendarView.this.context);
							builder.setMessage("Do you really wanna paste to "+sdf.format(date)+" \nPrevious contents cannot be brought back once blank is inserted");
							builder.setTitle("Warning");
							builder.setNegativeButton("Belay that !", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
								}});
							builder.setPositiveButton("That be true !", new DialogInterface.OnClickListener(){

								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									arg0.dismiss();
									alert = builderOfDialog.create();
									alert.show();
									
									
									
									BackgroundApplier bg = new BackgroundApplier();
									bg.OP = 1;
									bg.execute();
									
									
								
								}});
							dialog = builder.create();
							dialog.show();
				        	}
				        break;
				        case 4:
				        	if(sched.stats.inBlackHole(date)){
								//to modified structure activity
				        		Intent intent = new Intent(CalendarView.this.context,StructureActivity.class);
								intent.putExtra("date", sdf2.format(date));
								intent.putExtra("count", "-1");
								CalendarView.this.context.startActivity(intent);
								
							}
							else{
								Intent intentx = new Intent(CalendarView.this.context,StructureActivity.class);
								intentx.putExtra("date", sdf2.format(date));
								intentx.putExtra("count", "0");
								//one day - structure with records
								CalendarView.this.context.startActivity(intentx);
								
							}
				        break;
				        
				        case 5:
				        	sched.execQuery("update "+sched.meta.selectRecord(date)+" set attendance = 1;");
				        	sched.stats.addToPipeline(sched.stats.selectWeekly(date));
				        	sched.stats.addToPipeline(sched.stats.selectMonthly(date));
				        	Cursor c = sched.rawQuery("select rname from ranges where start <= '"+sdf2.format(date)+"' and end >= '"+sdf2.format(date)+"' and rname not like '%fixed%'");
							if(c.moveToFirst()){
								do{

									sched.stats.addToPipeline(c.getString(0));
								}
								while(c.moveToNext()); 
							}
							c.close();
							CalendarView.this.context.startService(new Intent(CalendarView.this.context,ValidatorService.class));
				        	
							break;
				        case 6:
				        	System.out.println("jVoss "+sdf2.format(date));
				        	sched.execQuery("update "+sched.meta.selectRecord(date)+" set attendance = 0;");
				        	System.out.println("jVoss "+sched.stats.selectWeekly(date));
				        	sched.stats.addToPipeline(sched.stats.selectWeekly(date));
				        	sched.stats.addToPipeline(sched.stats.selectMonthly(date));
				        	Cursor cx = sched.rawQuery("select rname from ranges where start <= '"+sdf2.format(date)+"' and end >= '"+sdf2.format(date)+"' and rname not like '%fixed%'");
							if(cx.moveToFirst()){
								do{ 

									sched.stats.addToPipeline(cx.getString(0));
								} 
								while(cx.moveToNext()); 
							}
							cx.close();
							CalendarView.this.context.startService(new Intent(CalendarView.this.context,ValidatorService.class));
				        	break;
				        default:
				        
				        	
				        }
				        dismiss();
				    }
				});
				
				
			}
			
		}
		
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			//if (row == null) {
				LayoutInflater inflater = (LayoutInflater) _context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				row = inflater.inflate(R.layout.screen_gridcell, parent, false);
			//}
   
			// Get a reference to the Day gridcell
			final Button gridcell = (Button) row.findViewById(R.id.calendar_day_gridcell);
			//final RelativeLayout container = (RelativeLayout) row.findViewById(R.id.screengridcell_container);
			//final TextView perc = (TextView) row.findViewById(R.id.num_events_per_day);
			// ACCOUNT FOR SPACING

			
			String[] day_color = list.get(position).split("-");
			String theday = day_color[0];
			String themonth = day_color[2];
			String theyear = day_color[3];
			//System.out.println(theday+" "+themonth+" "+theyear);
			
			
			final Calendar cal = Calendar.getInstance();
			final Calendar now = Calendar.getInstance();
			now.set(Calendar.HOUR_OF_DAY, 0);
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			try {
				cal.setTime(sdf.parse(theday+" "+themonth+" "+theyear));
				//System.out.println(sdf.format(cal.getTime())+" times");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gridcell.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View view) {
					CalendarView.this.selected = cal.getTime();
					CalendarView.this.event.onDone(view);
				}});
			
			gridcell.setOnLongClickListener(new View.OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					AlertDialogForOptions options = new AlertDialogForOptions(context,cal.getTime());
					options.title.setText("Options for "+sdf.format(cal.getTime())); 
					options.show();
					
					
					
					return true;
				}
			});
			// Set the Day GridCell
			gridcell.setText(theday);
			gridcell.setTag(theday + "-" + themonth + "-" + theyear);
			
			
			if (day_color[1].equals("GREY")) {
				gridcell.setTextColor(context.getResources()
						.getColor(R.color.lightgray));
			}
			if (day_color[1].equals("WHITE")) {
				gridcell.setTextColor(context.getResources().getColor(
						R.color.dark));
			}
			if (day_color[1].equals("BLUE")) {
				if(cal.get(Calendar.DAY_OF_MONTH)==now.get(Calendar.DAY_OF_MONTH)&&cal.get(Calendar.MONTH)==now.get(Calendar.MONTH)&&cal.get(Calendar.YEAR)==now.get(Calendar.YEAR))
				gridcell.setTextColor(context.getResources().getColor(R.color.orrange));
				else
					gridcell.setTextColor(context.getResources()
							.getColor(R.color.dark));
			}
			try {
			//its in the black hole
			//its normal
			//its blank
			//its outside term
			Calendar strt = Calendar.getInstance(),nd = Calendar.getInstance();
			strt.setTime(sched.standards.getStartOfTerm());
			nd.setTime(sched.standards.getEndOfTerm());
			if(sched.stats.inBlackHole(cal.getTime())){

				//gridcell.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.blackhole_selector));
				gridcell.setBackgroundResource(R.drawable.blackhole_selector);
				//perc.setText("");
				Cursor c = sched.rawQuery("select rname from ranges where start = '"+sdf2.format(cal.getTime())+"' and rname like '%fixed%';");
				if(c.moveToFirst()){
					gridcell.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.blackhole_open_selector));
					//perc.setText("50%");
				}
				c.close();
				c = sched.rawQuery("select rname from ranges where end = '"+sdf2.format(cal.getTime())+"' and rname like '%fixed%';");
				if(c.moveToFirst()){

					gridcell.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.blackhole_close_selector));
				}
				c.close();
				
			}
			else if(cal.before(strt)||cal.after(nd)){
				gridcell.setBackgroundColor(context.getResources().getColor(R.color.lightgray));
				gridcell.setOnClickListener(null);
				gridcell.setOnLongClickListener(null);
				gridcell.setTextColor(context.getResources().getColor(R.color.black));
				//perc.setText("");
				
			}
			else if(sched.meta.selectFromIndex(cal.getTime()).equals("labsdecoreblank")){
				gridcell.setBackgroundColor(context.getResources().getColor(R.color.gray));
				if(now.compareTo(cal)!=0)
				gridcell.setTextColor(context.getResources().getColor(R.color.white));
			}
			if(cal.get(Calendar.DAY_OF_MONTH)==now.get(Calendar.DAY_OF_MONTH)&&cal.get(Calendar.MONTH)==now.get(Calendar.MONTH)&&cal.get(Calendar.YEAR)==now.get(Calendar.YEAR))
				gridcell.setTextColor(context.getResources().getColor(R.color.orrange));
			
			//outside term
			
			
				} catch (SQLiteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (BunkerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return row;
		}

		

		public int getCurrentDayOfMonth() {
			return currentDayOfMonth;
		}

		private void setCurrentDayOfMonth(int currentDayOfMonth) {
			this.currentDayOfMonth = currentDayOfMonth;
		}

		public void setCurrentWeekDay(int currentWeekDay) {
			this.currentWeekDay = currentWeekDay;
		}

		public int getCurrentWeekDay() {
			return currentWeekDay;
		}
	}
	
	
	class WeekDaysAdapter extends ArrayAdapter{
		String days[] = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
		public WeekDaysAdapter(Context context, int textViewResourceId, Object[] objects) {
			super(context, textViewResourceId, objects);
			
		}
		public View getView(int position, View convertView, ViewGroup parent){
			if(convertView == null){ convertView = context.getLayoutInflater().inflate(R.layout.calendar_item, null); }
			final TextView textDate = (TextView) convertView.findViewById(R.id.calendar_item_text);
			textDate.setText(days[position]);
			final int positionx = position;
			convertView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					try {
							Date date = sched.standards.getStartOfTerm();
							Calendar cal = Calendar.getInstance();
							cal.setTime(date);
							while(cal.get(Calendar.DAY_OF_WEEK)!=positionx+1){
								cal.add(Calendar.DATE, 1);
							}
							date = cal.getTime();
							//perform an insert
							Intent intent = new Intent(context,WeekDayStructure.class);
							intent.putExtra("date", sdf.format(date));
							intent.putExtra("count", ""+sched.meta.countWeeks(date, sched.standards.getEndOfTerm()));
							//one day - structure with records
							context.startActivity(intent);
							
						} catch (SQLiteException e) {
							e.printStackTrace();
						} catch (ParseException e) {
							e.printStackTrace();
						} catch (BunkerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
				}});
			return convertView;
		}
		
	}
	
}
