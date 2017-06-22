package com.xzwzz.exuseme;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.xzwzz.utils.ApkUtils;
import com.xzwzz.utils.FilePatchService;


public class MainActivity extends AppCompatActivity implements FilePatchService.OnLoadFinishListener {
    private String url="http://123.56.2.28:8080/excuseMe/apk.patch";
    private ApkUtils mUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                mUtils = new ApkUtils(this,this);
                mUtils.bspatch(url);
                break;
        }
    }

    @Override
    public void OnSuccess(String url) {
        Toast.makeText(this, "合并成功", Toast.LENGTH_SHORT).show();
        mUtils.installApk(this,url);
    }

    @Override
    public void OnFail() {
        Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
    }
}
