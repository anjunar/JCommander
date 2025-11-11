package com.anjunar.jcommander;

public class WinNativeCopy {

    static {
        System.loadLibrary("win_native_copy");
    }

    public static native void copyFiles(String[] sources, String targetDir, ProgressCallback callback);

    public interface ProgressCallback {
        void onProgress(String currentFile, double percent);
        void onComplete();
        void onError(String file, int errorCode);
    }

}
