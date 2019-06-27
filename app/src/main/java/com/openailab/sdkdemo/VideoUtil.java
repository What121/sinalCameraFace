package com.openailab.sdkdemo;

import android.content.res.Configuration;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * created by LiGuang
 * on 2018/11/12
 */
public class VideoUtil implements SurfaceHolder.Callback, Camera.PreviewCallback ,Camera.ErrorCallback{
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private Mat mRgba;
    private boolean syncFlag = false;
    private MainActivity mainActivity;
    private int mWidth;
    private int mHeight;
    private int cameraId;


    public VideoUtil(SurfaceHolder surfaceHolder, int mWidth, int mHeight, int cameraId,MainActivity mainActivity) {
        this.cameraId = cameraId;
        this.mWidth = mWidth;
        this.mHeight = mHeight;
        this.surfaceHolder = surfaceHolder;
        this.mainActivity = mainActivity;
        surfaceHolder.addCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCamera != null) {
            synchronized (this) {
                //  mCamera.addCallbackBuffer(buffers);
                mRgba.put(0, 0, data);
                //    Log.d("zheng", "data:" + data.length);
                syncFlag = true;
            }
            camera.addCallbackBuffer(data);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
       startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    /**
     * 开始预览
     */
    private void startPreview() {
        try {

            if (mRgba == null) {
                mRgba = new Mat(mWidth, mHeight, CvType.CV_8UC1);
            }

            //SurfaceView初始化完成，开始相机预览
            mCamera = Camera.open(cameraId);

            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mainActivity.camWidth, mainActivity.camHeight);
//            if (mainActivity.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
//                parameters.set("orientation", "portrait");
//                mCamera.setDisplayOrientation(90);
//                parameters.setRotation(90);
//            }
            mCamera.setParameters(parameters);
            mCamera.setPreviewDisplay(surfaceHolder);
            if(mainActivity.orientation == Configuration.ORIENTATION_PORTRAIT){
                if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    mCamera.setDisplayOrientation(90);
                }else if(cameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                    mCamera.setDisplayOrientation(180);
                }
            }
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            mCamera.setErrorCallback(this);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * 停止预览
     */
    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mRgba.release();
            syncFlag = false;
            mCamera = null;
            mRgba = null;
        }
    }

    public Mat getmRgba() {
        return mRgba;
    }
    public void turnCamera(int policy){
        if (policy == 1) {
            mCamera.setDisplayOrientation(90);
        }else if(policy == 2){
            mCamera.setDisplayOrientation(270);
        }
    }



    public boolean isSyncFlag() {
        return syncFlag;
    }

    @Override
    public void onError(int error, Camera camera) {

        Log.d("zheng","camera error:"+error);
    }
}
