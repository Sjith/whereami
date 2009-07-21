package org.addhen.whereami;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.gsm.SmsManager;
import android.util.Log;

public class LocationService extends Service {
	private NotificationManager mNM;
	LocationManager locationManager;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override  
	public void onCreate() {  
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
	}
	
	public String getAddr(Location location){
		String slocation = "unknown";
		Geocoder gc = new Geocoder(this, Locale.getDefault());
		Address addr;
		Double lat = location.getLatitude();
		Double lon = location.getLongitude();
		try{
			List<Address> myList = gc.getFromLocation(lat, lon, 1);
			addr = myList.get(0);
		}catch(Exception e){
			Log.e("PhoneFinder","LocationService Exception", e );
			return slocation;
		}		

		slocation = addr.getLocality();
		slocation += "|Lat:" + String.format("%f",lat);
		slocation += "|Lon:" + String.format("%f",lon);
		if( addr.getThoroughfare() != null )
			slocation += "|" + addr.getThoroughfare();
		if( addr.getFeatureName() != null )
			slocation += "|" + addr.getFeatureName();
		if( addr.getAdminArea() != null )
			slocation += "|" + addr.getAdminArea();
		if( addr.getSubAdminArea() != null )
			slocation += "|" + addr.getSubAdminArea();
		if( addr.getPostalCode() != null )
			slocation += "|" + addr.getPostalCode();
		slocation += "\n" + String.format("http://maps.google.com/maps?q=%f", lat) + "%20" + String.format("%f", lon);
		
		return slocation;
	}
	/*
	 * http://www.maximyudin.com/2008/12/07/android/vklyuchenievyklyuchenie-gps-na-g1-programmno/
	 */
	private boolean getGPSStatus()
	{
		String allowedLocationProviders =
			Settings.System.getString(getContentResolver(),
			Settings.System.LOCATION_PROVIDERS_ALLOWED);
	 
		if (allowedLocationProviders == null) {
			allowedLocationProviders = "";
		}
	 
		return allowedLocationProviders.contains(LocationManager.GPS_PROVIDER);
	}	
	 
	private void setGPSStatus(boolean pNewGPSStatus)
	{
		String allowedLocationProviders =
			Settings.System.getString(getContentResolver(),
			Settings.System.LOCATION_PROVIDERS_ALLOWED);
	 
		if (allowedLocationProviders == null) {
			allowedLocationProviders = "";
		}
		
		boolean networkProviderStatus =
			allowedLocationProviders.contains(LocationManager.NETWORK_PROVIDER);
	 
		allowedLocationProviders = "";
		if (networkProviderStatus == true) {
			allowedLocationProviders += LocationManager.NETWORK_PROVIDER;
		}
		if (pNewGPSStatus == true) {
			allowedLocationProviders += "," + LocationManager.GPS_PROVIDER;
		}	
	 
		Settings.System.putString(getContentResolver(),
			Settings.System.LOCATION_PROVIDERS_ALLOWED, allowedLocationProviders);	   
	 
		try
		{
			Method m =
				locationManager.getClass().getMethod("updateProviders", new Class[] {});
			m.setAccessible(true);
			m.invoke(locationManager, new Object[]{});
		}
		catch(Exception e)
		{
			Log.e("GetClassName", e.getClass().getName());
		}
		return;
	}
	
	public void onStart(final Intent intent, int startId) {
        super.onStart(intent, startId);
        
        if( ! getGPSStatus() )
        	setGPSStatus(true);

        LocationListener locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub
				PendingIntent dummyEvent = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent("com.yuki.phonefinder.IGNORE_ME"), 0);

				SmsManager.getDefault().sendTextMessage(intent.getExtras().getString("dest"), null, getAddr(location), dummyEvent, dummyEvent);
				
				locationManager.removeUpdates(this);
			}
            
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub

			}

			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
   
		Location location = locationManager.getLastKnownLocation("gps");
		if (location == null)
			location = locationManager.getLastKnownLocation("network");

		PendingIntent dummyEvent = PendingIntent.getBroadcast(this, 0, new Intent("com.yuki.phonefinder.IGNORE_ME"), 0);
		
		SmsManager.getDefault().sendTextMessage(intent.getExtras().getString("dest"), null, getAddr(location), dummyEvent, dummyEvent);
	}
	
	public void onDestroy(){
		
	}

}
