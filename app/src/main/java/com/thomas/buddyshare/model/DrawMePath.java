package com.thomas.buddyshare.model;

import android.graphics.Path;

public class DrawMePath extends Path {

    int colorId;
    int stokeSize;

    public DrawMePath(int color, int size) {
        this.colorId = color;
        this.stokeSize = size;
    }

    public int getColorId() {
        return colorId;
    }

    public int getStokeSize() {
        return stokeSize;
    }

    public void setColorId(int colorId) {
        this.colorId = colorId;
    }

    public void setStokeSize(int stokeSize) {
        this.stokeSize = stokeSize;
    }
}
