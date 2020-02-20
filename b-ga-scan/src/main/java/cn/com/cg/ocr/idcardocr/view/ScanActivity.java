package cn.com.cg.ocr.idcardocr.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.com.cg.ocr.R;
import cn.com.cg.ocr.common.bean.ScanResult;
import cn.com.cg.ocr.idcardocr.contract.ScanContract;
import cn.com.cg.ocr.idcardocr.presenter.ScanPresenter;
import cn.com.cg.ocr.idcardocr.utils.DisplayUtil;
import cn.com.cg.ocr.idcardocr.widget.ScanMaskView;
import cn.com.cg.ocr.idcardocr.widget.ScanSurfaceView;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static cn.com.cg.ocr.idcardocr.view.ScanActivity.FrameHandler.WHAT_HANDLEFRAME;
import static cn.com.cg.ocr.idcardocr.view.ScanActivity.FrameHandler.WHAT_INITCAMERA;
import static cn.com.cg.ocr.idcardocr.view.ScanActivity.FrameHandler.WHAT_ONESHOT;


/**
 * Discription  { 证件号码扫描 }
 * author  chenguo7
 * Date  2020/1/16 14:58
 */
public class ScanActivity extends AppCompatActivity implements ScanContract.View, SurfaceHolder.Callback, Camera.PreviewCallback {

    private static final int PERMISSIONS_REQUEST_CAMERA = 100;
    private ScanContract.Presenter mScanPresenter;


    private ScanSurfaceView svPreview;
    private TextView toEdit;
    private TextView toLigth;
    private LinearLayout lyBottomBar;
    private Disposable oneShotDisposable;
    private HandlerThread mFrameHandlerThread;
    private FrameHandler mHandler;

    private boolean isPreview = false;
    private Camera mCamera;
    private Camera.Size mSize;
    private Disposable focusDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ga_scan_activity_scan);
        setupViews();
        setUpData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_CAMERA);
        } else {
            setupCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CAMERA) {
            if (grantResults.length > 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                setupCamera();
                setUpData();
            } else {
                Toast.makeText(getApplicationContext(),getString(R.string.ga_scan_pls_allow_camera_permission),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupCamera() {
        SurfaceHolder surfaceHolder = svPreview.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void setUpData() {
        mScanPresenter = new ScanPresenter(this, this);
    }

    private void setupViews() {

        svPreview = findViewById(R.id.svPreview);
        lyBottomBar = findViewById(R.id.ly_bottom_bar);
        toEdit = findViewById(R.id.to_edit);
        toLigth = findViewById(R.id.to_light);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) lyBottomBar.getLayoutParams();
        layoutParams.setMargins(0, 0, 0, DisplayUtil.dip2px(this, 20));
        lyBottomBar.setLayoutParams(layoutParams);

        svPreview.setScanType(ScanMaskView.TYPE_IDCARD);
        svPreview.setHintText(getString(R.string.ga_scan_idcard_hint));
        toEdit.setRotation(90);
        toLigth.setRotation(90);
    }

    private void initCamera(SurfaceHolder holder) {
        Message message = new Message();
        message.what = WHAT_INITCAMERA;
        message.obj = holder;
        mHandler.handleMessage(message);
    }

    @Override
    public void onOCRSuccess( ScanResult bean) {
        Log.e("CG", "onOCRSuccess id = " + bean.id);
        Toast.makeText(this,"id = "+bean.id + " 性别 = " + bean.sex + " 出生年月 = " + bean.birthday,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mFrameHandlerThread = new HandlerThread("FrameHandlerThread");
        mFrameHandlerThread.start();
        mHandler = new FrameHandler(mFrameHandlerThread.getLooper());
        initCamera(holder);

        autoFocus();
    }

    private void autoFocus() {
        if (focusDisposable == null) {
            focusDisposable = Observable.interval(500,3000,TimeUnit.MILLISECONDS)
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                                @Override
                                public void onAutoFocus(boolean success, Camera camera) {

                                }
                            });
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


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //每隔500ms识别一次
        if (oneShotDisposable == null) {
            oneShotDisposable = Observable.interval(500, 500, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            if (null != mCamera && isPreview) {
                                if (mFrameHandlerThread != null && mHandler != null && mFrameHandlerThread.isAlive()) {
                                    Message message = new Message();
                                    message.what = WHAT_ONESHOT;
                                    mHandler.sendMessage(message);
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

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Message message = new Message();
        message.what = WHAT_HANDLEFRAME;
        message.obj = data;
        mHandler.sendMessage(message);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (focusDisposable != null) {
            focusDisposable.dispose();
            focusDisposable = null;
        }

        if (oneShotDisposable != null) {
            oneShotDisposable.dispose();
            oneShotDisposable = null;
        }
        if (mFrameHandlerThread != null) {
            mFrameHandlerThread.quit();
            mFrameHandlerThread = null;
        }
        if (null != mCamera && isPreview) {
            holder.removeCallback(this);
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreview = false;
            mCamera.release();
            mCamera = null;
        }
    }

    class FrameHandler extends Handler {

        static final int WHAT_INITCAMERA = 0x1;
        static final int WHAT_HANDLEFRAME = 0x2;
        static final int WHAT_ONESHOT = 0x3;


        FrameHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == WHAT_INITCAMERA) {
                try {
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    if (null != mCamera) {
                        Camera.Parameters parameters = mCamera.getParameters();
                        //设置预览尺寸
                        mSize = getBestSupportedSize(parameters.getSupportedPreviewSizes(), svPreview.getWidth(), svPreview.getHeight());
                        Log.d("ScanActivity", "width = " + mSize.width + " height = " + mSize.height + " svPreview.getWidth() = " + svPreview.getWidth() + " svPreview.getHeight() = " + svPreview.getHeight());
                        parameters.setPreviewSize(mSize.width, mSize.height);
                        if (Configuration.ORIENTATION_LANDSCAPE == getResources()
                                .getConfiguration().orientation) {
                            parameters.set("orientation", "landscape");
                            mCamera.setDisplayOrientation(0);
                        } else {
                            parameters.set("orientation", "portrait");
                            parameters.set("rotation", 90);
                            mCamera.setDisplayOrientation(90);
                        }
                        mCamera.setParameters(parameters);
                        mCamera.setPreviewDisplay((SurfaceHolder) msg.obj);
                        mCamera.setOneShotPreviewCallback(ScanActivity.this);
                        mCamera.startPreview();
                        Log.d("lh", "after start preview");
                        isPreview = true;
                        svPreview.refreshScanRect(mSize);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (msg.what == WHAT_HANDLEFRAME) {
                if (mCamera != null) {
                    mScanPresenter.analysisIDCard((byte[]) msg.obj, mCamera, svPreview.getScanRect(), svPreview.getWidth(), svPreview.getHeight());
//                    catchPreviewLight((byte[]) msg.obj, mCamera);
//                    if (isScanningIdCard(rgBottomBar.getCheckedRadioButtonId())) {
//
//                    } else if (isScanningVehiclePlate(rgBottomBar.getCheckedRadioButtonId())) {
//                        mScanPresenter.analysisVehiclePlate((byte[]) msg.obj, mCamera, svPreview.getScanRect(), svPreview.getScanRectOnScreen(), svPreview.getWidth(), svPreview.getHeight());
//                    }
                }
            } else if (msg.what == WHAT_ONESHOT) {
                try {
                    if (mCamera != null) {
                        mCamera.setOneShotPreviewCallback(ScanActivity.this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
        Camera.Size retSize = sizes.get(0);
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
}
