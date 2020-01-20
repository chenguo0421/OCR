package cn.com.cg.ocr.ocrbyface.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import cn.com.cg.ocr.R;
import cn.com.cg.ocr.ocrbyface.customview.CameraPreviewViewWithRect;
import cn.com.cg.ocr.ocrbyface.customview.intf.OnScanSuccessListener;

/**
 * Discription  {}
 * author  chenguo7
 * Date  2020/1/19 9:25
 */
public class FaceCaptureActivity extends AppCompatActivity implements OnScanSuccessListener {

    private CameraPreviewViewWithRect mSurfaceView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tesseract_activity_facecapture);

        mSurfaceView = findViewById(R.id.mSurfaceView);
        mSurfaceView.setListener(this);
    }

    @Override
    public void onOCRSuccess(String id, String tempFilePath) {
        Log.e("CG", "onOCRSuccess id = " + id);
        Toast.makeText(this,"id = "+id,Toast.LENGTH_SHORT).show();
    }
}
