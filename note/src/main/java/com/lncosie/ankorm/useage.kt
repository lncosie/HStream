package com.lncosie.ankorm

import android.content.Context
import android.util.Log
import kotlin.Int

TableName("S")
public open class Ts : Table() {
    NotNull()
    public open var name: String = "abc";
    Column()
    public open var p: Int = 1;
    Column()
    public open var by: ByteArray = byteArrayOf(0,0,1,2,3,4);
}

ViewName("Vs", "select * from S")
public open class Vs : View() {
    Column()
    public open var name: String = "abc";
    Column()
    public open var p: Int = 1;
    Column()
    public open var by: ByteArray?=null
}
public open class Useage {

    public fun useage(context: Context) {

        AnkOrm.open(context,"app.db",1)
        //AnkOrm.get().scahmeInit<Ts>();
        //AnkOrm.get().scahmeInit<Vs>();
        var t=Ts();
        t.save();
        var l:Vs?=null
        val tl = View.all<Ts>()
        for (v in tl) {
            v.p=v.Id?.toInt()?:0;
            v.by.set(0,v.p.toByte())
            v.save()
            l=View.load<Vs>(v.Id!!)
        }
        val vl= View.where<Vs>("id>? and id<?",3,6)
        vl.forEach {
            it.name
        }
    }
}
