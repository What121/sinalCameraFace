package com.openailab.sdkdemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

class PermissionsDelegate {

    private static final int REQUEST_CODE = 10;
    private final Activity activity;
    //所需要申请的权限数组
    private static final String[] permissionsArray = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    //还需申请的权限列表
    private List<String> permissionsList = new ArrayList<String>();
    //申请权限后的返回码
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    PermissionsDelegate(Activity activity) {
        this.activity = activity;
    }

    //获取多权限
    public void checkRequiredPermission() {
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if (permissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]), REQUEST_CODE_ASK_PERMISSIONS);
        }
    }

    boolean hasCameraPermission() {
        int permissionCheckResult = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
        );
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED;
    }

    void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE
        );
    }
    boolean hasExtSDPermission() {
        int permissionCheckResult = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED;
    }
    void requestExtSDPermission(){
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_CODE
        );
    }
    boolean hasaudioPermission() {
        int permissionCheckResult = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
        );
        return permissionCheckResult == PackageManager.PERMISSION_GRANTED;
    }
    void requestaudioPermission(){
        ActivityCompat.requestPermissions(
                activity,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_CODE
        );
    }
    boolean resultGranted(int requestCode,
                          String[] permissions,
                          int[] grantResults) {

        if (requestCode != REQUEST_CODE) {
            return false;
        }

        if (grantResults.length < 1) {
            return false;
        }
        if (!(permissions[0].equals(Manifest.permission.CAMERA))) {
            return false;
        }

        // View noPermissionView = activity.findViewById(R.id.no_permission);
        Toast.makeText(activity, "not support camera", Toast.LENGTH_LONG).show();
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // noPermissionView.setVisibility(View.GONE);
            return true;
        }

        requestCameraPermission();
        requestExtSDPermission();
        requestaudioPermission();
        //noPermissionView.setVisibility(View.VISIBLE);
        return false;
    }

    public void getAppDetailSettingIntent(Context context){

        Intent intent = new Intent();

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(Build.VERSION.SDK_INT >= 9){

            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");

            intent.setData(Uri.fromParts("package", context.getPackageName(), null));

        } else if(Build.VERSION.SDK_INT <= 8){

            intent.setAction(Intent.ACTION_VIEW);

            intent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");

            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());

        }

        context.startActivity(intent);

    }
}
