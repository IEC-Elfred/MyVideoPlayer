package com.example.myvideoplayer.ui

import android.app.Application
import android.os.Environment

import android.util.Log
import com.example.myvideoplayer.R
import java.io.*

class MyApplication : Application() {

    companion object {
        private const val TAG = "MyApplication"

        val MP4_PLAY_PATH = Environment.getExternalStorageDirectory().absolutePath + "/video.mp4"

    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")

        copyResourceToMemory(R.raw.video, MP4_PLAY_PATH)
    }



    private fun copyResourceToMemory(srcPath: Int, destPath: String) {
        var fileInputStream: InputStream? = null
        var fileOutputStream: FileOutputStream? = null
        try {
            fileInputStream = resources.openRawResource(srcPath)
            val file = File(destPath)
            if (file.exists()) {
                return
            }
            file.createNewFile()
            fileOutputStream = FileOutputStream(file)
            val bytes = ByteArray(1024)
            while (fileInputStream.read(bytes) > 0) {
                fileOutputStream.write(bytes)
            }
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "copyVideoResourceToMemory FileNotFoundException : $e")
        } catch (e: IOException) {
            Log.e(TAG, "copyVideoResourceToMemory IOException : $e")
        } finally {
            try {
                fileInputStream?.close()
                fileOutputStream?.close()
            } catch (e: IOException) {
                Log.e(TAG, "close stream IOException : $e")
            }
        }
    }
}