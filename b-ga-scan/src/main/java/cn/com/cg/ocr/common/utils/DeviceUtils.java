package cn.com.cg.ocr.common.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceUtils {


    public static WindowManager getWindowManager(Context context){
        return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public static int getScreenWidth(Context context){
        WindowManager manager = getWindowManager(context);
        int width = 0;
        if (manager != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(outMetrics);
            width = outMetrics.widthPixels;
        }
        return width;
    }

    public static int getScreenHeight(Context context){
        WindowManager manager = getWindowManager(context);
        int height = 0;
        if (manager != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(outMetrics);
            height = outMetrics.heightPixels;
        }
        return height;
    }
}
