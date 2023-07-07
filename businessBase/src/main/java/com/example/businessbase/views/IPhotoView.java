package com.example.businessbase.views;

import android.graphics.Matrix;
import android.graphics.RectF;

public interface IPhotoView {

    boolean isReady();

    int getViewWidth();

    int getViewHeight();

    RectF getImageRectF();

    RectF getImageDisplayRectF();

    Matrix getInnerMatrix(Matrix matrix);

    void vibrate(int effectId);
}
