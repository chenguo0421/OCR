package cn.com.cg.ocr.ocrbyphoto.customview.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import cn.com.cg.ocr.common.bean.ScanResult;
import cn.com.cg.ocr.common.helper.IDCardOCRHelper;
import cn.com.cg.ocr.common.utils.IDCardRegxUtils;
import cn.com.cg.ocr.ocrbyphoto.customview.contract.PreviewContract;
import cn.com.cg.ocr.ocrbyphoto.customview.model.PreviewModel;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/19 11:25
 */
public class PreviewPresenter implements PreviewContract.IPresenter {

    private PreviewContract.IView mView;
    private static final int maxActive = 4;//不建议太大，容易出
    private PreviewContract.IModel model;
    private IDCardOCRHelper idCardOCRHelper;
    private volatile String effectiveNation = null;
    private LinkedBlockingQueue<Bitmap[]> nationQueue = new LinkedBlockingQueue<>(maxActive * 2);
    private LinkedBlockingQueue<Bitmap[]> idCardQueue = new LinkedBlockingQueue<>(maxActive * 2);
    private ExecutorService offerPools = Executors.newFixedThreadPool(maxActive);
    private ExecutorService takeNationPools = Executors.newFixedThreadPool(maxActive * 2);
    private ExecutorService takeIdcardPools = Executors.newFixedThreadPool(maxActive);
    private volatile boolean isDestroy = false;

    public PreviewPresenter(PreviewContract.IView view) {
        this.mView = view;
        isDestroy = false;
        model = new PreviewModel();
        createHelper(mView.getContext());
    }

    private void createHelper(final Context context) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                createIDCardScanHelper(context);
                emitter.onNext(new Object());
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {
                        startScan();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void startScan() {
        while (true){
            final Bitmap[] idCardBitmaps = idCardQueue.poll();
            final Bitmap[] nationBitmaps = nationQueue.poll();
            if (idCardBitmaps != null && idCardBitmaps.length > 0) {
                takeIdcardPools.execute(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("CG", Thread.currentThread().getName() + "start scan idcard");
                        //识别证件号码区域
                        String[] paths = new String[idCardBitmaps.length];
                        for (int i = 0; i < idCardBitmaps.length; i++) {
                            paths[i] = model.saveToSDCard(idCardBitmaps[i]);
                        }

                        for (int i = 0; i < idCardBitmaps.length; i++) {
                            if (paths[i] == null) {
                                idCardBitmaps[i].recycle();
                                continue;
                            }
                            String id = idCardOCRHelper.doOCREngAnalysis(idCardBitmaps[i]);

                            if (id != null && (id.length() >= 18)) {
                                id = id.substring(id.length() - 18);
                                Log.e("CG", "sub id = " + id);
                            }

                            if (idCardBitmaps[i] != null) {
                                idCardBitmaps[i].recycle();
                            }
                            ScanResult scanResult = IDCardRegxUtils.checkIdCard(id);
                            if (scanResult != null) {
                                scanResult.idPath = paths[i];
                                scanResult.nation = effectiveNation;
                                checkIDOCRResult(scanResult);
                                break;
                            }
                        }
                        Log.e("CG", Thread.currentThread().getName() + "end scan");
                    }
                });
            }

            if (nationBitmaps != null && nationBitmaps.length > 0) {
                takeNationPools.execute(new Runnable() {
                    @Override
                    public void run() {
                            String[] nationPaths = null;
                            String nation = null;
                            if (nationBitmaps != null) {
                                nationPaths = new String[nationBitmaps.length];
                                for (int i = 0; i < nationBitmaps.length; i++) {
                                    nationPaths[i] = model.saveToSDCard(nationBitmaps[i]);
                                }
                                Log.e("CG", Thread.currentThread().getName() + "scan save nation bitmap to sdcard ok,start scan nation");
                                for (int i = 0; i < nationBitmaps.length; i++) {
                                    if (nationPaths[i] == null) {
                                        nationBitmaps[i].recycle();
                                        continue;
                                    }
                                    nation = idCardOCRHelper.doOCRChiAnalysis(nationBitmaps[i]);
                                    if (nationPaths[i] != null) {
                                        nationBitmaps[i].recycle();
                                    }
                                    nation = IDCardRegxUtils.checkNation(nation);
//                        Log.e("CG", "check nation = " + nation);
                                    if (nation != null) {
                                        effectiveNation = nation;
                                        break;
                                    }
                                }
                                Log.e("CG", Thread.currentThread().getName() + "scan nation bitmap ok nation = " + nation );
                            }
                    }
                });
            }
        }
    }


    private void createIDCardScanHelper(Context context) {
        if (idCardOCRHelper == null) {
            idCardOCRHelper = IDCardOCRHelper.getInstance(context);
            idCardOCRHelper.init();
        }
    }


    /**
     * 处理一帧数据
     * @param data
     * @param camera
     * @param svWidth
     * @param svHeight
     * @param idCardRects
     */
    @Override
    public void analysisIDCard(final byte[] data,final Camera camera,final int svWidth,final int svHeight,final RectF[] idCardRects) {
        if (isDestroy){
            return;
        }
        offerPools.execute(new Runnable() {
            @Override
            public void run() {
                clipeAreaBitmap(data,camera,svWidth,svHeight,idCardRects);
            }
        });

    }


    /**
     * 创建任务
     * @param data
     * @param camera
     * @param svWidth
     * @param svHeight
     * @param idCardRects
     * @return
     */
    private void clipeAreaBitmap(final byte[] data,final Camera camera,final int svWidth,final int svHeight,final RectF[] idCardRects) {

        Log.e("CG", Thread.currentThread().getName() + "start scan");
        Camera.Size previewSize = camera.getParameters().getPreviewSize();
        Bitmap bitmap = model.createYUVBitmap(data, previewSize);
        if (idCardOCRHelper == null || !idCardOCRHelper.hasInit()) {
            return;
        }
        //裁剪存储证件号码区域图片
        Bitmap[] idNumberBitmaps = model.clipBitmap(bitmap, idCardRects[1], svWidth, svHeight);
        if (idNumberBitmaps == null) {
            Log.e("CG", Thread.currentThread().getName() + "end scan with idNumberBitmaps is null");
            return;
        }

        //裁剪存储民族区域图片并识别，当有一次识别成功，则记录该识别结果为最终民族结果
        Bitmap[] nationBitmaps = model.clipBitmap(bitmap, idCardRects[2], svWidth, svHeight);
        Log.e("CG", Thread.currentThread().getName() + "scan bitmap clip ok");
        if (bitmap != null) {
            bitmap.recycle();
        }

        nationQueue.offer(nationBitmaps);
        idCardQueue.offer(idNumberBitmaps);

    }

    @Override
    public void ondestroy() {

        isDestroy = true;

        if (offerPools != null) {
            offerPools.shutdownNow();
        }
        if (takeNationPools != null) {
            takeNationPools.shutdownNow();
        }

        if (takeIdcardPools != null) {
            takeIdcardPools.shutdownNow();
        }
    }


    /**
     * 检验身份证ocr结果
     *
     * @param bean
     */
    private void checkIDOCRResult(ScanResult bean) {
        if (isDestroy){
            return;
        }
        ScanResult scanResult = IDCardRegxUtils.checkIdCard(bean.id);
        if (scanResult != null) {
            scanResult.idPath = bean.idPath;
            scanResult.name = bean.name;
            scanResult.nation = effectiveNation;
            mView.onOCRSuccess(scanResult);
        }
    }
}
