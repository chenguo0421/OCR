package cn.com.cg.ocr.idcardocr.contract;

import android.graphics.Rect;
import android.hardware.Camera;

import cn.com.cg.ocr.common.bean.ScanResult;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 15:07
 */
public interface ScanContract {
    interface View {
        void onOCRSuccess( ScanResult scanResult);
    }

    interface Presenter {
        void analysisIDCard(byte[] data, Camera camera, Rect scanRect, int svWidth, int svHeight);
    }
}
