package com.example.myvideoplayer.ui;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myvideoplayer.R;

import java.io.IOException;

public class CameraSurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private SurfaceView mSurfaceView;
    private Camera mCamera;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_surface_view);
        mSurfaceView = findViewById(R.id.surfaceview);
        mSurfaceView.getHolder().addCallback(this);
        //打开摄像头并展示方向旋转90度
        mCamera = Camera.open(0);
        mCamera.setDisplayOrientation(90);
    }

    //surfaceView预览

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        //在控件创建的时候，进行相应的初始化工作
        try {
            mCamera.setPreviewDisplay(holder);
            //开始预览
            mCamera.startPreview();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        //释放摄像头
        mCamera.release();
    }
}
