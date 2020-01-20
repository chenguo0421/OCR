package cn.com.cg.ocr.ocrbyface.customview.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;

import cn.com.cg.ocr.common.helper.IDCardOCRHelper;
import cn.com.cg.ocr.ocrbyface.customview.bean.ScanResult;
import cn.com.cg.ocr.ocrbyface.customview.contract.PreviewContract;
import cn.com.cg.ocr.ocrbyface.customview.model.PreviewModel;
import cn.com.cg.ocr.ocrbyface.utils.IDCardRegxUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/19 11:25
 */
public class PreviewPresenter implements PreviewContract.IPresenter {

    private PreviewContract.IView mView;
    private boolean isClip = false;
    private ArrayList<Disposable> disposables = new ArrayList<>();
    private static final int maxActive = 4;
    private PreviewContract.IModel model;
    private IDCardOCRHelper idCardOCRHelper;

    public PreviewPresenter(PreviewContract.IView view) {
        this.mView = view;
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
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
     * @param scanRect
     * @param idCardRect
     */
    @Override
    public void analysisIDCard(final byte[] data,final Camera camera,final int svWidth,final int svHeight,final RectF scanRect,final RectF idCardRect) {
//        if (isClip){
//            return;
//        }
        if (disposables == null) {
            disposables = new ArrayList<>();
        }
        if (disposables.size() > maxActive) {
            Disposable disposable = disposables.remove(0);
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        Observable.create(new ObservableOnSubscribe<ScanResult>() {
            @Override
            public void subscribe(ObservableEmitter<ScanResult> emitter) throws Exception {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();

                isClip = true;
                Bitmap idNumberBitmap = model.clipIDCardNumberBitmap(data, previewSize, idCardRect, svWidth, svHeight);

                String tempPath = model.saveToSDCard(idNumberBitmap);
                if (idCardOCRHelper == null || !idCardOCRHelper.hasInit()) {
                    isClip = false;
                    return;
                }
                String id = idCardOCRHelper.doOCREngAnalysis(idNumberBitmap);
                isClip = false;
                Log.e("CG", "id = " + id);

                if (id != null && (id.length() >= 18)) {
                    id = id.substring(id.length() - 18);
                    Log.e("CG", "sub id = " + id);
                }


                if (tempPath == null) {
                    isClip = false;
                    return;
                }


                if (idNumberBitmap != null) {
                    idNumberBitmap.recycle();
                }

                emitter.onNext(new ScanResult(id,tempPath));
                emitter.onComplete();

            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ScanResult>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(ScanResult bean) {
                        if (bean != null) {
                            Log.e("CG", "onNext id = " + bean.id);
                            checkIDOCRResult(bean.id, bean.path);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        isClip = false;
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }

    @Override
    public void ondestroy() {

    }


    /**
     * 检验身份证ocr结果
     *
     * @param id
     * @param tempFilePath
     */
    private void checkIDOCRResult(String id, String tempFilePath) {
        if (IDCardRegxUtils.is18ByteIdCardComplex(id)) {
            mView.onOCRSuccess(id, tempFilePath);
        }
    }
}
