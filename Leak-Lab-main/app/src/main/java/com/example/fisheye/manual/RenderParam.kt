//package com.example.fisheye.manual
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.os.Bundle
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Slider
//import androidx.compose.material3.Text
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.key
//import androidx.compose.runtime.mutableDoubleStateOf
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.runtime.snapshotFlow
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.unit.dp
//import androidx.core.graphics.createBitmap
//import com.example.R
//import com.example.fisheye.opencv.CameraCalibration
//import com.example.fisheye.opencv.CameraRenderer
//import com.example.fisheye.opencv.OpenCVMapper
//import com.example.fisheye.opencv.VirtualCameras
//import kotlinx.coroutines.flow.debounce
//import kotlinx.coroutines.flow.distinctUntilChanged
//import org.opencv.android.OpenCVLoader
//
//data class RenderParam(
//    val horizontalFov: Double,
//    val verticalFov: Double,
//    val yaw: Double,
//    val pitch: Double
//)
//
//class CameraActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        check(OpenCVLoader.initDebug()) {
//            "OpenCV init failed"
//        }
//
//        //--------------------------------------------------
//        // Input
//        //--------------------------------------------------
//
//        val inputBitmap = BitmapFactory.decodeResource(
//            resources,
//            R.drawable.fisheye_demo
//        ).copy(
//            Bitmap.Config.ARGB_8888,
//            false
//        ).also {
//            Log.d("MAP","Input Bitmap: ${it.width} x ${it.height}")
//        }
//
//        //--------------------------------------------------
//        // Output Bitmap (Create Once)
//        //--------------------------------------------------
//
//        val outputFrontBitmap = createBitmap(VirtualCameras.FrontWheel.outputWidth, VirtualCameras.FrontWheel.outputHeight)
//        val outputRearBitmap = createBitmap(VirtualCameras.RearWheel.outputWidth, VirtualCameras.RearWheel.outputHeight)
//
//        setContent {
//            MaterialTheme {
//                var yaw by remember { mutableDoubleStateOf(-VirtualCameras.FrontWheel.yaw) }
//                var pitch by remember { mutableDoubleStateOf(VirtualCameras.FrontWheel.pitch) }
//                var horizontalFov by remember { mutableDoubleStateOf(VirtualCameras.FrontWheel.horizontalFov) }
//                var verticalFov by remember { mutableDoubleStateOf(VirtualCameras.FrontWheel.verticalFov) }
//                var roll by remember { mutableDoubleStateOf(VirtualCameras.FrontWheel.roll) }
//                var version by remember { mutableIntStateOf(0) }
//
//                fun render() {
//                    val frontCamera = VirtualCameras.FrontWheel.copy(
//                        horizontalFov = horizontalFov,
//                        verticalFov = verticalFov,
//                        yaw = -yaw,
//                        pitch = pitch,
//                        roll = roll
//                    )
//                    val rearCamera = VirtualCameras.RearWheel.copy(
//                        horizontalFov = horizontalFov,
//                        verticalFov = verticalFov,
//                        yaw = yaw,
//                        pitch = pitch,
//                        roll = roll
//                    )
//                    val frontMap = OpenCVMapper.create(
//                        CameraCalibration,
//                        frontCamera
//                    )
//                    val rearMap = OpenCVMapper.create(
//                        CameraCalibration,
//                        rearCamera
//                    )
//                    CameraRenderer.render(
//                        inputBitmap,
//                        outputFrontBitmap,
//                        frontMap
//                    )
//                    CameraRenderer.render(
//                        inputBitmap,
//                        outputRearBitmap,
//                        rearMap
//                    )
//                    version++
//                }
//
//                LaunchedEffect(Unit) {
//                    snapshotFlow {
//                        RenderParam(
//                            horizontalFov = horizontalFov,
//                            verticalFov = verticalFov,
//                            yaw = yaw,
//                            pitch = pitch
//                        )
//                    }.debounce(250)
//                        .distinctUntilChanged()
//                        .collect {
//                            render()
//                        }
//                }
//
//                Column(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .background(Color.White)
//                ) {
//                    key(version) {
//                        Image(
//                            bitmap = outputFrontBitmap.asImageBitmap(),
//                            contentDescription = null,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Image(
//                            bitmap = outputRearBitmap.asImageBitmap(),
//                            contentDescription = null,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                    Spacer(Modifier.weight(1f))
//
//                    Text("Horizontal FOV : %.1f".format(horizontalFov))
//                    Slider(
//                        value = horizontalFov.toFloat(),
//                        onValueChange = {
//                            horizontalFov = it.toDouble()
//                        },
//                        valueRange = 40f..180f
//                    )
//
//                    Text("Vertical FOV : %.1f".format(verticalFov))
//                    Slider(
//                        value = verticalFov.toFloat(),
//                        onValueChange = {
//                            verticalFov = it.toDouble()
//                        },
//                        valueRange = 40f..180f
//                    )
//
//                    Text("Yaw : %.1f".format(yaw))
//                    Slider(
//                        value = yaw.toFloat(),
//                        onValueChange = {
//                            yaw = it.toDouble()
//                        },
//                        valueRange = -180f..100f
//                    )
//
//                    Text("Pitch : %.1f".format(pitch))
//                    Slider(
//                        value = pitch.toFloat(),
//                        onValueChange = {
//                            pitch = it.toDouble()
//                        },
//                        valueRange = -90f..90f
//                    )
//                    Text("Roll : %.1f".format(roll))
//                    Slider(
//                        value = roll.toFloat(),
//                        onValueChange = {
//                            roll = it.toDouble()
//                        },
//                        valueRange = -90f..90f
//                    )
//
//                    Spacer(modifier = Modifier.height(30.dp))
//                }
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        CameraRenderer.release()
//    }
//
//}