package com.example.matrixchat.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.text.TextUtils
import android.util.TypedValue
import android.view.Menu
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.matrix.androidsdk.core.Log
import java.util.*

/**
 * Util class for managing themes.
 */
object ThemeUtils {
    const val LOG_TAG = "ThemeUtils"

    // preference key
    const val APPLICATION_THEME_KEY = "APPLICATION_THEME_KEY"

    // the theme possible values
    private const val THEME_DARK_VALUE = "dark"
    private const val THEME_LIGHT_VALUE = "light"
    private const val THEME_BLACK_VALUE = "black"
    private const val THEME_STATUS_VALUE = "status"

    private val mColorByAttr = HashMap<Int, Int>()

    /**
     * Provides the selected application theme
     *
     * @param context the context
     * @return the selected application theme
     */
    fun getApplicationTheme(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(
                APPLICATION_THEME_KEY,
                THEME_LIGHT_VALUE
            )
    }





    /**
     * Translates color attributes to colors
     *
     * @param c              Context
     * @param colorAttribute Color Attribute
     * @return Requested Color
     */
    @ColorInt
    fun getColor(c: Context, @AttrRes colorAttribute: Int): Int {
        if (mColorByAttr.containsKey(colorAttribute)) {
            return mColorByAttr[colorAttribute] as Int
        }

        var matchedColor: Int

        try {
            val color = TypedValue()
            c.theme.resolveAttribute(colorAttribute, color, true)
            matchedColor = color.data
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Unable to get color", e)
            matchedColor = ContextCompat.getColor(c, android.R.color.holo_red_dark)
        }

        mColorByAttr[colorAttribute] = matchedColor

        return matchedColor
    }

    /**
     * Get the resource Id applied to the current theme
     *
     * @param c          the context
     * @param resourceId the resource id
     * @return the resource Id for the current theme
     */
    fun getResourceId(c: Context, resourceId: Int): Int {
        if (TextUtils.equals(
                getApplicationTheme(c),
                THEME_LIGHT_VALUE
            )
            || TextUtils.equals(
                getApplicationTheme(c),
                THEME_STATUS_VALUE
            )) {
        }
        return resourceId
    }

    /**
     * Update the menu icons colors
     *
     * @param menu  the menu
     * @param color the color
     */
    fun tintMenuIcons(menu: Menu, color: Int) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            val drawable = item.icon
            if (drawable != null) {
                val wrapped = DrawableCompat.wrap(drawable)
                drawable.mutate()
                DrawableCompat.setTint(wrapped, color)
                item.icon = drawable
            }
        }
    }

    /**
     * Tint the drawable with a theme attribute
     *
     * @param context   the context
     * @param drawable  the drawable to tint
     * @param attribute the theme color
     * @return the tinted drawable
     */
    fun tintDrawable(context: Context, drawable: Drawable, @AttrRes attribute: Int): Drawable {
        return tintDrawableWithColor(
            drawable,
            getColor(context, attribute)
        )
    }

    /**
     * Tint the drawable with a color integer
     *
     * @param drawable the drawable to tint
     * @param color    the color
     * @return the tinted drawable
     */
    fun tintDrawableWithColor(drawable: Drawable, @ColorInt color: Int): Drawable {
        val tinted = DrawableCompat.wrap(drawable)
        drawable.mutate()
        DrawableCompat.setTint(tinted, color)
        return tinted
    }
}