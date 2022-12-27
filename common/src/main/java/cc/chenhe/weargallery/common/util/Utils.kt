/**
 * Copyright (C) 2020 Chenhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cc.chenhe.weargallery.common.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.content.pm.PackageInfoCompat
import cc.chenhe.weargallery.common.log.MLog
import cc.chenhe.weargallery.common.log.Mmap
import com.squareup.moshi.JsonAdapter
import java.io.File
import java.util.*
import kotlin.math.abs

const val HUA_WEI = "https://wg.chenhe.me/announcement/huawei/"
const val GITHUB = "https://github.com/ichenhe/wear-gallery/"
const val TELEGRAM = "https://t.me/weargallery_news"
const val API_VER = "https://wg.chenhe.me/api/ver.json"
const val LIVE_PREVIEW_HELP_URL = "https://wg.chenhe.me/tutorials/live-preview/"

const val CHECK_UPDATE_INTERVAL = 24 * 3600 * 3

fun xlogAppenderCloseSafely() {
    if (MLog.isInitialized()) {
        Mmap.flush()
    }
}

fun xlogAppenderFlushSafely() {
    if (MLog.isInitialized()) {
        Mmap.flush()
    }
}

fun checkHuaWei(): Boolean {
    return android.os.Build.MANUFACTURER.lowercase(Locale.getDefault()).contains("huawei")
}

fun getVersionCode(context: Context): Long {
    return try {
        val manager = context.packageManager
        PackageInfoCompat.getLongVersionCode(manager.getPackageInfo(context.packageName, 0))
    } catch (e: Exception) {
        e.printStackTrace()
        -1
    }
}

fun getVersionName(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

fun getLogDir(context: Context): File = File(context.externalCacheDir ?: context.cacheDir, "log")

private const val MILLIS_IN_DAY = 24 * 3600 * 1000L

/**
 * Determine if it's the same day for two timestamps in ms.
 * Calculate it directly instead of use [Calendar] for better performance.
 *
 * @param t1 Timestamp in millis.
 * @param t2 Timestamp in millis.
 */
fun isSameDay(t1: Long, t2: Long): Boolean {
    if (abs(t1 - t2) > MILLIS_IN_DAY) {
        return false
    }
    return (t1 + TimeZone.getDefault().getOffset(t1)) / MILLIS_IN_DAY ==
            (t2 + TimeZone.getDefault().getOffset(t2)) / MILLIS_IN_DAY
}

inline val String.fileName: String
    get() {
        val s = this.lastIndexOf(File.separator) + 1
        return if (s > 0) {
            this.substring(s)
        } else {
            this
        }
    }

inline val String.filePath: String?
    get() {
        val s = this.lastIndexOf(File.separator)
        return when {
            s > 0 -> this.substring(0, s)
            s == 0 -> this.first().toString()
            else -> null
        }
    }

/**
 * Parse json quietly.
 *
 * @return `null` if any exceptions are caught.
 */
fun <T> JsonAdapter<T>.fromJsonQ(string: String): T? {
    return try {
        fromJson(string)
    } catch (e: Exception) {
        null
    }
}

/**
 * Try to get the corresponding Activity。Usually used in Compose。
 */
fun Context.getActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.getActivity()
    else -> null
}