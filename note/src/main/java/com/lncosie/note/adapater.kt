package com.lncosie.note

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import kotlin.properties.Delegates
public open class  ViewHolder()
{
    var view:TextView by Delegates.notNull()
    fun init(view:TextView) {
        this.view=view
    }
    fun refresh(data:Any)
    {
        view.setText(data.toString())
    }
}
public open class Adapater<T>(val context:Context,val layoutRes:Int,
                              val Hold:Class<ViewHolder>,
                              val textId:Int=0): BaseAdapter(), Filterable
{

    var items:List<T> by Delegates.notNull();
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val view: View
        val hold: ViewHolder
        if(convertView==null)
        {
            view=inflater.inflate(layoutRes,parent,false)
            hold =Hold.newInstance()
            if(textId==0)
            {
                hold.init(view as TextView)
            }else
            {
                hold.init(view.findViewById(textId) as TextView)
            }
            view.setTag(hold)
        }
        else
        {
            view=convertView;
            hold= view.getTag() as ViewHolder
        }
        hold.refresh(items.get(position))
        return view
    }
    override fun getItem(position: Int): T? {
        return items.get(position);
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getCount(): Int {
        return items.size()
    }

    override fun getFilter(): Filter? {
        return null;
    }

}