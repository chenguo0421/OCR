package cn.com.cg.ocr.idcardocr.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.hardware.Camera;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import cn.com.cg.ocr.R;
import cn.com.cg.ocr.idcardocr.utils.DisplayUtil;


/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 14:56
 */
public class ScanMaskView extends View {
    public static final String TAG = "ScanMaskView";
    private Rect mScanRect;
    private Paint mLinePaint;
    private Paint mCornerPaint;
    private TextPaint mTextPaint;

    private volatile int mType = 1;
    public static final int TYPE_VEHICLE = 1;
    public static final int TYPE_IDCARD = 2;

    private int mHeight;
    private int mWidth;
    private int mCornerWidth;
    private int mRectLineWidth = 5;
    private int mGridLineWidth = 2;
    private int mCornerLineWith;
    private String mString;

    private Path mGridPath;
    private int mGridDensity = 50;
    private ValueAnimator mValueAnimator;
    private LinearGradient mLinearGradientGrid;
    private LinearGradient mLinearGradientRadar;
    private Matrix mScanMatrix;
    private Paint mScanPaintGrid;
    private Paint mScanPaintRadar;
    private int mScanColor;
    private long mScanAnimatorDuration = 1800;

    private int xLocation;
    private int yLocation;
    private int previewWidth;
    private int previewHeight;


    public ScanMaskView(Context context) {
        this(context, null);
    }

    public ScanMaskView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanMaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        Log.e(TAG, "onSizeChanged " + w + " " + h + " " + oldw + " " + oldh);
        mWidth = w;
        mHeight = h;
        if (previewWidth == 0) {
            previewWidth = w;
        }
        if (previewHeight == 0) {
            previewHeight = h;
        }

        changeRect();
        initScan();
        postInvalidate();
    }

    private void initScan() {
//        Log.e(TAG, "initScan");
        if (mScanRect == null) {
            return;
        }
        initGridPathAndStyle();
        initRadarStyle();
        initScanValueAnim();
    }

    public void setPreviewSize(Camera.Size size) {
        if (size != null) {
            previewHeight = size.width;
            previewWidth = size.height;
            changeRect();
            initScan();
            postInvalidate();
        }
    }

    private void changeRect() {
//        Log.e(TAG, "changeRect");
        if (mWidth == 0 || mHeight == 0) {
            return;
        }
        int width = 0;
        int height = 0;
        if (mType == TYPE_VEHICLE) {
            width = (int) (mWidth * 0.78);
            height = (int) (mWidth * 0.78 * 0.6);

        } else if (mType == TYPE_IDCARD) {
            Log.d("lh", "mWidth = " + mWidth + " previewWidth = " + previewWidth);
            Log.d("lh", "mHeight = " + mHeight + " previewHeight = " + previewHeight);

            //计算surfaceView 和 preview宽高比
            float wRatio = mWidth / (previewWidth + 0.0f);
            //按照宽度等比缩放后的高度
            float eqHeight = previewHeight * wRatio;
            float heightZoom = eqHeight / mHeight;
            float whRatio = (float) (0.62 * heightZoom);

            height = (int) ((mHeight - DisplayUtil.dip2px(getContext(), 65)) * 0.9);
            width = (int) (height * whRatio);
            if (width >= mWidth * 0.85) {
                width = (int) (mWidth * 0.68);
                height = (int) (mWidth * 0.68 / whRatio);
            }
            Log.d("lh", "wRatio = " + wRatio + " eqHeight = " + eqHeight + " heightZoom = " + heightZoom + " height = " + (mWidth * 0.73 / 0.62) + "/" + height);
        }
        int left = (mWidth - width) / 2;
        int top = (mHeight - height) / 3;
        int right = left + width;
        int bottom = top + height;
        mScanRect = new Rect(left, top, right, bottom);
    }

    private void initScanValueAnim() {
//        Log.e(TAG, "initScanValueAnim");
        if (mValueAnimator == null) {
            mValueAnimator = new ValueAnimator();
            mValueAnimator.setDuration(mScanAnimatorDuration);
            if (mType == TYPE_VEHICLE) {
                mValueAnimator.setIntValues(-mScanRect.height(), 0);
            } else {
                mValueAnimator.setIntValues(mScanRect.width(), 0);
            }
            mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
            mValueAnimator.setInterpolator(new DecelerateInterpolator());
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
//                    Log.e(TAG, "onAnimationUpdate------" + animation.getAnimatedValue());
                    if (mScanMatrix != null && mLinearGradientGrid != null/* && mScanPaint != null*/) {
                        float animatedValue = (int) animation.getAnimatedValue() + 0f;
                        if (mType == TYPE_VEHICLE) {
                            mScanMatrix.setTranslate(0, animatedValue);//操作偏移矩阵
                        } else {
                            mScanMatrix.setTranslate(animatedValue, 0);
                        }
                        mLinearGradientGrid.setLocalMatrix(mScanMatrix);//设置矩阵到网格画笔的着色器
                        mLinearGradientRadar.setLocalMatrix(mScanMatrix);//设置便宜后的矩阵到渐变效果的作色器上
                        invalidate();//重新绘制
                    }
                }
            });
        }
        mValueAnimator.start();//启动动画
    }

    private void initGridPathAndStyle() {
//        Log.e(TAG, "initGridPathAndStyle");
        mGridPath = new Path();
        float wUnit = mScanRect.width() / (mGridDensity + 0f);
        float hUnit = mScanRect.height() / (mGridDensity + 0f);
        for (int i = 0; i <= mGridDensity; i++) {
            mGridPath.moveTo(mScanRect.left + i * wUnit, mScanRect.top);
            mGridPath.lineTo(mScanRect.left + i * wUnit, mScanRect.bottom);
        }
        for (int i = 0; i <= mGridDensity; i++) {
            mGridPath.moveTo(mScanRect.left, mScanRect.top + i * hUnit);
            mGridPath.lineTo(mScanRect.right, mScanRect.top + i * hUnit);
        }

        if (mType == TYPE_VEHICLE) {
            mLinearGradientGrid = new LinearGradient(0, mScanRect.top, 0, mScanRect.bottom + 0.01f * mScanRect.height(),
                    new int[]{Color.TRANSPARENT, mScanColor, Color.TRANSPARENT},
                    new float[]{0, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);//构建着色器 注意此处的模式为：LinearGradient.TileMode.CLAMP，最后的区域0.99f到1f的区域为Color.TRANSPARENT
        } else {
            mLinearGradientGrid = new LinearGradient(mScanRect.right, 0, mScanRect.left /*+ 0.01f * mScanRect.height()*/, 0,
                    new int[]{Color.TRANSPARENT, mScanColor, Color.TRANSPARENT},
                    new float[]{0, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);//构建着色器 注意此处的模式为：LinearGradient.TileMode.CLAMP，最后的区域0.99f到1f的区域为Color.TRANSPARENT
        }
        mLinearGradientGrid.setLocalMatrix(mScanMatrix);//设置本地矩阵，以便操作着色器偏移以产生动画效果
        mScanPaintGrid.setShader(mLinearGradientGrid);//给画笔设置着色器

    }

    private void initRadarStyle() {
//        Log.e(TAG, "initRadarStyle");
        if (mType == TYPE_VEHICLE) {
            mLinearGradientRadar = new LinearGradient(0, mScanRect.top, 0, mScanRect.bottom + 0.01f * mScanRect.height(),
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, mScanColor, Color.TRANSPARENT},
                    new float[]{0, 0.85f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);//构建着色器
        } else {
            mLinearGradientRadar = new LinearGradient(mScanRect.right, 0, mScanRect.left, 0,
                    new int[]{Color.TRANSPARENT, Color.TRANSPARENT, mScanColor, Color.TRANSPARENT},
                    new float[]{0, 0.85f, 0.99f, 1f}, LinearGradient.TileMode.CLAMP);//构建着色器
        }
        mLinearGradientRadar.setLocalMatrix(mScanMatrix);//给着色器设置变换矩阵
        mScanPaintRadar.setShader(mLinearGradientRadar);//给画笔设置着色器

    }

    @Override
    protected void onDraw(Canvas canvas) {
//        Log.e(TAG, "onDraw");
        super.onDraw(canvas);
        if (mScanRect.width() > 0 && mScanRect.height() > 0) {
            drawRect(canvas);
            drawCorner(canvas);
            if (!TextUtils.isEmpty(mString)) {
                drawHintText(canvas);
            }
        }
    }

    private void drawRect(Canvas canvas) {
//        Log.e(TAG, "drawRect");
        if (mType == TYPE_IDCARD) {
//            int left = (int) (mScanRect.left + 0.245 * mScanRect.width());
//            int top = (int) (mScanRect.bottom - 0.35 * mScanRect.height());
//            int right = (int) (mScanRect.right - 0.151 * mScanRect.width());
//            int bottom = (int) (mScanRect.bottom - 0.059 * mScanRect.height());
            int left = (int) (mScanRect.left + 0.26 * mScanRect.width());
            int top = (int) (mScanRect.bottom - 0.35 * mScanRect.height());
            int right = (int) (mScanRect.right - 0.145 * mScanRect.width());
            int bottom = (int) (mScanRect.bottom - 0.059 * mScanRect.height());

            Rect picRect = new Rect(left, top, right, bottom);
            canvas.drawRect(picRect, mLinePaint);
            //身份证号码框
//            int numLeft = (int) (mScanRect.left + 0.05 * mScanRect.width());
//            int numTtop = (int) (mScanRect.bottom - 0.6 * mScanRect.height());
//            int numRight = (int) (mScanRect.right - 0.8 * mScanRect.width());
//            int numBottom = (int) (mScanRect.bottom - 0.06 * mScanRect.height());

            int numLeft = (int) (mScanRect.left + 0.05 * mScanRect.width());
            int numTtop = (int) (mScanRect.bottom - 0.6 * mScanRect.height());
            int numRight = (int) (mScanRect.right - 0.8 * mScanRect.width());
            int numBottom = (int) (mScanRect.bottom - 0.06 * mScanRect.height());
            Rect numRect = new Rect(numLeft, numTtop, numRight, numBottom);
            canvas.drawRect(numRect, mLinePaint);

        }
        canvas.drawRect(mScanRect, mLinePaint);
        canvas.drawPath(mGridPath, mScanPaintGrid);
        canvas.drawRect(mScanRect, mScanPaintRadar);
    }

    private void drawCorner(Canvas canvas) {
//        Log.e(TAG, "drawCorner");
        int left = mScanRect.left;
        int top = mScanRect.top;
        int right = mScanRect.right;
        int bottom = mScanRect.bottom;
        canvas.drawLine(left, top, left + mCornerWidth, top, mCornerPaint);
        canvas.drawLine(left, top, left, top + mCornerWidth, mCornerPaint);
        canvas.drawLine(left, bottom, left + mCornerWidth, bottom, mCornerPaint);
        canvas.drawLine(left, bottom, left, bottom - mCornerWidth, mCornerPaint);
        canvas.drawLine(right, top, right - mCornerWidth, top, mCornerPaint);
        canvas.drawLine(right, top, right, top + mCornerWidth, mCornerPaint);
        canvas.drawLine(right, bottom, right - mCornerWidth, bottom, mCornerPaint);
        canvas.drawLine(right, bottom, right, bottom - mCornerWidth, mCornerPaint);
    }

    private void drawHintText(Canvas canvas) {
//        Log.e(TAG, "drawHintText");
        Rect bounds = new Rect();
        mTextPaint.getTextBounds(mString, 0, mString.length(), bounds);
        Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
        int baseline = (fontMetrics.bottom - fontMetrics.top) / 2;
        if (mType == TYPE_VEHICLE) {
//            canvas.drawText(mString, mWidth / 2, mScanRect.bottom + 120 + baseline, mTextPaint);
            canvas.translate( mWidth / 2,mScanRect.bottom + 120);
            StaticLayout staticLayout = new StaticLayout(mString, mTextPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            staticLayout.draw(canvas);
            yLocation = mScanRect.top + 30;
        } else if (mType == TYPE_IDCARD) {
            canvas.rotate(90);
            canvas.drawText(mString, (mScanRect.bottom + mScanRect.top) / 2, -(mScanRect.left - 50 - baseline), mTextPaint);
            xLocation = mScanRect.left + 30;
        }
        canvas.save();
    }

    public int getxLocation() {
        return xLocation;
    }

    public int getyLocation() {
        return yLocation;
    }

    private void init() {
//        Log.e(TAG, "init");
        mCornerLineWith = DisplayUtil.dip2px(getContext(), 4);
        mCornerWidth = DisplayUtil.dip2px(getContext(), 23);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mScanColor = getContext().getResources().getColor(R.color.ga_scan_mask_rect_color);
        mLinePaint.setColor(mScanColor);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mRectLineWidth);
        mCornerPaint = new Paint();
        mCornerPaint.setAntiAlias(true);
        mCornerPaint.setColor(getContext().getResources().getColor(R.color.ga_scan_mask_corner_color));
        mCornerPaint.setStrokeWidth(mCornerLineWith);
        mCornerPaint.setStrokeCap(Paint.Cap.SQUARE);
        mTextPaint = new TextPaint();
        mTextPaint.setColor(getContext().getResources().getColor(R.color.ga_scan_hint_white));
        mTextPaint.setTextSize(DisplayUtil.sp2px(getContext(), 16));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mScanMatrix = new Matrix();
        mScanMatrix.setTranslate(0, 30);

        mScanPaintGrid = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScanPaintGrid.setStyle(Paint.Style.STROKE);
        mScanPaintGrid.setStrokeWidth(mGridLineWidth);

        mScanPaintRadar = new Paint(Paint.ANTI_ALIAS_FLAG);//默认的Paint模式为Fill
        mScanPaintRadar.setStyle(Paint.Style.FILL);
    }

    public synchronized void setScanType(int type) {
//        Log.e(TAG, "setScanType");
        mType = type;
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }
        changeRect();
        initScan();
        postInvalidate();

    }

    public Rect getMaskRect() {
        return mScanRect;
    }

    public void setHintText(String string) {
        mString = string;
        postInvalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mValueAnimator != null) {
            mValueAnimator.cancel();
        }
    }
}
