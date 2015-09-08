package com.lncosie.ankorm



import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.view.ViewDebug
import java.lang.reflect.Type
import java.util.*
import kotlin.platform.platformStatic
import kotlin.properties.Delegates

enum    class Restrict
{
    Default,NotNull,PrimaryKey,Unique

    override fun toString(): String {
        return when(this)
        {
            NotNull->"Not NULL"
            PrimaryKey->"PRIMARY KEY"
            Unique->"UNIQUE"
            else->""
        }
    }
}

annotation class Table(val table:String)
annotation class View(val view:String,val sqlSelect:String)
annotation class Column(val relation:Array<Restrict>)


open    class Model{

    companion object  {
        public open fun create(orm:AnkOrm?=null):Model
        {
            return Model()
        }
        public open platformStatic fun <M>load(id:Long):M? {
            return null;
        }

        public open platformStatic fun <M>where(sql:String,vararg args:Any) :List<M>{
            return null as List<M>;
        }
    }
    public open fun save(){

    }
    public open fun delete(){
    }
}
open class  DynamicModel:Model(){
    val maps= HashMap<String, Any>()

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
    var Id:Long by Delegates.notNull()
}

Table("S")
class   Ts:Model()
{
    Column(arrayOf(Restrict.Unique,Restrict.NotNull))
    var name:String?=null;
}
View("Vs","select * from S")
class   Vs:Model()
{
    Column(arrayOf(Restrict.Unique,Restrict.NotNull))
    var name:String?=null;
}
fun useage()
{
    val s:Ts?=Model.load(1)
    val sl=Model.where<Ts>("id>?",5)
    val view:Vs?=Model.load(1);
    s?.save();
    s?.delete();
    for(v in sl)
    {
        v.delete()
    }
}
class DatabaseScahma{
    fun createDb() {
        val models=arrayOf(Model());
        for(m in models)
        {
            val table=m.javaClass.getAnnotation(javaClass<Table>())
            val fileds=m.javaClass.getFields().filter { it.isAnnotationPresent(javaClass<Column>())  }
                    .map { Triple(it.getName(),it.getType(),it.getAnnotation(javaClass<Column>())) }
            createTable(TableDefine(table,fileds))
        }
    }
    fun createTable(table:TableDefine)
    {
        val sql=StringBuilder();
        sql.append("create table if not exsits ").append(table.table.table).append("(")
        .append(table.fileds.map { it.first+" "+
                it.second.toString()+
                it.third.relation.toString()}.join(","))
        sql.append(")")
    }
    data    class TableDefine(val table:Table,val fileds:List<Triple<String,Type,Column>>)

}
