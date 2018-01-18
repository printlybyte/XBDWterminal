package com.baidu.baidulocationdemo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.baidu.moudle.NotifationBuild;
import com.baidu.tools.RegUtil;
import com.baidu.uitls.Constantss;

import static com.baidu.baidulocationdemo.MyService.isAirplaneModeOn;
import static com.baidu.baidulocationdemo.MyService.isOPen;
import static com.baidu.tools.RegUtil.strreg;
import static com.baidu.uitls.GPRSUtil.isRoot;
import static com.baidu.uitls.PublicUtils.setSettingsOnHigh;


public class LocationActivity extends Activity {

    public NotificationManager mNotificationManager;

    NotificationCompat.Builder mBuilder;

    int notifyId = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);


        if (!isAirplaneModeOn(getBaseContext())) {
            if (isRoot()) {
                setSettingsOnHigh(0);
            } else {

                Toast.makeText(getApplicationContext(), "请先退出飞行模式",
                        Toast.LENGTH_SHORT).show();
                finish();
//            Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
////            startActivityForResult(intent, 0);
                return;
            }
        }
        if (!isOPen(getBaseContext())) {

            Toast.makeText(getApplicationContext(), "请先开启GPS",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        if (!isNetworkAvailable(getBaseContext())) {
//            Toast.makeText(getApplicationContext(), "开启服务失败，请先检查网络连接",
//                    Toast.LENGTH_SHORT).show();
//        return;
//        }

        getRegInfo();


    }

    private void startServices() {
        if (Constantss.ifOpen) {

            Intent ii = new Intent(this, MyService.class);
            startService(ii);
            initNotify();
            Toast.makeText(this, "正在开启中", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(getBaseContext(), "已经开启了", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 获取注册码信息
     */
    private void getRegInfo() {
        SharedPreferences sp = getSharedPreferences("RegCode", MODE_PRIVATE);
        strreg = sp.getString("REGCODE", "");
        if (strreg != null && !TextUtils.isEmpty(strreg)) {
            startServices();
        } else {
            initRegCode();
        }
    }

    /**
     * 初始化注册码认证
     */
    private void initRegCode() {
        RegUtil ru = new RegUtil(this);
        ru.SetDialogCancelCallBack(new RegUtil.DialogCancelInterface() {
            @Override
            public void ToFinishActivity() {
                finish();
            }

            @Override
            public void toShowDialog() {
                startServices();
            }
        });
    }

//    /**
//     * 初始化notification
//     */
//    private void initNotify() {
//        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        mBuilder = new NotificationCompat.Builder(this);
//        mBuilder.setContentTitle("信标定位终端")
//                .setContentText("点此管理定位服务")
//                .setContentIntent(
//                        getDefalutIntent(Notification.FLAG_NO_CLEAR))
//                .setTicker("通知")// 通知首次出现在通知栏，带上升动画效果的
//                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
//                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
//                .setOngoing(true)// ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
//                .setSmallIcon(R.drawable.icon01)
//                .setAutoCancel(true);
//        mBuilder.setContentIntent(getContentIntent());
//        mNotificationManager.notify(notifyId, mBuilder.build());
//    }

//    private PendingIntent getContentIntent() {
//        Intent resultIntent = new Intent(this, NotifationBuild.class);
//        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//                resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//        return pendingIntent;
//    }
//
//    public PendingIntent getDefalutIntent(int flags) {
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
//                new Intent(), flags);
//        return pendingIntent;
//    }


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
        deleteIntent();

        mNotificationManager.notify(notifyId, mBuilder.build());


    }

    /**
    * 针对mate8手机  滑动删除启动服务
    *
    */
    private void   deleteIntent(){
        Intent deleteIntent = new Intent(this, DeleteService.class);
        int deleteCode = (int) SystemClock.uptimeMillis();
        PendingIntent deletePendingIntent = PendingIntent.getService(this,
                deleteCode, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setDeleteIntent(deletePendingIntent);
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

}
