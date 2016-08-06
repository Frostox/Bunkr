package com.vosaye.bunkr.externalpackages;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;



import com.vosaye.bunkr.R;
import com.vosaye.bunkr.externalpackages.master.TabsAdapter;

public class ScrollingTabs_Adapter implements TabsAdapter 
{
	private Activity mContext;
	private String[] mTitles = {"Subjects", "Types", "Terms"};

	public ScrollingTabs_Adapter(Activity ctx) 
	{
		this.mContext = ctx;
	}

	@Override
	public View getView(int position) 
	{
		Button tab;
		LayoutInflater inflater = mContext.getLayoutInflater();
		tab = (Button) inflater.inflate(R.layout.viewpager_tabscrolling, null); 

		if (position < mTitles.length)
		{
			tab.setText(mTitles[position]);
		}
		return tab;
	}
	
	public void setStringsTitles(String title1, String title2, String title3){
		mTitles[0] = title1;
		mTitles[1] = title2;
		mTitles[2] = title3;
	}
}
