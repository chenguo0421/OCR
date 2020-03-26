package cn.com.cg.ocr.ocrbyphoto.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import cn.com.cg.ocr.common.utils.DeviceUtils;

/**
 * Discription  { 图片采集框 }
 * author  chenguo7
 * Date  2020/1/19 10:07
 */
public class PreviewBorderView extends View {

    private int mScreenW;
    private int mScreenH;
    private Paint mPaint;
    private Paint mPaintLine;
    private Paint mTextPaint;
    private float tipTextSize = 30;
    private static final String DEFAULT_TIPS_TEXT = "请将身份证照片置于框内,并尽量对齐边框";
    private String tipText = DEFAULT_TIPS_TEXT;
    private RectF borderRect;
    private int screenHight;
    private int screenWidth;

    public PreviewBorderView(Context context) {
        super(context);
        init();
    }

    public PreviewBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PreviewBorderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void initSurfaceViewWH(int width,int height){
        this.mScreenW = width;
        this.mScreenH = height;
        invalidate();
    }
  


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mScreenW <= 0 || mScreenH <= 0) {
            return;
        }
        drawBorder(canvas);
    }


    /**
     * 初始化绘图变量
     */
    private void init() {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setColor(Color.WHITE);
        this.mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        this.mPaintLine = new Paint();
        this.mPaintLine.setColor(Color.BLUE);
        this.mPaintLine.setStrokeWidth(5.0F);

        this.mTextPaint = new Paint();
        this.mTextPaint.setColor(Color.WHITE);
        this.mTextPaint.setStrokeWidth(3.0F);

        screenWidth = DeviceUtils.getScreenWidth(getContext());
        screenHight = DeviceUtils.getScreenHeight(getContext());

        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(screenWidth,screenHight);
    }

    public RectF getBorderRect(){
        return borderRect;
    }

    /**
     * 绘制取景框
     */
    private void drawBorder(Canvas canvas) {
        try {
            canvas.drawARGB(100, 0, 0, 0);
            // 1.6:1 -> width:height
            //            this.mScreenW = (this.mScreenH * 4 / 3);
            float left;
            float top;
            float right;
            float bottom;
            float reactHeight;
            float reactWidth;
            float rate = 2 / 3f;
            if (this.mScreenW > this.mScreenH) {
                // 横屏
                reactHeight = this.mScreenH * rate;
                reactWidth = (float) (reactHeight * 1.6);
                left = this.mScreenW / 2 - reactWidth / 2;
                top = this.mScreenH / 5;
                right = left + reactWidth;
                bottom = top + reactHeight;
            } else {
                reactWidth = this.mScreenW * rate;
                reactHeight = (float) (reactWidth / 1.6);
                left = this.mScreenW / 2 - reactWidth / 2;
                top = this.mScreenH / 2 - reactHeight / 2;
                right = left + reactWidth;
                bottom = top + reactHeight;
            }

            borderRect = new RectF(left, top, right, bottom);
            canvas.drawRect(borderRect, this
                    .mPaint);

            // 画边框
            float lineLength = reactHeight / 15;
            canvas.drawLine(left, top, left + lineLength, top, mPaintLine);
            canvas.drawLine(left, top, left, top + lineLength, mPaintLine);
            canvas.drawLine(left, bottom, left + lineLength, bottom, mPaintLine);
            canvas.drawLine(left, bottom, left, bottom - lineLength, mPaintLine);
            canvas.drawLine(right, top, right, top + lineLength, mPaintLine);
            canvas.drawLine(right, top, right - lineLength, top, mPaintLine);
            canvas.drawLine(right, bottom, right - lineLength, bottom, mPaintLine);
            canvas.drawLine(right, bottom, right, bottom - lineLength, mPaintLine);

            mTextPaint.setTextSize(tipTextSize);
            mTextPaint.setAntiAlias(true);
            mTextPaint.setDither(true);
            float length = mTextPaint.measureText(tipText);
//            canvas.drawText(tipText, (left + reactWidth / 2) - length / 2, top + reactHeight / 2, mTextPaint);

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

}
