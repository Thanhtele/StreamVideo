package com.example.fisheye.opencv

data class VirtualCamera(
    val outputWidth: Int,
    val outputHeight: Int,
    val horizontalFov: Double,
    val verticalFov: Double,
    val yaw: Double,
    val pitch: Double,
    val roll: Double,
    /**
     * Principal point offset
     *
     * pixel
     */
    val centerOffsetX: Double = 0.0,

    val centerOffsetY: Double = 0.0,
)