package com.jeeps.gamecollector.deprecated.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import id.zelory.compressor.Compressor;

public class FileUtils {

    public static void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static File compressImage(Context context, String fileName, Uri currImageURI) throws IOException {
        final File tempFile = new File(context.getFilesDir(), fileName);

        InputStream inputStream = context.getContentResolver()
                .openInputStream(currImageURI);
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        FileUtils.copyStream(inputStream, fileOutputStream);
        fileOutputStream.close();
        inputStream.close();
        //Compress image
        final File compressedImageFile = new Compressor(context)
                .setMaxWidth(300)
                .setQuality(75)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .compressToFile(tempFile);
        // Cleanup
        if (tempFile.exists())
            tempFile.delete();
        //Get URI from file
        return compressedImageFile;
    }
}
