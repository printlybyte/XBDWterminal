package com.baidu.baidulocationdemo;

import android.app.Application;
import android.app.Service;
import android.os.Environment;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.tools.CrashHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

;

/**
 * 注： 最后一项数据方向，只有在GPS开启的状态下才能获取，其他两种定位方式（网络定位，基站定位）无法获取，
 * 传输时如果方向无法获取时，源数据为-1，传输数据为000
 * 
 * 在三种定位模式中，其中网络定位包括wifi定位和基站定位两种方式，三种定位方式各有各自能获取到的数据
 */
public class LocationApplication extends Application {
	public static double pi = 3.1415926535897932384626;
	public static double a = 6378245.0;
	public static double ee = 0.00669342162296594323;
	public String msgonline, msgSMS, imei, LocType, Latitude, Longitude;
	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;
	public Vibrator mVibrator;
	private String TAG="LocationApplication";
	@Override
	public void onCreate() {
		super.onCreate();
		// 初始化捕获异常的
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(getApplicationContext());
		
		
		
		//判断是否存在配置文件，不存在的话就拷贝到SD卡
		File configFile=new File(Environment.getExternalStorageDirectory()+"/netconfig.properties");
		if (!configFile.exists()) {
			try {
				Log.i(TAG, "准备拷贝文件；");
				copyConfigFileToSDCard(Environment.getExternalStorageDirectory()+"/netconfig.properties");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		mVibrator = (Vibrator) getApplicationContext().getSystemService(
				Service.VIBRATOR_SERVICE);
	}

	/**
	 * 实现实时位置回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// 获取手机IMEI号码
			TelephonyManager mTm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			imei = mTm.getDeviceId();
			
			
			
			// 获取定位方式
			LocType = "";
			if (location.getLocType() == BDLocation.TypeGpsLocation) {
				LocType = "4"; // GPS定位
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
				LocType = "6";// 网络定位
			} else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
				LocType = "0";// 离线定位（基站定位）
			}

			Gps gps = transform(location.getLatitude(), location.getLongitude());
			// 获取纬度
			Latitude = String
					.valueOf(
							Math.round((location.getLatitude() * 2 - gps
									.getWgLat()) * 360000)).replace("-", "");
			String LatitudeSMS = String.valueOf(location.getLatitude());
			// 经度
			Longitude = String
					.valueOf(Math.round((location.getLongitude() * 2 - gps
							.getWgLon()) * 360000));
			String LongitudeSMS = String.valueOf(location.getLongitude());
			// 获取速度
			String Speed = String.format("%04d",
					Math.round(Math.abs(location.getSpeed())));
			// 获取方向，百度内部定义，如果没有获取到方向，则方向的值为-1
			String Direction = String.format("%03d",
					Math.round(Math.abs(location.getDirection())));
			if (Direction.equals("001")) {
				Direction = "000";
			}
			// 整合所有数据编译成传输所需的字符串
			Log.d("经度纬度速度和方向", Longitude + "--" + Latitude + "--" + Speed
					+ "--" + Direction + "--");
			System.out.println("实际经纬度" + location.getLatitude() + "-------"
					+ location.getLongitude());
			msgonline = LocType + Longitude + Latitude + Speed + Direction
					+ "|";
			msgSMS = ";" + LocType + ";" + LongitudeSMS + ";" + LatitudeSMS
					+ ";" + Speed + ";" + Direction + ";" + ";" + "|";
		}
	}

	public static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	public static Gps transform(double lat, double lon) {
		if (outOfChina(lat, lon)) {
			return new Gps(lat, lon);
		}
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		return new Gps(mgLat, mgLon);
	}

	public static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
				* pi)) * 2.0 / 3.0;
		return ret;
	}

	public static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}

	/**
	 * @param strOutFileName
	 * @throws IOException 拷贝配置文件到SD卡
	 */
	private void copyConfigFileToSDCard(String strOutFileName) throws IOException {
		InputStream myInput;
		OutputStream myOutput = new FileOutputStream(strOutFileName);
		myInput = this.getAssets().open("netconfig.properties");
		byte[] buffer = new byte[512];
		int length = myInput.read(buffer);
		while (length > 0) {
			myOutput.write(buffer, 0, length);
			length = myInput.read(buffer);
		}

		myOutput.flush();
		myInput.close();
		myOutput.close();
	}

}
