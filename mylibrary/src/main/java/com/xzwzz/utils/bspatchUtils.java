package com.xzwzz.utils;

/**
 * Created by dell on 2017/6/16.
 */

public class bspatchUtils {

    public static native void patch(String oldfile,String newfile,String patchfile);

    static{
        try{
            System.loadLibrary("bspatch");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
