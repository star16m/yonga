package com.yonga.auc.data.category;

/**
 * Extract Mode.
 */
public enum ExtractMode {

    INITIALIZE(true),
    EXTRACT(false)
    ;

    private boolean requiredInitialize = false;

    ExtractMode(boolean requiredInitialize) {
        this.requiredInitialize = requiredInitialize;
    }
    public boolean isRequiredInitialize() {
        return this.requiredInitialize;
    }
}