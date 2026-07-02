package com.jack.fisheye

import android.graphics.Bitmap
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object FisheyeMapper {

    var OUTPUT_FOV = 90.0

    fun buildFrontMap(
        inputWidth: Int,
        inputHeight: Int,
        outputWidth: Int,
        outputHeight: Int,
        yaw: Double = -40.0,
        pitch: Double = -50.0,
    ): Array<Array<PointMap>> {
        return buildMap(
            inputWidth,
            inputHeight,
            outputWidth,
            outputHeight,
            yaw = yaw,
            pitch = pitch,
        )
    }

    fun buildRearMap(
        inputWidth: Int,
        inputHeight: Int,
        outputWidth: Int,
        outputHeight: Int,
        yaw: Double = 40.0,
        pitch: Double = -50.0,
    ): Array<Array<PointMap>> {
        return buildMap(
            inputWidth,
            inputHeight,
            outputWidth,
            outputHeight,
            yaw = yaw,
            pitch = pitch,
        )
    }

    /**
     * Demo Equidistant Fisheye
     */
    private fun buildMap(
        inputWidth: Int,
        inputHeight: Int,
        outputWidth: Int,
        outputHeight: Int,
        yaw: Double,
        pitch: Double
    ): Array<Array<PointMap>> {
        val map = Array(outputHeight) {
            Array(outputWidth) {
                PointMap(0f, 0f)
            }
        }
        val cx = inputWidth / 2.0
        val cy = inputHeight / 2.0
        val radius = min(inputWidth, inputHeight) / 2.0
        val focal = outputWidth / (2.0 * tan(Math.toRadians(OUTPUT_FOV / 2)))

        for (y in 0 until outputHeight) {
            for (x in 0 until outputWidth) {
                //---------------------------------------
                // Perspective Camera
                //---------------------------------------

                var vx = (x - outputWidth / 2.0) / focal
                var vy = (y - outputHeight  * 0.4) / focal
                var vz = 1.0

                //---------------------------------------
                // Normalize
                //---------------------------------------

                val len = sqrt(vx * vx + vy * vy + vz * vz)
                vx /= len
                vy /= len
                vz /= len

                //---------------------------------------
                // Rotate Pitch
                //---------------------------------------

                val pitchRad = Math.toRadians(pitch)
                val py = cos(pitchRad) * vy - sin(pitchRad) * vz
                val pz = sin(pitchRad) * vy + cos(pitchRad) * vz

                //---------------------------------------
                // Rotate Yaw
                //---------------------------------------
                val yawRad = Math.toRadians(yaw)
                val rx = cos(yawRad) * vx + sin(yawRad) * pz
                val rz = -sin(yawRad) * vx + cos(yawRad) * pz
                val ry = py

                //---------------------------------------
                // Convert to fisheye
                //---------------------------------------

                val theta = acos(rz)
                val phi = atan2(ry, rx)

                /**
                 * Equidistant Projection
                 *
                 * r = f * theta
                 */
                val r = radius * theta / Math.PI
                val sx = cx + r * cos(phi)
                val sy = cy + r * sin(phi)

                map[y][x] = PointMap(
                    sx.toFloat(),
                    sy.toFloat()
                )
            }
        }
        return map
    }

    fun remap(
        input: Bitmap,
        map: Array<Array<PointMap>>,
        outputWidth: Int,
        outputHeight: Int
    ): Bitmap {

        val inputPixels = BitmapUtils.toPixels(input)
        val outputPixels = IntArray(outputWidth * outputHeight)
        val inputWidth = input.width
        val inputHeight = input.height

        for (y in 0 until outputHeight) {
            for (x in 0 until outputWidth) {
                val point = map[y][x]
                outputPixels[y * outputWidth + x] =
                    bilinear(
                        inputPixels,
                        inputWidth,
                        inputHeight,
                        point.x,
                        point.y
                    )
            }
        }

        return BitmapUtils.fromPixels(
            outputPixels,
            outputWidth,
            outputHeight
        )
    }

    /**
     * Bilinear interpolation
     */
    private fun bilinear(
        pixels: IntArray,
        width: Int,
        height: Int,
        fx: Float,
        fy: Float
    ): Int {

        val x0 = floor(fx).toInt()
        val y0 = floor(fy).toInt()

        val x1 = x0 + 1
        val y1 = y0 + 1

        if (x0 < 0 || y0 < 0 || x1 >= width || y1 >= height)
            return 0xFF000000.toInt()

        val dx = fx - x0
        val dy = fy - y0

        val c00 = sample(pixels, width, x0, y0)
        val c10 = sample(pixels, width, x1, y0)
        val c01 = sample(pixels, width, x0, y1)
        val c11 = sample(pixels, width, x1, y1)

        val r = interpolate(
            (c00 shr 16) and 255,
            (c10 shr 16) and 255,
            (c01 shr 16) and 255,
            (c11 shr 16) and 255,
            dx,
            dy
        )

        val g = interpolate(
            (c00 shr 8) and 255,
            (c10 shr 8) and 255,
            (c01 shr 8) and 255,
            (c11 shr 8) and 255,
            dx,
            dy
        )

        val b = interpolate(
            c00 and 255,
            c10 and 255,
            c01 and 255,
            c11 and 255,
            dx,
            dy
        )

        return (255 shl 24) or
                (r shl 16) or
                (g shl 8) or
                b
    }

    private fun interpolate(
        c00: Int,
        c10: Int,
        c01: Int,
        c11: Int,
        dx: Float,
        dy: Float
    ): Int {
        val top = c00 * (1f - dx) + c10 * dx
        val bottom = c01 * (1f - dx) + c11 * dx
        return (top * (1f - dy) + bottom * dy).toInt()
    }

    private fun sample(
        pixels: IntArray,
        width: Int,
        x: Int,
        y: Int
    ): Int {
        return pixels[y * width + x]
    }
}