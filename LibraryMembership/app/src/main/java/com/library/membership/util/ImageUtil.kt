package com.library.membership.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

object ImageUtil {
    /**
     * Reads the image at [uri], downsizes it so its longest side is at most
     * [maxDimension] px, JPEG-compresses it, and returns a
     * "data:image/jpeg;base64,...." string — the same shape the web
     * registration form sends as `photoUrl`, which the Worker decodes
     * directly (see decodePhotoData in worker.js).
     */
    fun uriToJpegDataUri(context: Context, uri: Uri, maxDimension: Int = 800, quality: Int = 82): String? {
        return try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream)
            } ?: return null

            val longSide = max(bitmap.width, bitmap.height)
            val scaled = if (longSide > maxDimension) {
                val scale = maxDimension.toFloat() / longSide
                Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else bitmap

            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            val base64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } catch (e: Exception) {
            null
        }
    }
}
