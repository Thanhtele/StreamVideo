package com.example.fisheye.opencv

import android.util.Log
import org.opencv.calib3d.Calib3d
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

object OpenCVMapper {

    data class OpenCVMap(
        val map1: Mat,
        val map2: Mat
    )

    /**
     * Cache map.
     *
     * Key:
     * width_height_hFov_vFov_yaw_pitch_roll
     */
    private val cache = HashMap<String, OpenCVMap>()

    fun create(
        calibration: CameraCalibration,
        camera: VirtualCamera
    ): OpenCVMap {
        val key = buildKey(camera)
        cache[key]?.let {
            return it
        }

        val r = buildRotationMatrix(camera)
        Log.d("MAP","Rotation Matrix: ${r.dump()}")

        val p = buildProjectionMatrix(camera)
        Log.d("MAP","Projection Matrix: ${p.dump()}")

        val map1 = Mat()
        val map2 = Mat()

        Calib3d.fisheye_initUndistortRectifyMap(
            calibration.K,
            calibration.D,
            r,
            p,
            Size(
                camera.outputWidth.toDouble(),
                camera.outputHeight.toDouble()
            ),
            CvType.CV_32FC1,
            map1,
            map2
        )

        return OpenCVMap(
            map1,
            map2
        ).also {
            cache[key] = it
        }
    }

    /**
     * Remove all cached map.
     *
     * Call when calibration changed.
     */
    fun clearCache() {
        cache.values.forEach {
            it.map1.release()
            it.map2.release()
        }
        cache.clear()
    }

    /**
     * Rotation Matrix
     */
    private fun buildRotationMatrix(
        camera: VirtualCamera
    ): Mat {
        val yaw = Math.toRadians(camera.yaw)
        val pitch = Math.toRadians(camera.pitch)
        val roll = Math.toRadians(camera.roll)

        val cy = cos(yaw)
        val sy = sin(yaw)

        val cp = cos(pitch)
        val sp = sin(pitch)

        val cr = cos(roll)
        val sr = sin(roll)

        return Mat(
            3,
            3,
            CvType.CV_64F
        ).apply {
            put(
                0,
                0,

                cy * cr + sy * sp * sr,
                -cy * sr + sy * sp * cr,
                sy * cp,

                cp * sr,
                cp * cr,
                -sp,

                -sy * cr + cy * sp * sr,
                sy * sr + cy * sp * cr,
                cy * cp
            )
        }
    }

    /**
     * Virtual Camera Intrinsic
     */
    private fun buildProjectionMatrix(
        camera: VirtualCamera
    ): Mat {
        val fx = camera.outputWidth / (2.0 * tan(Math.toRadians(camera.horizontalFov / 2.0)))
        val fy = camera.outputHeight / (2.0 * tan(Math.toRadians(camera.verticalFov / 2.0)))
        val cx = camera.outputWidth * 0.5
        val cy = camera.outputHeight * 0.5

        return Mat(
            3,
            3,
            CvType.CV_64F
        ).apply {
            put(
                0,
                0,

                fx,
                0.0,
                cx,

                0.0,
                fy,
                cy,

                0.0,
                0.0,
                1.0
            )
        }
    }

    private fun buildKey(
        camera: VirtualCamera
    ): String {
        return buildString {
            append(camera.outputWidth)
            append('_')

            append(camera.outputHeight)
            append('_')

            append(camera.horizontalFov)
            append('_')

            append(camera.verticalFov)
            append('_')

            append(camera.yaw)
            append('_')

            append(camera.pitch)
            append('_')

            append(camera.roll)
        }
    }
}