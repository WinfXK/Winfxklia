/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/7/17  上午8:43*/
package com.winfxk.winfxklia.tool.utils

import com.alibaba.fastjson2.JSON
import com.alibaba.fastjson2.JSONArray
import com.winfxk.winfxklia.tool.Tool
import java.math.BigDecimal

@Suppress("unused")
class MyList<T>() : ArrayList<T>() {
    constructor(list: List<T>) : this() {
        addAll(list)
    }

    fun getStringList(index: Int, default: List<String?>? = null): MyList<String?>? {
        val any = get(index) ?: return if (default == null) null else MyList(default);
        if (any is List<*>) {
            val list = MyList<String?>();
            for (value in any) list.add(Tool.objToString(value, null));
            return list;
        }
        return if (default == null) null else MyList(default);
    }

    fun getList(index: Int, default: List<Any?>? = null): MyList<Any?>? {
        val any = get(index) ?: return if (default == null) null else MyList(default);
        if (any is List<*>) {
            val base = any as List<Any?>;
            val list = MyList<Any?>();
            list.addAll(base);
            return list;
        }
        return if (default == null) null else MyList(default);
    }

    fun getMap(index: Int, default: Map<Any, Any?>? = null): MyMap<Any, Any?>? {
        val any = get(index) ?: return if (default == null) null else MyMap(default);
        if (any is Map<*, *>) {
            val map = MyMap<Any, Any?>();
            for ((k, v) in any) if (k != null) map[k] = v;
            return map;
        }
        return if (default == null) null else MyMap(default);
    }

    fun getBigDecimal(index: Int, default: BigDecimal? = null): BigDecimal? {
        val any = get(index) ?: return default;
        return Tool.objToBigDecimal(any, default);
    }

    fun getInt(index: Int, default: Int = 0): Int {
        val any = get(index) ?: return default;
        return Tool.ObjToInt(any, default);
    }

    fun getBoolean(index: Int, default: Boolean = false): Boolean {
        val any = get(index) ?: return default;
        return Tool.ObjToBool(any, default);
    }

    fun getDouble(index: Int, default: Double = 0.0): Double {
        val any = get(index) ?: return default;
        return Tool.objToDouble(any, default);
    }

    fun getFloat(index: Int, default: Float = 0f): Float {
        val any = get(index) ?: return default;
        return Tool.objToFloat(any, default);
    }

    fun getLong(index: Int, default: Long = 0L): Long {
        val any = get(index) ?: return default;
        return Tool.objToLong(any, default);
    }

    fun getString(index: Int, default: String? = null): String? {
        val any = get(index) ?: return default;
        return Tool.objToString(any, default);
    }

    fun toJsonString(): String {
        return JSON.toJSONString(this);
    }

    fun toJsonArray(): JSONArray {
        return JSONArray(this);
    }

    companion object {
        fun <T> make(vararg values: T?): MyList<T?> {
            val list = MyList<T?>();
            for (value in values) list.add(value);
            return list;
        }

        fun <T> make(list: List<T>?): MyList<T?> {
            return MyList(list ?: emptyList())
        }
    }
}