package ocr.cg.com.cn.android;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import cn.com.cg.ocr.common.utils.FileUtils;
import cn.com.cg.ocr.crash.CrashHandler;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/17 11:21
 */
public class MyApplication extends Application {

    private RefWatcher refWatcher;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        //每次使用前，清除历史图片缓存
        FileUtils.deleteCatchImageFile();
        refWatcher= setupLeakCanary();
    }

    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    public static RefWatcher getRefWatcher(Context context) {
        MyApplication leakApplication = (MyApplication) context.getApplicationContext();
        return leakApplication.refWatcher;
    }
}
