package com.jack.fisheye

import android.graphics.Bitmap

object BitmapUtils {

    /**
     * Convert Bitmap -> IntArray (ARGB8888)
     */
    fun toPixels(bitmap: Bitmap): IntArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)

        bitmap.getPixels(
            pixels,
            0,
            width,
            0,
            0,
            width,
            height
        )
        return pixels
    }

    /**
     * Convert IntArray -> Bitmap
     */
    fun fromPixels(
        pixels: IntArray,
        width: Int,
        height: Int
    ): Bitmap {

        val bitmap = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        bitmap.setPixels(
            pixels,
            0,
            width,
            0,
            0,
            width,
            height
        )

        return bitmap
    }

    /**
     * Copy Bitmap
     */
    fun copy(bitmap: Bitmap): Bitmap {
        return bitmap.copy(
            Bitmap.Config.ARGB_8888,
            true
        )
    }

}