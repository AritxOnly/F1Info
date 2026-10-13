package com.aritxonly.f1info.notification

import android.content.Context
import android.graphics.drawable.Icon
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.core.content.ContextCompat
import android.os.Bundle
import androidx.annotation.DrawableRes
import org.json.JSONObject
import com.aritxonly.f1info.R

// Deadliner 210e4a2 XiaomiIslandExtras generic Focus V3 template and icon closure.
internal object XiaomiIslandExtras {
    private const val PICTURE = "miui.focus.pic_f1info"
    private const val SMALL_PICTURE = "miui.land.pic_f1info"
    private data class IconKey(val resource: Int, val size: Int, val tint: String?, val contentSize: Int)
    private val icons = mutableMapOf<IconKey, Icon>()
    internal fun bitmapIcon(context: Context, resource: Int, size: Int, tint: String? = null, contentSize: Int = size): Icon = synchronized(icons) {
        icons.getOrPut(IconKey(resource, size, tint, contentSize)) {
            val drawable = requireNotNull(ContextCompat.getDrawable(context, resource)).mutate()
            if (tint != null) drawable.setColorFilter(Color.parseColor(tint), PorterDuff.Mode.SRC_IN)
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val content = contentSize.coerceIn(1, size)
            val inset = (size - content) / 2
            drawable.setBounds(inset, inset, inset + content, inset + content)
            drawable.draw(Canvas(bitmap))
            Icon.createWithBitmap(bitmap)
        }
    }
    fun create(context: Context, title: String, content: String, status: String, @DrawableRes icon: Int): Bundle {
        val picture = JSONObject().put("type", 1).put("pic", PICTURE)
        val island = JSONObject().put("islandProperty", 1)
            .put("bigIslandArea", JSONObject()
                .put("imageTextInfoLeft", JSONObject().put("type", 1).put("picInfo", picture)
                    .put("textInfo", JSONObject().put("title", title).put("narrowFont", false)))
                .put("textInfo", JSONObject().put("title", status).put("narrowFont", false)))
            .put("smallIslandArea", JSONObject().put("combinePicInfo", JSONObject().put("picInfo",
                JSONObject().put("type", 1).put("pic", SMALL_PICTURE))))
        val parameters = JSONObject().put("business", "f1info_live")
            .put("isShowNotification", true).put("enableFloat", false).put("updatable", true)
            .put("islandFirstFloat", false).put("filterWhenNoPermission", false)
            .put("ticker", status).put("tickerPic", PICTURE).put("aodTitle", status)
            .put("baseInfo", JSONObject().put("type", 2).put("title", title).put("content", content))
            .put("picInfo", picture)
            .put("param_island", island)
        return Bundle().apply {
            putString("miui.focus.param", JSONObject().put("param_v2", parameters).toString())
            putBundle("miui.focus.pics", Bundle().apply {
                putParcelable(PICTURE, bitmapIcon(context, R.mipmap.ic_launcher, 120))
                putParcelable(SMALL_PICTURE, bitmapIcon(context, icon, 88))
            })
        }
    }
}
