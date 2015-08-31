package com.lncosie.note

import kotlin.properties.Delegates


open class  Note(content:String?,pure:String?=null)
{
    var annex:List<Any>?=null;
    var time:data by    Delegates.notNull()
}

open class  Category(val category:String):Iterable<Note>
{

    fun add(content:String?):Note
    {
        val note=Note(content)
        notes.add(note)
        return note;
    }
    override fun iterator(): Iterator<Note> {
        return notes.iterator();
    }
    var notes:MutableCollection<Note> by Delegates.notNull();

}

open class FsNote:Iterable<Category>{

    fun add(category:String):Category
    {
        val first=categorys.firstOrNull { it.category.equals(category) }
        return first?.let { it }?:Category(category)

    }
    override fun iterator(): Iterator<Category> {
        return categorys.iterator();
    }
    val categorys:MutableCollection<Category> by Delegates.notNull();
}