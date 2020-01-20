package cn.com.cg.ocr.idcardocr.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import cn.com.cg.ocr.testocr.utils.PictureHandler;
import cn.com.cg.ocr.idcardocr.utils.FileUtils;
import cn.com.cg.ocr.utils.ImageUtils;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 15:14
 */
public class PreviewDataModel {

    public Bitmap clipScanRectBitmap(byte[] data, Camera.Size previewSize, Rect scanRect, int svWidth, int svHeight) {
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
        Log.e("CG", "start BinaryImage");
//        bitmap = ImageUtils.binaryImg(bitmap);
        Log.e("CG", "finish BinaryImage");
        if (bitmap == null) {
            Log.d("zka", "bitmap is nlll");
            return null;
        } else {
            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            float heightRatio = svWidth * 1.0f / height;
            float withRatio = svHeight * 1.0f / width;
            int left = (int) (scanRect.top / withRatio);
            int top = (int) ((svWidth - scanRect.right) / heightRatio);
            int right = (int) (scanRect.bottom / withRatio);
            int bottom = (int) ((svWidth - scanRect.left) / heightRatio);

            final Bitmap bitmap1 = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top);
            bitmap.recycle();
            return bitmap1;

        }
    }

    /**
     * 之前的裁剪证件号码区域的方法，已废弃
     * @param bitmap
     * @return
     */
    public Bitmap clipIDCardNumberBitmap(@NonNull Bitmap bitmap) {
        int x, y, w, h;
        x = (int) (bitmap.getWidth() * 0.340);
        y = (int) (bitmap.getHeight() * 0.800);
        w = (int) (bitmap.getWidth() * 0.6 + 0.5f);
        h = (int) (bitmap.getHeight() * 0.12 + 0.5f);
        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, x, y, w, h);
        bitmap.recycle();
        return bitmap2;
    }

    /**
     * 裁剪取景框内证件号码区域
     * 备注：方法内数值设置均与取景框内数值有关联，不可随意更改，若需要更改，需要同时更改取景框内相关数值
     * @param bitmap
     *             int numLeft = (int) (mScanRect.left + 0.05 * mScanRect.width());
     *             int numTtop = (int) (mScanRect.bottom - 0.6 * mScanRect.height());
     *             int numRight = (int) (mScanRect.right - 0.83 * mScanRect.width());
     *             int numBottom = (int) (mScanRect.bottom - 0.06 * mScanRect.height());
     * @return
     */
    public Bitmap clipIDCardNumberBitmap1(@NonNull Bitmap bitmap) {
        Bitmap bitmap2 = null;
        Bitmap bitmap1 = null;
        try {
            int x, y, w, h;
//            x = (int) (bitmap.getWidth() * 0.4 + 0.5f);
//            y = (int) (bitmap.getHeight() * 0.8 + 0.5f);
//            w = (int) (bitmap.getWidth() * 0.54 + 0.5f);
//            h= (int) (bitmap.getHeight() * 0.15 + 0.5f);

            x = (int) (0);
            y = (int) (bitmap.getHeight() * 0.8 + 0.5f);
            w = (int) (bitmap.getWidth());
            h= (int) (bitmap.getHeight() * 0.15 + 0.5f);
            bitmap1 = Bitmap.createBitmap(bitmap, x, y, w, h);

            /*传入bitmap参数，返回bitmap。 在使用tessdata时，只能检测ARGB_8888格式的bitmap,所以才多这一步转换，这里暂时没有用到，可以先注释掉，直接返回RGB_565的bitmap*/
//            ByteArrayOutputStream dataByte = new ByteArrayOutputStream();
//            bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, dataByte);
//            BitmapFactory.Options opts = new BitmapFactory.Options();
//            opts.inSampleSize = 1;
//            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
//            bitmap2 = BitmapFactory.decodeByteArray(dataByte.toByteArray(), 0, dataByte.size(), opts);
//            bitmap1.recycle();
            if (bitmap != null){
                bitmap.recycle();
            }
        }catch (Exception e){
            bitmap2 = null;
            bitmap1 = null;
        }

        return bitmap1;
    }

    public Bitmap rotateClippedBitmap(int degree, Bitmap bitmap) {
        Matrix matrix = new Matrix();
        // 缩放原图
        matrix.postScale(1f, 1f);
        // 参数为负向左旋转，参数为正则向右旋转
        matrix.postRotate(degree);
        Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return dstbmp;
    }

    public String saveToSDCard(Bitmap idNumberBitmap) {
        return FileUtils.saveToSDCard(idNumberBitmap);
    }
}
