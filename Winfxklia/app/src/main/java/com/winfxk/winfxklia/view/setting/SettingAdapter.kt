/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  上午11:16*/
package com.winfxk.winfxklia.view.setting

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CompoundButton
import com.winfxk.winfxklia.R
import com.winfxk.winfxklia.tool.Tool
import com.winfxk.winfxklia.view.setting.data.*

class SettingAdapter(private val main: BaseSetting) : BaseAdapter() {
    override fun getCount(): Int {
        return main.menus.size;
    }

    override fun getItem(position: Int): SettingItem {
        return main.menus[position];
    }

    override fun getItemId(position: Int): Long {
        return position.toLong();
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position)
        var view: View? = item.view;
        if (view != null) return view;
        val data: LineView?;
        if (convertView == null || convertView.tag == null || convertView.tag !is LineView) view = initializeView(item);
        else {
            data = convertView.tag as LineView;
            view = if (data.getType() != item.type) initializeView(item);
            else initializeView(item, convertView)
        }
        item.obj?.setValue(main, view.tag as LineView)
        return view;
    }


    private fun initializeView(item: SettingItem, view: View? = null): View {
        return when (item.type) {
            Type.Line -> initializeLine(item, view);
            Type.Button -> initializeButton(item, view);
            Type.Switch -> initializeSwitch(item, view);
            Type.Check -> initializeCheck(item, view);
            Type.Empty -> initializeEmpty(item, view);
            Type.Next -> initializeNext(item, view);
            Type.Text -> initializeText(item, view);
            Type.Input -> initializeInput(item, view);
            else -> initializeUnknown(item)
        }
    }

    private fun initializeInput(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data: InputView = if (view.tag != null && view.tag is InputView) view.tag as InputView else
            InputView(view.findViewById(R.id.imageView1), view.findViewById(R.id.textView1), view.findViewById(R.id.textView2), view.findViewById(R.id.switch1))
        if (item.bitmap == null) data.icon.layoutParams.width = 0;
        else {
            data.icon.setImageBitmap(item.bitmap);
            data.icon.layoutParams.width = main.resources.getDimensionPixelSize(R.dimen.winfxkliba_setting_item_height);
        }
        item.switchListener?.let {
            data.text.setOnClickListener { view -> it.onSettingChanged(main, view) }
            notifyDataSetChanged()
        }
        data.title.text = item.title ?: "";
        data.hint.text = item.hint ?: "";
        data.edit.setText(Tool.objToString(item.value?.getValue(), ""))
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun initializeText(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data: TextView = if (view.tag != null && view.tag is TextView) view.tag as TextView else
            TextView(view.findViewById(R.id.imageView1), view.findViewById(R.id.textView1), view.findViewById(R.id.textView2), view.findViewById(R.id.switch1))
        if (item.bitmap == null) data.icon.layoutParams.width = 0;
        else {
            data.icon.imageBitmap = item.bitmap;
            data.icon.layoutParams.width = main.resources.getDimensionPixelSize(R.dimen.winfxkliba_setting_item_height);
        }
        item.switchListener?.let {
            data.text.setOnClickListener { view -> it.onSettingChanged(main, view) }
            notifyDataSetChanged()
        }
        data.title.text = item.title ?: "";
        data.hint.text = item.hint ?: "";
        data.text.text = Tool.objToString(item.value?.getValue(), "");
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun initializeLine(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data = LineView(view.findViewById(R.id.textView2))
        if (item.hint != null) data.hint.text = item.hint;
        else if (item.title != null) data.hint.text = item.title;
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun initializeButton(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data: ButtonView = if (view.tag != null && view.tag is ButtonView) view.tag as ButtonView else ButtonView(view.findViewById(R.id.textView2), view.findViewById(R.id.switch1))
        item.switchListener?.let {
            data.switch.setOnClickListener { view -> it.onSettingChanged(main, view) }
            notifyDataSetChanged()
        }
        data.switch.text = item.title ?: "";
        data.hint.text = item.hint ?: "";
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun initializeSwitch(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data: SwitchView = if (view.tag != null && view.tag is SwitchView) view.tag as SwitchView else SwitchView(view.findViewById(R.id.imageView1), view.findViewById(R.id.textView1), view.findViewById(R.id.textView2), view.findViewById(R.id.switch1))
        if (item.bitmap == null) data.icon.layoutParams.width = 0;
        else {
            data.icon.setImageBitmap(item.bitmap);
            data.icon.layoutParams.width = main.resources.getDimensionPixelSize(R.dimen.winfxkliba_setting_item_height);
        }
        item.switchListener?.let {
            data.switch.setOnClickListener { view -> it.onSettingChanged(main, view) }
            notifyDataSetChanged()
        }
        data.title.text = item.title ?: "";
        data.hint.text = item.hint ?: "";
        (data.switch as CompoundButton).isChecked = Tool.ObjToBool(item.value?.getValue(), false);
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun initializeCheck(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data: CheckView = if (view.tag != null && view.tag is CheckView) view.tag as CheckView else CheckView(view.findViewById(R.id.imageView1), view.findViewById(R.id.textView1), view.findViewById(R.id.textView2), view.findViewById(R.id.switch1))
        if (item.bitmap == null) {
            data.icon.layoutParams.width = 0;
        } else {
            data.icon.setImageBitmap(item.bitmap);
            data.icon.layoutParams.width = main.resources.getDimensionPixelSize(R.dimen.winfxkliba_setting_item_height);
        }
        data.title.text = item.title ?: "";
        data.hint.text = item.hint ?: "";
        (data.switch as CompoundButton).isChecked = Tool.ObjToBool(item.value?.getValue(), false);
        data.item = item;
        item.switchListener?.let {
            data.switch.setOnClickListener { view -> it.onSettingChanged(main, view) }
            notifyDataSetChanged()
        }
        view.tag = data;
        return view;
    }

    private fun initializeEmpty(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data: EmptyView = if (view.tag != null && view.tag is EmptyView) view.tag as EmptyView else EmptyView(view.findViewById(R.id.imageView1), view.findViewById(R.id.textView1), view.findViewById(R.id.textView2))
        if (item.bitmap == null) {
            data.icon.layoutParams.width = 0;
        } else {
            data.icon.setImageBitmap(item.bitmap);
            data.icon.layoutParams.width = main.resources.getDimensionPixelSize(R.dimen.winfxkliba_setting_item_height);
        }
        data.title.text = item.title ?: "";
        data.hint.text = item.hint ?: "";
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun initializeNext(item: SettingItem, vint: View? = getView(item)): View {
        val view = vint ?: getView(item);
        val data: NextView = if (view.tag != null && view.tag is NextView) view.tag as NextView else NextView(view.findViewById(R.id.imageView1), view.findViewById(R.id.textView1), view.findViewById(R.id.textView2), view.findViewById(R.id.switch1))
        if (item.bitmap == null) {
            data.icon.layoutParams.width = 0;
        } else {
            data.icon.setImageBitmap(item.bitmap);
            data.icon.layoutParams.width = main.resources.getDimensionPixelSize(R.dimen.winfxkliba_setting_item_height);
        }
        data.title.text = item.title ?: "";
        data.hint.text = item.hint ?: "";
        item.switchListener?.let {
            data.switch.setOnClickListener { view -> it.onSettingChanged(main, view) }
            notifyDataSetChanged()
        }
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun initializeUnknown(item: SettingItem): View {
        val view = View.inflate(main, R.layout.winfxklia_setting_item_line, null);
        val data = LineView(view.findViewById(R.id.textView2))
        data.hint.text = main.resources.getString(R.string.winfxkliba_empty);
        if (item.hint != null) data.hint.text = item.hint;
        else if (item.title != null) data.hint.text = item.title;
        data.item = item;
        view.tag = data;
        return view;
    }

    private fun getView(item: SettingItem): View {
        return View.inflate(main, item.type!!.res, null);
    }
}