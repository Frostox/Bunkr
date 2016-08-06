package com.vosaye.bunkr.fragments;


import com.vosaye.bunkr.R;
import com.vosaye.bunkr.app.TOS;
import com.vosaye.bunkr.customviews.TermSetter;
import com.vosaye.bunkr.events.ScrollListListener;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class Terms  extends Fragment implements ScrollListListener{
	
	
	boolean isTos = false;
	TOS tos;
	TermSetter term;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		if(term==null)
		term = new TermSetter(this.getActivity(), inflater);
		if(isTos) {term.submit.setText("Set and Proceed"); term.setIsTos(isTos);}
		term.setOnDoneListener(this);
		
		
		if (container == null) 
		{
			return null;
		}
		return term.getView();
	}
	
	public void setDisabled(){
		term.setDisabled();
	}


	public void setTOS(TOS tos){
		this.tos = tos;
		isTos = true;
		
		
	}
	Handler hdlr = new Handler(Looper.getMainLooper());
	Runnable r;
	@Override
	public void onDone(View view) {
		// TODO Auto-generated method stub
		if(tos!=null){
			
			
			
			tos.onDone();
			
		}
	}

	@Override
	public void onError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}
}
