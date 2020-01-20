package cn.com.cg.ocr.idcardocr.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 14:56
 */
public class ScanSurfaceView extends ViewGroup {


    private SurfaceView mSurfaceView;
    private ScanMaskView mMaskView;


    public ScanSurfaceView(Context context) {
        this(context, null);
    }

    public ScanSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            removeAllViews();
            if (mSurfaceView != null) {
                addView(mSurfaceView);
                mSurfaceView.layout(l, t, r, b);
            }
            if (mMaskView != null) {
                addView(mMaskView);
                mMaskView.layout(l, t, r, b);
            }
        }
    }

    private void init() {
        mSurfaceView = new SurfaceView(getContext());
        mMaskView = new ScanMaskView(getContext());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }

    public void setScanType(int type) {
        if (mMaskView != null) {
            mMaskView.setScanType(type);
        }
    }

    public int getXLocation() {
        return mMaskView.getxLocation();
    }

    public int getYLocation() {
        return mMaskView.getyLocation();
    }

    public void refreshScanRect(Camera.Size size) {
        mMaskView.setPreviewSize(size);
    }

    public SurfaceHolder getHolder() {
        return mSurfaceView.getHolder();
    }

    public void setHintText(String string) {
        mMaskView.setHintText(string);
    }

    public Rect getScanRect() {
        return mMaskView.getMaskRect();
    }

    public Rect getScanRectOnScreen() {
        int[] position = new int[2];
        mMaskView.getLocationOnScreen(position);
        Rect rect = mMaskView.getMaskRect();
        int left = rect.left;
        int top = rect.top;
        int right = rect.right;
        int bottom = rect.bottom;
        Rect rect1 = new Rect();
        rect1.left = left + position[0];
        rect1.top = top + position[1];
        rect1.right = right + position[0];
        rect1.bottom = bottom + position[1];
        return rect1;
    }
}
