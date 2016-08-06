package com.vosaye.bunkr.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vosaye.bunkr.R;
import com.vosaye.bunkr.events.ScrollListListener;

public class StatsDay   extends Fragment implements ScrollListListener{

	@Override
	public void onDone(View view) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}
	private TextView title, sub, info;
	private ImageView image;
	
	private String titletext = "", subtext = "", infotext = "";
	private int imageid = R.drawable.here;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		if (container == null) 
		{
			return null;
		}
		View view = (RelativeLayout) inflater.inflate(R.layout.help_image, container, false);
		info = (TextView) view.findViewById(R.id.help_image_textView1);
		info.setText(infotext);
		sub = (TextView) view.findViewById(R.id.help_image_textView2);
		sub.setText(subtext);
		title = (TextView) view.findViewById(R.id.help_image_textView3);
		title.setText(titletext);
		
		image = (ImageView) view.findViewById(R.id.help_image_imageView1);
		image.setImageDrawable(this.getResources().getDrawable(imageid));
		
		
		return view;
	}
	
	public void setContent(int id, String title, String sub, String info){
		titletext = title;
		subtext = sub;
		infotext = info;
		imageid = id;
	}

}
