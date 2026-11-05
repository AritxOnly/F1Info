package com.aritxonly.f1info.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.*
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** About composition follows Deadliner: floating glow, centered identity, translucent information groups. */
@Composable
fun AboutContent() {
    LazyColumn(Modifier.fillMaxSize(), state = LocalSecondaryListState.current, contentPadding = secondaryContentPadding()) {
        item {
            Column(Modifier.fillMaxWidth().padding(vertical = 36.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(com.aritxonly.f1info.R.drawable.ic_about_f1info), "F1Info 应用图标", Modifier.size(88.dp))
                Text("F1Info", style = MiuixTheme.textStyles.title1, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 22.dp))
                Text("你的围场，每一个比赛周末", style = MiuixTheme.textStyles.footnote1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.padding(top = 8.dp))
                Text(com.aritxonly.f1info.BuildConfig.VERSION_NAME, style = MiuixTheme.textStyles.footnote2, modifier = Modifier.padding(top = 12.dp))
            }
            SettingsSection(topLabel = "关于应用") {
                InfoRow("开发者", "aritxonly")
                InfoRow("界面与材质", "MIUIX / Deadliner / HyperModifier")
                InfoRow("数据来源", "Jolpica F1 / OpenF1")
                InfoRow("艺术数字字体", "Oxanium · OFL")
                InfoRow("赛道几何", "bacinger/f1-circuits · MIT")
                InfoRow("图片与标识", "Formula 1 / Simple Icons")
            }
            Text("非官方 F1 信息应用。赛程、积分与比赛结果来自 Jolpica，比赛动态来自 OpenF1。品牌标识与肖像属于其原权利方。",
                style = MiuixTheme.textStyles.footnote2, color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(vertical = 12.dp))
        }
    }
}

