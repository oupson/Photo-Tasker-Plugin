package oupson.phototaskerplugin.activity.test

import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import kotlinx.android.synthetic.main.activity_palette_test.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import oupson.phototaskerplugin.R
import oupson.phototaskerplugin.helper.OverlayHelper

class PaletteTestActivity : AppCompatActivity() {
    private val wallpaperManager : WallpaperManager by lazy {
        WallpaperManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_palette_test)
    }

    override fun onResume() {
        super.onResume()

        loadColors()
        val handler = Handler()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            wallpaperManager.addOnColorsChangedListener({ _, _ -> loadColors() }, handler)
        }
    }

    private fun loadColors() = GlobalScope.launch(Dispatchers.IO) {
        val bitmap = if (wallpaperManager.wallpaperInfo != null) {
            if (wallpaperManager.wallpaperInfo.packageName == "net.nurik.roman.muzei") {
                val uri = Uri.parse("content://com.google.android.apps.muzei/artwork")
                val input = contentResolver.openInputStream(uri)
                val btm = BitmapFactory.decodeStream(input)
                input?.close()
                btm
            } else {
                TODO("others : ${wallpaperManager.wallpaperInfo.packageName}")
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                drawableToBitmap(wallpaperManager.getBuiltInDrawable(WallpaperManager.FLAG_SYSTEM))
            } else {
                drawableToBitmap(wallpaperManager.builtInDrawable)
            }
        } ?: return@launch
        withContext(Dispatchers.Main) {
            palette_preview_imageView.setImageBitmap(bitmap)
        }


        val palette = Palette.from(bitmap).generate()
        withContext(Dispatchers.Main) {
            if (palette.lightVibrantSwatch != null) {
                palette_lightVibrant_textView.text =
                    String.format("Light Vibrant : #%06X", palette.lightVibrantSwatch?.rgb)
                palette_lightVibrant_textView.setBackgroundColor(palette.lightVibrantSwatch!!.rgb)
                palette_lightVibrant_textView.setTextColor(palette.lightVibrantSwatch!!.titleTextColor)
            } else {
                palette_lightVibrant_textView.text =
                    String.format("Light Vibrant : null")
                palette_lightVibrant_textView.setBackgroundColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.transparent))
                palette_lightVibrant_textView.setTextColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.black))
            }

            if (palette.vibrantSwatch != null) {
                palette_vibrant_textView.text =
                    String.format("Vibrant : #%06X", palette.vibrantSwatch?.rgb)
                palette_vibrant_textView.setBackgroundColor(palette.vibrantSwatch!!.rgb)
                palette_vibrant_textView.setTextColor(palette.vibrantSwatch!!.titleTextColor)
            } else {
                palette_vibrant_textView.text =
                    String.format("Vibrant : null")
                palette_vibrant_textView.setBackgroundColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.transparent))
                palette_vibrant_textView.setTextColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.black))
            }

            if (palette.darkVibrantSwatch != null) {
                palette_darkVibrant_textView.text =
                    String.format("Dark Vibrant : #%06X", palette.darkVibrantSwatch?.rgb)
                palette_darkVibrant_textView.setBackgroundColor(palette.darkVibrantSwatch!!.rgb)
                palette_darkVibrant_textView.setTextColor(palette.darkVibrantSwatch!!.titleTextColor)
            } else {
                palette_darkVibrant_textView.text =
                    String.format("Dark Vibrant : null")
                palette_darkVibrant_textView.setBackgroundColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.transparent))
                palette_darkVibrant_textView.setTextColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.black))
            }

            if (palette.lightMutedSwatch != null) {
                palette_lightMuted_textView.text =
                    String.format("Light Muted : #%06X", palette.lightMutedSwatch?.rgb)
                palette_lightMuted_textView.setBackgroundColor(palette.lightMutedSwatch!!.rgb)
                palette_lightMuted_textView.setTextColor(palette.lightMutedSwatch!!.titleTextColor)
            } else {
                palette_lightMuted_textView.text =
                    String.format("Light Muted : null")
                palette_lightMuted_textView.setBackgroundColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.transparent))
                palette_lightMuted_textView.setTextColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.black))
            }

            if (palette.mutedSwatch != null) {
                palette_muted_textView.text =
                    String.format("Muted : #%06X", palette.mutedSwatch?.rgb)
                palette_muted_textView.setBackgroundColor(palette.mutedSwatch!!.rgb)
                palette_muted_textView.setTextColor(palette.mutedSwatch!!.titleTextColor)
            } else {
                palette_muted_textView.text =
                    String.format("Muted : null")
                palette_muted_textView.setBackgroundColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.transparent))
                palette_muted_textView.setTextColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.black))
            }

            if (palette.darkMutedSwatch != null) {
                palette_darkMuted_textView.text =
                    String.format("Light Muted : #%06X", palette.darkMutedSwatch?.rgb)
                palette_darkMuted_textView.setBackgroundColor(palette.darkMutedSwatch!!.rgb)
                palette_darkMuted_textView.setTextColor(palette.darkMutedSwatch!!.titleTextColor)
            } else {
                palette_darkMuted_textView.text =
                    String.format("Dark Muted : null")
                palette_darkMuted_textView.setBackgroundColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.transparent))
                palette_darkMuted_textView.setTextColor(ContextCompat.getColor(this@PaletteTestActivity, android.R.color.black))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val suggestion = OverlayHelper.getSuggestion(this@PaletteTestActivity, bitmap)
            val colorList = OverlayHelper.getColorList(this@PaletteTestActivity, suggestion.first).entries.associateBy({it.value}) {it.key}
            withContext(Dispatchers.Main) {
                suggestion_text_View.text = String.format("Suggestion : %s (#%08X)", suggestion.second, colorList[suggestion.second])
                suggestion_text_View.setBackgroundColor(colorList[suggestion.second]!!)
                suggestion_text_View.setTextColor(if (suggestion.first) getColor(android.R.color.white) else getColor(android.R.color.black))
            }
        } else {
            withContext(Dispatchers.Main) {
                suggestion_text_View.visibility = View.GONE
            }
        }
    }


    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }
        var width = drawable.intrinsicWidth
        width = if (width > 0) width else 1
        var height = drawable.intrinsicHeight
        height = if (height > 0) height else 1
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
