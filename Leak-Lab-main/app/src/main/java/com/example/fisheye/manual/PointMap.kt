package com.jack.fisheye

/**
 * Mapping của một pixel output
 * tới vị trí tương ứng trong ảnh fisheye gốc.
 *
 * x, y là tọa độ float để hỗ trợ bilinear interpolation.
 */
data class PointMap(

    val x: Float,

    val y: Float

)