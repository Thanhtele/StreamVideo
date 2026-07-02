package com.example.fisheye.opencv

data class VirtualCamera(
    val outputWidth: Int,
    val outputHeight: Int,
    val horizontalFov: Double,
    val verticalFov: Double,
    val yaw: Double,
    val pitch: Double,
    val roll: Double
)