package com.qpsoft.cdc.ui.adapter

import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qpsoft.cdc.R
import com.qpsoft.cdc.ui.entity.MySection


class GradeClazzListAdapter(layoutResId: Int, sectionHeadResId: Int, data: MutableList<MySection>?, private val isRetest: Boolean) : BaseSectionQuickAdapter<MySection, BaseViewHolder>(sectionHeadResId, data) {

    init {
        setNormalLayout(layoutResId)
    }

    override fun convertHeader(helper: BaseViewHolder, item: MySection) {
        if (item.any is String) {
            helper.setText(R.id.tvHeader, item.any as String)
        }
        if (isRetest) helper.setVisible(R.id.tvTips, false)
        helper.setText(R.id.tvTips, "已筛查有效人数"+item.header)
    }

    override fun convert(holder: BaseViewHolder, item: MySection) {
        val clazz = item.any as String
        holder.setText(R.id.tvName, clazz)
    }


}