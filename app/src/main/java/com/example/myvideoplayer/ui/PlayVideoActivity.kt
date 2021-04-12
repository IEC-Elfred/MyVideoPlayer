package com.example.myvideoplayer.ui

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.myvideoplayer.R
import com.example.myvideoplayer.manager.AudioDecoderManager
import com.example.myvideoplayer.manager.MediaDecoderManager
import kotlinx.android.synthetic.main.activity_play_video.*

class PlayVideoActivity : AppCompatActivity() {

    private lateinit var surfaceHolder: SurfaceHolder

    private val INIT_MANAGER_MSG: Int = 1

    private val INIT_MANAGER_DELAY = 1 * 1000

    companion object {
        public lateinit var surfaceView: SurfaceView
        public fun getSurface(): Surface {
            return surfaceView.holder.surface
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_video)
        surfaceView = surfaceview
        val endBtn = end
        endBtn.setOnClickListener {
            MediaDecoderManager.getInstance()!!.close()
            AudioDecoderManager.getInstance()!!.close()
            finish()
        }
        initSurface()
    }

    private fun initSurface() {
        surfaceHolder = surfaceView.holder
        surfaceHolder.addCallback(object : SurfaceHolder.Callback {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun surfaceCreated(holder: SurfaceHolder) {
                mHandler.sendEmptyMessage(INIT_MANAGER_MSG)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                MediaDecoderManager.getInstance()!!.close()
                AudioDecoderManager.getInstance()!!.close()
                finish()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initManager() {
        MediaDecoderManager.getInstance()?.startMP4code()
        AudioDecoderManager.getInstance()!!.setContext(applicationContext)
        AudioDecoderManager.getInstance()!!.startThread()
    }

    private val mHandler: Handler = object : Handler() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == INIT_MANAGER_MSG) {
                initManager()
            }
        }
    }
}