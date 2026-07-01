package com.example.presentation.screens.hardware


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.collection.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.theme.CosmicSurface
import com.example.presentation.theme.MidnightBlack
import com.example.presentation.theme.OnSpaceWhite

// ==========================================
// BITMAP CACHE SYSTEM
// ==========================================

object StaticImageCache {
    // Shared system image cache mapping for fast hardware-accelerated drawing
    private val cache = LruCache<String, CachedValue>(30)

    data class CachedValue(val bitmap: Bitmap, val context: Context)

    fun put(key: String, bitmap: Bitmap, context: Context) {
        cache.put(key, CachedValue(bitmap, context))
    }

    fun get(key: String): Bitmap? {
        return cache.get(key)?.bitmap
    }
}

@Composable
fun GalleryScreen() {
    val context = LocalContext.current
    val items = remember {
        listOf(
            "Node Delta" to "https://images.unsplash.com/photo-1579546929518-9e396f3cc809",
            "Node Gamma" to "https://images.unsplash.com/photo-1550684848-fac1c5b4e853",
            "Node Epsilon" to "https://images.unsplash.com/photo-1541701494587-cb58502866ab",
            "Node Alpha" to "https://images.unsplash.com/photo-1507525428034-b723cf961d3e",
            "Node Zeta" to "https://images.unsplash.com/photo-1518770660439-4636190af475",
            "Node Theta" to "https://images.unsplash.com/photo-1451187580459-43490279c0fa"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlack)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Graphics Gallery", color = OnSpaceWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1.0f)
            ) {
                items(items) { item ->
                    // Generate a high-res bitmap locally to prevent crash, load into our static cache
                    val bitmap = remember(item.first) {
                        val b = Bitmap.createBitmap(250, 250, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(b)
                        val paint = Paint().apply {
                            color = android.graphics.Color.parseColor("#FF2A5F")
                            style = Paint.Style.FILL
                        }
                        canvas.drawRect(0f, 0f, 250f, 250f, paint)
                        paint.color = android.graphics.Color.WHITE
                        paint.textSize = 24f
                        canvas.drawText(item.first, 40f, 130f, paint)

                        // Save image and display boundaries to cache map
                        StaticImageCache.put(item.first, b, context)
                        b
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = CosmicSurface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(item.first, color = OnSpaceWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}