package com.baidu.baidulocationdemo;

import android.app.LauncherActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.baidu.uitls.GPRSUtil;

import java.io.File;

import static com.baidu.uitls.GPRSUtil.isRoot;

public class BootBroadcastReceivera extends BroadcastReceiver {
	
	final static String  pathString = Environment.getExternalStorageDirectory().toString();//获取SDCard目录
	
	static final String action_boot="android.intent.action.BOOT_COMPLETED";
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.d("bootbroadcastReceiver", "接收到了重启完成的广播");
		
				
		if (intent.getAction().equals(action_boot)) {

//			if (isRoot()){
//
//			GPRSUtil.setGprsOn_Off(true);
//			}
//			Intent ootStartIntent=new Intent(context,LocationActivity.class);
//            ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(ootStartIntent); 
			
			
//			File file=new File(pathString+"/重启完成");
//			file.mkdir();


			Intent intent2 = new Intent(context, LocationActivity.class);
			intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent2);
			
			
		}
	}
}
