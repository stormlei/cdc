package com.qpsoft.cdc.ui.adapter

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qpsoft.cdc.R
import com.qpsoft.cdc.ui.entity.MySection


class GradeClazzListAdapter(layoutResId: Int, sectionHeadResId: Int, data: MutableList<MySection>?) : BaseSectionQuickAdapter<MySection, BaseViewHolder>(sectionHeadResId, data) {

    init {
        setNormalLayout(layoutResId)
    }

    override fun convertHeader(helper: BaseViewHolder, item: MySection) {
        if (item.any is String) {
            helper.setText(R.id.header, item.any as String)
        }
    }

    override fun convert(holder: BaseViewHolder, item: MySection) {
        val clazz = item.any as String
        holder.setText(android.R.id.text1, clazz)
    }


}