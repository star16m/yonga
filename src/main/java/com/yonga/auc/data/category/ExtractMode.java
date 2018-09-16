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
    public Integer getExtractProductNumber(Category category) {
        Integer startAt = new Integer(0);
        switch (this) {
            case INITIALIZE:
                break;
            case EXTRACT:
                startAt = category.getExtProductNum();
                break;
        }
        return startAt;
    }
}