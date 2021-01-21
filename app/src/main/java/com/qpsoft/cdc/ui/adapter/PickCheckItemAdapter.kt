package com.qpsoft.cdc.ui.adapter

import android.widget.CheckBox
import com.chad.library.adapter.base.BaseSectionQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.qpsoft.cdc.App
import com.qpsoft.cdc.R
import com.qpsoft.cdc.ui.entity.CheckItem
import com.qpsoft.cdc.ui.entity.MySection


class PickCheckItemAdapter(layoutResId: Int, sectionHeadResId: Int, data: MutableList<MySection>?) : BaseSectionQuickAdapter<MySection, BaseViewHolder>(sectionHeadResId, data) {

    init {
        setNormalLayout(layoutResId)
    }

    override fun convertHeader(helper: BaseViewHolder, item: MySection) {
        if (item.any is String) {
            helper.setText(R.id.header, item.any as String?)
        }
    }

    override fun convert(holder: BaseViewHolder, item: MySection) {
        val checkItem = item.any as CheckItem
        holder.setText(R.id.tvOptional, if(checkItem.optional || checkItem.group == null) "" else "必查")
        val cbItem = holder.getView<CheckBox>(R.id.cbItem)
        cbItem.text = checkItem.name

        cbItem.setOnCheckedChangeListener(null)
        cbItem.isChecked = checkItem.check
        cbItem.setOnCheckedChangeListener { compoundButton, b ->
            checkItem.check = b
            App.instance.checkItemList.forEach {
                if (it.key == checkItem.key) {
                    it.check = b
                }
            }
            if (b) {
                App.instance.checkItemList.add(checkItem)
            } else {
                App.instance.checkItemList.remove(checkItem)
            }
        }
    }


}