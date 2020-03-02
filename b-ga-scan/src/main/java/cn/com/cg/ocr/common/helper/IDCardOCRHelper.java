package cn.com.cg.ocr.common.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.com.cg.ocr.idcardocr.utils.LanguageType;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/16 15:12
 */
public class IDCardOCRHelper {
    private TessBaseAPI mEngApi;
    private TessBaseAPI mChiApi;
    private String mSdPath;
    private volatile boolean finishInit = false;
    private CountDownLatch closeLatch;
    private final static long TIMEOUT = 5;
    private volatile static IDCardOCRHelper idCardOCRHelper;

    private static String chi_smi = "chi_sim.traineddata";
    private static String eng = "eng.traineddata";


    private LanguageType languageType = LanguageType.ENG;


    public static IDCardOCRHelper getInstance(Context context) {
        if (idCardOCRHelper == null) {
            synchronized (IDCardOCRHelper.class) {
                if (idCardOCRHelper == null) {
                    idCardOCRHelper = new IDCardOCRHelper(context);
                }
            }
        }
        return idCardOCRHelper;
    }


    public IDCardOCRHelper(Context context) {
        mSdPath = Environment.getExternalStorageDirectory() + "/ocr";
        File file = new File(mSdPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        try {
            copyAssetFile(context,eng);
            copyAssetFile(context,chi_smi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean copyAssetFile(Context context,String lgType) throws Exception {
        String dir = mSdPath + "/tessdata";
        String filePath = mSdPath + "/tessdata/" + lgType;
        File f = new File(dir);
        if (f.exists()) {
        } else {
            f.mkdirs();
        }
        File dataFile = new File(filePath);
        if (dataFile.exists()) {
            return true;// 文件存在
        } else {
            InputStream in = context.getResources().getAssets().open(lgType);
            File outFile = new File(filePath);
            if (outFile.exists()) {
                outFile.delete();
            }
            OutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
        return true;
    }

    public synchronized void init() {
        try {
            if (closeLatch != null) {
                closeLatch.await(TIMEOUT, TimeUnit.SECONDS);
                closeLatch = null;
            }
            long time = System.currentTimeMillis();
            Log.e("ScanActivity", "init " + time);
            mEngApi = new TessBaseAPI();
            mEngApi.init(mSdPath, "eng");
//            mEngApi.setVariable("tessedit_char_whitelist", "0123456789Xx");
//            mEngApi.setVariable("tessedit_char_whitelist","陈果男汉湖北省阳新县木港镇坳头村伯清8号姓名性别民族出生年月日住址公民身份号码");
            mEngApi.setVariable("tessedit_char_whitelist","0123456789Xx");


            mChiApi = new TessBaseAPI();
            mChiApi.init(mSdPath, "chi_sim");
            mChiApi.setVariable("tessedit_char_whitelist","姓名性别民族出生年月日住址公民身份号码陈果吴丽敏男女汉湖北省阳新县木港镇坳头村伯清号1234567890");

            Log.e("ScanActivity", "init end " + (System.currentTimeMillis() - time));
            finishInit = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public synchronized String doOCREngAnalysis(Bitmap bitmap) {
        Bitmap bitmap8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mEngApi.setImage(bitmap8888);
        String text = mEngApi.getUTF8Text();
        mEngApi.clear();
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap8888.recycle();
        return text;
    }

    public synchronized String doOCRChiAnalysis(Bitmap bitmap) {
        Bitmap bitmap8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        mChiApi.setImage(bitmap8888);
        String text = mChiApi.getUTF8Text();
        mChiApi.clear();
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap8888.recycle();
        return text;
    }

    public boolean hasInit() {
        return finishInit;
    }

    public synchronized void close() {
        if (!finishInit) {
            closeLatch = new CountDownLatch(1);
            mEngApi.end();
            mChiApi.end();
            closeLatch.countDown();
        }
    }
}
