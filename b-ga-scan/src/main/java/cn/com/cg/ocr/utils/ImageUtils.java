package cn.com.cg.ocr.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.cv4j.core.binary.Threshold;
import com.cv4j.core.datamodel.ByteProcessor;
import com.cv4j.core.datamodel.CV4JImage;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/18 10:20
 */
public class ImageUtils {
    private static final String TAG = ImageUtils.class.getName();


    /**
     * 采用CV4J二值化图片
     * @param imgUri
     * @return
     */
    public static Bitmap binaryImg(Uri imgUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath(), options);

            CV4JImage cv4JImage = new CV4JImage(bitmap);
            Threshold threshold = new Threshold();
            threshold.adaptiveThresh((ByteProcessor)(cv4JImage.convert2Gray().getProcessor()), Threshold.ADAPTIVE_C_MEANS_THRESH, 12, 30, Threshold.METHOD_THRESH_BINARY);
            Bitmap newBitmap = cv4JImage.getProcessor().getImage().toBitmap(Bitmap.Config.ARGB_8888);
            return newBitmap;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }


    /**
     * 采用CV4J二值化图片
     * @param bitmap
     * @return
     */
    public static Bitmap binaryImg(Bitmap bitmap) {
        try {
            CV4JImage cv4JImage = new CV4JImage(bitmap);
            Threshold threshold = new Threshold();
            threshold.adaptiveThresh((ByteProcessor)(cv4JImage.convert2Gray().getProcessor()), Threshold.ADAPTIVE_C_MEANS_THRESH, 12, 30, Threshold.METHOD_THRESH_BINARY);
            Bitmap newBitmap = cv4JImage.getProcessor().getImage().toBitmap(Bitmap.Config.ARGB_8888);
            return newBitmap;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }

}
