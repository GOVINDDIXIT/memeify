

package com.govinddixit.memeify

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.support.media.ExifInterface
import android.net.Uri
import java.io.*

object BitmapResizer {

  fun shrinkBitmap(ctx: Context, uri: Uri, width: Int, height: Int): Bitmap {
    var input: InputStream
    try {
      input = ctx.contentResolver.openInputStream(uri)
    } catch (e: FileNotFoundException) {
      throw IllegalStateException(e)
    }

    if (!input.markSupported()) { // InputStream doesn't support mark(). so wrap it into BufferedInputStream & use that
      input = BufferedInputStream(input)
    }

    try {
      input.mark(input.available()) // input.isavailable() gives size of input stream
    } catch (e: IOException) {
      e.printStackTrace()
    }


    val bmpFactoryOptions = BitmapFactory.Options()
    bmpFactoryOptions.inJustDecodeBounds = true

    // Need to decodestream . else bmpFactoryOptions will be zero. so insamplesize will be zero
    BitmapFactory.decodeStream(input, null, bmpFactoryOptions)

    val heightRatio = Math.ceil((bmpFactoryOptions.outHeight / height.toFloat()).toDouble()).toInt()
    val widthRatio = Math.ceil((bmpFactoryOptions.outWidth / width.toFloat()).toDouble()).toInt()

    if (heightRatio > 1 || widthRatio > 1) {
      if (heightRatio > widthRatio) {
        bmpFactoryOptions.inSampleSize = heightRatio
      } else {
        bmpFactoryOptions.inSampleSize = widthRatio
      }
    }

    bmpFactoryOptions.inJustDecodeBounds = false

    try {
      input.reset() // Resetting input stream
    } catch (e: IOException) {
      e.printStackTrace()
    }

    var bitmap = BitmapFactory.decodeStream(input, null, bmpFactoryOptions)

    // Code to fix orientation issue on some devices
    bitmap = bitmap.rotateBitmapIfRequired(ctx)

    return bitmap
  }

  private fun Bitmap.rotateBitmapIfRequired(ctx: Context): Bitmap {
    var bitmap = this
    // captured image is saved as "default_image.jpg" inside "images" folder. "imgFile.getAbsolutePath()" will contain that path

    val imagePath = File(ctx.filesDir, "images")
    val imgFile = File(imagePath, "default_image.jpg")

    var exif: ExifInterface? = null
    try {
      //  exif = new ExifInterface(imgFile.getAbsolutePath());
      exif = ExifInterface(imgFile.absolutePath)
    } catch (e: IOException) {
      e.printStackTrace()
    }

    val orientation = exif!!.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

    when (orientation) {

      ExifInterface.ORIENTATION_ROTATE_90 -> bitmap = rotateImage(bitmap, 90f)

      ExifInterface.ORIENTATION_ROTATE_180 -> bitmap = rotateImage(bitmap, 180f)

      ExifInterface.ORIENTATION_ROTATE_270 -> bitmap = rotateImage(bitmap, 270f)

      else -> println("No rotation required")
    }
    return bitmap
  }

  private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height,
        matrix, true)
  }
}
