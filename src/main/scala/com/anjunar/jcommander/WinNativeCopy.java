package com.anjunar.jcommander;

public class WinNativeCopy {

    static {
        System.loadLibrary("win_native_copy");
    }

    public static native void copyFiles(String[] sources, String targetDir, ProgressCallback callback);
    public static native void moveFiles(String[] sources, String targetDir, ProgressCallback callback);
    public static native void deleteFiles(String[] sources, ProgressCallback callback);

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

        public ProgressEvent(Type type, String source, String target, double percent) {
            this.type = type;
            this.source = source;
            this.target = target;
            this.percent = percent;
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
