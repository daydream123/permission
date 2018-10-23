package com.feizhang.permission;

/**
 * Permission grant result but it is called by every grant operation.
 */
public interface OnEachGrantResult {

    /**
     * Called by every grant operation
     *
     * @param permission {@link Permission}
     */
    void onNext(Permission permission);
}
