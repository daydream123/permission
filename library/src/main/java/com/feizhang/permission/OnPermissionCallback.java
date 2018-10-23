package com.feizhang.permission;

/**
 * Inner callback used for {@link Permissions} to communicate with FragmentsFragment.
 */
interface OnPermissionCallback {

    /**
     * Called by every grant operation
     *
     * @param permission {@link Permission}
     */
    void onNext(Permission permission);

    /**
     * Called when all permission grant operations completed.
     */
    void onComplete();
}
