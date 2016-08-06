package com.vosaye.bunkr.events;

import android.view.View;

public interface ScrollListListener {
	public void onDone(View view);
	public void onError(String errorMessage);
}
