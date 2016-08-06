package com.vosaye.bunkr.datasctrs;


import com.vosaye.bunkr.customviews.Timeline;

import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class SessionViewDS {
	public RelativeLayout view;
	public RelativeLayout.LayoutParams viewParams;
	public int mins, duration;
	public String subjname, typname;
	public int startdp, enddp, height;
	Timeline timeline;
	public SessionViewDS(Timeline timeline, String sub, String typ, int mins, int dur, RelativeLayout view){
		this.subjname = sub;
		this.typname = typ;
		this.mins = mins;
		this.duration = dur;
		this.view = view;
		this.timeline = timeline;
	}
	public SessionViewDS(){
		
	}
	
}
