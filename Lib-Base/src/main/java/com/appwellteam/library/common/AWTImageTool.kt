package com.appwellteam.library.common

import android.content.ContentUris
import android.database.Cursor
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.appwellteam.library.AWTApplication
import com.appwellteam.library.extension.closeStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Created by Sambow on 15/12/27.
 */
@Suppress("unused")
object AWTImageTool {
    private const val PNG_EXTENSION = "png"
    private const val JPG_EXTENSION = "jpg"
    fun getImageFromUri(uri: Uri): Bitmap? {
        var result: Bitmap? = null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        val stream: InputStream = AWTApplication.app?.contentResolver?.openInputStream(uri)
                ?: return result
        var needClose = true
        try {
            result = BitmapFactory.decodeStream(stream, null, options)
            stream.closeStream()
            needClose = false
            result ?: return result

            result = rotateBitmap(uri, result)
        } catch (e: Exception) {
            AWTCommon.toastExceptionStack(e, 10)
        } finally {
            if (needClose) stream.closeStream()
        }
        return result
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getImageFromUriBySampleSize(uri: Uri, sampleSize: Int): Bitmap? {
        var result: Bitmap? = null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        val stream: InputStream = AWTApplication.app?.contentResolver?.openInputStream(uri)
                ?: return result
        var needClose = true
        val bScale: Bitmap?
        try {
            options.inSampleSize = sampleSize

            @Suppress("DEPRECATION")
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) { // api 19
                options.inPurgeable = true
                options.inInputShareable = true
            }

            bScale = BitmapFactory.decodeStream(stream, null, options)
            stream.closeStream()
            needClose = false
            bScale ?: return result

            result = rotateBitmap(uri, bScale)
//			releaseBitmap(bScale);
        } catch (e: Exception) {
        } finally {
            if (needClose) stream.closeStream()
        }
        return result
    }

    fun getImageFromUriByHalf(uri: Uri): Bitmap? {
        return getImageFromUriBySampleSize(uri, 2)
    }

    fun getImageFromUriByScale(uri: Uri, scale: Float): Bitmap? {
        var result: Bitmap? = null
        val options: BitmapFactory.Options = BitmapFactory.Options()
        val stream: InputStream = AWTApplication.app?.contentResolver?.openInputStream(uri)
                ?: return result
        var needClose = true

        try {
            val bScale = BitmapFactory.decodeStream(stream, null, options)
            stream.closeStream()
            needClose = false
            bScale ?: return result

            val bResize: Bitmap?
            if (scale < 1f) {
                bResize = Bitmap.createScaledBitmap(bScale, (bScale.width * scale).toInt(), (bScale.height * scale).toInt(), true)
                releaseBitmap(bScale)
            } else {
                bResize = bScale
            }

            result = rotateBitmap(uri, bResize)
//			releaseBitmap(bResize);
        } catch (e: Exception) {
        } finally {
            if (needClose) stream.closeStream()
        }
        return result
    }

    fun getImageFromUriByMaxSize(uri: Uri, maxSize: Int): Bitmap? {
        var result: Bitmap? = null
        var options: BitmapFactory.Options = BitmapFactory.Options()
        var stream: InputStream = AWTApplication.app?.contentResolver?.openInputStream(uri)
                ?: return result
        var needClose = true

        try {
            options.inJustDecodeBounds = true
            BitmapFactory.decodeStream(stream, null, options)
            stream.closeStream()
            needClose = false

            val heightRatio = Math.ceil((options.outHeight.toFloat() / maxSize.toFloat()).toDouble()).toInt()
            val widthRatio = Math.ceil((options.outWidth.toFloat() / maxSize.toFloat()).toDouble()).toInt()
            var scale = 1
            if (heightRatio > 1 && widthRatio > 1) {
                scale = if (heightRatio > widthRatio) heightRatio else widthRatio
            }

            options = BitmapFactory.Options()
            options.inSampleSize = scale
            @Suppress("DEPRECATION")
            options.inDither = false

            stream = AWTApplication.app?.contentResolver?.openInputStream(uri) ?: return result

            val bScale = BitmapFactory.decodeStream(stream, null, options)
            stream.closeStream()
            needClose = false

            bScale ?: return result

            val longSide = if (bScale.width > bScale.height) bScale.width else bScale.height
            val ratio = longSide.toFloat() / maxSize.toFloat()

            val bResize: Bitmap
            if (longSide > maxSize) {
                bResize = Bitmap.createScaledBitmap(bScale, (bScale.width / ratio).toInt(), (bScale.height / ratio).toInt(), true)
                releaseBitmap(bScale)
            } else {
                bResize = bScale
            }

            result = rotateBitmap(uri, bResize)
//			releaseBitmap(bResize);
        } catch (e: Exception) {
            AWTCommon.toastExceptionStack(e, 10)
        } finally {
            if (needClose) stream.closeStream()
        }
        return result
    }

    @Throws(IOException::class)
    private fun rotateBitmap(uri: Uri, bResize: Bitmap): Bitmap {
        var degrees = 0
        val realPath = getPathFromURI(uri)

        if (realPath != null) {
            val exif = ExifInterface(realPath)
            val orientation1 = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            if (orientation1 != 0) {
                when (orientation1) {
                    ExifInterface.ORIENTATION_ROTATE_270 -> degrees = 270
                    ExifInterface.ORIENTATION_ROTATE_180 -> degrees = 180
                    ExifInterface.ORIENTATION_ROTATE_90 -> degrees = 90
                }
            }
        } else {
            val orientation2 = getOrientation(uri)
            if (orientation2 != 0) {
                degrees = orientation2
            }
        }

        val options = BitmapFactory.Options()

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            options.inPurgeable = true
            options.inInputShareable = true
        }

        val mtx = Matrix()
        mtx.postRotate(degrees.toFloat())
        val bRotate = Bitmap.createBitmap(bResize, 0, 0, bResize.width, bResize.height, mtx, true)

        if (bRotate != bResize) {
            releaseBitmap(bResize)
        }

        return bRotate
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun releaseBitmap(bitmap: Bitmap) {
        bitmap.recycle()
        System.gc()
    }

    fun getRealPathFromURI(uri: Uri): String? {
        return try {
            val tFile = File(uri.path)
            if (tFile.exists()) {
                uri.path
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getPathFromURI(uri: Uri): String? {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                DocumentsContract.isDocumentUri(AWTApplication.app, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))

                return getDataColumn(contentUri, null, null)
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                val contentUri = when (split[0]) {
                    "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    else -> return null
                }


                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(contentUri, selection, selectionArgs)
            }
        }
        // MediaStore (and general)
        else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(uri, null, null)
        }
        // File
        else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getDataColumn(uri: Uri, selection: String?,
                      selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor = AWTApplication.app?.contentResolver?.query(uri, projection, selection, selectionArgs, null)
            if (cursor?.moveToFirst() == true) {
                return cursor.getString(cursor.getColumnIndexOrThrow(column))
            }
        } catch (e: Exception) {
            return null
        } finally {

            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return null
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getOrientation(uri: Uri): Int {
        var cursor: Cursor? = null
        try {
            cursor = AWTApplication.app?.contentResolver?.query(uri, arrayOf(MediaStore.Images.ImageColumns.ORIENTATION), null, null, null)
            if (cursor?.moveToFirst() == true) {
                return cursor.getInt(0)
            }
        } catch (e: Exception) {
            return 0
        } finally {
            if (cursor != null && !cursor.isClosed) {
                cursor.close()
            }
        }
        return 0
    }

    private fun saveToFile(bitmap: Bitmap, dir: File, fileName: String, quality: Int, extension: String): Uri? {

        if (!dir.exists()) {
            dir.mkdirs()
        }
        val targetFile = File(dir, "$fileName.$extension")
        val out = FileOutputStream(targetFile)

        return try {
            if (PNG_EXTENSION.equals(extension, ignoreCase = true)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, out)
            } else if (JPG_EXTENSION.equals(extension, ignoreCase = true)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }

            Uri.fromFile(targetFile)
        } catch (e: Exception) {
            null
        } finally {
            out.closeStream()
        }
    }

    fun cropCenterInSquare(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val length = if (width > height) height else width
        val diff = if (width > height) width - height else height - width

        val output = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = -0x1000000

        if (width > height) {
            canvas.drawBitmap(bitmap, -(diff / 2f), 0f, paint)
        } else {
            canvas.drawBitmap(bitmap, 0f, -(diff / 2f), paint)
        }
        return output
    }

    fun cropCenterInCircle(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val length = if (width > height) height else width
        val halfLength = length / 2f

        val output = Bitmap.createBitmap(length, length, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint()

        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        paint.isDither = true
        canvas.drawARGB(0, 0, 0, 0)
        canvas.drawCircle(halfLength, halfLength, halfLength, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, (length - width) / 2f, (length - height) / 2f, paint)
        return output
    }

    fun saveToPngFile(bitmap: Bitmap, dir: File, fileName: String, quality: Int): Uri? {
        return saveToFile(bitmap, dir, fileName, quality, PNG_EXTENSION)
    }

    fun saveToJpgFile(bitmap: Bitmap, dir: File, fileName: String, quality: Int): Uri? {
        return saveToFile(bitmap, dir, fileName, quality, JPG_EXTENSION)
    }
}
