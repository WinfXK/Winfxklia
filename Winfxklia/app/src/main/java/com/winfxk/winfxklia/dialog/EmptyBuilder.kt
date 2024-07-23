/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/12  下午3:10*/
package com.winfxk.winfxklia.dialog

import android.content.Context
import android.os.Message
import android.view.View
import android.widget.RelativeLayout
import com.winfxk.winfxklia.R

class EmptyBuilder(val main: Context, cancelable: Boolean = false) : BaseBuilder(main, cancelable) {
    private val view: RelativeLayout;
    private lateinit var custom: View;

    init {
        setContentView(R.layout.winfxkliba_empty_dialog)
        view = findViewById(R.id.line1);
    }

    fun setView(view: View): EmptyBuilder {
        this.custom = view;
        this.view.addView(view);
        return this;
    }

    fun getView(): View {
        return custom;
    }

    override fun show() {
        handler.post { super.show() }
    }
    @Deprecated("此方法无效", ReplaceWith("super.setTitle(titleId)", "android.app.Dialog"))
    override fun setDismissMessage(msg: Message?) {
        super.setDismissMessage(msg)
    }
    @Deprecated("此方法无效", ReplaceWith("super.setTitle(titleId)", "android.app.Dialog"))
    override fun setCancelMessage(msg: Message?) {
        super.setCancelMessage(msg)
    }
    @Deprecated("此方法无效", ReplaceWith("super.setTitle(titleId)", "android.app.Dialog"))
    override fun setTitle(title: CharSequence?) {
        super.setTitle(title)
    }
    @Deprecated("此方法无效", ReplaceWith("super.setTitle(titleId)", "android.app.Dialog"))
    override fun setTitle(titleId: Int) {
        super.setTitle(titleId)
    }
}