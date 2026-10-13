package com.aritxonly.f1info.data.racing

import java.net.HttpURLConnection
import java.net.URI

class RacingHttpError(val code: Int) : Exception(when (code) {
    401, 403 -> "需要有效的 OpenF1 实时授权，请在“实时授权与超级岛”中登录。"
    429 -> "数据服务请求过于频繁，请稍后下拉重试。"
    else -> "数据服务暂时不可用（$code）。"
})
object RacingHttp {
    private var nextOpenRequest = 0L
    fun get(url: String, bearer: String = ""): String {
        if (url.startsWith("https://api.openf1.org/")) synchronized(this) {
            val wait = nextOpenRequest - System.currentTimeMillis()
            if (wait > 0) Thread.sleep(wait)
            nextOpenRequest = System.currentTimeMillis() + 400
        }
        val c = URI(url).toURL().openConnection() as HttpURLConnection
        c.connectTimeout = 10000; c.readTimeout = 15000
        c.setRequestProperty("Accept", "application/json")
        if (bearer.isNotBlank()) c.setRequestProperty("Authorization", "Bearer $bearer")
        return try {
            if (c.responseCode != 200) throw RacingHttpError(c.responseCode)
            c.inputStream.bufferedReader().use { it.readText() }
        } finally { c.disconnect() }
    }
}
