package com.anjunar.jcommander;

public class NativeCopy {

    public interface CopyProgress {
        boolean onProgress(long totalBytes, long transferredBytes);
    }

    static {
        try {
            System.loadLibrary("com_anjunar_jcommander_NativeCopy");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public static native int copyFile(String src, String dst, int flags, CopyProgress cb);
    
}
