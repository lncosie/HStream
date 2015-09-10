package com.lncosie.ankorm


import android.app.Application
import android.content.Context
import android.util.Log
import java.util.HashMap
import kotlin.Int
import kotlin.platform.platformStatic
import kotlin.properties.Delegates



public annotation class TableName(val table: String)
public annotation class ViewName(val view: String, val sqlSelect: String)
public annotation class Column()
public annotation class NotNull()
public annotation class Unique()
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
open class Table : View(){
    public open fun delete() {
        AnkOrm.get().delete(this)
    }
}
public open class AnkOrm {
    companion object {
        public final fun get():SqliteDriver{
            return sqlite
        }
        private var sqlite: SqliteDriver by Delegates.notNull()
        public fun open(context:Context,db:String,version:Int): SqliteDriver {
            val modual=ModuleInit()
            modual.scanForModel(context.getApplicationContext() as Application)
            sqlite = SqliteDriver(context, db, version);
            return sqlite
        }
    }

    fun close(): Unit {
        sqlite.close()
    }
}

