package com.qpsoft.cdc.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.ui.entity.School
import com.qpsoft.cdc.ui.entity.Student
import me.yokeyword.indexablerv.IndexableAdapter


class UpLoadDataAdapter(private val context: Context): IndexableAdapter<School>() {

    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateTitleViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = mInflater.inflate(R.layout.item_index_student, parent, false)
        return IndexVH(view)
    }

    override fun onCreateContentViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = mInflater.inflate(R.layout.item_upload_data_list, parent, false)
        return ContentVH(view)
    }

    override fun onBindTitleViewHolder(holder: RecyclerView.ViewHolder?, indexTitle: String) {
        val vh = holder as IndexVH
        vh.tvIndex.text = indexTitle
    }

    override fun onBindContentViewHolder(holder: RecyclerView.ViewHolder?, student: School) {
        val vh = holder as ContentVH
        vh.tvName.text = student.name
        val realm = App.instance.backgroundThreadRealm
        val s = realm.where(Student::class.java).equalTo("school.id", student.id)
            .equalTo("upload", "0".toInt()).findFirst()
        if (s == null) {
            vh.tvDownLoad.setTextColor(context.resources.getColor(R.color.color_cb7))
            vh.tvDownLoad.setText("上传成功")
        } else {
            vh.tvDownLoad.setTextColor(context.resources.getColor(R.color.color_26))
            vh.tvDownLoad.setText("上传数据")
        }
    }

    private class IndexVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvIndex: TextView = itemView.findViewById(R.id.tvIndex)
    }

    private class ContentVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tvName)
        var tvDownLoad: TextView = itemView.findViewById(R.id.tvDownLoad)
    }
}