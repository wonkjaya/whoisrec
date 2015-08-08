package com.rohman.suseda;


import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Call detect service. 
 * This service is needed, because MainActivity can lost it's focus,
 * and calls will not be detected.
 * 
 * @author Moskvichev Andrey V.
 *
 */
public class CallDetectService extends Service {
	private CallHelper callHelper;
 
    public CallDetectService() {
    }

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Notification notification = new Notification(R.drawable.ic_launcher, "whois recorder is activated", System.currentTimeMillis());

		Intent main = new Intent(this, MainActivity.class);
		main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, main,	PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(this, "whois", "Connected...", pendingIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT |	Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR;

		startForeground(2, notification);

		callHelper = new CallHelper(this);
		
		int res = super.onStartCommand(intent, flags, startId);
		callHelper.start();
		return START_STICKY;
	}
	
    @Override
	public void onDestroy() {
		callHelper.stop();
		
	}

	@Override
    public IBinder onBind(Intent intent) {
		// not supporting binding
    	return null;
    }
}
