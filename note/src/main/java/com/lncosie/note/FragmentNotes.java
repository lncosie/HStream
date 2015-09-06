package com.lncosie.note;


import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.activeandroid.query.Select;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentNotes extends Fragment {

    ListView listCategory;
    ListView    listNotes;
    NotesAdapter    notesAdapter;
    CategoryAdapter categoryAdapter;

    public FragmentNotes() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_notes, container, false);
        Toolbar toolbar=(Toolbar)view.findViewById(R.id.toolbar);
        listCategory=(ListView)view.findViewById(R.id.list_cateogry);
        listNotes=(ListView)view.findViewById(R.id.list_notes);

        toolbar.inflateMenu(R.menu.menu_activity_note);
        notesAdapter=new NotesAdapter(this.getActivity(),"Menu");
        categoryAdapter=new CategoryAdapter(this.getActivity());
        listCategory.setDivider(null);
        listCategory.setDividerHeight(2);
        listCategory.setAdapter(categoryAdapter);
        listCategory.setOnItemClickListener(categoryClick);

        listNotes.setAdapter(notesAdapter);
        listNotes.setDivider(null);
        listNotes.setDividerHeight(2);
        listNotes.setOnItemClickListener(noteClick);
        toolbar.setOnMenuItemClickListener(menu_click);
        return view;
    }


    Toolbar.OnMenuItemClickListener menu_click=new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            switch (id)
            {
                case R.id.button_show_category:
                    final boolean hide=listCategory.getX()>=0;
                    final int width=listCategory.getWidth();
                    listCategory.animate().x(hide ? -width : 0).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            listNotes.setLayoutParams(new LinearLayout.LayoutParams(
                                    0, listNotes.getHeight(), hide ? 1 : (float) 0.7));
                        }
                    }).start();

                    break;
                case R.id.button_note_add:
                    ((ActivityNote)getActivity()).gotoEditor(notesAdapter.notes.add("abc"));
                    notesAdapter.notifyDataSetChanged();
                    break;
                case R.id.button_note_delete:
                    break;
                case R.id.button_note_select:
                    break;
            }
            return false;
        }
    };

    AdapterView.OnItemClickListener categoryClick=new AdapterView.OnItemClickListener() {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Category    category=categoryAdapter.getItem(position);
            if(category.getCategory().equals("Menu"))
            {
                categoryAdapter.fsNote.add("X");
                categoryAdapter.notifyDataSetChanged();
                return;
            }
            listCategory.setSelection(-1);
            listCategory.setSelection(position);
            notesAdapter.setCategory(category.getCategory());

        }
    };
    AdapterView.OnItemClickListener noteClick=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ((ActivityNote)getActivity()).gotoEditor(notesAdapter.getItem(position));
        }
    };


    static class    CategoryAdapter extends Adapater<Category>
    {
        FsNote  fsNote;
        public CategoryAdapter(Context context) {
            super(context, R.layout.item_category,ViewHolder.class,R.id.text_category);
            List<Category> load=new Select().from(Category.class).execute();
            fsNote=new FsNote(load);
            fsNote.add("Menu");
            setItems(fsNote);
        }
    }

    static class    NotesAdapter extends Adapater<Note>
    {
        Category    notes;
        public NotesAdapter(Context context,String category) {
            super(context, R.layout.item_note, ViewHolder.class, R.id.text_note_pure);
            setCategory(category);
        }
        void setCategory(String category)
        {
            List<Note> load=new Select().from(Note.class).where("category=?",category).execute();
            notes=new Category(category,load);
            setItems(notes);
            notifyDataSetChanged();
        }

    }

}
