/*Created by IntelliJ IDEA.
  Author： Winfxk
  PCUser: kc4064 
  Date: 2024/8/12  下午2:07*/
package com.winfxk.winfxklia.dialog

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.winfxk.winfxklia.tool.Tool


class Tip private constructor(private val main: Context, resource: Int, private var timeout: Int = LENGTH_LONG) {
    private val view = View.inflate(main, com.winfxk.winfxklia.R.layout.winfxkliba_tip, null);
    private val icon: ImageView = view.findViewById(com.winfxk.winfxklia.R.id.imageView1);
    private val title: TextView = view.findViewById(com.winfxk.winfxklia.R.id.textView1)
    private var gravity: Int = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM;
    private var xOffset: Int = 0;
    private var yOffset: Int = 0;
    fun setTitle(title: Any?): Tip {
        this.title.text = Tool.objToString(title, "") ?: ""
        return this
    }

    init {
        icon.setImageResource(resource)
    }

    fun setResource(resources: Int): Tip {
        icon.setImageResource(resources)
        return this
    }

    fun setDuration(timeout: Int): Tip {
        this.timeout = timeout;
        return this
    }

    fun show() {
        val toas = Toast(main);
        toas.view = view;
        toas.setGravity(gravity, xOffset, yOffset)
        toas.setDuration(timeout)
        toas.show()
    }

    fun setGravity(gravity: Int = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, xOffset: Int = 0, yOffset: Int = 0) {
        this.gravity = gravity;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    companion object {
        const val LENGTH_LONG = Toast.LENGTH_LONG;
        const val LENGTH_SHORT = Toast.LENGTH_SHORT;
        fun makeTip(main: Context, title: Any?, timeout: Int = Toast.LENGTH_LONG): Tip {
            val id = getResources();
            return Tip(main, id, timeout).setTitle(title)
        }

        private val resources = listOf(com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon1, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon2,
            com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon3, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon4, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon5, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon6,
            com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon7, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon8, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon9, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon10,
            com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon11, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon12, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon13, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon14,
            com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon15, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon16, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon17, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon18,
            com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon19, com.winfxk.winfxklia.R.drawable.winfxkliba_tip_icon20)

        private fun getResources(): Int {
            return resources[Tool.getRand(0, resources.size - 1)]
        }
    }
}