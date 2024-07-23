/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/5  上午10:30*/
package com.winfxk.winfxklia.view.setting;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.Nullable;
import com.winfxk.winfxklia.BaseActivity;
import com.winfxk.winfxklia.dialog.Toast;
import com.winfxk.winfxklia.view.setting.data.LineView;

import java.util.List;

public abstract class BaseSetting extends BaseActivity implements AdapterView.OnItemClickListener {
    protected SettingAdapter adapter;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeSetting();
    }

    protected abstract List<SettingItem> getMenus();

    protected abstract ListView getListView();

    private void initializeSetting() {
        adapter = new SettingAdapter(this);
        getListView().setAdapter(adapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (view == null || view.getTag() == null || !(view.getTag() instanceof LineView)) return;
            LineView data = (LineView) view.getTag();
            if (data.item == null || data.item.getListener() == null) return;
            data.item.getListener().onSettingChanged(this, view);
        } catch (Exception e) {
            Log.e(getTAG(), "在监听ListView Item 点击事件时出现异常！", e);
            Toast.makeText(this, "出错了！请稍后重试！\n" + e.getMessage()).show();
        }
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }
}
