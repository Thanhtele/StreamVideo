package com.example.fisheye.opencv

object VirtualCameras {
    var FrontWheel = VirtualCamera(
        outputWidth = 1280,
        outputHeight = 720,

        horizontalFov = 80.0,
        verticalFov = 83.0,

        yaw = -5.0,
        pitch = 59.0,
        roll = 0.0
    )

    var RearWheel = VirtualCamera(
        outputWidth = 1280,
        outputHeight = 720,

        horizontalFov = 80.0,
        verticalFov = 83.0,

        yaw = 5.0,
        pitch = 59.0,
        roll = 0.0
    )
}