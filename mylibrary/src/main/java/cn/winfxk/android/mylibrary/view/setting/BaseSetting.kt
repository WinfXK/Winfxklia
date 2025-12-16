package cn.winfxk.android.mylibrary.view.setting

import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import cn.winfxk.android.mylibrary.BaseActivity
import cn.winfxk.android.mylibrary.view.setting.items.BaseItem

abstract class BaseSetting : BaseActivity(), AdapterView.OnItemClickListener {
    protected open val adapter by lazy { SettingAdapter(this) }
    /**
     * 当前页的所有设置项目
     */
    abstract val items: ArrayList<BaseItem>
    /**
     * 显示设置列表的Listview
     */
    abstract val listView: ListView
    /**
     * 初始化完毕后调用
     */
    abstract fun initView();
    override fun initializeView() {
        listView.adapter = adapter;
        listView.onItemClickListener = this;
        items.forEach {
            it.context = this;
            it.init();
        }
        initView();
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        adapter.getItem(position).onItemClick()
    }
}