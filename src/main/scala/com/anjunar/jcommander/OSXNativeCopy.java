package com.anjunar.jcommander;

import com.anjunar.jcommander.utils.NativeUtils;

import java.io.IOException;

public class OSXNativeCopy {

    public static native void copyFiles(String[] sources, String targetDir, boolean overwrite, ProgressListener listener);
    public static native void moveFiles(String[] sources, String targetDir, boolean overwrite, ProgressListener listener);
    public static native void deleteFiles(String[] sources, boolean useTrash, ProgressListener listener);

    public interface ProgressListener {
        void onFileProgress(int operation, String source, String target, long bytesDone, long bytesTotal);
        void onFileComplete(int operation, String source, String target);
        void onComplete(int operation);
        void onError(int operation, String source, String target, int code, String message);
        boolean isCancelled();
    }

}
