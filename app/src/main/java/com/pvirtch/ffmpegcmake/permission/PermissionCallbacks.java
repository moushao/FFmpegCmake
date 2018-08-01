package com.pvirtch.ffmpegcmake.permission;

import android.support.v4.app.ActivityCompat;

import java.util.List;

public interface PermissionCallbacks extends
            ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

        void onPermissionsAllGranted();

    }