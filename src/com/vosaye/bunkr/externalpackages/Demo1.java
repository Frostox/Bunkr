package com.vosaye.bunkr.externalpackages;

import com.vosaye.bunkr.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;



public class Demo1 extends Fragment 
{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		if (container == null) 
		{
			return null;
		}
		return (RelativeLayout) inflater.inflate(R.layout.activity_main, container, false);
	}
}
