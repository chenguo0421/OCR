package cn.com.cg.ocr.common.utils;


import android.hardware.Camera;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/20 11:46
 */
public class LightUtil {

    /**
     * 打开闪光灯
     */
    public static void openFlashLight(Camera camera) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameter = camera.getParameters();
        parameter.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameter);
    }

    /**
     * 关闭闪光灯
     */
    public static void closeFlashLight(Camera camera) {
        if (camera == null) {
            return;
        }
        Camera.Parameters parameter = camera.getParameters();
        parameter.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameter);
    }

}
