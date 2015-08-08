package com.rohman.suseda;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSReciever extends BroadcastReceiver {
	private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String ACTIVITY_SERVICE = null;
    private Context mContext;
    private Intent mIntent;

    // Retrieve SMS
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mIntent = intent;
//Toast.makeText(mContext, "test", Toast.LENGTH_SHORT).show();
        String action = intent.getAction();

        if(action.equals(ACTION_SMS_RECEIVED)){
            String address, str = "";
            int contactId = -1;
            //Toast.makeText(mContext, "log", Toast.LENGTH_SHORT).show();

            SmsMessage[] msgs = getMessagesFromIntent(mIntent);
            if (msgs != null) {
                for (int i = 0; i < msgs.length; i++) {
                	address = msgs[i].getOriginatingAddress();
                	long tgl = msgs[i].getTimestampMillis();
                    //contactId = ContactsUtils.getContactId(mContext, address, "address");
                    str += msgs[i].getMessageBody().toString();
                    str += "\n";
                    Toast.makeText(mContext, "Incoming Message from \n" + address, Toast.LENGTH_SHORT).show();
                    //suseda.SMSExecute();
                }
            }

            if(contactId != -1){
                showNotification(contactId, str);
            }
            Intent i = new Intent(context, UnderActivity.class);  
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i); 
            
        }
        

    }

    public static SmsMessage[] getMessagesFromIntent(Intent intent) {
        Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
        byte[][] pduObjs = new byte[messages.length][];

        for (int i = 0; i < messages.length; i++) {
            pduObjs[i] = (byte[]) messages[i];
        }
        byte[][] pdus = new byte[pduObjs.length][];
        int pduCount = pdus.length;
        SmsMessage[] msgs = new SmsMessage[pduCount];
        for (int i = 0; i < pduCount; i++) {
            pdus[i] = pduObjs[i];
            msgs[i] = SmsMessage.createFromPdu(pdus[i]);
        }
        return msgs;
    }
	
    /**
    * The notification is the icon and associated expanded entry in the status
    * bar.
    */
    protected void showNotification(int contactId, String message) {
        //Display notification...
    }
	
}

