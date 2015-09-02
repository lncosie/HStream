package com.lncosie.note;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.activeandroid.query.Select;

import java.util.List;

public class ActivityNote extends Activity {
    ListView    listCategory;
    ListView    listNotes;
    NotesAdapter    notesAdapter;
    CategoryAdapter categoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar);
        listCategory=(ListView)findViewById(R.id.list_cateogry);
        listNotes=(ListView)findViewById(R.id.list_notes);

        toolbar.inflateMenu(R.menu.menu_activity_note);
        notesAdapter=new NotesAdapter(this);
        categoryAdapter=new CategoryAdapter(this);
        listCategory.setAdapter(categoryAdapter);
        listNotes.setAdapter(notesAdapter);
        listCategory.setOnItemClickListener(categoryClick);
        listNotes.setOnItemClickListener(noteClick);
        toolbar.setOnMenuItemClickListener(menu_click);
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
                    notesAdapter.add(new Note("Only Test","MY"));
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
            if(category.getCategory().equals("+"))
            {
                categoryAdapter.add(new Category("Cio.1"));
                return;
            }
            notesAdapter.changeCategory(category);
        }
    };
    AdapterView.OnItemClickListener noteClick=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Toast.makeText(getApplication(),"cc",Toast.LENGTH_SHORT).show();
        }
    };

    static class    CategoryAdapter extends ArrayAdapter<Category>
    {
        public CategoryAdapter(Context context) {
            super(context, R.layout.item_category,R.id.text_category);
            add(new Category("+"));
            List<Category>  categories=new Select().from(Category.class).execute();
            addAll(categories);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
    }
    static class    NotesAdapter extends ArrayAdapter<Note>
    {
        public NotesAdapter(Context context) {
            super(context, R.layout.item_note, R.id.text_note_pure);

        }
        Category    category;
        public void changeCategory(Category category)
        {
            this.category=category;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return super.getView(position, convertView, parent);
        }
    }
}
