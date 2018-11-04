package com.feizhang.permission;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class Permissions {
    private static final String TAG = Permissions.class.getSimpleName();

    private Lazy<PermissionsFragment> mPermissionsFragment;

    public Permissions(@NonNull final FragmentActivity activity) {
        mPermissionsFragment = getLazySingleton(activity.getSupportFragmentManager());
    }

    public Permissions(@NonNull final Fragment fragment) {
        mPermissionsFragment = getLazySingleton(fragment.getChildFragmentManager());
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    public void request(final OnGrantResult result, final String... permissions) {
        final List<Permission> resList = new ArrayList<>();
        requestImplementation(new OnPermissionCallback() {
            @Override
            public void onNext(Permission permission) {
                resList.add(permission);
            }

            @Override
            public void onComplete() {
                for (Permission permission : resList) {
                    if (!permission.granted) {
                        result.onDenied();
                        return;
                    }
                }

                result.onGrant();
            }
        }, permissions);
    }

    /**
     * Request permissions immediately, <b>must be invoked during initialization phase
     * of your application</b>.
     */
    public void requestEach(final OnEachGrantResult result, final String... permissions) {
        requestImplementation(new OnPermissionCallback() {
            @Override
            public void onNext(Permission permission) {
                result.onNext(permission);
            }

            @Override
            public void onComplete() {
            }
        }, permissions);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestImplementation(OnPermissionCallback onPermissionCallback, final String... permissions) {
        if (permissions == null || permissions.length == 0) {
            throw new IllegalArgumentException("Permissions.request/requestEach requires at least one input permission");
        }

        mPermissionsFragment.get().setOnPermissionCallback(onPermissionCallback);
        List<String> unrequestedPermissions = new ArrayList<>();

        // In case of multiple permissions, we create an Observable for each of them.
        // At the end, the observables are combined to have a unique response.
        for (String permission : permissions) {
            mPermissionsFragment.get().log("Requesting permission " + permission);
            if (isGranted(permission)) {
                // Already granted, or not Android M
                // Return a granted Permission object.
                onPermissionCallback.onNext(new Permission(permission, true, false));
                continue;
            }

            if (isRevoked(permission)) {
                // Revoked by a policy, return a denied Permission object.
                onPermissionCallback.onNext(new Permission(permission, false, false));
                continue;
            }

            // Create a new subject if not exists
            if (!mPermissionsFragment.get().containsPermission(permission)) {
                unrequestedPermissions.add(permission);
                mPermissionsFragment.get().addPermission(permission);
            }
        }

        if (unrequestedPermissions.isEmpty()) {
            onPermissionCallback.onComplete();
        } else {
            requestPermissionsFromFragment(unrequestedPermissions.toArray(new String[0]));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermissionsFromFragment(String[] permissions) {
        mPermissionsFragment.get().log("requestPermissionsFromFragment " + TextUtils.join(", ", permissions));
        mPermissionsFragment.get().requestPermissions(permissions);
    }

    @NonNull
    private Lazy<PermissionsFragment> getLazySingleton(@NonNull final FragmentManager fragmentManager) {
        return new Lazy<PermissionsFragment>() {
            private PermissionsFragment permissionsFragment;

            @Override
            public synchronized PermissionsFragment get() {
                if (permissionsFragment == null) {
                    permissionsFragment = getPermissionsFragment(fragmentManager);
                    permissionsFragment.setLogging(true);
                }
                return permissionsFragment;
            }
        };
    }

    private PermissionsFragment getPermissionsFragment(@NonNull final FragmentManager fragmentManager) {
        PermissionsFragment permissionsFragment = (PermissionsFragment) fragmentManager.findFragmentByTag(TAG);
        boolean isNewInstance = permissionsFragment == null;
        if (isNewInstance) {
            permissionsFragment = new PermissionsFragment();
            fragmentManager
                    .beginTransaction()
                    .add(permissionsFragment, TAG)
                    .commitNow();
        }
        return permissionsFragment;
    }

    /**
     * Returns true if the permission is already granted.
     * <p>
     * Always true if SDK &lt; 23.
     */
    @SuppressWarnings("WeakerAccess")
    public boolean isGranted(String permission) {
        return !isMarshmallow() || mPermissionsFragment.get().isGranted(permission);
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     * <p>
     * Always false if SDK &lt; 23.
     */
    public boolean isRevoked(String permission) {
        return isMarshmallow() && mPermissionsFragment.get().isRevoked(permission);
    }

    private boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    @FunctionalInterface
    interface Lazy<V> {
        V get();
    }

}
