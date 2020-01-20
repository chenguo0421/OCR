package cn.com.cg.ocr.idcardocr.contract;

import android.graphics.Rect;
import android.hardware.Camera;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 15:07
 */
public interface ScanContract {
    interface View {

        void onOCRSuccess(String content, String tempFileName);
    }

    interface Presenter {
        void analysisIDCard(byte[] data, Camera camera, Rect scanRect, int svWidth, int svHeight);
    }
}
