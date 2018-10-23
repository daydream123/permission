package com.feizhang.permission;

/**
 * It's a permission grant result callback and please known that
 * {@link #onGrant()} would be called only when all permissions
 * are granted otherwise it {@link #onDenied()} would be called.
 */
public abstract class OnGrantResult {

    /**
     * Called when all permissions are granted.
     */
    public abstract void onGrant();

    /**
     * Called when any one of permissions is denied.
     */
    public void onDenied() {
    }
}
