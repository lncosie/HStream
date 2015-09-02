package com.lncosie.ankorm



import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.ViewDebug
import java.util.*
enum    class Restrict
{
    Default,NotNull,PrimaryKey
}
annotation class Relation(val restrict:Restrict)
annotation class Table(val table:String)
annotation class Column(val column:String,val relation:Relation=Relation(Restrict.Default))


open    class Model{
    companion object  {
        fun create(orm:AnkOrm?=null):Model
        {
            return Model()
        }
        fun <M>load(id:Long):M {
            return null as M;
        }
        fun <M,T:String>where(sql:String,vararg args:T) :List<M>{
            return null as List<M>;
        }
    }
    open fun save() {

    }
}
open class  DynamicModel:Model(){
    val maps= HashMap<String, Any>()
    override fun save()=throw Exception()
}

open class AnkOrm{
    companion object{
        fun open(db:String):AnkOrm
        {
            return AnkOrm()
        }
    }

    fun close():Unit
    {

    }
}
Table("S")
class   S:Model()
{
    Column("name")
    var name:String?=null;
}
