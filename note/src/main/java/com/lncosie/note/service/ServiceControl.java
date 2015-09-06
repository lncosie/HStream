package com.lncosie.note.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.content.Context;

/**
 * Created by lncosie.org on 2015/9/3.
 */
public class ServiceControl {
    ServiceConnection   connection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder=((ServiceBinder)service) ;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    ServiceBinder binder;
    ServiceBinder start(Context context) {
        context.bindService(new Intent(), connection, Context.BIND_AUTO_CREATE);
        return binder;
    }
    void stop(Context context)
    {
        context.unbindService(connection);
    }
}
