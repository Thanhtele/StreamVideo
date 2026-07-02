package com.example.fisheye.opencv

import org.opencv.core.CvType
import org.opencv.core.Mat

/**
 * Camera calibration parameters.
 *
 * Demo version:
 * - Các giá trị hiện tại chỉ là placeholder.
 * - Sau này thay bằng kết quả calibration thực tế của camera.
 */
object CameraCalibration {
    /**
     * Camera resolution used during calibration.
     *
     * IMPORTANT:
     * Input image phải cùng resolution này.
     */
    const val IMAGE_WIDTH = 1600
    const val IMAGE_HEIGHT = 1200

    /**
     * Camera intrinsic matrix
     *
     * | fx  0  cx |
     * | 0  fy  cy |
     * | 0   0   1 |
     */
    val K: Mat by lazy {
        Mat(3, 3, CvType.CV_64F).apply {
            put(
                0,
                0,
                // fx
                850.0,
                // skew
                0.0,
                // cx
                IMAGE_WIDTH / 2.0,
                // fy
                0.0,
                850.0,
                // cy
                IMAGE_HEIGHT / 2.0,
                // last row
                0.0,
                0.0,
                1.0
            )
        }
    }

    /**
     * Fisheye distortion coefficients
     *
     * OpenCV fisheye model:
     *
     * k1
     * k2
     * k3
     * k4
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