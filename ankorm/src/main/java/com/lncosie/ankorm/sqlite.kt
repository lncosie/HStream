package com.lncosie.ankorm

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short



public open class SqliteDriver(context: Context?, name: String?,version: Int)
            : SQLiteOpenHelper(context, name, null, version) {
    override fun onCreate(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun<M> save(m:M){
        val database=getWritableDatabase()
        database.update(m.getTable)
    }
    fun delete(){

    }
    fun <M>select(sql:String,vararg bound:String, jClass:Class<M>):Iterable<M> {
        val cursor = getReadableDatabase().rawQuery(sql, bound)
        val iterator=object : Iterator<M> {
            override fun next(): M {
                val m: M = jClass.newInstance();
                for (i in 0..cursor.getColumnCount()) {
                    val col = cursor.getColumnName(i)
                    val fd = m.javaClass.getField(col)
                    val type = fd.getType();
                    when (type) {
                        Short.TYPE -> fd.set(m, cursor.getShort(i));
                        Integer.TYPE -> fd.set(m, cursor.getInt(i));
                        Long.TYPE -> fd.set(m, cursor.getLong(i));
                        Float.TYPE -> fd.set(m, cursor.getFloat(i));
                        Double.TYPE -> fd.set(m, cursor.getDouble(i));
                        String.javaClass -> fd.set(m, cursor.getString(i));
                        javaClass<ByteArray>() -> fd.set(m, cursor.getInt(i));
                    }
                }
                return m;
            }
            override fun hasNext(): Boolean {
                return cursor.moveToNext()
            }

        }
        val iterable=object : Iterable<M> {
            override fun iterator(): Iterator<M> {
                return iterator
            }
        }
        return iterable;
    }
    inline fun <M>getTable(){
        val c=Model::class .javaClass
    }
}
