package com.openailab.sdkdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;

import com.openailab.facelibrary.FaceAPP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;


/**
 * created by LiGuang
 * on 2018/9/28
 */
public class LoginActivity extends Activity {

    TextView login_text;
    MyHandler mHandler;
    private FaceAPP face = FaceAPP.GetInstance();
    private final PermissionsDelegate permissionsDelegate = new PermissionsDelegate(this);
    private boolean hasCameraPermission;
    private boolean hasExtSDPermission;
    private final static String ROOT_DIR = "/sdcard/openailab/";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        login_text =(TextView)this.findViewById(R.id.login_text);

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

        FileOperator.createFolder(ROOT_DIR);


        mHandler = new MyHandler();

        new Thread() {
            @Override
            public void run() {
                super.run();
                authenticationResult();
            }
        }.start();



//        Intent intent = new Intent(LoginActivity.this,
//                MainActivity.class);
//        LoginActivity.this.startActivity(intent);
//        LoginActivity.this.finish();

//        Intent openFileBrowser = new Intent(LoginActivity.this, GetSDTreeActivity.class);
//        face.OpenDB();
//        startActivity(openFileBrowser);

    }


    private void authenticationResult() {
        Message tempMsg =null;

        Log.d("zheng", "login start!");

        String oem_id ="1000000000000001";//OEMID
        String contract_id ="0001";//合同号
        String password = "0123456789abcdef0123456789abcdef";//初始授权密码
        String uidStr = oem_id+contract_id;
        int res =face.AuthorizedDevice(uidStr,password, LoginActivity.this);

        Log.d("zheng", "auth res:"+res);

        if (res == 0) {
            Log.d("zheng", "auth is success!");
            tempMsg = mHandler.obtainMessage();
            tempMsg.what = 1;
            mHandler.sendMessage(tempMsg);
            return;
        }else {
            tempMsg = mHandler.obtainMessage();
            tempMsg.what = 2;
            mHandler.sendMessage(tempMsg);
        }
    }


    class MyHandler extends Handler {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("zheng","msg.what:"+msg.what);

            switch (msg.what) {
                case 1: {
                    login_text.setText("鉴权成功！正在启动SDK......");
                }
                break;
                case 2: {
                    login_text.setText("鉴权失败！正在启动SDK......");
                }
                break;
            }

            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(LoginActivity.this,
                    MainActivity.class);
            LoginActivity.this.startActivity(intent);
            LoginActivity.this.finish();
        }
    }




}
