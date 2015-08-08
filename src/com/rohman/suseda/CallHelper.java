package com.rohman.suseda;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Helper class to detect incoming and outgoing calls.
 * 
 * @author Moskvichev Andrey V.
 * 
 */
public class CallHelper {
	private Boolean Recording = false;
	private static final String TAG = "SoundRecordingDemo";
	MediaRecorder recorder;// mrec = new MediaRecorder();

	File audiofile = null;
	File sampleDir = Environment.getExternalStorageDirectory();
	private String timeRing;
	private String timeStart;
	private String timeStop;
	private String fromNumber;
	private boolean call;
	private String nomor;
	int serverResponseCode = 0;
	int i;
	private String mFileName;
	String uploadFileName = null;
	HttpURLConnection connection = null;
	DataOutputStream outputStream = null;
	DataInputStream inputStream = null;
	String pathToOurFile = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/audio2.mp3";//
	//String urlServer = "http://ss.dm:88/adm/index.php/service/log_call_submit";// "http://192.168.100.215:88/upload/upload_file.php";
	String lineEnd = "\r\n";
	String twoHyphens = "--";
	String boundary = "*****";

	String userpr;
	String keypr;

	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 1 * 1024 * 1024;


	private String getHost() {
		String host;
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		host = sharedPrefs.getString("prefHost", "0");
		return "http://"+host+"/";
	}
	/*
	 * Listener to detect incoming calls.
	 */
	private class CallStateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat names = new SimpleDateFormat("mmss");
			String cu = sdf.format(new Date());
			String second = names.format(new Date());
			SharedPreferences a = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			userpr = a.getString("prefNoTelp", null);
			keypr = a.getString("prefKey", null);
			switch (state) {

			case TelephonyManager.CALL_STATE_IDLE:
				if (call == true) {
					call = false;
					// Toast.makeText(ctx, Recording.toString(),
					// Toast.LENGTH_SHORT).show();
					// stopRecording();
					timeStop = cu;
					if (Recording == true) {
						try {
							stopRecording();
							timeStop = cu;
							Toast.makeText(ctx, "stoping record...",
									Toast.LENGTH_SHORT).show();
							Recording = false;
						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(ctx, "Failed stoping recording...",
									Toast.LENGTH_SHORT).show();
						}
					}
					if (uploadFileName == null) {
						Toast.makeText(ctx, "please wait...",
								Toast.LENGTH_SHORT).show();
						postData();
					} else {
						Toast.makeText(ctx, "please wait...",
								Toast.LENGTH_SHORT).show();
						uploadFile(uploadFileName);// path
					}
					setnull();

				} else {
					// Toast.makeText(ctx, "tak ada",
					// Toast.LENGTH_SHORT).show();
					Log.i("Info", "tidak ada panggilan masuk");
				}
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				timeRing = cu;
				// Toast.makeText(ctx, Recording.toString(), Toast.LENGTH_SHORT)
				// .show();
				call = true;
				nomor = incomingNumber;
				getnama(nomor);
				Recording = false;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				if (Recording == true) {
					try {
						stopRecording();
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
				i++;

				try {
					mFileName = createFile("DCIM").toString();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					Toast.makeText(ctx, "tidak bisa buat file",
							Toast.LENGTH_SHORT).show();
				}
				uploadFileName = mFileName;
				recorder = new MediaRecorder();

				try {
					recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
					recorder.setOutputFile(mFileName);
					recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
					recorder.prepare();
					recorder.start();
					Recording = true;
					call = true;
					timeStart = cu;
					Toast.makeText(ctx, "Recording...", Toast.LENGTH_SHORT)
							.show();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					Toast.makeText(ctx, "IllegalStateException called",
							Toast.LENGTH_LONG).show();

				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(ctx, "prepare() failed", Toast.LENGTH_LONG)
							.show();
				}
				break;

			}

		}

		private File createFile(String lokasi) throws IOException {
			// Create an image file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
					.format(new Date());
			String FileName = timeStamp;
			File file = File.createTempFile(FileName, ".mp3",
					getAlbumDir(lokasi));
			mFileName = file.getAbsolutePath();
			//Toast.makeText(ctx, mFileName, Toast.LENGTH_SHORT).show();
			return file;
		}

		private File getAlbumDir(String lokasi) {
			File storageDir = null;
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				if (lokasi.equals("DCIM")) {
					storageDir = new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
							"RECORDDCIM");
				} else {
					storageDir = new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
							"RECORD");
				}

				if (storageDir != null) {
					if (!storageDir.mkdirs()) {
						if (!storageDir.exists()) {
							Log.d(TAG,
									"failed to create directory CameraSample");
							return null;
						}
					}
				}
			} else {
				Log.v("tag", "External storage is not mounted READ/WRITE.");
			}
			return storageDir;
		}

		protected void stopRecording() {
			try {
				recorder.stop();
			} catch (Exception e) {
				// TODO: handle exception
				Toast.makeText(ctx, "failed stop record", Toast.LENGTH_SHORT)
						.show();
			}
			recorder.release();
			recorder = null;
		}

		public void setnull() {
			timeStop = null;
			timeRing = null;
			timeStart = null;
			uploadFileName = null;
		}
	}

	/**
	 * Broadcast receiver to detect the outgoing calls. duration
	 */
	public class OutgoingReceiver extends BroadcastReceiver {
		public OutgoingReceiver() {
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
			Toast.makeText(ctx, "You Calling: " + number, Toast.LENGTH_LONG)
					.show();
			nomor=number;
		}

		String sourceFileUri;
	}

	private Context ctx;
	private TelephonyManager tm;
	private CallStateListener callStateListener;

	private OutgoingReceiver outgoingReceiver;

	public CallHelper(Context ctx) {
		this.ctx = ctx;

		callStateListener = new CallStateListener();
		outgoingReceiver = new OutgoingReceiver();
	}

	/**
	 * Start calls detection.
	 */
	public void start() {
		tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		tm.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		//Toast.makeText(ctx, "Starting Detection", Toast.LENGTH_SHORT).show();
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_NEW_OUTGOING_CALL);
		ctx.registerReceiver(outgoingReceiver, intentFilter);
	}

	/**
	 * Stop calls detection.
	 */
	public void stop() {
		//Toast.makeText(ctx, "Stoping Detection", Toast.LENGTH_SHORT).show();
		tm.listen(callStateListener, PhoneStateListener.LISTEN_NONE);
		ctx.unregisterReceiver(outgoingReceiver);
		Log.i("endcall", "call ending");
		//start();
	}

	public void getnama(String nomor) {
		NodeList outbox = getOutbox();
		int jmlOutbox = outbox.getLength();
		String nama1 = "";
		String nama = "";
		String sms = "";
		for (int i = 0; i < jmlOutbox; i++) {
			nama1 = (outbox.item(i).getChildNodes().item(2).getFirstChild()
					.getNodeValue())
					+ "\n("
					+ (outbox.item(i).getChildNodes().item(4).getFirstChild()
							.getNodeValue())
					+ ") "
					+ (outbox.item(i).getChildNodes().item(3).getFirstChild()
							.getNodeValue());
			nama = nama1;
			fromNumber = nama;
			for (int a = 1; a <= 1; a++) {
				final Toast toast = Toast.makeText(ctx, "call in: " + nama + "\n" + a,
						Toast.LENGTH_LONG);
				toast.show();
			}
		}
	}

	public NodeList getNodeFromXML(String url) {
		URL URL = null;
		InputStream stream = null;
		DocumentBuilder builder = null;
		Document document = null;
		org.w3c.dom.Element root;
		NodeList nodeList;
		try {
			URL = new URL(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // The URL of the site you posted goes here.
		try {
			stream = URL.openStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Set up and initialize the document.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		try {
			builder = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			document = builder.parse(stream);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		document.getDocumentElement().normalize();

		root = document.getDocumentElement();
		nodeList = root.getChildNodes();
		return nodeList;
	}

	// Ambil outbox //
	public NodeList getOutbox() {
		String url = getHost()+"adm/index.php/service/get_name/"
				+ nomor;
		Log.i("url",url);
		NodeList nodeList = getNodeFromXML(url);
		return nodeList;
	}

	public void postData() {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				getHost()+"adm/index.php/service/log_call_submit");

		try {
			// Add your data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			// Intent a= new Intent(csnumber);

			if (nomor == "0") {

			} else {

				if (timeRing == null) {
					timeRing = timeStart;
				}
				//Toast.makeText(ctx, nomor, Toast.LENGTH_SHORT).show();
				nameValuePairs.add(new BasicNameValuePair("call_center_number",
						userpr));
				nameValuePairs.add(new BasicNameValuePair("key", keypr));/**/
				nameValuePairs
						.add(new BasicNameValuePair("from_number", nomor));
				nameValuePairs.add(new BasicNameValuePair("call_ringing",
						timeRing));
				nameValuePairs.add(new BasicNameValuePair("call_start",
						timeStart));
				nameValuePairs
						.add(new BasicNameValuePair("call_end", timeStop));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				timeStop = null;
				timeRing = null;
				timeStart = null;
				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);
				Toast.makeText(ctx, "Saving to Server...", Toast.LENGTH_SHORT)
						.show();
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Toast.makeText(ctx, "error connecting to server...",
					Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Toast.makeText(ctx, "Internal error detected...",
					Toast.LENGTH_SHORT).show();
		}
	}

	public int uploadFile(String sourceFileUri) {

		String fileName = sourceFileUri;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);

		if (!sourceFile.isFile()) {
			return 0;
		} else {
			try {
			//	Toast.makeText(ctx, nomor+"\n"+userpr+"\n"+keypr, Toast.LENGTH_SHORT).show();
				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(
						sourceFile);
				URL url = new URL(getHost()+"adm/index.php/service/log_call_submit");

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("frecording", fileName);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"call_center_number\""
						+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes(userpr + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"key\""
						+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes(keypr + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"from_number\""
						+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes(nomor + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"call_ringing\""
						+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes(timeRing + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"call_start\""
						+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes(timeStart + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"call_end\""
						+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes(timeStop + lineEnd);

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: from-data; name=\"frecording\";filename=\""
						+ fileName + "\"" + lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				while (bytesRead > 0) {

					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				String serverResponseMessage = conn.getResponseMessage();

				Log.i("uploadFile", "HTTP Response is : "
						+ serverResponseMessage + ": " + serverResponseCode);

				if (serverResponseCode == 200) {
					Toast.makeText(ctx, serverResponseMessage,
							Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(ctx, serverResponseMessage,
							Toast.LENGTH_SHORT).show();
				}

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();
				// Toast.makeText(ctx, "berhasil", Toast.LENGTH_SHORT).show();

			} catch (MalformedURLException ex) {
				Toast.makeText(ctx, "error mall", Toast.LENGTH_SHORT).show();
				ex.printStackTrace();
				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {
				Toast.makeText(ctx, e.getMessage(), Toast.LENGTH_SHORT).show();
				e.printStackTrace();
				Log.e("Upload file to server Exception",
						"Exception : " + e.getMessage(), e);
			}
			return serverResponseCode;

		} // End else block
	}

}
