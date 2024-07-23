/*Created by IntelliJ IDEA.
  User: kc4064 
  Date: 2024/6/17  下午1:25*/
package com.winfxk.winfxklia.tool.utils

import com.alibaba.fastjson2.JSONObject
import com.winfxk.winfxklia.tool.Tool
import java.math.BigDecimal

@Suppress("unused")
class MyMap<T, V>() : HashMap<T, V>() {
    constructor(key: T, value: V) : this() {
        this[key] = value
    }

    constructor(map: Map<T, V>?) : this() {
        this.putAll(map !!)
    }

    fun add(key: T, value: V): MyMap<T, V> {
        this[key] = value
        return this
    }

    fun getKey(value: V): ArrayList<T> {
        val keys = ArrayList<T>();
        if (! containsValue(value)) return keys;
        for ((k, v) in this) if (v == value) keys.add(k);
        return keys;
    }

    fun getStringMap(key: T, default: Map<String, Any?>? = null): MyMap<String, Any?>? {
        val any = get(key) ?: return if (default == null) null else make(default);
        if (any is Map<*, *>) {
            val map = MyMap<String, Any?>();
            for ((k, v) in any) map[Tool.objToString(k, null)] = v;
            return map;
        }
        return if (default == null) null else make(default);
    }

    fun getMap(key: T, default: Map<Any, Any?>? = null): MyMap<Any, Any?>? {
        val any = get(key) ?: return if (default != null) MyMap(default) else null;
        if (any is Map<*, *>) {
            val map = MyMap<Any, Any?>();
            for ((k, v) in any) if (k != null) map[k] = v
            return map;
        }
        return if (default != null) MyMap(default) else null;
    }

    fun getStringList(key: T, default: ArrayList<String?>? = null): MyList<String?>? {
        val list = getList(key, default);
        if (list.isNullOrEmpty()) return if (default == null) null else MyList(default);
        val ok = MyList<String?>();
        for (item in list) ok.add(Tool.objToString(item))
        return ok;
    }

    fun getList(key: T, default: List<*>? = null): MyList<*>? {
        val any = get(key) ?: return if (default == null) null else MyList(default);
        return if (any is List<*>) return any as MyList<*> else if (default == null) null else MyList(default);
    }

    fun getBoolean(key: T, default: Boolean = false): Boolean {
        return Tool.ObjToBool(get(key) ?: default, default);
    }

    fun getBigDecimal(key: T, default: BigDecimal? = null): BigDecimal? {
        return Tool.objToBigDecimal(get(key) ?: default, default);
    }

    fun getInt(key: T, default: Int = 0): Int {
        return Tool.ObjToInt(get(key) ?: default, default);
    }

    fun getLong(key: T, default: Long = 0L): Long {
        return Tool.objToLong(get(key) ?: default, default);
    }

    fun getDouble(key: T, default: Double = 0.0): Double {
        return Tool.objToDouble(get(key) ?: default, default);
    }

    fun getFloat(key: T, default: Float = 0f): Float {
        return Tool.objToFloat(get(key) ?: default, default);
    }

    fun getString(key: T, default: String? = null): String? {
        return Tool.objToString(get(key) ?: default, default);
    }

    fun toJsonString(): String {
        return JSONObject.toJSONString(this);
    }

    fun toJsonObject(): JSONObject {
        return JSONObject(this);
    }

    fun addAll(map: Map<T, V>): MyMap<T, V> {
        this.putAll(map)
        return this
    }

    companion object {
        fun <T, V> make(key: T, value: V): MyMap<T, V> {
            return MyMap(key, value)
        }

        fun <T, V> make(map: Map<T, V>): MyMap<T, V> {
            return MyMap(map)
        }
    }
}
