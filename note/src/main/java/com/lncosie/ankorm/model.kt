package com.lncosie.ankorm


import android.app.Application
import android.content.Context
import android.util.Log
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.util.HashMap
import kotlin.Int
import kotlin.platform.platformStatic
import kotlin.properties.Delegates


Retention(RetentionPolicy.RUNTIME)
Target(ElementType.TYPE)
public annotation class TableName(val table: String)
Retention(RetentionPolicy.RUNTIME)
Target(ElementType.TYPE)
public annotation class ViewName(val view: String, val viewAsSelect: String)
Retention(RetentionPolicy.RUNTIME)
Target(ElementType.FIELD)
public annotation class Column()
Retention(RetentionPolicy.RUNTIME)
Target(ElementType.FIELD)
public annotation class NotNull()
Retention(RetentionPolicy.RUNTIME)
Target(ElementType.FIELD)
public annotation class Unique()
Retention(RetentionPolicy.RUNTIME)
Target(ElementType.FIELD)
public annotation class PrimaryKey()


public open class View {
    public companion object {
        public final platformStatic inline fun <reified M: View>load(id: Long): M? {
            return AnkOrm.get().load<M>(id)
        }
        public final platformStatic inline fun <reified M: View>all(): Iterable<M> {
            return AnkOrm.get().all<M>()
        }
        public final platformStatic inline fun <reified M: View>where(sql: String, vararg args: Any): Iterable<M> {
            return AnkOrm.get().where<M>(sql,args)
        }
    }
    public open fun save() {
        AnkOrm.get().save(this)
    }
    public open var Id: Long? = null;
}
public open  class Table : View(){
    public fun delete() {
        AnkOrm.get().delete(this)
    }
}
public open class AnkOrm {
    companion object {
        public fun get():SqliteDriver{
            return sqlite
        }
        private var sqlite: SqliteDriver by Delegates.notNull()
        public fun open(context:Context,db:String,version:Int) {
            sqlite = SqliteDriver(context, db, version);
            val module=ModuleInit()
            module.scanForModel(context)
        }
        public fun exec(sql:String){
            sqlite.execSQL(sql)
        }
        public fun close(): Unit {
            sqlite.close()
        }
    }
}

