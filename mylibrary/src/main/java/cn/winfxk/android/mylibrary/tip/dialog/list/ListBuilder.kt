package cn.winfxk.android.mylibrary.tip.dialog.list

import android.content.Context
import android.widget.LinearLayout
import cn.winfxk.android.mylibrary.R
import cn.winfxk.android.mylibrary.tip.dialog.BaseBuilder

class ListBuilder(context: Context) : BaseBuilder(context) {
    private val view by lazy { findViewById<LinearLayout>(R.id.loading) }
    override fun getLayoutId(): Int = R.layout.winfxklia_listbuilder

    override fun initializeView() {

    }

    fun addItem(builder: ListItemView.() -> Unit) {
        val view = ListItemView(context);
        builder.invoke(view)
        this.view.addView(view.button)
    }
}