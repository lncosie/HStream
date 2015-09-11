package com.lncosie.ankorm

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dalvik.system.DexFile
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short
import java.lang.reflect.Field
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.Int
import kotlin.properties.Delegates


public class SqliteDriver(context: Context, name: String, version: Int)
: SQLiteOpenHelper(context, name, null, version) {
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: kotlin.Int, newVersion: kotlin.Int) {
        throw UnsupportedOperationException()
    }

    override fun onCreate(db: SQLiteDatabase) {
    }

    public fun execSQL(sql: String) {
        getWritableDatabase().execSQL(sql)
    }
    public fun<M : View> save(m: M) {
        val database = getWritableDatabase()
        val tableInfo = TableInfo(m.javaClass)
        val values: ContentValues = ContentValues()
        tableInfo.tableWriteInit()
        tableInfo.export(m, values)
        if (m.Id == null) {
            values.remove("Id")
            m.Id = database.insert(tableInfo.tableName, null, values)
        } else {
            database.replace(tableInfo.tableName, null, values)
        }
    }

    public fun delete<M : View>(m: M) {
        val database = getWritableDatabase()
        val tableInfo = TableInfo(m.javaClass)
        tableInfo.tableWriteInit()
        database.delete(tableInfo.tableName, "id=?", arrayOf(m.Id?.toString()))
    }

    public inline fun <reified M : View>load(Id: kotlin.Long): M? {
        val tableInfo = TableInfo<M>(javaClass<M>())
        val sql = StringBuilder { this.append("SELECT * FROM ").append(tableInfo.tableName).append(" WHERE Id=").append(Id.toString()) }
        val it = rawQuery(tableInfo, sql.toString(), null);
        for (element in it)
            return element
        return null
    }

    public inline fun <reified M : View>all(): Iterable<M> {
        val tableInfo = TableInfo<M>(javaClass<M>())
        val sql = StringBuilder { this.append("SELECT * FROM ").append(tableInfo.tableName) }
        return rawQuery(tableInfo, sql.toString(), null);
    }

    public inline fun <reified M : View>where(where: String, bound: Array<out Any>): Iterable<M> {
        val tableInfo = TableInfo<M>(javaClass<M>())
        val sql = StringBuilder { this.append("SELECT * FROM ").append(tableInfo.tableName).append(" WHERE ").append(where) }
        return rawQuery(tableInfo, sql.toString(),bound.map { it.toString() }.toTypedArray());
    }

    public fun <M : View>rawQuery(tableInfo: TableInfo<M>, sql: String, selectionArgs: Array<String>?): Iterable<M> {
        val cursor = getReadableDatabase().rawQuery(sql, selectionArgs)
        tableInfo.tableQeueryInit(cursor)
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

public class TableInfo<M : View>(val m: Class<M>) {
    var Id: Int = 0;
    public var tableName: String by Delegates.notNull()
    var columns: MutableList<ColumnInfo> = ArrayList()

    init {
        val table = m.getAnnotation(kotlin.javaClass<com.lncosie.ankorm.TableName>())
        tableName = table?.table ?: m.getAnnotation(javaClass<ViewName>()).view
    }

    public fun tableQeueryInit(cursor: Cursor) {
        m.getDeclaredFields()
        assert(isTable() or isView(), "Query need mark Table or View annotations")
        Id = cursor.getColumnIndex("Id")
        m.getDeclaredFields().filter {
            it.getDeclaredAnnotations().size() > 0
        }.forEach {
            val name = it.getName()
            val index = cursor.getColumnIndex(name)
            columns.add(ColumnInfo(it, name, index))
        }
    }

    public fun tableWriteInit() {
        assert(isTable(), "Write only for Table annotation class")
        m.getDeclaredFields().filter {
            it.getDeclaredAnnotations().size() > 0
        }.forEach {
            val name = it.getName()
            columns.add(ColumnInfo(it, name, -1))
        }
    }

    fun isTable(): Boolean {
        return (m.getAnnotation(javaClass<TableName>()) != null)
    }

    fun isView(): Boolean {
        return (m.getAnnotation(javaClass<ViewName>()) != null)
    }

    public fun getSchema(): String {
        if (isTable()) {
            val sql = StringBuilder()
            sql.append("CREATE TABLE IF NOT EXISTS  ")
            sql.append(tableName)
            sql.append(columns.map { it.name + restrict(it.field) }.join(",").let { "(Id INTEGER PRIMARY KEY," + it + ")" })
            return sql.toString()
        } else {
            val view = m.getAnnotation(javaClass<ViewName>())
            val sql = StringBuilder()
            sql.append("CREATE VIEW IF NOT EXISTS ")
            sql.append(tableName)
            sql.append(" AS ")
            sql.append(view.viewAsSelect)
            return sql.toString()
        }
    }

    fun restrict(field: Field): String {
        val restrictsql = StringBuilder()
        field.getDeclaredAnnotations().forEach {
            restrictsql.append(
                    when (field.getType()) {
                        javaClass<Byte>() -> " INTEGER"
                        javaClass<Char>() -> " INTEGER"
                        javaClass<Integer>() -> " INTEGER"
                        javaClass<Int>() -> " INTEGER"
                        javaClass<Short>()->" INTEGER"
                        javaClass<Boolean>()->" INTEGER"

                        javaClass<Long>() -> " BIG INTEGER"
                        javaClass<Float>() -> " REAL"
                        javaClass<Double>() -> " REAL"
                        javaClass<String>() -> " TEXT"
                        javaClass<ByteArray>() -> " BLOB"

                        javaClass<Date>()-> " TEXT "
                        javaClass<Calendar>()-> " TEXT "
                        else -> -1
                    }
            )
            restrictsql.append(
                    when (it) {
                        is NotNull -> " Not NULL "
                        is PrimaryKey -> "  PRIMARY KEY "
                        is Unique -> " Unique "
                        else -> ""
                    }
            )
        }
        return restrictsql.toString()

    }

    fun import(cursor: Cursor): M {
        val model = m.newInstance()
        model.Id = cursor.getLong(Id);
        for (column in columns) {
            column.invoke(model, cursor)
        }
        return model
    }

    fun export(m: M, values: ContentValues) {
        values.put("Id", m.Id)
        for (column in columns) {
            when (column.type) {
                0 -> values.put(column.name, column.field.getByte(m))
                1 -> values.put(column.name, column.field.getInt(m))
                2 -> values.put(column.name, column.field.getLong(m))
                3 -> values.put(column.name, column.field.getFloat(m))
                4 -> values.put(column.name, column.field.getDouble(m))
                5 -> values.put(column.name, column.field.get(m) as String)
                6 -> values.put(column.name, column.field.get(m) as ByteArray)
                7 -> values.put(column.name, column.field.getShort(m))
                8 -> values.put(column.name, column.field.getBoolean(m))
                9 -> values.put(column.name, (column.field.get(m) as Date).toString())
                else -> values.putNull(column.name)
            }


        }
    }

    inner class ColumnInfo {
        constructor(field: Field, name: String, index: Int) {
            field.setAccessible(true)
            this.field = field
            this.index = index
            this.name = name
            type = when (field.getType()) {
                javaClass<Byte>() -> 0
                javaClass<Integer>() -> 1
                javaClass<Int>() -> 1
                javaClass<Long>() -> 2
                javaClass<Float>() -> 3
                javaClass<Double>() -> 4
                javaClass<String>() -> 5
                javaClass<ByteArray>() -> 6
                javaClass<Short>()->7
                javaClass<Boolean>()->8
                javaClass<Date>()->9
                //javaClass<Calendar>()->10
                else -> -1
            }
        }

        var field: Field by Delegates.notNull()
        var type: Int = 0;
        var index: Int = 0;
        var name: String by Delegates.notNull()

        fun invoke(m: M, cursor: Cursor) {

            when (type) {
                0 -> field.set(m, cursor.getShort(index));
                1 -> field.set(m, cursor.getInt(index));
                2 -> field.set(m, cursor.getLong(index));
                3 -> field.set(m, cursor.getFloat(index));
                4 -> field.set(m, cursor.getDouble(index));
                5 -> field.set(m, cursor.getString(index));
                6 -> field.set(m, cursor.getBlob(index));
                7 -> field.set(m, cursor.getShort(index));
                8 -> field.set(m, cursor.getInt(index)==0);
                9 -> field.set(m, Date(cursor.getString(index)));
                else -> field.set(m, null)

            }
        }
    }
}

class ModuleInit() {
    public fun scanForModel(application: Context) {
        val path = application.getPackageCodePath()
        val loader = application.getClassLoader()
        val file = DexFile(path)
        for (code in file.entries()) {
            val classT = file.loadClass(code, loader)
            var viewT = classT?.getAnnotation(javaClass<ViewName>())
            if (viewT != null) {
                val viewDef = TableInfo<View>(classT as Class<View>)
                viewDef.tableWriteInit()
                AnkOrm.get().execSQL(viewDef.getSchema())
                continue
            }
            var tableT = classT?.getAnnotation(javaClass<TableName>())
            if (tableT != null) {
                val tableDef = TableInfo<Table>(classT as Class<Table>)
                tableDef.tableWriteInit()
                AnkOrm.get().execSQL(tableDef.getSchema())
            }
        }
    }
}
