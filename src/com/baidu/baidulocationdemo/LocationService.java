package com.baidu.baidulocationdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service{
	private static final String TAG = "LocationService" ;  
    public static final String ACTION = "com.baidu.baidulocationdemo.LocationService"; 
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.v(TAG, "LocationService onBind");  
        return null;  
	}
	@Override  
    public void onCreate() {  
        Log.v(TAG, "LocationService onCreate");  
        super.onCreate();  
    }  
      
    @Override  
    public void onStart(Intent intent, int startId) {  
        Log.v(TAG, "LocationService onStart");  
        super.onStart(intent, startId);  
    }  
      
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        Log.v(TAG, "LocationService onStartCommand");  
        return super.onStartCommand(intent, flags, startId);  
    }  
	
}
