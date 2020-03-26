package cn.com.cg.ocr.ocrbyphoto.customview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
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
    private RectF nameRect = new RectF();
    private RectF nationRect = new RectF();

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

                //获取人脸图像中心点位置
                float centerX = (mRect.right + mRect.left) / 2;
                float centerY = (mRect.bottom + mRect.top) / 2;

                //获取人脸Rect宽高
                double faceRectW = mRect.right - mRect.left;

                //调整偏移系数，纠正位置选取不正确问题
                double pid = 1;
                if (faceRectW < 79) {
                    pid = 1.2;
                }else if (79 < faceRectW && faceRectW < 99){
                    pid = 1.1;
                }else if (99 < faceRectW && faceRectW < 119){
                    pid = 1;
                }else if(119 < faceRectW && faceRectW < 139){
                    pid = 0.9;
                }else{
                    pid = 0.8;
                }

                faceRectW = faceRectW * 1;

//                Log.e("cg","faceW = " + faceRectW);

                idRect.left = (int) (centerX - 2.8 * faceRectW);
                idRect.right = (int) (centerX + 0.8 * faceRectW);
                idRect.top = (int) (centerY + 1 * faceRectW);
                idRect.bottom = (int) (centerY + 1.5 * faceRectW);


                nameRect.left = (int) (centerX - 3.8 * faceRectW);
                nameRect.right = (int) (centerX - 0.6 * faceRectW);
                nameRect.top = (int) (centerY -  1.5 * faceRectW);
                nameRect.bottom = (int) (centerY + 0.7 * faceRectW);


                nationRect.left = (int) (centerX - 2.2 * faceRectW);
                nationRect.right = (int) (centerX - 1 * faceRectW);
                nationRect.top = (int) (centerY -  0.7 * faceRectW);
                nationRect.bottom = (int) (centerY - 0.3 * faceRectW);

                canvas.drawRect(mRect,myPaint);
//                canvas.drawRect(idRect,myPaint);
//                canvas.drawRect(nameRect,myPaint);
//                canvas.drawRect(nationRect,myPaint);
            }
        }
    }


    private RectF getIDCardRect(){
        return idRect;
    }
    private RectF getNameRect(){
        return nameRect;
    }
    private RectF getNationRect(){return nationRect;}

    public RectF[] getAllCardRects() {
        return new RectF[]{getNameRect(),getIDCardRect(),getNationRect()};
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

