package cn.com.cg.ocr.ocrbyphoto.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import cn.com.cg.ocr.R;
import cn.com.cg.ocr.common.bean.ScanResult;
import cn.com.cg.ocr.common.intf.OnScanSuccessListener;
import cn.com.cg.ocr.common.utils.VibratorUtils;
import cn.com.cg.ocr.ocrbyphoto.customview.CameraPreviewViewWithRect;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/3/26 15:28
 */
public class ScanWithPhotoActivity extends AppCompatActivity implements OnScanSuccessListener, View.OnClickListener {

    private CameraPreviewViewWithRect surface_view;
    private Button auto_focus_btn;
    private Button take_photo_scan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tesseract_activity_scanwithphoto);
        surface_view = findViewById(R.id.surface_view);
        surface_view.setListener(this);
        auto_focus_btn = (Button)findViewById(R.id.auto_focus_btn);
        auto_focus_btn.setTag(true);
        take_photo_scan = (Button)findViewById(R.id.take_photo_scan);
        auto_focus_btn.setOnClickListener(this);
        take_photo_scan.setOnClickListener(this);
    }

    @Override
    public void onOCRSuccess(final ScanResult bean) {
        VibratorUtils.start(this);
        Log.e("CG", "onOCRSuccess id = " + bean.id);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ScanWithPhotoActivity.this,"id = "+bean.id + " 姓名 = " + bean.name +" 性别 = " + bean.sex + " 出生年月 = " + bean.birthday + "民族 = " + bean.nation,Toast.LENGTH_SHORT).show();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.auto_focus_btn){
            surface_view.autoFocus((boolean)auto_focus_btn.getTag());
            auto_focus_btn.setTag(!(boolean)auto_focus_btn.getTag());
            auto_focus_btn.setText("自动对焦(" + ((boolean)auto_focus_btn.getTag()?"开":"关") +")");
        }else if (v.getId() == R.id.take_photo_scan){
            surface_view.oneShotFrame();
        }
    }
}
