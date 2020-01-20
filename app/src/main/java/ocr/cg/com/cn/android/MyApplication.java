package ocr.cg.com.cn.android;

import android.app.Application;

import cn.com.cg.ocr.crash.CrashHandler;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/17 11:21
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
    }
}
