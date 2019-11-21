package com.example.lesson73;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;


public class VectorView extends View {

    private final Paint mPaint = new Paint();

    private float mNormalizedX;
    private float mNormalizedY;
    private float mNormalizedZ;

    private float mPixelLength;
    private PointF mViewCenter;

    public VectorView(Context context) {
        this(context, null);
    }

    public VectorView( Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VectorView(Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint.setStrokeWidth(2);

        mNormalizedX = 1;
    }

    @Override
    protected void onSizeChanged (final int w, final int h, final int oldw, final int oldh) {
        mPixelLength = w < h ? w / 2 : h / 2;
        mViewCenter = new PointF(w / 2, h / 2);
    }

    public void setVector(final float x, final float y, final float z) {
        final double length = Math.sqrt(x * x + y * y + z * z);
        mNormalizedX = (float) (x / length);
        mNormalizedY = (float) (y / length);
        mNormalizedZ = (float) (z / length);

        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas)
    {
        if (mNormalizedZ < 0) {
            mPaint.setColor(0xff000080);
        } else {
            mPaint.setColor(0xff800000);
        }
        final float startX = mViewCenter.x;
        final float startY = mViewCenter.y;
        final float endX = startX + mNormalizedX * mPixelLength;
        final float endY = startY + mNormalizedY * mPixelLength;
        canvas.drawLine(startX, startY, endX, endY, mPaint);
    }
}
