package cn.com.cg.ocr.ocrbyface.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;



import cn.com.cg.ocr.R;
import cn.com.cg.ocr.common.bean.ScanResult;
import cn.com.cg.ocr.common.intf.OnScanSuccessListener;
import cn.com.cg.ocr.common.utils.VibratorUtils;
import cn.com.cg.ocr.ocrbyface.customview.CameraPreviewViewWithRect;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/19 9:25
 */
public class FaceCaptureActivity extends AppCompatActivity implements OnScanSuccessListener {

    private CameraPreviewViewWithRect surface_view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tesseract_activity_facecapture);
        surface_view = findViewById(R.id.surface_view);
        surface_view.setListener(this);

    }

    @Override
    public void onOCRSuccess(ScanResult bean) {
        VibratorUtils.start(this);
        Log.e("CG", "onOCRSuccess id = " + bean.id);
        Toast.makeText(this,"id = "+bean.id + " 姓名 = " + bean.name +" 性别 = " + bean.sex + " 出生年月 = " + bean.birthday,Toast.LENGTH_SHORT).show();
    }
}
