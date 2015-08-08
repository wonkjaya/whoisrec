package com.rohman.suseda;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class UnderActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int smsstate=ServiceSMS();
		if (smsstate==1){
			Log.d("tag","SMS state Active");
		}else{
			Log.d("tag","SMS state notActive");
			Intent iSMS2 = new Intent(this, SMS.class);
			PendingIntent piSMS2 = PendingIntent.getService(this, 0, iSMS2,
					PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager2 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			alarmManager2.cancel(piSMS2);
			alarmManager2.setRepeating(AlarmManager.RTC_WAKEUP,
					System.currentTimeMillis(), 5000, piSMS2);
		}

		//=========================================================
		finish();
	}
	
	private int ServiceSMS() {
	    ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if ("com.rohman.suseda.SMS".equals(service.service.getClassName())) {
	            return 1;
	        }
	    }
	    return 0;
	}	
	
}
