package cn.winfxk.android.mylibrary

import com.alibaba.fastjson2.JSONObject


open class WmsRbacUser(val json: JSONObject) {
    /**
     * 工号
     */
    open var userId: String = json.getString("userId")
    /**
     * 名称
     */
    open var userName: String = json.getString("userName")
    /**
     * 部门编码
     */
    open var userDeptc: String = json.getString("userDeptc")
    /**
     * 部门名称
     */
    open var userDeptn: String = json.getString("userDeptn")
    /**
     * 职务
     */
    open var userPos: String = json.getString("userPos")
    /**
     * 邮件
     */
    open var userEmail: String = json.getString("userEmail")
    /**
     * 状态
     */
    open var userState: String = json.getString("userState")
    /**
     * KC号
     */
    open var userKcl: String = json.getString("userKcl")
    /**
     * 密码
     */
    open var userPwd: String = json.getString("userPwd")
    /**
     * 创建日期
     */
    open var userCdate: String = json.getString("userCdate")
    /**
     * 修改日期
     */
    open var userMdate: String = json.getString("userMdate")
    /**
     * 创建人id
     */
    open var userCnameid: String = json.getString("userCnameid")
    /**
     * 创建人名称
     */
    open var userCname: String = json.getString("userCname")
}