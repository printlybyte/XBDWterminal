package com.baidu.uitls;

import android.net.ConnectivityManager;

import java.io.DataOutputStream;
import java.io.File;
import java.lang.reflect.Method;

/**
 * Created by ${王sir} on 2017/6/21.
 * application
 */

public class GPRSUtil {

    private final static String COMMAND_L_ON = "svc data enable\n ";
    private final static String COMMAND_L_OFF = "svc data disable\n ";
    private final static String COMMAND_SU = "su";

    /**
     * 开启或关闭GPRS移动数据
     * 应用环境，root 系统5.0以上
     * @param enable
     */
    public static void setGprsOn_Off(boolean enable){

        String command;
        if(enable)
            command = COMMAND_L_ON;
        else
            command = COMMAND_L_OFF;

        try{
            Process su = Runtime.getRuntime().exec(COMMAND_SU);
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            outputStream.writeBytes(command);
            outputStream.flush();

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
            }

            outputStream.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 判断手机是否root
     */

    public static boolean isRoot() {
        boolean bool = false;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists())) {
                bool = false;
            } else {
                bool = true;
            }
        } catch (Exception e) {
        }
        return bool;
    }


    /**
     * 检测设备GPRS是否打开
     * @param mCM
     * @return
     */
    public static boolean gprsIsOpenMethod(ConnectivityManager mCM )
    {
        Class cmClass       = mCM.getClass();
        Class[] argClasses  = null;
        Object[] argObject  = null;

        Boolean isOpen = false;
        try
        {
            Method method = cmClass.getMethod("getMobileDataEnabled", argClasses);

            isOpen = (Boolean) method.invoke(mCM, argObject);
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return isOpen;
    }
}
