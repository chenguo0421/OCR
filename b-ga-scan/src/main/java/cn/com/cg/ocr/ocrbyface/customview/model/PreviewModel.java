package cn.com.cg.ocr.ocrbyface.customview.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cn.com.cg.ocr.common.utils.FileUtils;
import cn.com.cg.ocr.ocrbyface.customview.contract.PreviewContract;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/19 11:25
 */
public class PreviewModel implements PreviewContract.IModel {


    /**
     * 保存bitmap到SDCARD
     * @param bitmap
     * @return
     */
    @Override
    public String saveToSDCard(Bitmap bitmap) {
        return FileUtils.saveToSDCard(bitmap);
    }


    /**
     * 裁剪出Rect区域
     * @param data
     * @param previewSize
     * @param idCardRect
     * @param svWidth
     * @param svHeight
     * @return
     */
    @Override
    public Bitmap[] clipIDCardNumberBitmap(byte[] data, Camera.Size previewSize, RectF idCardRect, int svWidth, int svHeight) {
        ByteArrayOutputStream baos;
        byte[] rawImage;
        Bitmap bitmap;
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        YuvImage yuvimage = new YuvImage(
                data,
                ImageFormat.NV21,
                previewSize.width,
                previewSize.height,
                null);
        baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);// 80--JPG图片的质量[0-100],100最高
        rawImage = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            e.printStackTrace();
        }
        //将rawImage转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmap = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length, options);
        if (bitmap == null) {
            Log.e("CG", "bitmap is nlll");
            return null;
        } else {
            Bitmap[] bitmaps = new Bitmap[3];
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            float heightRatio = svWidth * 1.0f / height;
            float withRatio = svHeight * 1.0f / width;
            int left = (int) (idCardRect.top / withRatio);
            int top = (int) ((svWidth - idCardRect.right) / heightRatio);
            int right = (int) (idCardRect.bottom / withRatio);
            int bottom = (int) ((svWidth - idCardRect.left) / heightRatio);

            int offset = (int) ((idCardRect.bottom - idCardRect.top) / 4);
            if (bitmap.getWidth() > bitmap.getHeight()){
                Bitmap bitmap0 = Bitmap.createBitmap(bitmap, left - offset, top, right - left, bottom - top);
                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
                Bitmap bitmap2 = Bitmap.createBitmap(bitmap, left + offset, top, right - left, bottom - top);
                bitmaps[0] = roteBitmap(bitmap0);
                bitmaps[1] = roteBitmap(bitmap1);
                bitmaps[2] = roteBitmap(bitmap2);
            }else {
                Bitmap bitmap0 = Bitmap.createBitmap(bitmap, left, top - offset, right - left, bottom - top);
                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
                Bitmap bitmap2 = Bitmap.createBitmap(bitmap, left, top + offset, right - left, bottom - top);
                bitmaps[0] = roteBitmap(bitmap0);
                bitmaps[1] = roteBitmap(bitmap1);
                bitmaps[2] = roteBitmap(bitmap2);
            }





            bitmap.recycle();

            return bitmaps;
        }
    }

    private Bitmap roteBitmap(Bitmap bitmap) {
        /**
         * 若宽>高,需要旋转图片
         */
        if (bitmap.getWidth() < bitmap.getHeight()) {
            Bitmap bitmap2 = rotateClippedBitmap(90, bitmap);
            bitmap.recycle();
            return bitmap2;
        }
        return bitmap;
    }


    /**
     * 旋转bitmap
     * @param degree
     * @param bitmap
     * @return
     */
    public Bitmap rotateClippedBitmap(int degree, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        // 缩放原图
        matrix.postScale(1f, 1f);
        // 参数为负向左旋转，参数为正则向右旋转
        matrix.postRotate(degree);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return dstbmp;
    }
}
