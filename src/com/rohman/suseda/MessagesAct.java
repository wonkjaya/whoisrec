package com.rohman.suseda;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class MessagesAct extends Service {
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public Runnable CheckRun = new Runnable() {

		public void run() {
			SMSCheck SMS = new SMSCheck();
			SMS.execute();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		CheckRun.run();
		return START_STICKY;
	}

	public class SMSCheck extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			//==Log.d("tag", "********--**********");
			String urlService = getUrlService();
			// hapus inbox //
			deleteMessages();
			return null;
		}
		
		private String getHost() {
			String host;
			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			host = sharedPrefs.getString("prefHost", "0");
			return "http://"+host+"/";
		}

		private String getUrlService() {

			String url = getHost()+"adm/index.php/service/";
			String urlService = "";

			URL requestURL = null;

			int respon = -1;
			try {
				requestURL = new URL(url);
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			HttpURLConnection connection = null;
			try {
				connection = (HttpURLConnection) requestURL.openConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				respon = connection.getResponseCode();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (respon == -1) {
				urlService = "";
			} else {
				urlService = url;
			}

			return urlService;
		}

		public void executeHTML(String url) {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			HttpResponse response = null;
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

		public void deleteMessages() {
			// delete inbox yang sudah terbaca dan lebih dari 1 jam //
			//String url = null;
			String id;
			//Boolean read;
			Long jamSekarang = System.currentTimeMillis();
			Long maxSync = jamSekarang - 3600000;//
			//Long tgl = null;
			//==Log.d("tag","=========deleting Message==========");
			Cursor cur = getContentResolver().query(
					Uri.parse("content://sms/"), null,
					"date <= " + maxSync.toString() + " and read = 1", null,
					null);
			while (cur.moveToNext()) {
				id = cur.getString(cur.getColumnIndexOrThrow("_id"));
				getContentResolver().delete(Uri.parse("content://sms/" + id),
						null, null);
			}
		}
	}
}
