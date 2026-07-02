package com.example.fisheye.opencv

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.imgproc.Imgproc
import org.opencv.core.Mat
import androidx.core.graphics.createBitmap

object CameraRenderer {
    /**
     * Reusable Mats để tránh tạo object liên tục.
     */
    private val src = Mat()
    private val dst = Mat()

    /**
     * Render bitmap bằng OpenCV remap.
     *
     * input Bitmap
     *      ↓
     * bitmapToMat()
     *      ↓
     * remap()
     *      ↓
     * matToBitmap()
     */
    fun render(
        input: Bitmap,
        output: Bitmap,
        map: OpenCVMapper.OpenCVMap
    ) {
        //-----------------------------------------
        // Bitmap -> Mat
        //-----------------------------------------

        Utils.bitmapToMat(
            input,
            src
        )

        //-----------------------------------------
        // Remap
        //-----------------------------------------

        Imgproc.remap(
            src,
            dst,
            map.map1,
            map.map2,
            Imgproc.INTER_LINEAR
        )

        //-----------------------------------------
        // Mat -> Bitmap
        //-----------------------------------------

        Utils.matToBitmap(
            dst,
            output
        )
    }

    fun release() {
        src.release()
        dst.release()
    }
}