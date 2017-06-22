package com.xzwzz.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
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
public class ApkUtils {
    private ApkMergeListener mListener;
    private static ApkUtils apkUtils;
    private Context context;
    private String oldApkPath;
    private String newApkPath = "";
    private String patchPathForLocal;

    public static ApkUtils getInstant(Context context) {
        if (apkUtils == null) {
            apkUtils = new ApkUtils();
            apkUtils.context = context;
            apkUtils.oldApkPath = getSourceApkPath(context, context.getPackageName());
        }
        return apkUtils;
    }

    private ApkUtils() {}

    public ApkUtils addApkMergeListener(ApkMergeListener listener) {
        this.mListener = listener;
        return apkUtils;
    }

    public ApkUtils bspatch(String url) {
        new ApkUpdateTask().execute(url);
        return apkUtils;
    }

    public ApkUtils setLocalApkCache(String LocalApkPath) {
        this.patchPathForLocal = LocalApkPath;
        return apkUtils;
    }

    private class ApkUpdateTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            if (newApkPath.equals("")) {
                newApkPath = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + "_new.apk";
            }
            if (patchPathForLocal == null) {
                patchPathForLocal = Environment.getExternalStorageDirectory() + File.separator + context.getPackageName() + "_ApkPatch.patch";
            }
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
                mListener.Onsuccess(newApkPath);
                if (file.exists()) {
                    file.delete();
                }
            } else {
                mListener.Onfail();
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
        Log.d("ApkUtils", "下载已结束");
        if (is != null) {
            is.close();
        }
        if (os != null) {
            os.close();
        }
        return file;
    }

    public static boolean isInstalled(Context context, String packageName) {
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

    /**
     * 获取已安装Apk文件的源Apk文件
     * 如：/data/app/my.apk
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getSourceApkPath(Context context, String packageName) {
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

    /**
     * 安装Apk
     *
     * @param context
     * @param apkPath
     */
    public static void installApk(Context context, String apkPath) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + apkPath),
                "application/vnd.android.package-archive");
        context.startActivity(intent);

    }
}