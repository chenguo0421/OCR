package cn.com.cg.ocr.ocrbyface.customview;

import android.content.Context;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.com.cg.ocr.common.bean.ScanResult;
import cn.com.cg.ocr.common.intf.OnScanSuccessListener;
import cn.com.cg.ocr.common.utils.LightUtil;
import cn.com.cg.ocr.ocrbyface.customview.contract.PreviewContract;
import cn.com.cg.ocr.ocrbyface.customview.presenter.PreviewPresenter;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Discription  { 封装相机相关操作 }
 * author  chenguo7
 * Date  2020/1/19 9:21
 */
public class CameraPreviewViewWithRect extends FrameLayout implements Camera.PreviewCallback, PreviewContract.IView {
    private static final String TAG = CameraPreviewViewWithRect.class.getSimpleName() + " ";

    private Camera mCamera;
    private int cameraPosition = 1;//默认前摄
    private SurfaceView mSurfaceView;
    private ShowView mShowView;
    private Disposable focusDisposable;
    private PreviewBorderView mBoderView;
    private boolean isPreview = false;
    private Disposable oneShotDisposable;
    private PreviewContract.IPresenter mPresenter;



    private OnScanSuccessListener listener;

    public CameraPreviewViewWithRect(Context context) {
        super(context);
        init();
    }

    public CameraPreviewViewWithRect(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraPreviewViewWithRect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化预览层和方形显示层,添加至该组件
     */
    private void init() {
        mPresenter = new PreviewPresenter(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        initSurfaceView();
        addView(mSurfaceView, params);
        mShowView = new ShowView(getContext());
        mShowView.updateCameraPosition(cameraPosition);

        mBoderView = new PreviewBorderView(getContext());

        FrameLayout.LayoutParams btnParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final Button button = new Button(getContext());
        button.setText("手电筒-开");
        button.setTag(false);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button.getTag() instanceof Boolean){
                    if ((boolean)button.getTag()){
                        button.setTag(false);
                        button.setText("手电筒-开");
                        LightUtil.closeFlashLight(mCamera);
                    }else {
                        button.setTag(true);
                        button.setText("手电筒-关");
                        LightUtil.openFlashLight(mCamera);
                    }
                }
            }
        });

        addView(mBoderView,params);
        addView(mShowView, params);
        addView(button,btnParams);
    }

    /**
     * 设置回调监听
     * @param listener
     */
    public void setListener(OnScanSuccessListener listener) {
        this.listener = listener;
    }

    /**
     * 初始化SurfaceView
     */
    private void initSurfaceView() {
        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.getHolder().setKeepScreenOn(true);
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(TAG, "main sufraceCreate");
                initCamera(mSurfaceView.getHolder());
                autoFocus();
                if (mBoderView != null) {
                    mBoderView.initSurfaceViewWH(getWidth(), getHeight());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG, "main sufraceChanged width = " + width + " height = " + height);
                //每隔500ms识别一次
                oneShotFrame();

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG, "main surfaceDestroyed");
                freeCameraResource();
            }
        });
    }


    /**
     * 间隔200毫秒捕捉一帧数据
     */
    private void oneShotFrame() {
        if (oneShotDisposable == null) {
            oneShotDisposable = Observable.interval(500, 500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (null != mCamera && isPreview) {
                                try {
                                    if (mCamera != null) {
                                        mCamera.setOneShotPreviewCallback(CameraPreviewViewWithRect.this);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (null != mCamera) {
                                mCamera.release();
                                mCamera = null;
                            }
                        }
                    });
        }
    }

    /**
     * 切换前后摄像头
     */
    public void switchCamera() {
        cameraPosition = cameraPosition == 1 ? 0 : 1;
        initCamera(mSurfaceView.getHolder());
    }

    /**
     * 激活相机预览
     */
    public void refCamera() {
        initCamera(mSurfaceView.getHolder());
    }

    /**
     * 初始化相机
     * @param holder
     */
    private void initCamera(SurfaceHolder holder) {
        Log.d(TAG, "initCamera ");
        //未避免二次init,调用此方法是试着释放相机资源
        if (mCamera != null) {
            freeCameraResource();
        }
        try {
            mCamera = getCamera();
            if (mCamera == null)
                return;

            if (mCamera != null) {
                Camera.Parameters params = mCamera.getParameters();
                params.set("orientation", "portrait");
                Camera.Size size = getBestSupportedSize(params.getSupportedPreviewSizes(),getWidth(), getHeight());
                params.setPreviewSize(size.width, size.height);
                mCamera.setParameters(params);
            }


            //调正相机预览
            mCamera.setDisplayOrientation(90);
            //让相机预览显示在第二层SurfaceView上
            mCamera.setPreviewDisplay(holder);
            //设置人脸检测监听
            mCamera.setFaceDetectionListener(new Camera.FaceDetectionListener() {
                @Override
                public void onFaceDetection(Camera.Face[] faces, Camera camera) {
                    Log.d(TAG, " onFaceDetection faces = " + faces.length);
                    //传给showView进行Rect绘制
                    mShowView.setFaces(faces);
                }
            });
            mCamera.startPreview();
            //只需要调用start,系统会自动调用stop
            mCamera.startFaceDetection();
            isPreview = true;
        } catch (Exception e) {
            e.printStackTrace();
            //init过程中出错释放资源,避免因程序错误导致系统无法调用相机
            freeCameraResource();
        }
    }


    /**
     * 间隔三秒，连续自动对焦
     */
    private void autoFocus() {
        if (focusDisposable == null) {
            focusDisposable = Observable.interval(500,3000, TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (mCamera != null) {
                                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success, Camera camera) {

                                    }
                                });
                            }
                        }
                    },new Consumer<Throwable>(){
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            if (null != mCamera) {
                                mCamera.release();
                                mCamera = null;
                            }
                        }
                    });
        }
    }

    /**
     * 释放照相机及相关资源
     */
    private void freeCameraResource() {

        isPreview = false;

        if (oneShotDisposable!=null && !oneShotDisposable.isDisposed()){
            oneShotDisposable.dispose();
            oneShotDisposable = null;
        }

        if (focusDisposable != null && !focusDisposable.isDisposed()) {
            focusDisposable.dispose();
            focusDisposable = null;
        }

        if (mPresenter != null) {
            mPresenter.ondestroy();
        }

        if (mCamera != null) {
            LightUtil.closeFlashLight(mCamera);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }


    /**
     * 通过对比得到与宽高比最接近的尺寸（如果有相同尺寸，优先选择）
     *
     * @param sizes  需要被进行对比的原宽
     * @param width 需要被进行对比的原高
     * @param height   需要对比的预览尺寸列表
     * @return 得到与原宽高比例最接近的尺寸
     */
    private Camera.Size getBestSupportedSize(List<Camera.Size> sizes, int width, int height) {

        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : sizes) {
            if ((size.width == height) && (size.height == width)) {
                return size;
            }
        }
        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) height) / width;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : sizes) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin && size.height >= width) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }

    /**
     * open 前/后摄像头，默认开启后置摄像头
     *
     * @return
     */
    private Camera getCamera() {
        Log.d(TAG,"camera getCamera");
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraPosition == 1) {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    return Camera.open(i);
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    return Camera.open(i);
                }
            }
        }
        return null;
    }

    /**
     * 处理每一帧数据
     * @param data
     * @param camera
     */
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mPresenter != null) {
            mPresenter.analysisIDCard(data,camera,getWidth(),getHeight(),getBorderRect(),getIdCardRect());
        }
    }

    /**
     * 获取证件号码框Rect
     * @return
     */
    private RectF getIdCardRect(){
        if (mShowView != null) {
            return mShowView.getIDCardRect();
        }
        return null;
    }


    /**
     * 获取边框Rect
     * @return
     */
    private RectF getBorderRect(){
        if (mBoderView != null) {
           return mBoderView.getBorderRect();
        }
        return null;
    }

    /**
     * 得到正确证件号码，回调
     * @param bean
     */
    @Override
    public void onOCRSuccess(ScanResult bean) {
        if (listener != null) {
            listener.onOCRSuccess(bean);
        }
    }

}
