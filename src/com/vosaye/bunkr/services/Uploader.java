package com.vosaye.bunkr.services;

import com.vosaye.bunkr.BunKar;
import com.vosaye.bunkr.R;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class Uploader  extends IntentService{

	public NotificationManager notifyManager;
	public NotificationCompat.Builder notifyBuilder;
	public Notification notification;
	
	public Uploader(String name) {
		super(name);
	}
	
	public Uploader() {
		super("com.vosaye.bunkr.services.Uploader");
	}
	
	
	@Override
	protected void onHandleIntent(Intent intent) {
		BunKar bnkr = (BunKar) this.getApplication();
		notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notifyBuilder = new NotificationCompat.Builder(this);
		notifyBuilder.setContentTitle("Uploading "+bnkr.name)
		    .setContentText("This may take a few minutes !")
		    .setSmallIcon(R.drawable.noticoninfo);
		notification = notifyBuilder.build();
		
		
		Intent resultIntent = new Intent();
		PendingIntent resultPendingIntent =
		    PendingIntent.getActivity(
		    this,
		    0,
		    resultIntent,
		    PendingIntent.FLAG_UPDATE_CURRENT
		);
		
		notifyBuilder.setContentText("Uploading "+bnkr.name);
		notifyBuilder.setContentIntent(resultPendingIntent);
		notification = notifyBuilder.build();
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		notifyManager.notify(-1, notification);
		
		
		
		bnkr.uploadDbase();
		
		notifyManager.cancel(-1);
		Toast.makeText(this, ""+bnkr.name+" successfully uploaded to cloud", Toast.LENGTH_LONG).show();
	}

}
