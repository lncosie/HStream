package com.lncosie.ankorm

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.activeandroid
import com.activeandroid.serializer.TypeSerializer
import com.activeandroid.util.Log
import com.activeandroid.util.ReflectionUtils
import dalvik.system.DexFile
import java.io.File
import java.io.IOException
import java.lang.Double
import java.lang.Float
import java.lang.Long
import java.lang.Short
import java.lang.reflect.Field
import java.net.URL
import java.util.*
import kotlin.Int
import kotlin.properties.Delegates


public open class SqliteDriver(context: Context, name: String, version: Int)
: SQLiteOpenHelper(context, name, null, version) {
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: kotlin.Int, newVersion: kotlin.Int) {
        throw UnsupportedOperationException()
    }

    override fun onCreate(db: SQLiteDatabase) {

    }
    public fun execSQL(sql:String){
        getWritableDatabase().execSQL(sql)
    }
//    public inline fun <reified M : View>scahmeInit() {
//        val tableInfo = TableInfo(javaClass<M>())
//        tableInfo.tableWriteInit()
//        getWritableDatabase().execSQL(tableInfo.getSchema())
//    }

    public fun<M : View> save(m: M) {
        val database = getWritableDatabase()
        val tableInfo = TableInfo(m.javaClass)
        val values: ContentValues = ContentValues()
        tableInfo.tableWriteInit()
        tableInfo.export(m, values)
        if (m.Id == null) {
            values.remove("Id")
            m.Id=database.insert(tableInfo.tableName, null, values)
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
        val it = rawQuery(tableInfo, sql.toString(), javaClass<M>(), null);
        for (element in it)
            return element
        return null
    }

    public inline fun <reified M : View>all(): Iterable<M> {
        val tableInfo = TableInfo<M>(javaClass<M>())
        val sql = StringBuilder { this.append("SELECT * FROM ").append(tableInfo.tableName) }
        return rawQuery(tableInfo, sql.toString(), javaClass<M>(), null);
    }

    public inline fun <reified M : View>where(where: String, bound: Array<out Any>): Iterable<M> {
        val tableInfo = TableInfo<M>(javaClass<M>())
        val sql = StringBuilder { this.append("SELECT * FROM ").append(tableInfo.tableName).append(" WHERE ").append(where) }
        return rawQuery(tableInfo, sql.toString(), javaClass<M>(), bound.map { it.toString() }.toTypedArray());
    }

    public fun <M : View>rawQuery(tableInfo: TableInfo<M>, sql: String, jClass: Class<M>, selectionArgs: Array<String>?): Iterable<M> {
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

public class TableInfo<M : View>(val m: Class<?>) {
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
            it.getAnnotation(javaClass<Column>()) != null
                    || it.getAnnotation(javaClass<NotNull>()) != null
                    || it.getAnnotation(javaClass<PrimaryKey>()) != null
                    || it.getAnnotation(javaClass<Unique>()) != null
        }.forEach {
            val name = it.getName()
            val index = cursor.getColumnIndex(name)
            columns.add(ColumnInfo(it, name, index))
        }
    }

    public fun tableWriteInit() {
        assert(isTable(), "Write only for Table annotation class")
        m.getDeclaredFields().filter {
            it.getAnnotation(javaClass<Column>()) != null
                    || it.getAnnotation(javaClass<NotNull>()) != null
                    || it.getAnnotation(javaClass<PrimaryKey>()) != null
                    || it.getAnnotation(javaClass<Unique>()) != null
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
            sql.append(view.sqlSelect)
            return sql.toString()
        }
    }

    fun restrict(field: Field): String {
        val restrictsql = StringBuilder()


        if (field.getAnnotation(javaClass<PrimaryKey>()) != null)
            restrictsql.append(" PRIMARY KEY ")
        if (field.getAnnotation(javaClass<NotNull>()) != null)
            restrictsql.append(" Not NULL ")
        if (field.getAnnotation(javaClass<Unique>()) != null)
            restrictsql.append(" Unique ")
        return restrictsql.toString()
//            field.getDeclaredAnnotations().forEach {
//                append(
//                        when (it) {
//                            is NotNull -> " Not NULL "
//                            is PrimaryKey -> "  PRIMARY KEY "
//                            is Unique -> " Unique "
//                            else -> ""
//                        }
//                )
//            }


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
                javaClass<Short>() -> 0
                javaClass<Integer>() -> 1
                javaClass<Int>() -> 1
                javaClass<Long>() -> 2
                javaClass<Float>() -> 3
                javaClass<Double>() -> 4
                javaClass<String>() -> 5
                javaClass<ByteArray>() -> 6
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
                else -> field.set(m, null)
            }
        }
    }
}
class ModuleInit()
{
    public  fun scanForModel(application: Application) {
        val packageName = application.getPackageName()
        val sourcePath = application.getApplicationInfo().sourceDir
        val paths = ArrayList<String>()
        if (sourcePath != null) {
            val path = DexFile(sourcePath)
            val resources = path.entries()
            while (resources.hasMoreElements()) {
                paths.add(resources.nextElement() as String)
            }
        } else {
            val path1 = Thread.currentThread().getContextClassLoader()
            val resources = path1.getResources("")

            while (resources.hasMoreElements()) {
                val file = (resources.nextElement() as URL).getFile()
                if (file.contains("bin")) {
                    paths.add(file)
                }
            }
        }
        val resources1 = paths.iterator()

        while (resources1.hasNext()) {
            val file1 = File(resources1.next())
            this.scanForModelClasses(file1, packageName, application.javaClass.getClassLoader())
        }
    }

    private fun scanForModelClasses(path: File, packageName: String, classLoader: ClassLoader) {
        var e: Int
        if (path.isDirectory()) {
            val var7=path.listFiles()
            val typeSerializer = var7.size()
            e = 0
            while (e < typeSerializer) {
                val className = var7[e]
                this.scanForModelClasses(className, packageName, classLoader)
                ++e
            }
        } else {
            var var11 = path.getName()
            if (path.getPath() != var11) {
                var11 = path.getPath()
                if (!var11.endsWith(".class")) {
                    return
                }
                var11 = var11.substring(0, var11.length() - 6)
                var11 = var11.replace("/", ".")
                e = var11.lastIndexOf(packageName)
                if (e < 0) {
                    return
                }

                var11 = var11.substring(e)
            }

            try {
                val var12 = Class.forName(var11, false, classLoader)
                if(var12.getAnnotation(javaClass<ViewName>())!=null||var12.getAnnotation(javaClass<TableName>())!=null)
                {
                    val tableInfo = TableInfo<View>(var12)
                    tableInfo.tableWriteInit()
                    AnkOrm.get().execSQL(tableInfo.getSchema())
                }
//                if (ReflectionUtils.isModel(var12)) {
//                    //this.mTableInfos.put(var12, activeandroid.TableInfo(var12))
//                } else if (ReflectionUtils.isTypeSerializer(var12)) {
//                    val var13 = var12.newInstance() as TypeSerializer
//                    //this.mTypeSerializers.put(var13.javaClass, var13)
//                }
            } catch (var8: ClassNotFoundException) {
                Log.e("Couldn\'t create class.", var8)
            } catch (var9: InstantiationException) {
                Log.e("Couldn\'t instantiate TypeSerializer.", var9)
            } catch (var10: IllegalAccessException) {
                Log.e("IllegalAccessException", var10)
            }

        }

    }
}
