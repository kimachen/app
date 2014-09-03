package com.brainydroid.daydreaming.db;

import com.brainydroid.daydreaming.background.Logger;
import com.google.gson.annotations.Expose;

public abstract class TypedStatusModel<M extends TypedStatusModel<M,S>,
        S extends TypedStatusModelStorage<M,S>> extends StatusModel<M,S> {

    private static String TAG = "TypedStatusModel";

    @Expose protected String type;

    protected synchronized void setType(String type) {
        Logger.v(TAG, "Setting type");
        this.type = type;
        saveIfSync();
    }

    public synchronized String getType() {
        return type;
    }

}
