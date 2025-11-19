package com.anjunar.jcommander;

import com.anjunar.jcommander.utils.NativeUtils;

import java.io.IOException;

public class LinuxNativeCopy {

    static {
        try {
            NativeUtils.loadWinNativeCopy("linux_native_copy.so");
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Failed to load native library: " + e.getMessage());
        }
    }

    public static final int OP_COPY = 0;
    public static final int OP_MOVE = 1;
    public static final int OP_DELETE = 2;

    public static native void copyFiles(String[] sources, String targetDir, boolean overwrite, ProgressListener listener);
    public static native void moveFiles(String[] sources, String targetDir, boolean overwrite, ProgressListener listener);
    public static native void deleteFiles(String[] sources, boolean useTrash, ProgressListener listener);
    public static native byte[] getFileIcon(String path, boolean large);

    public interface ProgressListener {
        void onFileProgress(int operation, String source, String target, long bytesDone, long bytesTotal);
        void onFileComplete(int operation, String source, String target);
        void onComplete(int operation);
        void onError(int operation, String source, String target, int code, String message);
        boolean isCancelled();
    }


}
