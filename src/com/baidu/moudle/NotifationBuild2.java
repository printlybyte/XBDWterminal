package com.baidu.moudle;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.Window;

import com.baidu.baidulocationdemo.R;
import com.baidu.baidulocationdemo.afterRebootService;

public class NotifationBuild2 extends Activity {
    private AlertDialog dialog;
    public NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    int notifyId = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        dialog = new AlertDialog.Builder(this)
                .setTitle("信标定位终端").setMessage("系统正在运行中...")
                .setCancelable(false)
                .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent afterRebootIntent = new Intent(
                                getApplicationContext(),
                                afterRebootService.class);
                        mNotificationManager.cancel(notifyId);
                        getApplicationContext().stopService(afterRebootIntent);
                        System.exit(0);
                    }
                }).setNegativeButton("后台运行", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        initNotify();
                        finish();

                    }
                }).create();
        dialog.show();
    }

    /**
     * 初始化notification
     */
    private void initNotify() {
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("信标定位终端")
                .setContentText("点此管理定位服务")
                .setContentIntent(
                        getDefalutIntent(Notification.FLAG_AUTO_CANCEL))
                .setTicker("通知")// 通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())// 通知产生的时间，会在通知信息里显示
                .setPriority(Notification.PRIORITY_DEFAULT)// 设置该通知优先级
                .setOngoing(true)// ture，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务,用户积极参与(如播放音乐)或以某种方式正在等待,因此占用设备(如一个文件下载,同步操作,主动网络连接)
                .setSmallIcon(R.mipmap.start_icon)
                .setAutoCancel(true);
        mBuilder.setContentIntent(getContentIntent());
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    private PendingIntent getContentIntent() {
        Intent resultIntent = new Intent(this, NotifationBuild2.class);
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