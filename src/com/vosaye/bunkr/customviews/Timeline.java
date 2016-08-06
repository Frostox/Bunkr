package com.vosaye.bunkr.customviews;

import java.util.Date;
import java.util.Vector;

















import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteException;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.WeekDayStructure;
import com.vosaye.bunkr.base.ScheduleDatabase;
import com.vosaye.bunkr.datasctrs.SessionViewDS;
import com.vosaye.bunkr.exception.BunkerException;
import com.vosaye.bunkr.services.MaintenanceManager;
import com.vosaye.bunkr.services.ValidatorService;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.View.OnTouchListener;

public class Timeline extends RelativeLayout implements OnTouchListener{
	java.text.DecimalFormat nft = new java.text.DecimalFormat("#00.###");
	Paint paint = new Paint();
	Activity activity;
	public ScrollView scroll;
	public Cursor c;
	//Toast toast;
	float lastyposition = 0, yposition = 0;
	Vector<RelativeLayout> views;
	Vector<SessionViewDS> viewsx;
	public float metrics = 130;
	private int _yDelta;
	private int _xDelta;
	boolean locked = false;
	ScheduleDatabase sched;
	BunKar bunker;
	Date date;
	public Timeline(Context context, AttributeSet attrs) {
		super(context, attrs);
		nft.setDecimalSeparatorAlwaysShown(false);
		viewsx = new Vector<SessionViewDS>();
		// TODO Auto-generated constructor stub
	}

	public void setLocked(boolean locked){
		this.locked = locked;
	}
	public boolean getLocked(){
		return locked;
	}
	
	public void closeList(){
		if(c!=null)
			c.close();
	}
	
	public void setActivity(Activity activity, Date date){
		this.activity = activity;
		bunker = ((BunKar) activity.getApplication());
		sched = bunker.getDatabase(bunker.name);
		//scroll = (ScrollView) activity.findViewById(R.id.str_scroller);
		//scroll = (ScrollView) this.getParent().getParent();
		this.date = date;
	}
	
	public void setCursor(Cursor c){
		nft.setDecimalSeparatorAlwaysShown(false);  
		this.c = c;
	}
	
	public Timeline(Context context) {
		super(context);
		viewsx = new Vector<SessionViewDS>();
		nft.setDecimalSeparatorAlwaysShown(false);  
		bunker = ((BunKar) activity.getApplication());
		sched = bunker.getDatabase(bunker.name);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
		paint.setTextSize(12 * this.getResources().getDisplayMetrics().scaledDensity);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		paint.setColor(0x600000ff);

		float beg = paint.measureText("00 : 00 ");
		canvas.drawLine(beg + 10  * this.getResources().getDisplayMetrics().density, 0, beg + 10  * this.getResources().getDisplayMetrics().density, params.height, paint);
		int i = 0;
		float prevdp = 0, prevmins = 0;
		for(i=0;i<viewsx.size();i++){
			float traverseMins=prevmins+10;
			while(traverseMins<viewsx.elementAt(i).mins-10){
				if(traverseMins%60==0){
					//draw
					float temp= traverseMins/60;
					canvas.drawText(""+nft.format((int)(temp>12?temp-12:temp))+" : "+nft.format(0), 10 * this.getResources().getDisplayMetrics().density, (int) ((((traverseMins-prevmins)/60)*metrics* this.getResources().getDisplayMetrics().density)+prevdp), paint);
				}
				else traverseMins-=traverseMins%60; //make 0
				traverseMins+=60;
			}
			RelativeLayout.LayoutParams lparams = (LayoutParams) viewsx.elementAt(i).view.getLayoutParams();
			lparams.leftMargin = (int) (paint.measureText("00 : 00 ") + 10  * this.getResources().getDisplayMetrics().density + 40* this.getResources().getDisplayMetrics().density);
			
			
			prevmins =  (viewsx.elementAt(i).mins+viewsx.elementAt(i).duration);
			prevdp = (viewsx.elementAt(i).startdp+viewsx.elementAt(i).height);
			canvas.drawLine(beg + 10  * this.getResources().getDisplayMetrics().density, viewsx.elementAt(i).startdp, beg + 10  * this.getResources().getDisplayMetrics().density + 40* this.getResources().getDisplayMetrics().density, viewsx.elementAt(i).startdp, paint);
			canvas.drawLine(beg + 10  * this.getResources().getDisplayMetrics().density, viewsx.elementAt(i).startdp+viewsx.elementAt(i).height, beg + 10  * this.getResources().getDisplayMetrics().density+ 40* this.getResources().getDisplayMetrics().density, viewsx.elementAt(i).startdp+viewsx.elementAt(i).height, paint);
			
		}
		paint.setColor(0x600000ff);
		float traverseMins = prevmins+10;
		while(traverseMins<(24*60)){
			if(traverseMins%60==0){
				//draw
				float temp = traverseMins/60;
				canvas.drawText(""+nft.format((int)(temp>12?temp-12:temp))+" : "+nft.format(0), 10 * this.getResources().getDisplayMetrics().density, (int) ((((traverseMins-prevmins)/60)*metrics* this.getResources().getDisplayMetrics().density)+prevdp), paint);
			}
			else traverseMins-=traverseMins%60; //make 0
			traverseMins+=60;
		}
		if(traverseMins<=prevmins){
			params.height = (int) (prevdp);
		}else{
			params.height = (int)(prevdp+(((traverseMins-prevmins)/60)*metrics* this.getResources().getDisplayMetrics().density));
		}
		this.setLayoutParams(params);
	}
	
	/** The method inflates the views to datastrctr, which is used
	 *  by the on draw method to draw on the screen.
	 */
	public void inflate(){
		this.removeAllViews();
		if(c!=null)
		c.requery();
		int i = 0;
		float prevdp = 0, prevmins = 0;
		RelativeLayout current;
		SessionViewDS currentDS;
		//RelativeLayout.LayoutParams paramsx;
		TextView subname, typname, startmins, endmins;
		
		if(c!=null&&activity!=null){
			//Toast.makeText(activity, "inside "+c.getCount(), Toast.LENGTH_SHORT).show();
			if(c.moveToFirst())
				do{
					
					if(i>=viewsx.size()){
						viewsx.add(new SessionViewDS());
					}
					if(viewsx.elementAt(i).view==null){
						viewsx.elementAt(i).view = (RelativeLayout) Timeline.inflate(activity, R.layout.activity_structures_str, null);
						
					}
					currentDS = viewsx.elementAt(i);
					current = viewsx.elementAt(i).view;
					this.addView(current);
					final RelativeLayout.LayoutParams paramsx = (RelativeLayout.LayoutParams) current.getLayoutParams();
					viewsx.elementAt(i).viewParams = paramsx;
					final float mins = c.getInt(0);
					float duration = c.getInt(3);
					int attendance = c.getInt(4);
					if(duration<30) duration = 30;
					if(duration>120) duration = 120;
					int height = (int) ((metrics) * (duration/60) * getResources().getDisplayMetrics().density);
					int top = (int) ((((mins-prevmins)/60)*metrics) * getResources().getDisplayMetrics().density +prevdp);
					prevmins = mins + c.getInt(3);
					prevdp = top + height;
					paramsx.topMargin = top;
					paramsx.leftMargin =  (int) (paint.measureText("00 : 00 ") + 10  * this.getResources().getDisplayMetrics().density + 40* this.getResources().getDisplayMetrics().density);
					paramsx.height = height;
					paramsx.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					subname = (TextView) current.findViewById(R.id.str_session_subjnames);
					subname.setText(c.getString(1)+" "+c.getString(2));
					float tempmins= (mins/60), tempdur= ((mins+c.getInt(3))/60);
					startmins = ((TextView)current.findViewById(R.id.str_top));
					startmins.setText(""+nft.format((int)(tempmins>12?tempmins-12:tempmins))+ " : "+nft.format((int)(mins%60))+ (tempmins>12?" PM":" AM"));
					endmins = ((TextView)current.findViewById(R.id.str_duration));
					endmins.setText(""+nft.format((int)((tempdur)>12?tempdur-12:(tempdur)))+" : "+nft.format((int)((mins+c.getInt(3))%60))+(tempdur>12?" PM":" AM"));
					//current.setOnTouchListener(this);
					final ToggleButton yes, no, dontcare;
					yes = (ToggleButton) current.findViewById(R.id.str_session_yess);
					no = (ToggleButton) current.findViewById(R.id.str_session_nos);
					dontcare = (ToggleButton) current.findViewById(R.id.str_session_dontcares);
					
					if(!locked&&!sched.stats.inBlackHole(date)){
					if(attendance==0){
						//Toast.makeText(activity, "attendance is "+attendance, Toast.LENGTH_LONG).show();
						yes.setChecked(false);
						no.setChecked(true);
						dontcare.setChecked(false);
					}
					else if(attendance==1||attendance==2){

						//Toast.makeText(activity, "attendance is "+attendance, Toast.LENGTH_LONG).show();
						
						yes.setChecked(true);
						no.setChecked(false);
						dontcare.setChecked(false);
					} 
					else{

						//Toast.makeText(activity, "attendance is "+attendance, Toast.LENGTH_LONG).show();
						
						yes.setChecked(false);
						no.setChecked(false);
						dontcare.setChecked(true);
					}
					yes.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							
							if((!yes.isChecked())&&(!no.isChecked())&&(!dontcare.isChecked())){
								((ToggleButton)v).setChecked(true);
							}
							else{
								//if(yes.isChecked()){
									//Toast.makeText(activity, "yes is checked", Toast.LENGTH_LONG).show();
									
									try {
										sched.meta.setAttendance(date, (int)mins, 1);
										//c.requery();
										//Toast.makeText(activity, "attendance "+c.getInt(4), Toast.LENGTH_LONG).show();
										inflate();
									} catch (SQLiteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (BunkerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} finally {
										if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
											Timeline.this.getContext().startService(new Intent(Timeline.this.getContext(),ValidatorService.class));
									}
									
								//}
							}
							no.setChecked(false);
							dontcare.setChecked(false);
							
						}
					});
					no.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							if((!yes.isChecked())&&(!no.isChecked())&&(!dontcare.isChecked())){
								((ToggleButton)v).setChecked(true);
							}else{
								//Toast.makeText(activity, "updating", Toast.LENGTH_LONG).show();
								
								//else if(no.isChecked()){
									//Toast.makeText(activity, "no is checked", Toast.LENGTH_LONG).show();
									
									try {
										sched.meta.setAttendance(date, (int)mins, 0);
										//c.requery();
										//Toast.makeText(activity, "attendance "+c.getInt(4), Toast.LENGTH_LONG).show();
										inflate();
									} catch (SQLiteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (BunkerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} finally{
										if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
											Timeline.this.getContext().startService(new Intent(Timeline.this.getContext(),ValidatorService.class));
										
									}
								//}
								
							}
							
							
							
							yes.setChecked(false);
							dontcare.setChecked(false);
							
						}
					});
					dontcare.setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							if((!yes.isChecked())&&(!no.isChecked())&&(!dontcare.isChecked())){
								((ToggleButton)v).setChecked(true);
							}else{
								
								//else{
									//Toast.makeText(activity, "dont care is checked", Toast.LENGTH_LONG).show();
									
									try {
										sched.meta.setAttendance(date, (int)mins, 3);
										//c.requery();
										//Toast.makeText(activity, "attendance "+c.getInt(4), Toast.LENGTH_LONG).show();
										inflate();
									} catch (SQLiteException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (BunkerException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									} finally{
										if(!bunker.isMyServiceRunning("com.vosaye.bunkr.services.ValidatorService"))
											Timeline.this.getContext().startService(new Intent(Timeline.this.getContext(),ValidatorService.class));
										
									}
								//}
							}
							no.setChecked(false);
							yes.setChecked(false);
							
						}
					});
					yes.setEnabled(true);
					no.setEnabled(true);
					dontcare.setEnabled(true);
					
					}else if(sched.stats.inBlackHole(date)){
						yes.setVisibility(View.INVISIBLE);
						no.setVisibility(View.INVISIBLE);
						dontcare.setVisibility(View.INVISIBLE);
					}
					else{
						yes.setEnabled(false);
						no.setEnabled(false);
						dontcare.setEnabled(false);
					}
					
					
					
					currentDS.duration = c.getInt(3);
					currentDS.mins = c.getInt(0);
					currentDS.subjname = c.getString(1);
					currentDS.typname = c.getString(2);
					currentDS.startdp = paramsx.topMargin;
					currentDS.enddp = paramsx.topMargin + paramsx.height;
					currentDS.height = paramsx.height;
					
					
					/*
 endmins.setOnTouchListener(new OnTouchListener(){

						@Override
						public boolean onTouch(View view, MotionEvent touchevent) {
							RelativeLayout current = (RelativeLayout)view.getParent();
							RelativeLayout.LayoutParams param = (android.widget.RelativeLayout.LayoutParams) current.getLayoutParams();
							current.bringToFront();
							final int X = (int) touchevent.getRawX();
						    final int Y = (int) touchevent.getRawY();
						    switch (touchevent.getAction() & MotionEvent.ACTION_MASK) {
						        case MotionEvent.ACTION_DOWN:

						    		scroll.requestDisallowInterceptTouchEvent(true);
						            _yDelta = Y - param.height;
						            break;
						        case MotionEvent.ACTION_UP:
						        	scroll.requestDisallowInterceptTouchEvent(false);
						        	break;
						        case MotionEvent.ACTION_POINTER_DOWN:
						            break;
						        case MotionEvent.ACTION_POINTER_UP:
						            break;
						        case MotionEvent.ACTION_MOVE:
						        	if(Y-_yDelta>(30f/60f)*metrics&&Y-_yDelta<(120/60)*metrics)
						            param.height = (Y - _yDelta);
						        	else{
						        		if(Y-_yDelta<(30f/60f)*metrics){
						        			param.height = (int) ((30f/60f)*metrics);
						        		}
						        		else if(Y-_yDelta>(120f/60f)*metrics){
						        			param.height = (int) ((120f/60f)*metrics);
						        		}
						        	}
						        	//((TextView) view).setText("height : "+param.height+" delta : "+(Y-_yDelta)+" "+(30f/60f)*metrics);
						        	current.setLayoutParams(param); 
						            break;
						    }
						    
						    return true;
						}});
*/
					i++;
				}
				while(c.moveToNext());
		}
		this.invalidate();
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent touchevent) {
		RelativeLayout.LayoutParams param = (android.widget.RelativeLayout.LayoutParams) view.getLayoutParams();
		view.bringToFront();
		final int X = (int) touchevent.getRawX();
	    final int Y = (int) touchevent.getRawY();
	    switch (touchevent.getAction() & MotionEvent.ACTION_MASK) {
	        case MotionEvent.ACTION_DOWN:

	    		scroll.requestDisallowInterceptTouchEvent(true);
	            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
	            _xDelta = lParams.leftMargin;
	            _yDelta = Y - lParams.topMargin;
	            break;
	        case MotionEvent.ACTION_UP:

	    		scroll.requestDisallowInterceptTouchEvent(false);
	        	break;
	        case MotionEvent.ACTION_POINTER_DOWN:
	            break;
	        case MotionEvent.ACTION_POINTER_UP:
	            break;
	        case MotionEvent.ACTION_MOVE:
	            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
	            layoutParams.leftMargin = _xDelta;
	            layoutParams.topMargin = (Y - _yDelta);
	            view.setLayoutParams(layoutParams); 
	            break;
	    }
	    return true;
	}
}
