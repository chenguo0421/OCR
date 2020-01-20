package ocr.cg.com.cn.android;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;


import cn.com.cg.ocr.utils.OCRUtils;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OCRUtils.fixedSlicer(MainActivity.this);
            }
        });

        findViewById(R.id.btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OCRUtils.multiZoneSlicer(MainActivity.this);
            }
        });


        findViewById(R.id.btn_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OCRUtils.dynamicalSlicer(MainActivity.this);
            }
        });

    }

}
