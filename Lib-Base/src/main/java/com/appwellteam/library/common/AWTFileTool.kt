package com.appwellteam.library.common

import android.net.Uri
import com.appwellteam.library.AWTApplication
import com.appwellteam.library.extension.closeStream
import com.appwellteam.library.extension.getExternalFilesDirPath
import java.io.*

/**
 * Created by Sambow on 16/8/15.
 */
@Suppress("unused")
object AWTFileTool {
    private var APP_FILE_DIR: String? = null

    val fileDir: String
        get() {
            if (APP_FILE_DIR == null) {
                APP_FILE_DIR = AWTApplication.app?.getExternalFilesDirPath("") ?: return ""
            }
            return APP_FILE_DIR ?: ""
        }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getSubDir(parent: String, dirName: String): File {
        val subDir = File("$parent${File.separator}$dirName${File.separator}")
        if (!subDir.exists()) {
            subDir.mkdir()
        }
        return subDir
    }

    fun getSubDir(parent: File, dirName: String): File {
        return getSubDir(parent.absolutePath, dirName)
    }

    fun copyFile(source: File?, target: File?): Boolean {
        target ?: return false
        source ?: return false
        if (!source.exists()) return false

        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            inputStream = FileInputStream(source)
            outputStream = FileOutputStream(target)

            // Transfer bytes from in to out
            val buf = ByteArray(1024)
            var len: Int = inputStream.read(buf)
            while (len > 0)
            {
                outputStream.write(buf, 0, len)
                len = inputStream.read(buf)
            }
            inputStream.close()
            outputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return false
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            inputStream?.closeStream()
            outputStream?.closeStream()
        }
        return true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun deleteFile(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                deleteFile(it)
            }
            file.delete()
        } else {
            file.delete()
        }
    }

    fun writeToFile(data: String, target: File) {
        try {
            val outputStreamWriter = OutputStreamWriter(FileOutputStream(target))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            AWTLog.e("FileTool", "File write failed: $e")
        }

    }

    fun readFromFile(target: File): String {
        var result = ""
        try {
            val inputStream = FileInputStream(target)
            result = inputStreamToString(inputStream)
        } catch (e: FileNotFoundException) {
            AWTLog.e("FileTool", "File not found: $e")
        } catch (e: IOException) {
            AWTLog.e("FileTool", "Can not read file: $e")
        }

        return result
    }

    fun readFileFromAsset(path: String): String {
        var result = ""
        val assetManager = AWTApplication.app?.activity?.resources?.assets ?: return result
        try {
            val inputStream = assetManager.open(path)
            result = inputStreamToString(inputStream)
        } catch (e: IOException) {
            AWTLog.e("FileTool", "Can not read file: $e")
        }

        return result
    }

    @Throws(IOException::class)
    private fun inputStreamToString(inputStream: InputStream?): String {
        var ret = ""
        inputStream ?: return ret

        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        var receiveString = bufferedReader.readLine()

        while (receiveString != null) {
            stringBuilder.append(receiveString)
            receiveString = bufferedReader.readLine()
        }

        inputStream.close()
        ret = stringBuilder.toString()
        return ret
    }

    fun getFileFromUrl(source: Uri, target: File): String? {
        val context = AWTApplication.app ?: return null
        var stream: InputStream? = null
        var outputStream: OutputStream? = null
        try {
            stream = context.contentResolver.openInputStream(source)
            outputStream = FileOutputStream(target)

            val buf = ByteArray(1024)
            var len = stream.read(buf)
            while (len > 0) {
                outputStream.write(buf, 0, len)
                len = stream.read(buf)
            }
            stream.close()
            outputStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } finally {
            stream?.closeStream()
            outputStream?.closeStream()
        }
        return target.absolutePath
    }
}
