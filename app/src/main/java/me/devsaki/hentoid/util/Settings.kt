package me.devsaki.hentoid.util

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceManager
import me.devsaki.hentoid.enums.PictureEncoder
import kotlin.reflect.KProperty

object Settings {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * FIELDS
     */
    // LIBRARY
    var libraryDisplay: Int by IntSetting(Key.LIBRARY_DISPLAY, Value.LIBRARY_DISPLAY_DEFAULT)
    var libraryDisplayGridFav: Boolean by BoolSetting(Key.LIBRARY_DISPLAY_GRID_FAV, true)
    var libraryDisplayGridRating: Boolean by BoolSetting(Key.LIBRARY_DISPLAY_GRID_RATING, true)
    var libraryDisplayGridSource: Boolean by BoolSetting(Key.LIBRARY_DISPLAY_GRID_SOURCE, true)
    var libraryDisplayGridStorageInfo: Boolean by BoolSetting(
        Key.LIBRARY_DISPLAY_GRID_STORAGE,
        true
    )
    var libraryDisplayGridTitle: Boolean by BoolSetting(Key.LIBRARY_DISPLAY_GRID_TITLE, true)
    var libraryDisplayGridLanguage: Boolean by BoolSetting(Key.LIBRARY_DISPLAY_GRID_LANG, true)
    var libraryGridCardWidthDP: Int by IntSetting(Key.LIBRARY_GRID_CARD_WIDTH, 150)

    // DOWNLOADER

    // LOCK
    var lockType: Int by IntSetting(Key.LOCK_TYPE, 0)

    // MASS OPERATIONS
    var massOperation: Int by IntSetting("MASS_OPERATION", 0)
    var massOperationScope: Int by IntSetting("MASS_SCOPE", 0)

    // TRANSFORM
    var isResizeEnabled: Boolean by BoolSetting("TRANSFORM_RESIZE_ENABLED", false)
    var resizeMethod: Int by IntSetting("TRANSFORM_RESIZE_METHOD", 0)
    var resizeMethod1Ratio: Int by IntSetting("TRANSFORM_RESIZE_1_RATIO", 120)
    var resizeMethod2Height: Int by IntSetting("TRANSFORM_RESIZE_2_HEIGHT", 0)
    var resizeMethod2Width: Int by IntSetting("TRANSFORM_RESIZE_2_WIDTH", 0)
    var resizeMethod3Ratio: Int by IntSetting("TRANSFORM_RESIZE_3_RATIO", 80)
    var transcodeMethod: Int by IntSetting("TRANSFORM_TRANSCODE_METHOD", 0)
    var transcodeEncoderAll: Int by IntSetting(
        "TRANSFORM_TRANSCODE_ENC_ALL",
        PictureEncoder.PNG.value
    )
    var transcodeEncoderLossless: Int by IntSetting(
        "TRANSFORM_TRANSCODE_ENC_LOSSLESS",
        PictureEncoder.PNG.value
    )
    var transcodeEncoderLossy: Int by IntSetting(
        "TRANSFORM_TRANSCODE_ENC_LOSSY",
        PictureEncoder.JPEG.value
    )
    var transcodeQuality: Int by IntSetting("TRANSFORM_TRANSCODE_QUALITY", 90)

    // ARCHIVES
    var archiveTargetFolder: String by StringSetting(
        "ARCHIVE_TARGET_FOLDER",
        Value.ARCHIVE_TARGET_FOLDER_DOWNLOADS
    )
    var latestTargetFolderUri: String by StringSetting("ARCHIVE_TARGET_FOLDER_LATEST", "")
    var archiveTargetFormat: Int by IntSetting("ARCHIVE_TARGET_FORMAT", 0)
    var isArchiveOverwrite: Boolean by BoolSetting("ARCHIVE_OVERWRITE", true)
    var isArchiveDeleteOnSuccess: Boolean by BoolSetting("ARCHIVE_DELETE_ON_SUCCESS", false)

    // BROWSER
    var isBrowserAugmented: Boolean by BoolSetting(Key.WEB_AUGMENTED_BROWSER, true)
    var isAdBlockerOn: Boolean by BoolSetting(Key.WEB_ADBLOCKER, true)
    var isBrowserForceLightMode: Boolean by BoolSetting(Key.WEB_FORCE_LIGHTMODE, false)
    var isBrowserLanguageFilter: Boolean by BoolSetting("pref_browser_language_filter", false)
    var browserLanguageFilterValue: String by StringSetting("pref_language_filter_value", "english")

    // READER
    var colorDepth: Int by IntSetting(Key.READER_COLOR_DEPTH, 0)

    // ACHIEVEMENTS
    var achievements: ULong by ULongSetting(Key.ACHIEVEMENTS, 0UL)
    var nbAIRescale: Int by IntSetting(Key.ACHIEVEMENTS_NB_AI_RESCALE, 0)

    // MISC
    var isTextMenuOn: Boolean by BoolSetting(Key.TEXT_SELECT_MENU, false)


    // Public Helpers

    fun registerPrefsChangedListener(listener: OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterPrefsChangedListener(listener: OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }


    // Delegates

    private class ULongSetting(val key: String, val default: ULong) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): ULong {
            return (sharedPreferences.getString(key, default.toString()) + "").toULong()
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: ULong) {
            sharedPreferences.edit().putString(key, value.toString()).apply()
        }
    }

    private class IntSetting(val key: String, val default: Int) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
            return (sharedPreferences.getString(key, default.toString()) + "").toInt()
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
            sharedPreferences.edit().putString(key, value.toString()).apply()
        }
    }

    private class BoolSetting(val key: String, val default: Boolean) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
            return sharedPreferences.getBoolean(key, default)
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            sharedPreferences.edit().putBoolean(key, value).apply()
        }
    }

    private class StringSetting(val key: String, val default: String) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
            return sharedPreferences.getString(key, default) ?: ""
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
            sharedPreferences.edit().putString(key, value).apply()
        }
    }


    // Consts
    object Key {
        const val LIBRARY_DISPLAY = "pref_library_display"
        const val READER_COLOR_DEPTH = "viewer_color_depth"
        const val LOCK_TYPE = "LOCK_TYPE"
        const val LIBRARY_DISPLAY_GRID_FAV = "LIBRARY_DISPLAY_GRID_FAV"
        const val LIBRARY_DISPLAY_GRID_RATING = "LIBRARY_DISPLAY_GRID_RATING"
        const val LIBRARY_DISPLAY_GRID_SOURCE = "LIBRARY_DISPLAY_GRID_SOURCE"
        const val LIBRARY_DISPLAY_GRID_STORAGE = "LIBRARY_DISPLAY_GRID_STORAGE"
        const val LIBRARY_DISPLAY_GRID_TITLE = "LIBRARY_DISPLAY_GRID_TITLE"
        const val LIBRARY_DISPLAY_GRID_LANG = "LIBRARY_DISPLAY_GRID_LANG"
        const val LIBRARY_GRID_CARD_WIDTH = "grid_card_width"
        const val ACHIEVEMENTS = "achievements"
        const val ACHIEVEMENTS_NB_AI_RESCALE = "ach_nb_ai_rescale"
        const val WEB_AUGMENTED_BROWSER = "pref_browser_augmented"
        const val WEB_ADBLOCKER = "WEB_ADBLOCKER"
        const val WEB_FORCE_LIGHTMODE = "WEB_FORCE_LIGHTMODE"
        const val TEXT_SELECT_MENU = "TEXT_SELECT_MENU"
    }

    object Value {
        const val ARCHIVE_TARGET_FOLDER_DOWNLOADS = "downloads"

        const val LIBRARY_DISPLAY_LIST = 0
        const val LIBRARY_DISPLAY_GRID = 1
        const val LIBRARY_DISPLAY_DEFAULT = LIBRARY_DISPLAY_LIST
    }
}