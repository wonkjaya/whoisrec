package com.rohman.suseda;



import java.util.Calendar;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main activity, with button to toggle phone calls detection on and off.
 * 
 * @author Moskvichev Andrey V.
 * 
 */

public class MainActivity extends Activity {
	private static final String ALARM_REFRESH_ACTION = "it.trento.alchemiasoft.casagranda.simone.alarmmanagertutorial.ALARM_REFRESH_ACTION";
	private static final int ALARM_CODE = 20; 

	private BroadcastReceiver alarmReceiver;
	private PendingIntent pendingIntent;

	private AlarmManager alarmManager;

	private int sambung = 0;
	private static final int HELLO_ID = 1;

	private static final int RESULT_SETTINGS = 1;
	private boolean detectEnabled;
	SharedPreferences sharedPrefs;
	public StringBuilder builder = new StringBuilder();
	private TextView tv;
	ImageButton bconfig;
	TextView settingsTextView;
	UnderActivity ua;
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy (policy);
        settingsTextView = (TextView) findViewById(R.id.textView3);
        
        settingsTextView.setText(builder.toString());
        setDetectEnabled(true);
        //Toast.makeText(getApplicationContext(), Integer.toString(ServiceRunning()), Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = this.getSharedPreferences("name", MODE_PRIVATE);
        StringBuilder builder=new StringBuilder();	        
        builder.append("Nomor : "+sharedPrefs.getString("prefNoTelp", null));
        //builder.append("\n Key : *****");//+sharedPrefs.getString("Key_Member", null));
        //Toast.makeText(getApplicationContext(), builder, Toast.LENGTH_SHORT).show();
        settingsTextView.setText(builder.toString());
        
		tv = (TextView) findViewById(R.id.textView1);
		bconfig=(ImageButton)findViewById(R.id.imageButton1);
		bconfig.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), "configure", Toast.LENGTH_SHORT).show();
				openOptionsMenu();
			}
		});

		
		Intent iSMS = new Intent(this, UnderActivity.class);
		startActivity(iSMS);
		//Toast.makeText(getApplicationContext(), "dd", Toast.LENGTH_SHORT).show();
		
	}
	
    @Override
	public void onDestroy() {
		super.onDestroy();
		//super.startForeground(R, notification)
		
		setDetectEnabled(true);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private void setDetectEnabled(boolean enable) {
		detectEnabled = enable;

		Intent intent = new Intent(this, CallDetectService.class);
		if (detectEnabled==true) {
			// start detect service
			stopService(intent);
			startService(intent);

		} else {
			// stop detect service
			stopService(intent);
			tv.setText("Whois-rec Is Nonactive!!");
		}
	}

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	 
	        case R.id.menu_settings:
	            Intent i = new Intent(this, UserSettingActivity.class);
	            startActivityForResult(i, RESULT_SETTINGS);
	            break;
	 
	        }
	 
	        return true;
	    }
	 
	    @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	        super.onActivityResult(requestCode, resultCode, data);
	 
	        switch (requestCode) {
	        case RESULT_SETTINGS:
	            showUserSettings();
	            break;
	 
	        }
	 
	    }
	 
	    private void showUserSettings() {
	    	SharedPreferences sharedPrefs = PreferenceManager
	                .getDefaultSharedPreferences(this);
	        
	        Long tsLong = System.currentTimeMillis();
	        String ts = tsLong.toString();
	        StringBuilder builder=new StringBuilder();	        
	        builder.append("Nomor : "+sharedPrefs.getString("prefNoTelp", null));
	        //builder.append("\n Key : *****");
	        settingsTextView.setText(builder.toString());
	    }

	    
	    public class BootUpReceiver extends BroadcastReceiver{
	    	
	    	@Override
	    	public void onReceive(Context context, Intent intent) {
	    		Intent i = new Intent(context, CallDetectService.class);  
	    		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    		context.startActivity(i);  
	    	}
	    }
	    
		@SuppressLint("HandlerLeak")
		private Handler myHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ALARM_CODE:
					if (sambung ==3){
						sambung = 0;
					} else {
						sambung +=1;
					}
					
					String str = ". ";
					int i =1;
					String cDot = "";
					for (i=1;i<=sambung;i++){
						cDot += str;
					}
					
					TextView tStatus = (TextView) findViewById(R.id.textView3);
					
			        tStatus.setText("Tersambung " +cDot);
			        
					break;
				default:
					break;
				}
			}
		};
		
		protected void awal() {
			//super.onStart();
			// We get the AlarmManager
			alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
			// We prepare the pendingIntent for the AlarmManager
			Intent intent = new Intent(ALARM_REFRESH_ACTION);
			pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			// We create and register a broadcast receiver for alarms
			alarmReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// We increment received alarms
					// We notify to handler the arrive of alarm
					Message msg = myHandler.obtainMessage(ALARM_CODE, intent);
					myHandler.sendMessage(msg);
				}
			};
			// We register dataReceiver to listen ALARM_REFRESH_ACTION
			IntentFilter filter = new IntentFilter(ALARM_REFRESH_ACTION);
			registerReceiver(alarmReceiver, filter);
		}
		
		public void startRepeating(View v) {
			// We get value for repeating alarm
			
			int startTime = 3000;
			long intervals = 3000;
			// We have to register to AlarmManager
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(System.currentTimeMillis());
			calendar.add(Calendar.MILLISECOND, startTime);
			// We set a repeating alarm
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar
					.getTimeInMillis(), intervals, pendingIntent);
		}
		
		
}
