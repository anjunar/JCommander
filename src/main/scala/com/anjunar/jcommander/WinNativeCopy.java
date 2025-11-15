package com.anjunar.jcommander;

import java.io.IOException;

public class WinNativeCopy {

    static {
        try {
            NativeUtils.loadWinNativeCopy();
        } catch (IOException e) {
            throw new UnsatisfiedLinkError("Failed to load native library: " + e.getMessage());
        }
    }
    
    public static native void copyFiles(String[] sources, String targetDir, ProgressCallback callback, boolean overwrite);
    public static native void moveFiles(String[] sources, String targetDir, ProgressCallback callback, boolean overwrite);
    public static native void deleteFiles(String[] sources, ProgressCallback callback, boolean recycle);
    public static native void executeFile(String path);
    public static native byte[] getFileIcon(String path, boolean large);
    public static native void fileContext(String[] paths, boolean darkMode);

    public interface ProgressCallback {
        void onProgress(ProgressEvent event);
        void onError(ErrorEvent event);
        void onComplete();
        boolean isCancelled();
    }

    public static class ProgressEvent {
        public enum Type {
            START,
            PRE_COPY,
            UPDATE,
            POST_COPY,
            FINISH
        }

        public final Type type;
        public final String source;
        public final String target;
        public final double percent;
        public final long iWorkSofar;
        public final long iWorkTotal;

        public ProgressEvent(Type type, String source, String target, double percent, long iWorkSofar, long iWorkTotal) {
            this.type = type;
            this.source = source;
            this.target = target;
            this.percent = percent;
            this.iWorkSofar = iWorkSofar;
            this.iWorkTotal = iWorkTotal;
        }

        @Override
        public String toString() {
            return type +
                    (source != null ? " " + source : "") +
                    (target != null ? " → " + target : "") +
                    (percent >= 0 ? " (" + (int)(percent * 100) + "%)" : "");
        }
    }

    public static class ErrorEvent {
        public final String file;
        public final int hResult;

        public ErrorEvent(String file, int hResult) {
            this.file = file;
            this.hResult = hResult;
        }

        @Override
        public String toString() {
            return "Fehler bei " + file + " (HRESULT=0x" + Integer.toHexString(hResult) + ")";
        }
    }
}
