package com.baidu.baidulocationdemo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.baidu.DatabaseHelper.DatabaseHelper;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.moudle.NotifationBuild;
import com.baidu.tools.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class afterRebootService extends Service {
	public NotificationManager mNotificationManager;
	// Notification构造器
	NotificationCompat.Builder mBuilder;
	// NotificationID
	int notifyId = 100;
	private LocationApplication LocApp ;
	private LocationMode tempMode = LocationMode.Device_Sensors;
	private LocationClient mLocationClient;
	String time,timeSMS,sqlmsg19;
	Socket socket = null;
	private ExecutorService mThreadPool;
	private OutputStream outputStream;
	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d("afterRebootService", "执行onStart方法");

		super.onStart(intent, startId);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.d("afterRebootService", "执行onStartCommand方法");
		socketConneted();
		
		startUploadingData();
		

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	public void startUploadingData() {

		Toast.makeText(getApplicationContext(), "已启用定位服务", Toast.LENGTH_SHORT)
				.show();
		// 通知栏
		initNotify();
//		showIntentActivityNotify();
		// 百度init
		initLocation();
		// 点击后台运行
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		mLocationClient.start();
		// （计时器操作）
		TimerTask task = new TimerTask() {
			public void run() {
				time();
				if (true) {  // isNetworkAvailable(LocationActivity.this)
					DatabaseHelper dh = new DatabaseHelper(
							getApplicationContext(), "Locaiotn_sql");
					SQLiteDatabase db = dh.getWritableDatabase();
					Cursor cursor = db
							.rawQuery(
									"select * from Locationmsg order by sqltime asc limit 1",
									null);
					while (cursor.moveToNext()) {
						String sqltime = cursor.getString(0); // 获取第一列的值,第一列的索引从0开始
						sqlmsg19 = cursor.getString(1);// 获取第二列的值
						if (sqlmsg19 != null) {
							String sql = "delete from Locationmsg where sqltime ="
									+ sqltime;
							db.execSQL(sql);
						}
					}
					cursor.close();
					mLocationClient.stop();
					mLocationClient.start();
					socketConnectSend();

				} else {
					mLocationClient.stop();
					mLocationClient.start();
					// 像ContentValues中存放数据
					ContentValues values = new ContentValues();
					DatabaseHelper dh = new DatabaseHelper(
							getApplicationContext(), "Locaiotn_sql");
					SQLiteDatabase db = dh.getWritableDatabase();
					values.put("sqltime", time);
					values.put("sqlmsg", "**19" + (LocApp).msgonline);
					// 数据库执行插入命令
					db.insert("Locationmsg", null, values);
					return;
				}
			}
		};
		Timer timer = new Timer(true);
		timer.schedule(
				task,
				0,
				Integer.parseInt(Utils.getNetConfigProperties().getProperty(
						"LocationTime")));

	}


	/**
	 * 初始化notification
	 */
	private void initNotify() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mBuilder = new NotificationCompat.Builder(this);
		mBuilder.setContentTitle("信标定位终端系统")
				.setContentText("点此管理定位服务")
				.setContentIntent(
						getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
				.setTicker("通知")// 通知首次出现在通知栏，带上升动画效果的
				.setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
				.setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
				.setOngoing(true)// ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
				.setSmallIcon(R.mipmap.icon01)
				.setAutoCancel(true);
		mBuilder.setContentIntent(getContentIntent());
		mNotificationManager.notify(notifyId, mBuilder.build());
	}

	private PendingIntent getContentIntent() {
		Intent resultIntent = new Intent(this, NotifationBuild.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	public PendingIntent getDefalutIntent(int flags) {
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
				new Intent(), flags);
		return pendingIntent;
	}

	// initLocation
		private void initLocation() {
			LocApp = (LocationApplication) getApplication();
			mLocationClient = (LocApp).mLocationClient;
			
			
			LocationClientOption option = new LocationClientOption();
			if ((LocApp).msgonline == null) {
				tempMode = LocationMode.Hight_Accuracy;
			}
			option.setLocationMode(tempMode);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
			option.setCoorType("gcj02");// 可选，默认gcj02，设置返回的定位结果坐标系，
			option.setOpenGps(true);// 可选，默认false,设置是否使用gps
			option.setLocationNotify(false);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
			option.setIgnoreKillProcess(true);// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
			option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
			option.disableCache(true);// 禁止使用缓存数据
			mLocationClient.setLocOption(option);
		}
		private void time() {
			// 获取当前时间
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			Date curDate = new Date(System.currentTimeMillis());
			time = formatter.format(curDate);
			SimpleDateFormat formatter2 = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date curDate2 = new Date(System.currentTimeMillis());// 获取当前时间
			timeSMS = formatter2.format(curDate2);
		}
		public void sendMsg20() {

			try {
				// 创建socket对象，指定服务器端地址和端口号
				socket = new Socket(Utils.getNetConfigProperties()
						.getProperty("IP"), Integer.parseInt(Utils
						.getNetConfigProperties().getProperty("PORT")));
				// 获取 Client 端的输出流并填充信息
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(socket.getOutputStream())), true);
				out.println("**20" + (LocApp).imei + time + (LocApp).msgonline);
				Log.d("time", time);
				// 关闭
				out.close();
				socket.close();
			} catch (UnknownHostException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}



	/**
	 * 初始化socket连接
	 */

	private void socketConneted() {
		// 初始化线程池
		mThreadPool = Executors.newCachedThreadPool();
		// 利用线程池直接开启一个线程 & 执行该线程


		// 利用线程池直接开启一个线程 & 执行该线程
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {

				try {
					// 创建Socket对象 & 指定服务端的IP 及 端口号
					socket = new Socket(Utils.getNetConfigProperties()
							.getProperty("IP"), Integer.parseInt(Utils
							.getNetConfigProperties().getProperty("PORT")));
//
					socket.setKeepAlive(true);
					Log.i("QWEQWEQ", " 连接成功  初始化" + socket.isConnected());
				} catch (IOException e) {
					Log.i("qweqwe","连接异常");

				}
			}
		});


	}
	private  void   socketConnectSend(){
		// 利用线程池直接开启一个线程 & 执行该线程

		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				try {
					if (socket != null) {
						// 步骤1：从Socket 获得输出流对象OutputStream
						// 该对象作用：发送数据
						outputStream = socket.getOutputStream();
//                        if (alwaysResulta != null) {
						// 步骤2：写入需要发送的数据到输出流对象中
						outputStream.write(("**20" + (LocApp).imei + time + (LocApp).msgonline + "\n").getBytes("utf-8"));
						// 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
						// 步骤3：发送数据到服务端
						outputStream.flush();
//                        }
					}
				} catch (IOException e) {
					socketConneted();
					Log.i("qweqwe","发送异常");
				}
				//连接后启动监听socket的状态



			}
		});
	}

}
