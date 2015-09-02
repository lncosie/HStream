package com.lncosie.note

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table
import com.activeandroid.query.Select
import kotlin.properties.Delegates

@Table(name="NOTES")
open class  Note(@Column(name="category")var category:String?,@Column(name="content")val content:String?): Model()
{
    var annex:List<Any>?=null;

    override fun toString(): String = content?.toString()?:"";
}
@Table(name="CATEGORY")
open class  Category(@Column(name="NAME")val category:String,var notes:MutableList<Note>):Model(),MutableList<Note> by notes
{
    constructor(category:String,note:Iterable<Note>):this(category,note.toArrayList())
    {
    }
    fun add(content:String?):Note
    {
        val note=Note(content,category)
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
        return first?.let { it }?:Category(category,arrayListOf<Note>())
    }
}