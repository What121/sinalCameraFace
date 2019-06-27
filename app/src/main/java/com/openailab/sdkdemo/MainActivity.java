/*License Agreement
 For Open Source Computer Vision Library
 (3-clause BSD License)


 Copyright (C) 2000-2018, Intel Corporation, all rights reserved.
 Copyright (C) 2009-2011, Willow Garage Inc., all rights reserved.
 Copyright (C) 2009-2016, NVIDIA Corporation, all rights reserved.
 Copyright (C) 2010-2013, Advanced Micro Devices, Inc., all rights reserved.
 Copyright (C) 2015-2016, OpenCV Foundation, all rights reserved.
 Copyright (C) 2015-2016, Itseez Inc., all rights reserved.
 Third party copyrights are property of their respective owners.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
•Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
•Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
•Neither the names of the copyright holders nor the names of the contributors may be used to endorse or promote products derived from this software without specific prior written permission.

This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness for
a particular purpose are disclaimed. In no event shall copyright holders or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to,
procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise)
arising in any way out of the use of this software, even if advised of the possibility of such damage
*/
package com.openailab.sdkdemo;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.InputFilter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.openailab.facelibrary.FaceAPP;
import com.openailab.facelibrary.FaceAttribute;
import com.openailab.facelibrary.FaceInfo;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    private static final String TAG = "Opencv::Activity";
    private Mat mRgb;
    private Mat mRgbaFrame;

    private boolean canCloseCam=false;
    private boolean isLiveness = false;
    private int liveRegisterStatus = -1;
    private int mWidth;
    private int mHeight;
    public final int camWidth = 640;
    public final int camHeight = 480;
    Thread mainLoop = null;
    private Lock lock = null;
    private Lock  lockcam=null;
    private Lock lockth = null;
    private SurfaceView surfaceView;
    private VideoUtil videoUtil;
    private myDrawRectView drawRectView;
    private TextView tv_time;
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private boolean hasExtSDPermission;
    private boolean hasaudioPermission;
    private boolean drawMat=false;
    private int mState= mixController.STATE_IDLE;
    private static  int loop = 0;

    private byte[] posStr;

    private final static int SHOWTOAST=4;
    private final static int MAX_REGISTER= FileOperator.MAX_REGISTER; // set max number of faces ,could be modify

    private FaceAPP face= FaceAPP.GetInstance();//FaceAPP.getInstance();
    int orientation = Configuration.ORIENTATION_LANDSCAPE;



    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private int stattID;
    private boolean noExit=true;
    private final MyHandler mHandler = new MyHandler(this);

    public float[] feature=new float[128];

    private static final String PROC_CPU_INFO_PATH = "/proc/cpuinfo";

    private final static long MIN_CLICK_DELAY_TIME = 1000;
    private long lastClickTime = 0;

    private RelativeLayout settingLayout;
    private Button settingSaveBtn, settingCancel;
    private RadioGroup radioGroupFlip;
    private Map<String, String> settingMap;
    private int flipInt=0;//后置摄像头画面翻转，0为不需要翻转，1为翻转
    private File settingFile;
    private String editTextString, showName;
    private List<FaceInfo> FaceInfos;


    private final static int FACENUM = 1;//1为单框显示
    private final static int LIVENESS = 0;//0为非活体检测，1为活体检测
     public byte[] tmpPos = new byte[1024*FACENUM];
    String[] faceparams={"a","b","c","d", "factor","min_size","clarity","perfoptimize","livenessdetect","gray2colorScale","frame_num","quality_thresh","mode","facenum"};
    double[] VALUE = {0.75, 0.8, 0.9, 0.6, 0.65, 40, 200, 0, LIVENESS, 0.5, 1, 0.8, 1, FACENUM};


    /**
     * 声明一个静态的Handler内部类，并持有外部类的弱引用
     */
    private class MyHandler extends Handler {

        private final WeakReference<MainActivity> mActivty;

        private MyHandler(MainActivity mActivty) {
            this.mActivty = new WeakReference<MainActivity>(mActivty);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {

                case SHOWTOAST:{
                    if(msg.arg1==1){
                        //Toast.makeText(mActivty.get(), "注册成功", Toast.LENGTH_LONG).show();

                        tv_time.setText("注册成功");
                    }
                    else if(msg.arg1==0){
                        liveRegisterStatus = 2;
                        tv_time.setText("你是:" + showName);
                        Log.d("zheng", "toast:" + showName);
                        showName = "点击人脸注册";

                    }else if(msg.arg1==2){

                        tv_time.setText("已经注册超过"+MAX_REGISTER+"人");

                    } else if(msg.arg1==3){
                        if (isLiveness) {
                            liveRegisterStatus = 1;
                        }
                        tv_time.setText("点击人脸注册");

                    }else if (msg.arg1 == 5) {

                        tv_time.setText("注册请输入名称");

                    }
                }
                break;
                }

            }
        }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override

        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //    surfaceView.setVisibility(View.VISIBLE);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };


    protected void hideBottomUIMenu() {
        int flags;
        int curApiVersion = Build.VERSION.SDK_INT;
        // This work only for android 4.4+
        if(curApiVersion >= Build.VERSION_CODES.KITKAT){
            // This work only for android 4.4+
            // hide navigation bar permanently in android activity
            // touch the screen, the navigation bar will not show
            flags = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }else{
            // touch the screen, the navigation bar will show
            flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }

        // must be executed in main thread :)
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }

    boolean drawRect(List<com.openailab.facelibrary.FaceInfo> faceInfos,Mat mat){
      //  Log.d("zheng","drawRectView????");
      Log.d("cpdebugwwhh","view W:"+drawRectView.getWidth()+" H:"+drawRectView.getHeight());
      Log.d("cpdebugwwhh","cam W:"+camWidth+" H:"+camHeight);
      for (int icount = 0; icount < faceInfos.size(); icount++) {
            FaceInfo info = faceInfos.get(icount);
            if(orientation == Configuration.ORIENTATION_LANDSCAPE)
                drawRectView.updateRect(info.mRect.left * drawRectView.getWidth() / camWidth, info.mRect.top * drawRectView.getHeight() / camHeight, info.mRect.right * drawRectView.getWidth() / camWidth, info.mRect.bottom * drawRectView.getHeight() / camHeight,icount);
            else if(orientation == Configuration.ORIENTATION_PORTRAIT)
//                drawRectView.updateRect(info.mRect.left * drawRectView.getWidth() / camHeight, info.mRect.top * drawRectView.getHeight() / camWidth, info.mRect.right * drawRectView.getWidth() / camHeight, info.mRect.bottom * drawRectView.getHeight() / camWidth,icount);
                 drawRectView.updateRect(info.mRect.left * drawRectView.getWidth() / camWidth, info.mRect.top * drawRectView.getHeight() / camHeight, info.mRect.right * drawRectView.getWidth() / camWidth, info.mRect.bottom * drawRectView.getHeight() / camHeight,icount);

      }
//         Log.d("zheng","faceInfos.size():"+faceInfos.size());
        for (int icount = faceInfos.size(); icount < FACENUM; icount++) {
            drawRectView.updateRect(0, 0, 0, 0, icount);
        }
        return true;
  }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //copyFilesFassets(this,"openailab","/mnt/sdcard/openailab");
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
        getWindow().setAttributes(params);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            surfaceView = (SurfaceView) findViewById(R.id.java_camera_view);
            drawRectView = (myDrawRectView) findViewById(R.id.mipi_preview_content);
            tv_time = (TextView) findViewById(R.id.tv_time);
            drawRectView.getHolder().setFormat(PixelFormat.TRANSPARENT);
            permissionsDelegate.checkRequiredPermission();

            hasCameraPermission = permissionsDelegate.hasCameraPermission();
            while (!hasCameraPermission) {
                permissionsDelegate.requestCameraPermission();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                hasCameraPermission = permissionsDelegate.hasCameraPermission();
            }

            hasExtSDPermission = permissionsDelegate.hasExtSDPermission();
            while (!hasExtSDPermission) {
                permissionsDelegate.requestExtSDPermission();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                hasExtSDPermission = permissionsDelegate.hasExtSDPermission();
            }

            surfaceView.setVisibility(SurfaceView.VISIBLE);
            drawRectView.setVisibility(SurfaceView.VISIBLE);
            drawRectView.setZOrderOnTop(true);
            orientation = this.getResources().getConfiguration().orientation;

            posStr = new byte[1024];


            FaceInfos = new ArrayList<>();
            //  FaceInfos.add(new FaceInfo());

            copyFilesFassets(this, "openailab", "/sdcard/openailab");
            sharedPref = getSharedPreferences(getString(R.string.pref_start_id),Context.MODE_PRIVATE);
            editor= sharedPref.edit();
            stattID=sharedPref.getInt(getString(R.string.pref_start_id), 0);
            face.SetParameter(faceparams, VALUE);


            setSettingView();

      //  mOpenCvCameraView.setCvCameraViewListener(this);
            DisplayMetrics mDisplayMetrics = new DisplayMetrics();//屏幕分辨率容器
            getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
            mWidth = mDisplayMetrics.widthPixels;
            mHeight = mDisplayMetrics.heightPixels;
            videoUtil = new VideoUtil(surfaceView.getHolder(), 720, 640,CAMERA_FACING_BACK,this);
            mRgb = new Mat(480, 640, CvType.CV_8UC1);
            mRgbaFrame = new Mat(480, 640, CvType.CV_8UC1);
            surfaceView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {
                    lastClickTime = currentTime;
                    drawMat = false;
                    //switch to sleep mode
                    mixController.setState(mixController.STATE_FACE_RECOGNIZE, mixController.STATE_FACE_REGISTER);
                    // Toast.makeText(MainActivity.this,"注册成功",Toast.LENGTH_LONG).show();
                    final View pwdEntryView = MainActivity.this.getLayoutInflater().inflate(
                            R.layout.dialog_exit_pwd, null);

                    //图片导入
                    Button file_browser = (Button) pwdEntryView.findViewById(R.id.file_browser);
                    file_browser.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent openFileBrowser = new Intent(MainActivity.this, GetSDTreeActivity.class);
                            videoUtil.stopPreview();
                            drawRectView.surfaceDestroyed(drawRectView.getHolder());
                            startActivity(openFileBrowser);
                            //    MainActivity.this.finish();
                        }
                    });

                    final EditText register_edittext = (EditText) pwdEntryView.findViewById(R.id.register_edittext);
                    register_edittext.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});//10 char

                    TextView sdk_version_text = (TextView) pwdEntryView.findViewById(R.id.sdk_version_text);
                    sdk_version_text.setText("SDK Version: " + face.GetVersion() + "    " + "Lib Version: " + face.GetFacelibVersion());


                    new AlertDialog.Builder(MainActivity.this).setTitle("确认注册吗？")
                            .setIcon(android.R.drawable.ic_input_add)
                            .setView(pwdEntryView)
                            .setCancelable(false)
                            .setPositiveButton("点此采集图片注册", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // 点击“确认”后的采集图片，用于注册
                                    drawMat = false;// 默认关闭true;
                                    editTextString = register_edittext.getText().toString();
                                    editTextString = editTextString.replaceAll(" ", "");
                                    editTextString = editTextString.replaceAll("\r", "");
                                    editTextString = editTextString.replaceAll("\n", "");


                                    if (editTextString == null || "".equals(editTextString)|| liveRegisterStatus == 1) {
                                        Message tempMsg = mHandler.obtainMessage();
                                        tempMsg.arg1 = 5;
                                        tempMsg.what = SHOWTOAST;
                                        mHandler.sendMessage(tempMsg);

                                        drawMat = false;//true;
                                        mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_RECOGNIZE);
                                        return;
                                    }
                                    mixController.setState(mixController.STATE_FACE_REGISTER, mixController.STATE_FACE_RECOGNIZE);
                                    //    FileOperator.namelist[FileOperator.getfIndex()] = editTextString;
                                    Log.d("morrisdebug", "name is " + register_edittext.getText().toString());

                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    drawMat = false;//true;
                                    mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_RECOGNIZE);
                                    // 点击“返回”后的操作,这里不设置没有任何操作
                                    //Toast.makeText(MainActivity.this, "你点击了返回键", Toast.LENGTH_LONG).show();
                                }
                            }).show();

                }
            }
        });
        lock = new ReentrantLock();

        lockcam = new ReentrantLock();
        lockth = new ReentrantLock();

        face.OpenDB();
        mainLoop = new Thread() {
            public void run() {
                int j;
                int[] i=new int[1];
                float[] high = new float[1];
                int recogTimes = 0;
                long now;
                long pas = 0;

//                  String[] params={"perfoptimize","mode"};
                // boolean fileExist= FileOperator.setStorePath(STORE_PATH);

                while (true) {
                    now = System.currentTimeMillis();
                    float fps = 1000/((float)(now - pas));
                    pas = System.currentTimeMillis();

                    if(!noExit)
                    {
                        canCloseCam=true;
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("cpdebugquit", "sleeping");
                        continue;
                    }
                    if (!videoUtil.isSyncFlag()) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.d("cpdebugquit", "syncFlag");
                        continue;
                    }
                    lock.lock();
                    try{
                        //    mRgbaFrame = videoUtil.getmRgba().clone();
                        //   mRgbaFrame = new Mat(mWidth, mHeight, CvType.CV_8UC1);
                        Imgproc.cvtColor(videoUtil.getmRgba(), mRgbaFrame, Imgproc.COLOR_YUV2RGBA_NV12, 4);

                    }catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }finally {
                        lock.unlock();
                    }
                    //  Imgcodecs.imwrite("/sdcard/openailab/models/reg/1.jpg", mRgbaFrame);
                    FaceAPP.Image image= FaceAPP.GetInstance().new Image();
                    if(orientation == Configuration.ORIENTATION_PORTRAIT){
                        Core.flip(mRgbaFrame, mRgbaFrame, -1); //图像镜像 1:Y轴、0：X轴、-1:X,Y轴都旋转180
                        //Core.transpose(mRgbaFrame,mRgbaFrame);//逆时针旋转90
                    }
                    if (flipInt==1) {
                        Core.flip(mRgbaFrame, mRgbaFrame, 1);// Core.transpose(mRgbaFrame,mRgbaFrame);   //
                    }
                    String nameStr;
                    int Result;
                    switch (mixController.curState){
                        //region STATE_FACE_RECOGNIZE
                        case mixController.STATE_FACE_RECOGNIZE:
//                            if (FACENUM == 3) {
//                                mOpenCvCameraView.updateRect(0, 0, 0, 0, 0, false);
//                                mOpenCvCameraView.updateRect(0, 0, 0, 0, 1, false);
//                                mOpenCvCameraView.updateRect(0, 0, 0, 0, 2, false);
//                            } else {
//                                mOpenCvCameraView.updateRect(0, 0, 0, 0, 1, false);
//                            }
                            try {
                                Thread.sleep(100);
                               // mainLoop .interrupt();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            break;
                            //endregion
                        //region STATE_FACE_REGISTER
                        case mixController.STATE_FACE_REGISTER:
                            Log.d("cpdebugregister","register");

                            if (editTextString == null || "".equals(editTextString)) {
                                break;
                            }
                            image.matAddrframe=mRgbaFrame.getNativeObjAddr();
//                            double[] value_close = new double[]{ 0, 1};
//                            double[] value_open = new double[]{ 1, 1};
//                            face.SetParameters(params,value_close);
                            j=face.GetFeature(image,feature,i);
//                            face.SetParameters(params,value_open);
                            if(j==face.SUCCESS)
                            {
                               // Imgproc.cvtColor(mRgbaFrame, mRgbaFrame, Imgproc.COLOR_RGBA2BGR);//
                              //  Imgcodecs.imwrite("/sdcard/openailab/models/reg/"+fIndex+".jpg",mRgbaFrame);
                                Message tempMsg = mHandler.obtainMessage();
                                high = new float[1];
                                String name = face.QueryDB(feature, high);
                                Log.i("zheng queryDB", name + " " + high[0]);
                                if (!name.equals("unknown") && high[0] > 0.6) {
                                    tempMsg.what = SHOWTOAST;
                                    tempMsg.arg1 = 6;
                                    tempMsg.obj = name;
                                    mHandler.sendMessage(tempMsg);
                                    mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_REGISTER);
                                    break;
                                }

                                Log.d("zheng", "register editTextString:" + editTextString);
                                int res = face.AddDB(feature, editTextString);
                                Log.d("zheng", "register res:" + res);
                                tempMsg = mHandler.obtainMessage();
                                if (res == FaceAPP.SUCCESS) {
                                    tempMsg.arg1 = 1;
                                } else {
                                    tempMsg.arg1 = 2;
                                }
                                tempMsg.what = SHOWTOAST;
                                mHandler.sendMessage(tempMsg);


//                                editor.putInt(getString(R.string.pref_start_id), FileOperator.getfIndex());
//                                editor.commit();
                                mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_REGISTER);

                            }else if(j==face.ERROR_FAILURE){
                                //EventBus.getDefault().post(new ControllerMessage(mixController.M_REGISTER_ALREADY_REG));
                                mixController.setState(mixController.STATE_IDLE, mixController.STATE_FACE_REGISTER);
                            }
                            editTextString = "";

                            break;
                        //endregion
                        //region STATE_IDLE
                        case mixController.STATE_IDLE:
//                            float[] feature1 = face.GetFeature("/sdcard/openailab/img/xr.jpg");
//                            Log.d("cpdebugcompare","feature "+Arrays.toString(feature1));
//                            face.AddDB(feature1,"chensongsong");
//                            float[] feature1 = {(float)-0.047144875, (float)-0.005420217, (float)0.0036012456, (float)0.85728097, (float)-0.84090614, (float)0.6598927, (float)-0.23422688, (float)-0.6941659, (float)-0.30188024, (float)0.8000775, (float)-0.2745995, (float)0.2766395, (float)0.16512191,(float) -0.34506828,(float) -0.6066702, (float)0.33715948, (float)-0.20027098, (float)0.16061115, (float)-1.1285006, (float)0.31331402, (float)-0.21087024, (float)0.44637957, (float)-0.81895375, (float)0.23759812, (float)-0.9323507, (float)0.22950114, (float)-0.31274086, (float)0.25116214, (float)0.8306529, (float)2.1521256, (float)1.1601923, (float)-0.3917077, (float)0.9375248, (float)1.1242672, (float)0.5498112, (float)0.0154573545,(float)-0.56107503, (float)0.8528301,(float)-0.6503028, (float)0.21532713, (float)0.03657838, (float)0.7033859, (float)-0.47959942,(float)-0.37111256, (float)1.0426393, (float)-0.020661965, (float)0.20067592, (float)0.08086881, (float)-1.0275128, (float)-0.12985961, (float)-2.0615528, (float)-0.48695272, (float)0.45544085, (float)0.027987741,(float) 1.1126806, (float)1.0350212,(float) -0.36586142,(float) -0.6458785, (float)0.1510956, (float)-1.7797278, (float)1.0250428,(float) 0.8458639, (float)0.65939057, (float)-0.1961589, (float)-0.20434868,(float) 0.22450045, (float)0.96479475,(float) -0.5310764,(float) 1.0606737,(float) -0.4835981, (float)0.4624825,(float) -0.09310223, (float)-0.626134, (float)0.97702295,(float) -3.19371E-4, (float)-1.146345,(float) 0.508686,(float) 0.5542527, (float)-0.43980804, (float)-0.2076631, (float)0.28326434,(float) -0.63484335, (float)0.42679656, (float)0.5214988, (float)-0.936661, (float)-1.0985882,(float) 1.3618562,(float) -0.08548756, (float)-0.36228222, (float)0.36294758, (float)-1.0202066, (float)-0.0112367235, (float)0.06322753, (float)0.98281324, (float)-0.10643123,(float) -0.68729794, (float)-0.020534826, (float)0.025402412, (float)0.6330543,(float) -0.5056231,(float) -0.758785,(float) -0.09179802,(float) -0.11150506,(float) 0.29092383, (float)0.5171425,(float) 0.15719506, (float)1.0531104, (float)0.34779269, (float)0.4188182, (float)0.50517005, (float)2.3309655,(float)0.2548857,(float) -0.9441643, (float)-1.4256437, (float)0.16762695,(float) -0.7066759, (float)-0.19298698, (float)-0.09957867,(float) 0.5362976, (float)0.17925465, (float)0.13819239, (float)0.5878146,(float) 1.0735191, (float)-0.052457552, (float)-0.69183445, (float)0.26991752,(float) -0.086714454, (float)-0.31215551};
//                            float[] feature2 = {(float)-1.0548366, (float)-0.66977787, (float)-0.53207654, (float)0.1321981, (float)0.07622919, (float)1.4776361, (float)0.23269504, (float)-0.41584328, (float)-0.098721206, (float)1.2730495, (float)-0.31751952, (float)0.14875394, (float)-0.18179514, (float)0.5594324, (float)-0.85071623, (float)-0.692971, (float)-0.33253145, (float)-1.3956255, (float)-1.7062448,(float)0.5416481, (float)0.33291304, (float)0.6026625, (float)0.98189056, (float)0.16909309,(float)0.4955194, (float)0.23118125, (float)-1.1850481, (float)-0.13952745, (float)0.38855237, (float)1.4968474, (float)1.3939568, (float)0.3803649, (float)1.2341152, (float)1.076369, (float)1.1651728, (float)-0.024771549, (float)-0.88746816, (float)8.7557733E-4, (float)-0.50187534, (float)1.148891, (float)0.40545198, (float)1.0953568, (float)-0.35420045, (float)-0.72488046, (float)0.88419855, (float)-1.2763613, (float)0.73509175, (float)0.0041365996, (float)-0.6583025, (float)-0.36792827, (float)-1.5009096, (float)0.4492366, (float)0.7410163, (float)-0.031143893, (float)0.17647536,(float) 1.7334212,(float) -0.2836518,(float) 0.11542418,(float)-0.1527978, (float)-1.1764603, (float)1.0816518, (float)1.6023172, (float)0.9176276, (float)-0.7137016,(float) -0.28014362, (float)0.44032744,(float) 1.0299959, (float)-1.4592344, (float)0.21647848, (float)-0.79300094, (float)1.2706705, (float)0.14643341, (float)-1.0731881,(float) 1.2273581,(float)0.5423558, (float)-0.65153444, (float)-0.041308057, (float)0.26743165, (float)-0.8232568, (float)0.08661926, (float)0.74271744, (float)-0.947781, (float)0.61230606, (float)0.9928942, (float)-0.5133051,(float) -0.53072923, (float)1.3172853, (float)-0.1223027, (float)0.09987246,(float) 0.11436714, (float)-0.359479, (float)-0.13986897, (float)-0.3756425,(float) 0.784392, (float)-0.71151394, (float)-0.98842084, (float)-0.0606526, (float)0.38472494, (float)1.4629014, (float)-0.5438065, (float)-1.4894849, (float)0.25157586, (float)-0.22736527, (float)0.03218701, (float)1.3740467, (float)0.5033148, (float)0.5145144, (float)0.1308575, (float)-0.030407902, (float)1.0626982, (float)2.009136,(float) -0.8033429,(float) -1.5369862, (float)-1.746369, (float)-0.2502068, (float)-0.6141847,(float) -0.94739187, (float)0.05955346, (float)-0.62645537, (float)-0.071351804,(float)0.026106842, (float)1.0747392,(float) 1.9667438,(float) -0.14005254, (float)-0.71351635, (float)0.58252454,(float) 0.7723202, (float)0.70560265};
//                            Log.d("cpdebugcompare","score " + face.Compare(feature1,feature2));
                            if(mRgbaFrame.empty()||mRgbaFrame.channels()!=4){
                                break;
                            }
                            image.matAddrframe=mRgbaFrame.getNativeObjAddr();
                            j = face.Detect(image, FaceInfos, i);
//                             long liveness_t1 = System.currentTimeMillis();
//                            j=face.Recognize(image, FileOperator.getflist(), FileOperator.getfIndex(), FaceInfos,i);
//                              long  liveness_t2 = System.currentTimeMillis();
//                              Log.d("zheng", "Recognize time:" +j+"    " +(liveness_t2 - liveness_t1));
//                                face.Detect(image, FaceInfos, i);
//                                j = face.GetFeature(image, feature, i);
//                                j = face.GetFeature(image, feature, FaceInfos,i);

                             //Imgcodecs.imwrite("/sdcard/openailab/mRgbaFrame.jpg", mRgbaFrame);
                            //region success
                            if(j==face.SUCCESS){
                                FaceAttribute faceAttr = new FaceAttribute();
                                face.GetFaceAttr(image,FaceInfos.get(0),faceAttr,i);
                                face.GetFeature(image,FaceInfos.get(0),feature,i);
                                float[] score = {0};
                                // long liveness_t1 = System.currentTimeMillis();
                                showName = face.QueryDB(feature, score);
                                //  long  liveness_t2 = System.currentTimeMillis();
                                //  Log.d("zheng", "QueryDB time:" + (liveness_t2 - liveness_t1));
//                                Log.i("cpdebugfaceinfo",FaceInfos.toString());
                                Log.i("zheng", "name " + showName + " score " + score[0]);
                                drawRect(FaceInfos, mRgbaFrame);
                                String gender;
                                String emotion;
                                if(FaceAttribute.GENDER_MALE == faceAttr.mGender)
                                    gender = "男";
                                else
                                    gender = "女";
                                if(FaceAttribute.EMOTION_CALM == faceAttr.mEmotion)
                                    emotion = "calm";
                                else
                                    emotion = "开心";
                                Message tempMsg = mHandler.obtainMessage();
                                tempMsg.what = SHOWTOAST;
                                if (score[0] > VALUE[3]) {
                                        tempMsg.arg1 = 0;
                                        drawRectView.updateDrawFlag(true, showName+" "+faceAttr.mAge+" "+gender+" "+emotion, fps);
                                } else {
                                        drawRectView.updateDrawFlag(true, "未注册"+" "+faceAttr.mAge+" "+gender+" "+emotion, fps);
                                        tempMsg.arg1 = 3;
                                }
                                    //   tempMsg.obj = i[0];
                                tempMsg.what = SHOWTOAST;
                                mHandler.sendMessage(tempMsg);
                                recogTimes++;

                            }
                            //endregion
                            //region error
                            else if(j==face.ERROR_FAILURE){
                                drawRectView.updateDrawFlag(false, null, fps);
                                Log.d("morrisdebug","cant recoginze you");
                             //   Log.d("zheng", "!!!!!!!!!!!!!!!!");
                                if(i[0]==face.ERROR_NOT_EXIST){
                                    drawRect(FaceInfos,mRgbaFrame);
                                }else if(i[0]==face.ERROR_INVALID_PARAM){
                                    for (int icount = 0; icount < FACENUM; icount++) {
                                        drawRectView.updateRect(0, 0, 0, 0, icount);
                                    }
                                }
                                Message tempMsg = mHandler.obtainMessage();
                                tempMsg.arg1 = 3;
                                tempMsg.what = SHOWTOAST;
                                mHandler.sendMessage(tempMsg);
                            }
                            //enregion

                            break;
                        //endregion
                        //region default
                        default:
                            try {
                                currentThread().sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        //endregion
                    }
                    FaceInfos.clear();
                    mRgbaFrame.release();
                    mRgb.release();

                }
            }
        };
        mainLoop.start();

    }
    @Override
    public void onStart() {
        super.onStart();
       
    }
    @Override
    public void onStop() {
        super.onStop();
        videoUtil.stopPreview();
    }
    @Override

    public void onPause()

    {  super.onPause();
        // mView.onPause();
        lockth.lock();
        try{
            canCloseCam=false;
            noExit=false;
            while(!canCloseCam){
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockth.unlock();
        }

    }
    @Override

    public void onResume()
    {
        super.onResume();

        mixController.getInstance(this);
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        hideBottomUIMenu();
        lockth.lock();
        try{
            noExit=true;
        }finally {
            lockth.unlock();
        }

    }
    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.d("morrisdebug", "destroy you ");
        mixController.setState(mixController.STATE_FACE_RECOGNIZE, mixController.STATE_FACE_REGISTER);
        noExit=false;
        face.CloseDB();
        face.Destroy();
        face=null;
        if (videoUtil != null)
            videoUtil.stopPreview();
        mainLoop = null;
        System.exit(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionsDelegate.resultGranted(requestCode, permissions, grantResults)) {

            surfaceView.setVisibility(SurfaceView.VISIBLE);
        }
    }

    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context,oldPath + "/" + fileName,newPath+"/"+fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount=0;
                while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程

        }
    }


    private int isCPUInfoARMv7() {
        File cpuInfo = new File(PROC_CPU_INFO_PATH);
        if (cpuInfo != null && cpuInfo.exists()) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new FileInputStream(cpuInfo);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 512);
                String line = bufferedReader.readLine();
                //arch64
                Log.d("zheng", "line:" + line);
                if (line != null && line.length() > 0 && line.toLowerCase(Locale.US).contains("armv7")) {

                    return 1;
                } else {
                    if (line != null && line.length() > 0 && line.toLowerCase(Locale.US).contains("arch64"))
                        return 0;
                }
            } catch (Throwable t) {
                return -1;
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    //设置
    private void setSettingView() {
        getSettngFileFromPath();
        Button flipBtn = (Button) this.findViewById(R.id.flipBtn);
        settingLayout = (RelativeLayout) this.findViewById(R.id.settingLayout);
        settingSaveBtn = (Button) this.findViewById(R.id.settingSaveBtn);
//        settingCancel = (Button) this.findViewById(R.id.settingCancel);
        radioGroupFlip = (RadioGroup) this.findViewById(R.id.radioGroupFlip);
        RadioButton flipSettingsDisable = (RadioButton) findViewById(R.id.flipSettingsDisable);
        RadioButton flipSettingsEnable = (RadioButton) findViewById(R.id.flipSettingsEnable);

        String flipStr = settingMap.get("flip");
        if ("1".equals(flipStr)) {
            flipSettingsEnable.setChecked(true);
            flipSettingsDisable.setChecked(false);
            flipInt = 1;
        } else  if ("0".equals(flipStr)){
            flipSettingsEnable.setChecked(false);
            flipSettingsDisable.setChecked(true);
            flipInt = 0;
        }


        flipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingLayout.setVisibility(View.VISIBLE);
            }
        });

        settingLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        settingSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileOperator.saveSettingIntoTxt(settingMap, settingFile);
                settingLayout.setVisibility(View.GONE);
            }
        });

//        settingCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                settingLayout.setVisibility(View.GONE);
//            }
//        });

        radioGroupFlip.setOnCheckedChangeListener(
                new RadioGroup.OnCheckedChangeListener() {
                    public void onCheckedChanged(RadioGroup group, int checkedId) {

                        if (checkedId == R.id.flipSettingsEnable) {
                            flipInt = 1;
                            settingMap.put("flip", "1");
                        }
                        //else if(String.valueOf(radioButton.getText()).equalsIgnoreCase("启用"))
                        else if (checkedId == R.id.flipSettingsDisable) {
                            flipInt = 0;
                            settingMap.put("flip", "0");
                        }
                    }
                });


    }

    private boolean getSettngFileFromPath() {
        String path = "/sdcard/openailab/setting.conf";

        settingFile = new File(path);
        if (!settingFile.exists()) {
            settingMap = new HashMap<String, String>();
            try {
                settingFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else {
            settingMap = FileOperator.getSettingMapFromFile(settingFile);
            return true;
        }

    }




}
