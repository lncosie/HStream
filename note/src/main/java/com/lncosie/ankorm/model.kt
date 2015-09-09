package com.lncosie.ankorm


import android.content.Context
import android.util.Log
import java.util.HashMap
import kotlin.platform.platformStatic
import kotlin.properties.Delegates

enum class Restrict {
    Default, NotNull, PrimaryKey, Unique

    override fun toString(): String {
        return when (this) {
            NotNull -> "Not NULL"
            PrimaryKey -> "PRIMARY KEY"
            Unique -> "UNIQUE"
            else -> ""
        }
    }
}

public annotation class Table(val table: String)
public annotation class View(val view: String, val sqlSelect: String)
public annotation class Column()


public open class Model{

    public companion object {
        public open fun create(orm: AnkOrm? = null): Model {
            return Model()
        }

        public open platformStatic fun <M>load(id: Long): M? {
            return null;
        }

        public final platformStatic inline fun <reified M:Model>where(sql: String, vararg args: Any): Iterable<M> {
            return AnkOrm.get().select<M>(sql,javaClass<M>(),args)
        }
    }

    public open fun save() {
        AnkOrm.get().save(this)
    }
    public open var Id: Long? = null;
}
open class MutableModel:Model(){
    public open fun delete() {
        AnkOrm.get().delete(this)
    }
}
open class DynamicModel : Model() {
    val maps = HashMap<String, Any>()
}

public open class AnkOrm {
    companion object {
        public final fun get():SqliteDriver{
            return sqlite
        }
        private var sqlite: SqliteDriver by Delegates.notNull()
        public fun open(context:Context): SqliteDriver {
            sqlite = SqliteDriver(context, "app.db", 1);
            return sqlite
        }
    }
    fun close(): Unit {
        sqlite.close()
    }
}

Table("S")
public open class Ts : MutableModel() {
    Column()
    public open var name: String = "abc";
    Column()
    public open var p: Int = 1;
    Column()
    public open var by: ByteArray = byteArrayOf(0,0,1,2,3,4);


}

View("Vs", "select * from S")
public open class Vs : Model() {
    Column()
    public open var name: String = "abc";
    Column()
    public open var p: Int = 1;
    Column()
    public open var by: ByteArray?=null
}

public open class Useage {
    public fun useage(context:Context) {
        AnkOrm.open(context).scahmeInit<Ts>();
        AnkOrm.get().scahmeInit<Vs>();

        val sv=Ts();
        sv.save();
        val sl = Model.where<Ts>("select * from S")
        for (v in sl) {
            v.p=v.Id?.toInt()?:0;
            v.by.set(0,v.p.toByte())
            v.save()

        }
        val vl=Model.where<Vs>("select * from Vs")
        vl.drop(2).forEach {
            it.name
            Log.e("DbOut",it.toString())
        }
    }
}