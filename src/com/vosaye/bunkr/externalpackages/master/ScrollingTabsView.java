package com.vosaye.bunkr.externalpackages.master;

import java.util.ArrayList;








import com.vosaye.bunkr.app.StructureActivity;
import com.vosaye.bunkr.app.TOS;
import com.vosaye.bunkr.services.ValidatorService;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;



public class ScrollingTabsView extends HorizontalScrollView implements OnPageChangeListener 
{
	private Context mContext;
	private ViewPager mPager;
	private TabsAdapter mAdapter;
	private LinearLayout mContainer;
	private ArrayList<View> mTabs = new ArrayList<View>();
	private Drawable mDividerDrawable;
	private TabClickListener mClickListener;
	private int mDividerColor = 0xFF99CC00;
	private int mDividerMarginTop = 12;
	private int mDividerMarginBottom = 12;
	private int mDividerWidth = 1;
	
	TOS tos;

	public ScrollingTabsView(Context context) 
	{
		this(context, null);
	}

	public ScrollingTabsView(Context context, AttributeSet attrs) 
	{
		this(context, attrs, 0);
	}
	
	public void setTos(TOS tos){
		this.tos = tos;
	}

	@SuppressWarnings("deprecation")
	public ScrollingTabsView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs);
		this.mContext = context;
		mDividerMarginTop = (int) (getResources().getDisplayMetrics().density * mDividerMarginTop);
		mDividerMarginBottom = (int) (getResources().getDisplayMetrics().density * mDividerMarginBottom);
		mDividerWidth = (int) (getResources().getDisplayMetrics().density * mDividerWidth);

		

		this.setHorizontalScrollBarEnabled(false);
		this.setHorizontalFadingEdgeEnabled(false);

		mContainer = new LinearLayout(context);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mContainer.setLayoutParams(params);
		mContainer.setOrientation(LinearLayout.HORIZONTAL);
		this.addView(mContainer);
	}

	public void setTabClickListener(TabClickListener listener) 
	{
		this.mClickListener = listener;
	}

	public void setAdapter(TabsAdapter adapter) 
	{
		this.mAdapter = adapter;
		if (mPager != null && mAdapter != null)
		{
			initTabs();
		}
	}

	public void setViewPager(ViewPager pager) 
	{
		this.mPager = pager;
			mPager.setOnPageChangeListener(this);
		
		if (mPager != null && mAdapter != null)
		{
			initTabs();
		}
	}

	private void initTabs() 
	{
		mContainer.removeAllViews();
		mTabs.clear();

		if (mAdapter == null)
			return;

		for (int i = 0; i < mPager.getAdapter().getCount(); i++) 
		{
			final int index = i;
			View tab = mAdapter.getView(i);
			mContainer.addView(tab);
			tab.setFocusable(true);
			mTabs.add(tab);

			if (i != mPager.getAdapter().getCount() - 1) 
			{
				mContainer.addView(getSeparator());
			}

			tab.setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					if (mClickListener != null)
					{
						mClickListener.onClick(index);
					}
					
					if (mPager.getCurrentItem() == index)
					{
						selectTab(index);
					}
					else
					{
						
						mPager.setCurrentItem(index);
					}
				}
			});
		}
		selectTab(mPager.getCurrentItem());
	}
	public Activity act;
	@Override
	public void onPageScrollStateChanged(int state) 
	{
		
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) 
	{
		//System.out.println(position+" "+positionOffset+" "+positionOffsetPixels);
		//if(position==1)
			//ValidatorService.FREEFLOW = false;
		//else ValidatorService.FREEFLOW = true;
	}

	@Override
	public void onPageSelected(int position) 
	{
		selectTab(position);
		if(tos!=null){
			tos.invalidateOptionsMenu();
			if(!tos.scheduleDB.exists("select name from subject")&&mPager.getCurrentItem()==1){
				//plz setup subject first
				tos.toast.setText("You didn't enter any subjects, set'em up first!");
				tos.toast.show();
			}else if(!tos.scheduleDB.exists("select name from type")&&mPager.getCurrentItem()==2){
				//plz setup type first
				tos.toast.setText("You didn't enter any types, atleast one needed!");
				tos.toast.show();
			}
			
		
		}
		//if(position==1)
			//ValidatorService.FREEFLOW = true;
		//else ValidatorService.FREEFLOW = false;
		
	}
	//StructureActivity activity;
	
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) 
	{
		super.onLayout(changed, l, t, r, b);
		if (changed)
		{
			selectTab(mPager.getCurrentItem());
			if(tos!=null)
				tos.invalidateOptionsMenu();
		}
	}

	@SuppressWarnings("deprecation")
	private View getSeparator() 
	{
		View v = new View(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mDividerWidth, LayoutParams.FILL_PARENT);
		params.setMargins(0, mDividerMarginTop, 0, mDividerMarginBottom);
		v.setLayoutParams(params);
		if (mDividerDrawable != null)
		{
			v.setBackgroundDrawable(mDividerDrawable);
		}
		else
		{
			v.setBackgroundColor(mDividerColor);
		}
		return v;
	}

	public void selectTab(int position) 
	{
		for (int i = 0, pos = 0; i < mContainer.getChildCount(); i += 2, pos++) 
		{
			View tab = mContainer.getChildAt(i);
			tab.setSelected(pos == position);
		}
		View selectedTab = mContainer.getChildAt(position * 2);

		if (selectedTab != null) 
		{
			final int w = selectedTab.getMeasuredWidth();
			final int l = selectedTab.getLeft();
			final int x = l - this.getWidth() / 2 + w / 2;
			smoothScrollTo(x, this.getScrollY());
		}
	}

	public interface TabClickListener 
	{
		public void onClick(int position);
	}
}
