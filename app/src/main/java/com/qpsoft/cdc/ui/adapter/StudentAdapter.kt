package com.qpsoft.cdc.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.qpsoft.cdc.R
import com.qpsoft.cdc.ui.entity.Student
import me.yokeyword.indexablerv.IndexableAdapter


class StudentAdapter(context: Context): IndexableAdapter<Student>() {

    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateTitleViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = mInflater.inflate(R.layout.item_index_student, parent, false)
        return IndexVH(view)
    }

    override fun onCreateContentViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        val view = mInflater.inflate(R.layout.item_student_list, parent, false)
        return ContentVH(view)
    }

    override fun onBindTitleViewHolder(holder: RecyclerView.ViewHolder?, indexTitle: String) {
        val vh = holder as IndexVH
        vh.tvIndex.text = indexTitle
    }

    override fun onBindContentViewHolder(holder: RecyclerView.ViewHolder?, student: Student) {
        val vh = holder as ContentVH
        vh.tvName.text = student.name
    }

    private class IndexVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvIndex: TextView = itemView.findViewById(R.id.tvIndex)
    }

    private class ContentVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tvName)
    }
}