package com.example.myvideoplayer.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.TextureView;

import java.io.IOException;

public class CameraTextureViewActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private TextureView mTextureView;
    private Camera mCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_texture_view3);
        mTextureView = findViewById(R.id.textureView);
        mTextureView.setSurfaceTextureListener(this);
        //打开摄像头并将展示方向旋转90度
        mCamera = Camera.open(0);
        mCamera.setDisplayOrientation(90);
    }
    //textureView预览

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        try {
            mCamera.setPreviewTexture(surface);
            //开始预览
            mCamera.startPreview();
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        //释放相机
        mCamera.release();
        return false;
    }
}