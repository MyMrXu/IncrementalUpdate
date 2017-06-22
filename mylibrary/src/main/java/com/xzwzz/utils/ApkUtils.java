package com.xzwzz.utils;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*增量更新帮助类*/
public class ApkUtils implements FilePatchService.OnLoadFinishListener {
    private FilePatchService.OnLoadFinishListener mListener;
    private Context context;
    private String oldApkPath;
    private String newApkPath = "";
    private String patchPathForLocal;
    private ServiceConnection conn;
    public ApkUtils(Context context,FilePatchService.OnLoadFinishListener listener) {
        this.context = context;
        this.mListener=listener;
        this.oldApkPath = getSourceApkPath(context, context.getPackageName());
        this.conn=new SerConnection(this);
    }
    public ApkUtils bspatch(String url) {
        if (newApkPath.equals("")) {
            newApkPath = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + "_new.apk";
        }
        if (patchPathForLocal == null) {
            patchPathForLocal = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + "_ApkPatch.patch";
        }
        Intent intent=new Intent(context,FilePatchService.class);
        intent.putExtra("url",url);
        intent.putExtra("local",patchPathForLocal);
        intent.putExtra("old",oldApkPath);
        intent.putExtra("new",newApkPath);
        context.bindService(intent,conn,Context.BIND_AUTO_CREATE);
        return this;
    }
    public ApkUtils setLocalApkCache(String LocalApkPath) {
        this.patchPathForLocal = LocalApkPath;
        return this;
    }

//    private class ApkUpdateTask extends AsyncTask<String, Void, Boolean> {
//
//        @Override
//        protected Boolean doInBackground(String... params) {
//            if (newApkPath.equals("")) {
//                newApkPath = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + "_new.apk";
//            }
//            if (patchPathForLocal == null) {
//                patchPathForLocal = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + "_ApkPatch.patch";
//            }
//            try {
//                download(params[0], patchPathForLocal);
//                bspatchUtils.patch(oldApkPath, newApkPath, patchPathForLocal);
//            } catch (Exception e) {
//                e.printStackTrace();
//                return false;
//            }
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            super.onPostExecute(result);
//            //3.安装
//            if (result) {
//                File file = new File(patchPathForLocal);
//                mListener.Onsuccess(newApkPath);
//                if (file.exists()) {
//                    file.delete();
//                }
//            } else {
//                mListener.Onfail();
//            }
//        }
//
//    }
//
//    public static File download(String url, String downloadName) throws Exception {
//        File file = null;
//        InputStream is = null;
//        FileOutputStream os = null;
//        file = new File(downloadName);
//        if (file.exists()) {
//            file.delete();
//        }
//        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
//        conn.setDoInput(true);
//        is = conn.getInputStream();
//        os = new FileOutputStream(file);
//        byte[] buffer = new byte[1024];
//        int len = 0;
//        while ((len = is.read(buffer)) != -1) {
//            os.write(buffer, 0, len);
//        }
//        Log.d("ApkUtils", "下载已结束");
//        if (is != null) {
//            is.close();
//        }
//        if (os != null) {
//            os.close();
//        }
//        return file;
//    }
    public boolean isInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return installed;
    }
    public String getSourceApkPath(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return null;

        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(packageName, 0);
            return appInfo.sourceDir;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
    public void installApk(Context context, String apkPath) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkPath),
                "application/vnd.android.package-archive");
        context.startActivity(intent);

    }
    @Override
    public void OnSuccess(String result) {
        context.unbindService(conn);
        mListener.OnSuccess(result);
    }
    @Override
    public void OnFail() {
        context.unbindService(conn);
        mListener.OnFail();
    }

    class SerConnection implements ServiceConnection {
        FilePatchService.LocalBinder binder = null;
        private FilePatchService.OnLoadFinishListener mOnLoadFinishListener;

        public SerConnection(FilePatchService.OnLoadFinishListener onLoadFinishListener) {
            mOnLoadFinishListener = onLoadFinishListener;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder= (FilePatchService.LocalBinder) service;
            binder.getService().setOnLoadFinishListener(mOnLoadFinishListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

}