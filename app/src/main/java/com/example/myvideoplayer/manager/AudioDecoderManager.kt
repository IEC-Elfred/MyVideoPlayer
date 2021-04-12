package com.example.myvideoplayer.manager

import android.content.Context
import android.media.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.myvideoplayer.ui.MyApplication
import java.io.IOException
import java.nio.ByteBuffer

class AudioDecoderManager {
    private val TAG = "AudioPlayManager"

    private val mSampleRate = 44100

    private val channelCount = 2

    private var mAudioTrack: AudioTrack? = null

    private val channelConfig = AudioFormat.CHANNEL_IN_STEREO

    private var bufferSize = 0

    private val audioFormatEncode = AudioFormat.ENCODING_PCM_16BIT

    private var mAudioManager: AudioManager? = null

    private var mContext: Context? = null

    private var mediaCodec: MediaCodec? = null

    private var mediaFormat: MediaFormat? = null

    private var mediaExtractor: MediaExtractor? = null

    private var isDecodeFinish = false

    companion object {
        private var instance: AudioDecoderManager? = null
        fun getInstance(): AudioDecoderManager? {
            if (instance == null) {
                instance = AudioDecoderManager()
            }
            return instance
        }
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun setContext(context: Context?) {
        mContext = context
        init()
        initMediaExactor()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        mAudioManager = mContext!!.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        bufferSize = AudioTrack.getMinBufferSize(mSampleRate, channelConfig, audioFormatEncode)
        val sessionId = mAudioManager!!.generateAudioSessionId()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val audioFormat = AudioFormat.Builder().setSampleRate(mSampleRate)
            .setEncoding(audioFormatEncode)
            .setChannelMask(channelConfig)
            .build()
        mAudioTrack = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSize * 2,
            AudioTrack.MODE_STREAM,
            sessionId
        )
    }

    private fun initMediaExactor() {
        try {
            mediaCodec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
            mediaFormat = MediaFormat.createAudioFormat(
                MediaFormat.MIMETYPE_AUDIO_AAC,
                mSampleRate,
                channelCount
            )
            mediaExtractor = MediaExtractor()
            //MP4 文件存放位置
            mediaExtractor!!.setDataSource(MyApplication.MP4_PLAY_PATH)
            Log.d(TAG, "getTrackCount: " + mediaExtractor!!.trackCount)
            for (i in 0 until mediaExtractor!!.trackCount) {
                val format = mediaExtractor!!.getTrackFormat(i)
                Log.d(TAG, "format: $format")
                val mime = format.getString(MediaFormat.KEY_MIME)
                Log.d(TAG, "mime: $mime")
                if (mime!!.startsWith("audio")) {
                    mediaFormat = format
                    mediaExtractor!!.selectTrack(i)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        mediaCodec!!.configure(mediaFormat, null, null, 0)
        mediaCodec!!.start()
    }

    fun startThread() {
        isDecodeFinish = false
        PlayThread().start()
    }

    fun close() {
        isDecodeFinish = true
        mAudioTrack!!.stop()
        mAudioTrack!!.release()
        instance = null
    }

    inner class PlayThread : Thread() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun run() {
            super.run()
            mAudioTrack?.play()
            while (!isDecodeFinish) {
                val inputIndex: Int = mediaCodec!!.dequeueInputBuffer(-1)
                Log.d(TAG, "inputIndex: $inputIndex")
                if (inputIndex >= 0) {
                    val byteBuffer: ByteBuffer = mediaCodec!!.getInputBuffer(inputIndex)!!
                    //解封装并把数据写入Buffer
                    val sampSize: Int = mediaExtractor!!.readSampleData(byteBuffer, 0)
                    //读取时间戳
                    val time: Long = mediaExtractor!!.getSampleTime()
                    if (sampSize > 0 && time >= 0) {
                        mediaCodec!!.queueInputBuffer(inputIndex, 0, sampSize, time, 0)
                        //跳转到下一个数据
                        mediaExtractor!!.advance()
                    }
                }
                val bufferInfo = MediaCodec.BufferInfo()
                val outIndex: Int = mediaCodec!!.dequeueOutputBuffer(bufferInfo, 0)
                if (outIndex >= 0) {
                    val byteBuffer: ByteBuffer = mediaCodec!!.getOutputBuffer(outIndex)!!
                    val bytes = ByteArray(bufferInfo.size)
                    byteBuffer[bytes]
                    mAudioTrack!!.write(bytes, 0, bytes.size)
                    mediaCodec!!.releaseOutputBuffer(outIndex, true)
                }
            }
        }
    }

}

