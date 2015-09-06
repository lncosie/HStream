package com.lncosie.note

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table
import com.activeandroid.query.Select
import kotlin.properties.Delegates

@Table(name="notes")
open class  Note(@Column(name="category")var category:String?,@Column(name="content")var content:String?): Model()
{
    constructor():this(null,null)
    {

    }
    var annex:List<Any>?=null;

    override fun toString(): String = content?.toString()?:"";
}
@Table(name="category")
open class  Category(@Column(name="name")var category:String,var notes:MutableList<Note>):Model(),MutableList<Note> by notes
{
    constructor(category:String,note:Iterable<Note>):this(category,note.toArrayList())
    {
    }
    constructor():this("",arrayListOf())
    {

    }
    fun add(content:String?):Note
    {
        val note=Note(category,content)
        notes.add(note)
        return note;
    }
    override fun toString(): String = category;
}

open class FsNote(val categorys:MutableList<Category>):MutableList<Category> by categorys
{
    constructor(categorys:Iterable<Category>):this(categorys.toArrayList())
    {
    }

    fun add(category:String):Category
    {
        val first=categorys.firstOrNull { it.category.equals(category) }
        if(first!=null)
            return first;
        val cate=Category(category,arrayListOf<Note>())
        categorys.add(cate)
        cate.save()
        return cate
    }
}
open    class   ClipManager(val context:Context)
{
    val   listener: ClipboardManager.OnPrimaryClipChangedListener=object : ClipboardManager.OnPrimaryClipChangedListener {
        override fun onPrimaryClipChanged() {
            val clipdata=clipmanager.getPrimaryClip()
            //MIMETYPE_TEXT_PLAIN,MIMETYPE_TEXT_HTML
            val clipData = clipmanager.getPrimaryClip();
            for(i:Int=0;i<10;i++)
            {

            }
            val count = clipData.getItemCount();

            fore() {

                val item = clipData.getItemAt(i);
                val str = item.coerceToText();

            }
        }
    }
    val   clipmanager= context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager;
    fun init(context: Context)
    {
        clipmanager.addPrimaryClipChangedListener(listener)
    }
    fun destroy()
    {
        clipmanager.removePrimaryClipChangedListener(listener)
    }
}
