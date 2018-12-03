package com.feizhang.permission;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

/**
 * It's a none ui fragment and do request permission work inside.
 */
public class PermissionsFragment extends Fragment {
    private static final String TAG = "PermissionsFragment";

    private SparseArray<OnPermissionCallback> mCallbackStorage = new SparseArray<>();
    private List<String> mPermissions = new ArrayList<>();
    private boolean mLogging;
    private int mRequestCode = 100;

    public static PermissionsFragment newInstance(){
        return new PermissionsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    void requestPermissions(@NonNull String[] permissions) {
        requestPermissions(permissions, mRequestCode++);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == mRequestCode) {
            boolean[] shouldShowRequestPermissionRationale = new boolean[permissions.length];

            for(int i = 0; i < permissions.length; ++i) {
                shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i]);
            }

            onRequestPermissionsResult(requestCode, permissions, grantResults, shouldShowRequestPermissionRationale);
        }
    }

    private void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults, boolean[] shouldShowRequestPermissionRationale) {
        int i = 0;

        OnPermissionCallback callback = getMatchedPermissionCallback(requestCode);
        for(int size = permissions.length; i < size; ++i) {
            log("onRequestPermissionsResult  " + permissions[i]);
            if (callback == null) {
                Log.e(TAG, "Permissions.onRequestPermissionsResult invoked but didn't find the corresponding permission request.");
                return;
            }

            mPermissions.remove(permissions[i]);
            boolean granted = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            callback.onNext(new Permission(permissions[i], granted, shouldShowRequestPermissionRationale[i]));
        }

        callback.onComplete();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean isGranted(String permission) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            throw new IllegalStateException("This fragment must be attached to an activity.");
        } else {
            return fragmentActivity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    boolean isRevoked(String permission) {
        FragmentActivity fragmentActivity = getActivity();
        if (fragmentActivity == null) {
            throw new IllegalStateException("This fragment must be attached to an activity.");
        } else {
            return fragmentActivity.getPackageManager().isPermissionRevokedByPolicy(permission, getActivity().getPackageName());
        }
    }

    void setOnPermissionCallback(OnPermissionCallback onPermissionCallback){
        mCallbackStorage.put(mRequestCode, onPermissionCallback);
    }

    private OnPermissionCallback getMatchedPermissionCallback(int requestCode){
        return mCallbackStorage.get(requestCode);
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
