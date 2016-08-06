package com.vosaye.bunkr.fragments;


import com.vosaye.bunkr.customviews.StructureEditter;
import com.vosaye.bunkr.events.ScrollListListener;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StructureEditterFragment extends Fragment{
	
	String pseudo = "";
	StructureEditter str;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		pseudo = this.getArguments().getString("pseudoStructureName");
		str = new StructureEditter(this.getActivity(),pseudo);
		return str.getView();
		
	}
	public void setSaved(boolean saved){
		str.setSaved(saved);
	}
	
	public void updateList(){
		str.updateList();
	}
	
	public void closeList(){
		str.closeList();
	}
	
	public boolean getSaved(){
		return str.getSaved();
	}
	public void notifyData(){
		str.notifyData();
	}
}
