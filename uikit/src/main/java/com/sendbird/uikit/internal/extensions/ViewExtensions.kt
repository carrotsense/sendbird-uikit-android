package com.sendbird.uikit.internal.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.sendbird.uikit.consts.StringSet

@Suppress("DEPRECATION")
internal fun TextView.setAppearance(context: Context, res: Int) {
    if (Build.VERSION.SDK_INT < 23) {
        setTextAppearance(context, res)
    } else {
        setTextAppearance(res)
    }
}

internal fun EditText.setCursorDrawable(context: Context, res: Int) {
    ContextCompat.getDrawable(context, res)?.let {
        setCursorDrawable(it)
    }
}

@SuppressLint("DiscouragedPrivateApi")
internal fun EditText.setCursorDrawable(cursor: Drawable) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        textCursorDrawable = cursor
    } else {
        try {
            val f = TextView::class.java.getDeclaredField(StringSet.mCursorDrawableRes)
            f.isAccessible = true
            f[this] = cursor
        } catch (t: Throwable) {
        }
    }
}

internal fun View.loadToBackground(url: String, radius: Int = 0, useRipple: Boolean = false) {
    var builder = Glide.with(this)
        .asDrawable()
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
    if (radius > 0) {
        builder = builder.apply(RequestOptions().transform(RoundedCorners(context.resources.intToDp(radius))))
    }
    builder.into(object : CustomTarget<Drawable>() {
        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            if (useRipple) this@loadToBackground.addRipple(resource) else background = resource
        }

        override fun onLoadCleared(placeholder: Drawable?) {
        }
    })
}

internal fun ImageView.load(url: String) {
    Glide.with(this)
        .asDrawable()
        .load(url)
        // If the height of the image sets as a warp, it needs to be set to a specific size because it is unnatural when scrolling.(with adjustViewBounds true)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

internal fun ImageView.load(url: String, resizingSize: Pair<Int, Int>?) {
    var request = Glide.with(this)
        .asDrawable()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
    resizingSize?.let {
        if (it.first > 0 && it.second > 0) {
            request = request.override(it.first, it.second)
        }
    }
    request.load(url).into(this)
}

internal fun View.addRipple(background: Drawable?) = with(TypedValue()) {
    context.theme.resolveAttribute(android.R.attr.colorControlHighlight, this, true)
    val color = ContextCompat.getColor(context, resourceId)
    this@addRipple.background = createRippleDrawable(color, background)
}

private fun createRippleDrawable(pressedColor: Int, backgroundDrawable: Drawable?): RippleDrawable {
    return RippleDrawable(getPressedState(pressedColor), backgroundDrawable, null)
}

private fun getPressedState(pressedColor: Int): ColorStateList {
    return ColorStateList(arrayOf(intArrayOf()), intArrayOf(pressedColor))
}
