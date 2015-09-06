package com.lncosie.note.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import com.lncosie.note.camera.CarmeraReceiver;
import com.lncosie.note.clipboard.ClipboardListener;

public class ServiceListener extends Service {
    ClipboardListener clipboardListener;
    CarmeraReceiver   carmeraReceiver;
    IBinder serviceBinder;
    public ServiceListener() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        init();
        return serviceBinder;
    }
    private void init()
    {
        if(serviceBinder==null)
        {
            serviceBinder=new ServiceBinder();

            clipboardListener=new ClipboardListener(this);
            carmeraReceiver=new CarmeraReceiver();
            IntentFilter    intentFilter=new IntentFilter("android.hardware.action.NEW_PICTURE");
            this.registerReceiver(carmeraReceiver, intentFilter);
        }
    }

}
