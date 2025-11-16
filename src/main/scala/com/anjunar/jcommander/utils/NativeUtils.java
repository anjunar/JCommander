package com.anjunar.jcommander.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class NativeUtils {

    private static final String DLL_NAME = "win_native_copy.dll";

    /**
     * Lädt eine native Bibliothek aus dem JAR oder aus resources/.
     * @param path Pfad innerhalb des Classpaths, z.B. "/win_native_copy.dll"
     */
    public static void loadLibraryFromJar(String path) throws IOException {
        Objects.requireNonNull(path, "Path must not be null");
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }

        String[] parts = path.split("/");
        String filename = parts[parts.length - 1];

        // Temp-Datei erstellen
        Path tempDir = Files.createTempDirectory("native-libs");
        tempDir.toFile().deleteOnExit();
        Path tempFile = tempDir.resolve(filename);
        tempFile.toFile().deleteOnExit();

        // Aus Classpath kopieren
        try (InputStream is = NativeUtils.class.getResourceAsStream(path)) {
            if (is == null) {
                throw new FileNotFoundException("Native library not found in classpath: " + path);
            }
            Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // Laden
        System.load(tempFile.toAbsolutePath().toString());
    }

    // Optional: Direkter Aufruf für deine DLL
    public static void loadWinNativeCopy() throws IOException {
        loadLibraryFromJar("/" + DLL_NAME);
    }
}