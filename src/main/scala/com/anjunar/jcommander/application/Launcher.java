package com.anjunar.jcommander.application;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.stream.Stream;

public class Launcher {
    static void main(String[] args) throws Exception {
        File distDir = new File("lib");
        if (!distDir.exists() || !distDir.isDirectory()) {
            distDir = new File("app/lib");
            if (!distDir.exists() || !distDir.isDirectory()) {
                throw new RuntimeException("Dist-Directory not found: " + distDir.getAbsolutePath());
            }
        }

        URL[] urls = Stream.of(Objects.requireNonNull(distDir.listFiles()))
                .filter(f -> f.getName().endsWith(".jar"))
                .map(f -> {
                    try { return f.toURI().toURL(); }
                    catch (MalformedURLException e) { throw new RuntimeException(e); }
                })
                .toArray(URL[]::new);

        URLClassLoader loader = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());

        Thread.currentThread().setContextClassLoader(loader);

        Class<?> mainClass = Class.forName("com.anjunar.jcommander.Main", true, loader);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }
}
