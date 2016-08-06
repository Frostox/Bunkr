package com.vosaye.bunkr.externalpackages.master;

import com.vosaye.bunkr.externalpackages.master.ViewFlow_Master.ViewSwitchListener;



public interface FlowIndicator extends ViewSwitchListener 
{
	public void setViewFlow(ViewFlow_Master view);
	public void onScrolled(int h, int v, int oldh, int oldv);
}
