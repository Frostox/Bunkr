package com.vosaye.bunkr.customviews;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class CustomListView extends ListView{
	private int old_count = 0;
	public CustomListView(Context context) {
		super(context);
		
		
	}
	
	 public static void setListViewHeightBasedOnChildren(ListView listView) {
	        ListAdapter listAdapter = listView.getAdapter(); 
	        if (listAdapter == null) {
	            // pre-condition
	            return;
	        }

	        int totalHeight = 0;
	        for (int i = 0; i < listAdapter.getCount(); i++) {
	            View listItem = listAdapter.getView(i, null, listView);
	            listItem.measure(0, 0);
	            totalHeight += listItem.getMeasuredHeight();
	        }

	        ViewGroup.LayoutParams params = listView.getLayoutParams();
	        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	        listView.setLayoutParams(params);
	    }
	
	
	/*
 	public void onDraw(Canvas canvas){
		//if(this.getCount()!=old_count){
			old_count = getCount();
			android.view.ViewGroup.LayoutParams params = this.getLayoutParams();
			if(old_count<=0) params.height = 50;
			else
			params.height = getCount() * (old_count>0?getChildAt(0).getHeight():0);
			setLayoutParams(params);
		//}
		super.onDraw(canvas);
	}
*/
	
	
}
