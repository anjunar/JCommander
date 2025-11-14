package com.anjunar.jcommander;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.stream.Stream;

public class Launcher {
    public static void main(String[] args) throws Exception {
        File distDir = new File("app/lib");
        if (!distDir.exists() || !distDir.isDirectory()) {
            throw new RuntimeException("Dist-Verzeichnis nicht gefunden: " + distDir.getAbsolutePath());
        }

        URL[] urls = Stream.of(Objects.requireNonNull(distDir.listFiles()))
                .filter(f -> f.getName().endsWith(".jar"))
                .map(f -> {
                    try { return f.toURI().toURL(); }
                    catch (MalformedURLException e) { throw new RuntimeException(e); }
                })
                .toArray(URL[]::new);

        // URLClassLoader mit SystemClassLoader als Parent
        URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

        // Setze Thread Context ClassLoader für JavaFX/ScalaFX
        Thread.currentThread().setContextClassLoader(loader);

        // Main-Klasse laden und starten
        Class<?> mainClass = Class.forName("com.anjunar.jcommander.Main", true, loader);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }
}
