package cn.winfxk.android.mylibrary.tip.dialog.list

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
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

    fun addItem(text: String, onClick: OnListBuilderButtonClick) {
        addItem {
            this.text = text;
            this.onClick = onClick;
        }
    }
    @Synchronized
    fun removeItem(builder: ListItemView) {
        val view = builder.button;
        this.view.removeView(view);
    }

    @Synchronized
    fun removeItem(text: String) {
        this.view.children.toMutableList().forEach { view ->
            if (view is TextView && view.text.equals(text))
                this.view.removeView(view);
        }
    }
}