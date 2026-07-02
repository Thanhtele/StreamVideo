package com.example.fisheye.opencv

import org.opencv.core.CvType
import org.opencv.core.Mat
import kotlin.math.tan

object CameraCalibration {
    /**
     * Resolution used by the input fisheye image.
     */
    const val IMAGE_WIDTH = 3680
    const val IMAGE_HEIGHT = 2945

    /**
     * Camera Intrinsic Matrix
     *
     *      | fx  0  cx |
     * K =  | 0  fy  cy |
     *      | 0   0   1 |
     *
     * NOTE:
     * Đây vẫn chỉ là DEMO.
     * Sau này sẽ thay bằng kết quả calibration thật.
     */
    val K: Mat by lazy {
        val fx = IMAGE_WIDTH / (2.0 * tan(Math.toRadians(140 / 2.0)))
        val fy = IMAGE_HEIGHT / (2.0 * tan(Math.toRadians(115 / 2.0)))
        val cx = IMAGE_WIDTH * 0.5
        val cy = IMAGE_HEIGHT * 0.5

        Mat(3, 3, CvType.CV_64F).apply {
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

    /**
     * OpenCV fisheye distortion coefficients
     *
     * Demo values only.
     */
    val D: Mat by lazy {
        Mat(4, 1, CvType.CV_64F).apply {
            put(
                0,
                0,
                -0.08,
                0.02,
                0.0,
                0.0
            )
        }
    }
}