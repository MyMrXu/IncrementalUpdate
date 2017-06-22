package com.xzwzz.utils;


import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FilePatchService extends Service {

    private String url;
    private String oldApkPath;
    private String newApkPath;
    private String patchPathForLocal;
    private OnLoadFinishListener mOnLoadFinishListener;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("FilePatchService", "绑定FilePatchService");
        oldApkPath=intent.getStringExtra("old");
        newApkPath=intent.getStringExtra("new");
        patchPathForLocal=intent.getStringExtra("local");
        url=intent.getStringExtra("url");
        new ApkUpdateTask().execute(url);
        return new LocalBinder();
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("FilePatchService", "解除绑定FilePatchService");
        onDestroy();
        return super.onUnbind(intent);
    }
    private class ApkUpdateTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                download(params[0], patchPathForLocal);
                bspatchUtils.patch(oldApkPath, newApkPath, patchPathForLocal);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //3.安装
            if (result) {
                File file = new File(patchPathForLocal);
                if (file.exists()) {
                    file.delete();
                }
                Log.d("ApkUpdateTask", "成功");
                mOnLoadFinishListener.OnSuccess(newApkPath);
            } else {
                mOnLoadFinishListener.OnFail();
            }
        }

    }
    public static File download(String url, String downloadName) throws Exception {
        File file = null;
        InputStream is = null;
        FileOutputStream os = null;
        file = new File(downloadName);
        if (file.exists()) {
            file.delete();
        }
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setDoInput(true);
        is = conn.getInputStream();
        os = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        conn.disconnect();
        Log.d("ApkUtils", "下载已结束");
        if (is != null) {
            is.close();
        }
        if (os != null) {
            os.close();
        }
        return file;
    }
    public class LocalBinder extends Binder {
        FilePatchService getService() {
            return FilePatchService.this;
        }
    }
    public void setOnLoadFinishListener(OnLoadFinishListener listener){
        this.mOnLoadFinishListener=listener;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    public interface OnLoadFinishListener{
        void OnSuccess(String url);
        void OnFail();
    }
}
