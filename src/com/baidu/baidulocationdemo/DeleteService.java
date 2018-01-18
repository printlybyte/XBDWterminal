package com.baidu.baidulocationdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DeleteService extends Service {
    public DeleteService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("qweqweq","通知栏滑动删除停止服务");
        Intent afterRebootIntent = new Intent(
                getApplicationContext(),
                MyService.class);
        getApplicationContext().stopService(afterRebootIntent);

        Intent afterRebootIntent2 = new Intent(
                getApplicationContext(),
                DeleteService.class);
        getApplicationContext().stopService(afterRebootIntent2);

        System.exit(0);

        return super.onStartCommand(intent, flags, startId);
    }
}
