package cn.com.cg.ocr.idcardocr.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;

import java.util.ArrayList;

import cn.com.cg.ocr.idcardocr.bean.ScanResult;
import cn.com.cg.ocr.idcardocr.contract.ScanContract;
import cn.com.cg.ocr.common.helper.IDCardOCRHelper;
import cn.com.cg.ocr.idcardocr.model.PreviewDataModel;
import cn.com.cg.ocr.idcardocr.utils.IDCardRegxUtils;
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
 * Date  2020/1/16 15:06
 */
public class ScanPresenter implements ScanContract.Presenter {


    private final ScanContract.View mView;
    private final Context mContext;
    private final PreviewDataModel mPreViewDataModel;
    private IDCardOCRHelper idCardOCRHelper;
    private static final int maxActive = 2;
    private ArrayList<Disposable> disposables = new ArrayList<>();
    private boolean isClip = false;

    public ScanPresenter(final Context context, ScanContract.View view) {
        mView = view;
        mContext = context;

        createHelper(context);
        mPreViewDataModel = new PreviewDataModel();
    }

    private void createHelper(final Context context) {
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                createIDCardScanHelper(context);
                createVehiclePlateHelper(context);
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

    private void createVehiclePlateHelper(Context context) {

    }


    private void createIDCardScanHelper(Context context){
        if (idCardOCRHelper == null) {
            idCardOCRHelper = IDCardOCRHelper.getInstance(context);
            idCardOCRHelper.init();
        }

    }


    @Override
    public void analysisIDCard(final byte[] data,final Camera camera,final Rect scanRect,final int svWidth,final int svHeight) {
        if (isClip){
            return;
        }
        if (disposables!=null&&disposables.size()>maxActive){
            Disposable disposable = disposables.remove(0);
            if (!disposable.isDisposed()){
                disposable.dispose();
            }
        }
        Observable.create(new ObservableOnSubscribe<ScanResult>() {
            @Override
            public void subscribe(ObservableEmitter<ScanResult> emitter) throws Exception {
                Camera.Size previewSize = camera.getParameters().getPreviewSize();

                isClip = true;
                Bitmap clipedBitmap = mPreViewDataModel.clipScanRectBitmap(data, previewSize, scanRect, svWidth, svHeight);
                if (clipedBitmap == null) {
                    isClip = false;
                    return;
                }

                Bitmap idNumberBitmap = mPreViewDataModel.clipIDCardNumberBitmap1(clipedBitmap);
                isClip = false;
                String tempPath = mPreViewDataModel.saveToSDCard(idNumberBitmap);
                if (idCardOCRHelper == null || !idCardOCRHelper.hasInit()) {
                    return;
                }
                String id = idCardOCRHelper.doOCREngAnalysis(idNumberBitmap);
                Log.e("CG", "id = " + id);
//                String id = idCardOCRHelper.doOCREngAnalysis(idNumberBitmap);

                if (tempPath == null) {
                    return;
                }
                if (clipedBitmap != null) {
                    clipedBitmap.recycle();
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
