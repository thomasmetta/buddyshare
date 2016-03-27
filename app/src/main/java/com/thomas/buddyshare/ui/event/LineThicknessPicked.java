package com.thomas.buddyshare.ui.event;


public class LineThicknessPicked {

    private int lineThickness;
    public LineThicknessPicked(int size) {
        this.lineThickness = size;
    }

    public int getLineThickness() {
        return lineThickness;
    }
}
