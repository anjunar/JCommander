package com.anjunar.jcommander.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class NativeUtils {

    public static void loadLibraryFromJar(String path) throws IOException {
        Objects.requireNonNull(path, "Path must not be null");
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }

        String[] parts = path.split("/");
        String filename = parts[parts.length - 1];

        Path tempDir = Files.createTempDirectory("native-libs");
        tempDir.toFile().deleteOnExit();
        Path tempFile = tempDir.resolve(filename);
        tempFile.toFile().deleteOnExit();

        try (InputStream is = NativeUtils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Native library not found in classpath: " + path);
            }
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        System.load(tempFile.toAbsolutePath().toString());
    }

    public static void load(String library) throws IOException {
        loadLibraryFromJar("/" + library);
    }
}