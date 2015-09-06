package com.lncosie.note;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

public class ActivityNote extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        gotoMonitor();
    }

    boolean editing=false;
    @Override
    public void onBackPressed() {
        if(editing)
            gotoMonitor();
        else
            super.onBackPressed();
    }
    void gotoMonitor()
    {
        editing=false;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentNotes()).commit();
    }
    public void gotoEditor(Note note)
    {
        editing=true;
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,new FragmentNoteEditor().init(note)).commit();
    }
}
