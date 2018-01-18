package com.baidu.uitls;

/**
 * Created by Administrator on 2017/6/21.
 */


import android.content.Context;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PublicUtils {
    public static float dbCount = 40;

    private static float lastDbCount = dbCount;
    private static float min = 0.5f;  //设置声音最低变化
    private static float value = 0;   // 声音分贝值
    public static String HigherAirplaneModePref1 = "settings put global airplane_mode_on ";
    public static String HigherAirplaneModePref2 = "am broadcast -a android.intent.action.AIRPLANE_MODE --ez state ";


    public static float setDbCount(float dbValue) {
        if (dbValue > lastDbCount) {
            value = dbValue - lastDbCount > min ? dbValue - lastDbCount : min;
        } else {
            value = dbValue - lastDbCount < -min ? dbValue - lastDbCount : -min;
        }
        dbCount = lastDbCount + value * 0.2f; //防止声音变化太快
        lastDbCount = dbCount;
        return dbCount;
    }

    // 判断是否是飞行模式
    public static boolean AirplaneModeisOff(Context context) {

        return android.provider.Settings.System.getInt(
                context.getContentResolver(),
                android.provider.Settings.System.AIRPLANE_MODE_ON, 0) == 0;
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static void setSettingsOnHigh(int value) {
        // this should execute as system app, with write_secure_settings
        // permission
        // common app, can NOT do this
        // Settings.Global.putInt(
        // context.getContentResolver(),
        // Settings.Global.AIRPLANE_MODE_ON, value);
        String commond = HigherAirplaneModePref1 + value + ";";
        //settings put global airplane_mode_on 1;am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true
        //settings put global airplane_mode_on 0;am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false
        if (value == 1)
            commond += HigherAirplaneModePref2 + "true";
        else
            commond += HigherAirplaneModePref2 + "false";
        String result = ShellUtil.runRootCmd(commond);
    }

    public static void setSettingsWifi() {
        ShellUtil.runRootCmd("svc wifi enable");
    }

    /**
     * 获取下次提醒的时间,minute分后
     */
    public static String GetNextWarnTime(int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, minute);
        Date date = calendar.getTime();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        return time;
    }

    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        return time;
    }

    public static boolean fileIsExists() {
        try {
            File f = new File("/sdcard/.Properties/.investigation_terminal.properties");
            if (!f.exists()) {
                return false;
            }

        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
        return true;
    }

    /**
    *判断是否获取rooot权限
    *
    */

    public static boolean isRoot(){
        boolean bool = false;

        try{
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())){
                bool = false;
            } else {
                bool = true;
            }
            Log.d("qweqwe", "uitls 判断是是否是飞行模式 bool = " + bool);
        } catch (Exception e) {

        }
        return bool;
    }

    /**
     * @ 获取当前手机屏幕尺寸
     */
    public static float getPingMuSize(Context mContext) {
//    WindowManager wm = (WindowManager) context
//            .getSystemService(Context.WINDOW_SERVICE);
//    int width = wm.getDefaultDisplay().getWidth();
//    int height = wm.getDefaultDisplay().getHeight();
//    Log.i("qweqweq",""+width+"   ++++++  "+height);
//    int he =width*height;

        int densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;
        float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
        float density = mContext.getResources().getDisplayMetrics().density;
        float xdpi = mContext.getResources().getDisplayMetrics().xdpi;
        float ydpi = mContext.getResources().getDisplayMetrics().ydpi;
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = mContext.getResources().getDisplayMetrics().heightPixels;

        // 这样可以计算屏幕的物理尺寸
        float width2 = (width / xdpi)*(width / xdpi);
        float height2 = (height / ydpi)*(width / xdpi);




        return (float) Math.sqrt(width2+height2);
    }
}
