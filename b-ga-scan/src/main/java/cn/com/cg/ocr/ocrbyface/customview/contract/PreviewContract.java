package cn.com.cg.ocr.ocrbyface.customview.contract;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.hardware.Camera;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/19 11:26
 */
public interface PreviewContract {
    
    interface IView{

        void onOCRSuccess(String id, String tempFilePath);

        Context getContext();
    }
    
    interface IPresenter{

        void analysisIDCard(byte[] data, Camera camera, int width, int height, RectF borderRect, RectF idCardRect);

        void ondestroy();
    }
    
    interface IModel{

        String saveToSDCard(Bitmap clipedBitmap);

        Bitmap clipIDCardNumberBitmap(byte[] data, Camera.Size previewSize, RectF idCardRect, int svWidth, int svHeight);
    }
}
