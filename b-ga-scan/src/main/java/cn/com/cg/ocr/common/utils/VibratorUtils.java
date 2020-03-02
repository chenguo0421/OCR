package cn.com.cg.ocr.common.utils;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class VibratorUtils {
    private static Vibrator vibrator;

    public static void start(Context context){
        if (vibrator == null) {
            vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator.hasVibrator()){
            vibrator.vibrate(300);
        }
    }
}
