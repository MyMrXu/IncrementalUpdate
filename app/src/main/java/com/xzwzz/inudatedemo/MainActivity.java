package com.xzwzz.inudatedemo;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.xzwzz.utils.ApkUtils;
import com.xzwzz.utils.bspatchUtils;

import java.io.File;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                bspatchUtils.patch(ApkUtils.getSourceApkPath(this,getPackageName()),
                        Environment.getExternalStorageDirectory()+ File.separator+"new.apk"
                        ,Environment.getExternalStorageDirectory()+File.separator+"apk.patch"
                        );
                break;
            case R.id.btn2:
                ApkUtils.installApk(this,Environment.getExternalStorageDirectory()+ File.separator+"new.apk");
                break;
        }
    }
}
