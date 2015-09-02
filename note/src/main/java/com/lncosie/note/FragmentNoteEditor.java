package com.lncosie.note;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentNoteEditor extends Fragment {
    public FragmentNoteEditor() {
    }
    Note note;
    EditText    editor;
    public FragmentNoteEditor init(Note note)
    {
        this.note=note;
        return this;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_note_editor, container, false);
        editor=(EditText)view.findViewById(R.id.editor);
        editor.setText(note.getContent());
        return view;
    }

    @Override
    public void onStop() {
        note.setContent(editor.getText().toString());
        note.save();
        super.onStop();
    }
}
