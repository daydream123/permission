package com.feizhang.permission;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * It's a none ui fragment and do request permission work inside.
 */
public class PermissionsFragment extends Fragment {
    private static final String TAG = "PermissionsFragment";
    public static final int PERMISSIONS_REQUEST_CODE = 9999;

    private OnPermissionCallback mOnPermissionCallback;
    private List<String> mPermissions = new ArrayList<>();
    private boolean mLogging;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    void requestPermissions(@NonNull String[] permissions) {
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean[] shouldShowRequestPermissionRationale = new boolean[permissions.length];

            for(int i = 0; i < permissions.length; ++i) {
                shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i]);
            }

            onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale);
        }
    }

    private void onRequestPermissionsResult(String[] permissions, int[] grantResults, boolean[] shouldShowRequestPermissionRationale) {
        int i = 0;

        for(int size = permissions.length; i < size; ++i) {
            log("onRequestPermissionsResult  " + permissions[i]);
            if (mOnPermissionCallback == null) {
                Log.e(TAG, "Permissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
                return;
            }

            mPermissions.remove(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            mOnPermissionCallback.onNext(new Permission(permissions[i], granted, shouldShowRequestPermissionRationale[i]));
        }

        mOnPermissionCallback.onComplete();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            Log.e(TAG, "This fragment must be attached to an activity.");
            return false;
        }

        return fragmentActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean isRevoked(String permission) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            Log.e(TAG, "This fragment must be attached to an activity.");
            return false;
        }

        return fragmentActivity.getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
    }

    void setOnPermissionCallback(OnPermissionCallback onPermissionCallback){
        mOnPermissionCallback = onPermissionCallback;
    }

    boolean containsPermission(String permission){
        return mPermissions.contains(permission);
    }

    void addPermission(@NonNull String permission) {
        mPermissions.add(permission);
    }

    void setLogging(boolean logging) {
        mLogging = logging;
    }

    void log(String message) {
        if (mLogging) {
            Log.d(TAG, message);
        }
    }
}
