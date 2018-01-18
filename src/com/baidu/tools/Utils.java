package com.baidu.tools;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {
	   
    /**  
     * 得到netconfig.properties配置文件中的所有配置属性  
     *   
     * @return Properties对象  
     */  
    public static Properties getNetConfigProperties() {   
        Properties props = new Properties();   
        InputStream in = Utils.class.getResourceAsStream("/netconfig.properties");
        
        File file=new File(Environment.getExternalStorageDirectory()+"/netconfig.properties");
        
       
        
        
        try {
			FileInputStream inputStream = new FileInputStream(file);
			  try {   
		            props.load(inputStream);   
		        } catch (IOException e) {   
		            e.printStackTrace();   
		        }
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
//        try {   
//            props.load(in);   
//        } catch (IOException e) {   
//            e.printStackTrace();   
//        }

         
        return props;   
    }

	// 判断当前网络是否可用
	public static boolean isNetworkAvailable(Context activity) {
		Context context = activity.getApplicationContext();
		// 获取手机所有连接管理对象（包括对wifi,net等连接的管理）
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivityManager == null) {
			return false;
		} else {
			// 获取NetworkInfo对象
			NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

			if (networkInfo != null && networkInfo.length > 0) {
				for (int i = 0; i < networkInfo.length; i++) {
					// 判断当前网络状态是否为连接状态
					if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
    
    
}
