package com.lncosie.note.clipboard;

//import android.content.ClipboardManager;


import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardListener {
    ClipboardManager clipboardManager;
    ClipboardManager.OnPrimaryClipChangedListener eventOn=new ClipboardManager.OnPrimaryClipChangedListener()
    {
        public void onPrimaryClipChanged()
        {
            if (clipboardManager.hasPrimaryClip()){
                clipboardManager.getPrimaryClip().getItemAt(0).getText();
            }
        }
    };
    public ClipboardListener(Context context)
    {
        clipboardManager = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(eventOn);
    }
}
