package com.example.myvideoplayer.manager

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myvideoplayer.ui.MyApplication
import com.example.myvideoplayer.ui.PlayVideoActivity
import java.io.IOException

class MediaDecoderManager {

    private val TAG = "DecoderManager"

    private lateinit var mediaCodec: MediaCodec

    private lateinit var mediaFormat: MediaFormat

    private lateinit var mediaExtractor: MediaExtractor

    private var isDecodeFinish = false

    private lateinit var mDecodeMp4Thread: DecoderMP4Thread


    companion object {
        private var instance: MediaDecoderManager? = MediaDecoderManager()
        private val mSpeedController = SpeedManager()
        fun getInstance(): MediaDecoderManager? {
            if (instance == null) {
                instance = MediaDecoderManager()
            }
            return instance
        }
    }

    private fun initMediaCodec() {
        try {
            mediaCodec = MediaCodec.createDecoderByType("video/avc")
            mediaFormat = MediaFormat.createAudioFormat("video/avc", 1280, 720)
            mediaExtractor = MediaExtractor()
            //MP4文件存放位置
            mediaExtractor.setDataSource(MyApplication.MP4_PLAY_PATH)
            Log.d(TAG, "getTrackCount: " + mediaExtractor.trackCount)
            for (i in 0 until mediaExtractor.trackCount) {
                val format = mediaExtractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                Log.d(TAG, "mime: $mime")
                if (mime != null) {
                    if (mime.startsWith("video")) {
                        mediaFormat = format
                        mediaExtractor.selectTrack(i)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val surface = PlayVideoActivity.getSurface()
        mediaCodec.configure(mediaFormat, surface, null, 0)
        mediaCodec.start()

    }

    private inner class DecoderMP4Thread : Thread() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun run() {
            super.run()
            //将数据写入解码器
            while (!isDecodeFinish) {
                if (mediaCodec != null) {
                    val inputIndex = mediaCodec.dequeueInputBuffer(-1);
                    Log.d(TAG, "inputIndex: " + inputIndex)
                    if (inputIndex >= 0) {
                        //找到指定索引的Buffer
                        val byteBuffer = mediaCodec.getInputBuffer(inputIndex) ?: continue
                        //把视频的数据写入buffer，返回的是sampleSize,如果没有了返回的是-1
                        val sampSize = byteBuffer?.let { mediaExtractor.readSampleData(it, 0) }
                        //获取时间戳
                        val time = mediaExtractor.sampleTime

                        if (sampSize != null) {
                            if (sampSize > 0 && time > 0) {
                                //把Buffer放入队列
                                mediaCodec.queueInputBuffer(inputIndex, 0, sampSize, time, 0)
                                //控制帧率在30帧左右
                                mSpeedController.preRender(time)
                                mediaExtractor.advance()
//                                try {
//                                    sleep(40);
//                                } catch (e: InterruptedException) {
//                                    e.printStackTrace()
//                                }
                            }
                        }
                    }
                }
                val bufferInfo = MediaCodec.BufferInfo()
                val outIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0)
                if (outIndex >= 0) {
                    mediaCodec.releaseOutputBuffer(outIndex, true)
                }

            }
        }
    }

    fun close() {
        try {
            Log.d(TAG, "close start")
            if (mediaCodec != null) {
                isDecodeFinish = true
                try {
                    if (mDecodeMp4Thread != null) {
                        mDecodeMp4Thread.join(2000)
                    }
                } catch (e: InterruptedException) {
                    Log.e(TAG, "closeException: " + e)
                }
                val isAlive = mDecodeMp4Thread.isAlive
                Log.d(TAG, "close end isAlive: " + isAlive)
                mediaCodec.stop()
                mediaCodec.release()
                mSpeedController.reset()
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
        instance = null
    }

    fun startMP4code() {
        initMediaCodec()
        mDecodeMp4Thread = DecoderMP4Thread()
        mDecodeMp4Thread.name = "DecoderMP4Thread"
        mDecodeMp4Thread.start()
    }

}