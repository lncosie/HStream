package com.lncosie.ankorm

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short
import java.lang.reflect.Field
import java.util.*
import kotlin.properties.Delegates


public open class SqliteDriver(context: Context?, name: String?, version: Int)
: SQLiteOpenHelper(context, name, null, version) {
    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
    public inline fun <reified M:Model>scahmeInit(){
        val tableInfo=TableInfo(javaClass<M>())
        getWritableDatabase().execSQL(tableInfo.getSchema())
    }
    public fun<M:Model> save(m: M) {
        val database = getWritableDatabase()
        val tableInfo=TableInfo(m.javaClass)
        val values:ContentValues=ContentValues()
        tableInfo.tableInit()
        tableInfo.export(m,values)
        if(m.Id==null)
        {
            values.remove("Id")
            database.insert(tableInfo.tableName,null,values)
        }else{
            database.replace(tableInfo.tableName,null,values)
        }
    }

    public fun delete<M:Model>(m:M) {
        val database = getWritableDatabase()
        val tableInfo=TableInfo(m.javaClass)
        database.delete(tableInfo.getTable(),"id=?",arrayOf(m.Id?.toString()))
    }

    public fun <M:Model>select(sql: String, jClass: Class<M>,vararg bound: Any): Iterable<M> {
        val cursor = getReadableDatabase().rawQuery(sql,
                null//bound.drop(1).map{ it.toString() }.toTypedArray()
        )
        val tableInfo=TableInfo(jClass)
        tableInfo.tableInit(cursor)
        val iterator = object : Iterator<M> {
            override fun next(): M {
                return tableInfo.import(cursor)
            }
            override fun hasNext(): Boolean {
                return cursor.moveToNext()
            }
        }
        val iterable = object : Iterable<M> {
            override fun iterator(): Iterator<M> {
                return iterator
            }
        }
        return iterable;
    }
}

public  class TableInfo<M:Model>(val m: Class<M>) {
    inner class ColumnInfo {
        constructor(field: Field,name:String,index:Int) {
            field.setAccessible(true)
            this.field = field
            this.index=index
            this.name=name
            val by=field.getType()
            type = when (field.getType()) {
                javaClass<Short>() -> 0
                javaClass<Integer>() -> 1
                javaClass<Int>() -> 1
                javaClass<Long>()-> 2
                javaClass<Float>()-> 3
                javaClass<Double>()-> 4
                javaClass<String>() -> 5
                javaClass<ByteArray>() -> 6
                else -> -1
            }
        }
        var field: Field by Delegates.notNull()
        var type: Int = 0;
        var index:Int=0;
        var name:String by Delegates.notNull()

        fun invoke(m: M, cursor: Cursor) {

            when (type) {
                0 -> field.set(m, cursor.getShort(index));
                1 -> field.set(m, cursor.getInt(index));
                2 -> field.set(m, cursor.getLong(index));
                3 -> field.set(m, cursor.getFloat(index));
                4 -> field.set(m, cursor.getDouble(index));
                5 -> field.set(m, cursor.getString(index));
                6 -> field.set(m, cursor.getBlob(index));
                else -> field.set(m, null)
            }
        }
    }
    var Id:Int=0;
    var tableName: String by Delegates.notNull()
    var columns: MutableList<ColumnInfo> =ArrayList()
    fun tableInit(cursor:Cursor) {
        tableName=getTable()
        Id=cursor.getColumnIndex("Id")
        m.getDeclaredFields().filter { it.getAnnotation(javaClass<Column>())!=null }
                .forEach {
                    val name=it.getName()
                    val index=cursor.getColumnIndex(name)
                    columns.add(ColumnInfo(it,name,index))
                }
    }
    fun tableInit() {
        tableName=getTable()
        m.getDeclaredFields().filter { it.getAnnotation(javaClass<Column>())!=null}
                .forEach {
                    val name=it.getName()
                    columns.add(ColumnInfo(it,name,0))
                }
    }
    fun getTable():String{
        val table = m.getAnnotation(javaClass<Table>())
        return table?.table?:m.getAnnotation(javaClass<View>()).view
    }
    fun isTable(): Boolean {
        return (m.getAnnotation(javaClass<Table>())!=null)
    }
    fun isView(): Boolean {
        return (m.getAnnotation(javaClass<View>())!=null)
    }
    public fun getSchema():String{
        tableInit()
        if(isTable())
        {
            val sql=StringBuilder()
            sql.append("CREATE TABLE IF NOT EXISTS  ")
            sql.append(tableName)
            sql.append(columns.map { it.name }.join(",").let { "(Id INTEGER PRIMARY KEY," +it+")" })
            return sql.toString()
        }else{
            val view=m.getAnnotation(javaClass<View>())
            val sql=StringBuilder()
            sql.append("CREATE VIEW IF NOT EXISTS ")
            sql.append(tableName)
            sql.append(" AS ")
            sql.append(view.sqlSelect)
            return sql.toString()
        }

    }
    fun import(cursor:Cursor):M{
        val model=m.newInstance()
        model.Id=cursor.getLong(Id);
        for(column in columns){
            column.invoke(model,cursor)
        }
        return  model
    }
    fun export(m:M,values:ContentValues)
    {
        values.put("Id",m.Id)
        for(column in columns){
            when(column.type){
                0->values.put(column.name,column.field.getByte(m))
                1->values.put(column.name,column.field.getInt(m))
                2->values.put(column.name,column.field.getLong(m))
                3->values.put(column.name,column.field.getFloat(m))
                4->values.put(column.name,column.field.getDouble(m))
                5->values.put(column.name,column.field.get(m) as String)
                6->values.put(column.name,column.field.get(m) as ByteArray)
                else->values.putNull(column.name)
            }
        }
    }

}
