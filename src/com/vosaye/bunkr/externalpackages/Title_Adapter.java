package com.vosaye.bunkr.externalpackages;

import java.util.Vector;

import com.vosaye.bunkr.R;
import com.vosaye.bunkr.customviews.StructureEditter;
import com.vosaye.bunkr.externalpackages.master.TitleProvider;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;



public class Title_Adapter extends BaseAdapter implements TitleProvider
{
	private static final int VIEW1 = 0;
	private static final int VIEW2 = 1;
	private static final int VIEW3 = 2;
	private static final int VIEW4 = 3;
	private static final int VIEW5 = 4;
	private static final int VIEW6 = 5;
	private static final int VIEW7 = 6;
	private static final int VIEW_MAX_COUNT = VIEW7 + 1;
	private final String[] names = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday","Saturday","Sunday"};
	
	public static int SIDE_BUFFER = VIEW_MAX_COUNT - 1;
	public static int SIDE_BUFFER_GLOBAL = VIEW_MAX_COUNT - 2;
	Activity context;
	private LayoutInflater mInflater;
	//public Vector<String> pseudoForDays = new Vector<String>();
	public String[] pseudoForDays = new String[7];
	public Title_Adapter(Activity context) 
	{
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
	}

	@Override
	public int getItemViewType(int position) 
	{
		return position;
	}

	@Override
	public int getViewTypeCount() 
	{
		return VIEW_MAX_COUNT;
	}

	@Override
	public int getCount() 
	{
		return VIEW_MAX_COUNT;
	}

	@Override
	public Object getItem(int position) 
	{
		return position;
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}
	
	public void closeLists(){
		for(int i=0; i<v.size(); i++){
			if(v.elementAt(i)!=null)
			v.elementAt(i).closeList();
		}
	}
	
	public void updateLists(){
		for(int i=0; i<v.size(); i++){
			if(v.elementAt(i)!=null)
			v.elementAt(i).updateList();
		}
	}
	
	
	Vector<StructureEditter> v = new Vector<StructureEditter>();
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		int view = getItemViewType(position);
		if (convertView == null) 
		{
			StructureEditter str;
			switch (view) 
			{
				case VIEW1:
					
					str = new StructureEditter(context,"");
					str.onCreate();
					v.add(str);
					convertView = str.getView();
					pseudoForDays[1] = str.getPseudoStructureName();
					//convertView = mInflater.inflate(R.layout.demo_1, null);
					break;
					
				case VIEW2:
					str = new StructureEditter(context,"");
					str.onCreate();
					v.add(str);
					convertView = str.getView();
					pseudoForDays[2] = str.getPseudoStructureName();
					//convertView = mInflater.inflate(R.layout.demo_1, null);
					break;
					
				case VIEW3:
					str = new StructureEditter(context,"");
					str.onCreate();
					v.add(str);
					convertView = str.getView();
					pseudoForDays[3] = str.getPseudoStructureName();
					//convertView = mInflater.inflate(R.layout.demo_1, null);
					break;
					
				case VIEW4:
					str = new StructureEditter(context,"");
					str.onCreate();
					v.add(str);
					convertView = str.getView();
					pseudoForDays[4] = str.getPseudoStructureName();
					//convertView = mInflater.inflate(R.layout.demo_1, null);
					break;
					
				case VIEW5:
					str = new StructureEditter(context,"");
					str.onCreate();
					v.add(str);
					convertView = str.getView();
					pseudoForDays[5] = str.getPseudoStructureName();
					//convertView = mInflater.inflate(R.layout.demo_1, null);
					break;
					
				case VIEW6:
					str = new StructureEditter(context,"");
					str.onCreate();
					v.add(str);
					convertView = str.getView();
					pseudoForDays[6] = str.getPseudoStructureName();
					//convertView = mInflater.inflate(R.layout.demo_1, null);
					break;
					
				case VIEW7:
					str = new StructureEditter(context,"");
					str.onCreate();
					v.add(str);
					convertView = str.getView();
					pseudoForDays[0] = str.getPseudoStructureName();
					//convertView = mInflater.inflate(R.layout.demo_1, null);
					break;
			}
		}
		return convertView;
	}

	public String getTitle(int position) 
	{
		return names[position];
	}
}
