package com.lncosie.note

import com.activeandroid.Model
import com.activeandroid.annotation.Column
import com.activeandroid.annotation.Table
import com.activeandroid.query.Select
import kotlin.properties.Delegates

@Table(name="NOTES")
open class  Note(@Column(name="CATEGORY")var category:String?,@Column(name="CONTENT")val content:String?): Model()
{
    var annex:List<Any>?=null;

    override fun toString(): String = content?.toString()?:""
}
@Table(name="CATEGORY")
open class  Category(@Column(name="NAME")val category:String):Model(),Iterable<Note>
{
    init {
        val categories = Select().from(javaClass<Note>()).where("category=?",category).execute<Note>()
        notes.addAll(categories);
    }
    var notes:MutableCollection<Note> by Delegates.notNull();
    fun add(content:String?):Note
    {
        val note=Note(content,category)
        notes.add(note)
        return note;
    }
    override fun iterator(): Iterator<Note> =notes.iterator()
    override fun toString(): String = category;
}

open class FsNote:Iterable<Category>{

    val categorys:MutableCollection<Category> by Delegates.notNull();
    fun add(category:String):Category
    {
        val first=categorys.firstOrNull { it.category.equals(category) }
        return first?.let { it }?:Category(category)
    }
    override fun iterator(): Iterator<Category> = categorys.iterator();

}