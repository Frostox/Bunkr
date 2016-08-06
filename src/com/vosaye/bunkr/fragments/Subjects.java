package com.vosaye.bunkr.fragments;

import com.vosaye.bunkr.R;
import com.vosaye.bunkr.customviews.SubjectScrollList;
import com.vosaye.bunkr.events.ScrollListListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
public class Subjects extends Fragment implements ScrollListListener{
	private SubjectScrollList containerx;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		containerx = new SubjectScrollList(this.getActivity(),inflater);
		containerx.setOnDoneListener(this);
		
		if (container == null) 
		{
			return null;
		}
		return (containerx.getView());
	}


	@Override
	public void onDone(View view) {
		// TODO Auto-generated method stub
		
	}

	public void onResume(){
		super.onResume();
		//containerx.updateList();
	}
	
	public void onPause(){
		super.onPause();
		//containerx.closeList();
	}
	
	public void onDestroy(){
		super.onDestroy();
		containerx.closeList();
	}
	@Override
	public void onError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}
}
