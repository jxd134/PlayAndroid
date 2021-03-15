package com.zj.play.compose.repository

import com.zj.network.base.PlayAndroidNetwork

/**
 * 版权：Zhujiang 个人版权
 * @author zhujiang
 * 版本：1.5
 * 创建日期：2020/5/17
 * 描述：PlayAndroid
 *
 */

class AccountRepository {

    suspend fun getLogin(username: String, password: String) =
        PlayAndroidNetwork.getLogin(username, password)

    suspend fun getRegister(username: String, password: String, repassword: String) =
        PlayAndroidNetwork.getRegister(username, password, repassword)

    suspend fun getLogout() = PlayAndroidNetwork.getLogout()

}