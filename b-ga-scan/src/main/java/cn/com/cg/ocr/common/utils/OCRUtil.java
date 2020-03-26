package cn.com.cg.ocr.common.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import cn.com.cg.ocr.idcardocr.view.ScanActivity;
import cn.com.cg.ocr.ocrbyface.view.FaceCaptureActivity;
import cn.com.cg.ocr.ocrbyphoto.view.ScanWithPhotoActivity;
import cn.com.cg.ocr.testocr.MainActivity;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/20 15:23
 */
public class OCRUtil {


    public static void fixedSlicer(final AppCompatActivity activity) {
        checkPermissionRequest(activity, new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean permisionOK) {
                if (permisionOK){
                    activity.startActivity(new Intent(activity, ScanActivity.class));
                }else {
                    onPermissionLost(activity);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

    }

    public static void multiZoneSlicer(final AppCompatActivity activity) {
        checkPermissionRequest(activity, new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean permisionOK) {
                if (permisionOK){
                    activity.startActivity(new Intent(activity, ScanWithPhotoActivity.class));
                }else {
                    onPermissionLost(activity);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    public static void dynamicalSlicer(final AppCompatActivity activity) {
        checkPermissionRequest(activity, new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Boolean permisionOK) {
                if (permisionOK){
                    activity.startActivity(new Intent(activity, FaceCaptureActivity.class));
                }else {
                    onPermissionLost(activity);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private static void onPermissionLost(Context context) {
        Toast.makeText(context,"请先开启相机和存储权限",Toast.LENGTH_LONG).show();
    }


    private static void checkPermissionRequest(AppCompatActivity activity, Observer observer) {
        RxPermissions permissions = new RxPermissions(activity);
        permissions.request(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .subscribe(observer);
    }
}
