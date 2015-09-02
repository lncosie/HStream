package com.lncosie.note;

import android.app.Activity;
import android.os.Bundle;

public class ActivityNote extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        getFragmentManager().beginTransaction().add(R.id.fragment_container,new FragmentNotes()).commit();

    }


}
