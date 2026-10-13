package com.aritxonly.f1info.data.racing

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.net.URI
import java.net.HttpURLConnection
import java.net.URLEncoder
import org.json.JSONObject

/** Account and access token remain on-device, encrypted with an Android Keystore AES key. */
object LiveCredentials {
    private const val ALIAS = "f1info.openf1"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    private fun load(context: Context): JSONObject = runCatching {
        val prefs = context.getSharedPreferences("live_secrets", 0)
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(prefs.getString("iv", ""), Base64.NO_WRAP)))
        JSONObject(String(c.doFinal(Base64.decode(prefs.getString("data", ""), Base64.NO_WRAP)), Charsets.UTF_8))
    }.getOrDefault(JSONObject())
    private fun save(context: Context, value: JSONObject) {
        val c = Cipher.getInstance("AES/GCM/NoPadding"); c.init(Cipher.ENCRYPT_MODE, key())
        val data = c.doFinal(value.toString().toByteArray())
        context.getSharedPreferences("live_secrets", 0).edit().putString("iv", Base64.encodeToString(c.iv, Base64.NO_WRAP))
            .putString("data", Base64.encodeToString(data, Base64.NO_WRAP)).apply()
    }
    fun configured(context: Context) = load(context).optString("token").isNotBlank()
    @Synchronized fun read(context: Context): String {
        val value = load(context)
        if (value.optLong("expires") > System.currentTimeMillis() + 60000) return value.optString("token")
        if (value.optString("username").isNotBlank()) {
            return login(context, value.getString("username"), value.getString("password"))
        }
        return value.optString("token")
    }
    @Synchronized fun login(context: Context, username: String, password: String): String {
        require(username.isNotBlank() && password.isNotBlank()) { "请输入 OpenF1 账号和密码" }
        val c = URI("https://api.openf1.org/token").toURL().openConnection() as HttpURLConnection
        c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 10000; c.readTimeout = 15000
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        val body = "username=${URLEncoder.encode(username, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"
        return try {
            c.outputStream.use { it.write(body.toByteArray()) }
            if (c.responseCode != 200) throw RacingHttpError(c.responseCode)
            val response = JSONObject(c.inputStream.bufferedReader().use { it.readText() })
            val token = response.getString("access_token")
            save(context, JSONObject().put("username", username).put("password", password).put("token", token)
                .put("expires", System.currentTimeMillis() + response.optLong("expires_in", 3600) * 1000))
            token
        } finally { c.disconnect() }
    }
    fun clear(context: Context) { context.getSharedPreferences("live_secrets", 0).edit().clear().apply() }
}
