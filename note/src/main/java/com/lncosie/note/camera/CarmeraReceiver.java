package com.lncosie.note.camera;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import ui.ActivityNotify;

public class CarmeraReceiver extends BroadcastReceiver {
    public CarmeraReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = context.getContentResolver().query(intent.getData(),      null,null, null, null);
        cursor.moveToFirst();
        String image_path = cursor.getString(cursor.getColumnIndex("_data"));
        context.startActivity(new Intent(context, ActivityNotify.class));
        //Toast.makeText(context, "New Photo is Saved as : -" + image_path, Toast.LENGTH_SHORT).show();
        //Intent i = new Intent(context, MyAct.class);
    }
}
