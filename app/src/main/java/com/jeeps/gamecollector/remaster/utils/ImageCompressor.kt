package com.jeeps.gamecollector.remaster.utils

import android.content.Context
import android.net.Uri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

class ImageCompressor(
    private val context: Context
) {

    suspend fun compressImage(imageFileUri: Uri): File = withContext(Dispatchers.IO) {
        val cacheFile = File.createTempFile("compressed_cover.jpg", null, context.cacheDir)

        val inputStream = context.contentResolver
            .openInputStream(imageFileUri)
        val fileOutputStream = FileOutputStream(cacheFile)
        copyStream(inputStream!!, fileOutputStream)
        fileOutputStream.close()
        inputStream.close()

        val compressedImage = Compressor.compress(context, cacheFile) {
            default(width = 500, quality = 75)
        }

        cacheFile.delete()

        compressedImage
    }

    private fun copyStream(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }
    }
}