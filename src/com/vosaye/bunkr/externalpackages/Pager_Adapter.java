package com.vosaye.bunkr.externalpackages;

import java.util.List;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class Pager_Adapter extends FragmentStatePagerAdapter
{
	private List<Fragment> fragments;
	public int position;
	public Pager_Adapter(FragmentManager fm, List<Fragment> fragments) 
	{
		super(fm);
		this.fragments = fragments;
	}
	
	public Fragment getItem(int position) 
	{
		return this.fragments.get(position);
	}
	
	
	
	public int getCount() 
	{
		return this.fragments.size();
	}
	
	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object) {
	    //mCurrentView = (View)object;
		this.position = position;
	}

	
}
