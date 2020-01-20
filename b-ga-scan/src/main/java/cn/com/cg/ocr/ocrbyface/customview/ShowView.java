package cn.com.cg.ocr.ocrbyface.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Discription  { 人脸及待识别区域框 }
 * author  chenguo7
 * Date  2020/1/19 9:18
 */
public class ShowView extends View {
    private Matrix mMatrix = new Matrix();
    private RectF mRect = new RectF();
    private Camera.Face[] mFaces;
    private int cameraPosition = 1;//默认后置摄像头

    private RectF idRect = new RectF();

    public ShowView(Context context) {
        super(context);
    }

    public ShowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public void setFaces(Camera.Face[] faces){
        mFaces = faces;
        invalidate();
    }

    public void updateCameraPosition(int cameraPosition){
        this.cameraPosition = cameraPosition;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mFaces!=null){
            prepareMatrix(mMatrix, cameraPosition == 0, 90, getWidth(), getHeight());
            Paint myPaint = new Paint();
            myPaint.setColor(Color.GREEN);
            myPaint.setStyle(Paint.Style.STROKE);
            myPaint.setStrokeWidth(3);          //设置位图上paint操作的参数
            for (Camera.Face face:mFaces){
                mRect.set(face.rect);
                mMatrix.mapRect(mRect);

                idRect.left = (int) (mRect.left - 2 * (mRect.right - mRect.left));
                idRect.right = (int) (mRect.right + (mRect.right - mRect.left) / 4);
                idRect.top = (int) (mRect.bottom + 2 * (mRect.bottom - mRect.top) / 5);
                idRect.bottom = (int) (mRect.bottom + 4 * (mRect.bottom - mRect.top) / 5);

                canvas.drawRect(mRect,myPaint);
                canvas.drawRect(idRect,myPaint);
            }
        }
    }


    public RectF getIDCardRect(){
        return idRect;
    }


    /**
     * 准备用于旋转的矩阵工具
     * @param matrix
     * @param mirror
     * @param displayOrientation
     * @param viewWidth
     * @param viewHeight
     */
    public void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation,
                              int viewWidth, int viewHeight) {
        // Need mirror for front camera.
        matrix.setScale(mirror ? -1 : 1, 1);
        // This is the value for android.hardware.Camera.setDisplayOrientation.
        matrix.postRotate(displayOrientation);
        // Camera driver coordinates range from (-1000, -1000) to (1000, 1000).
        // UI coordinates range from (0, 0) to (width, height).
        matrix.postScale(viewWidth / 2000f, viewHeight / 2000f);
        matrix.postTranslate(viewWidth / 2f, viewHeight / 2f);
    }
}

